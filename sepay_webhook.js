/**
 * sepay_webhook.js — Server nhận webhook từ SePay, tự động xác nhận thanh toán VietQR.
 *
 * Luồng:
 *   1. Khách chuyển khoản với nội dung "GLASSITY <mã đơn>" (VD: GLASSITY GLS-260708-2223)
 *   2. SePay phát hiện giao dịch vào TK 0977780173 → gọi POST vào webhook này
 *   3. Server tìm order có orderCode khớp trong nội dung CK, kiểm tra số tiền,
 *      rồi set paymentStatus = "PAID" trên Firestore
 *   4. App Android đang lắng nghe realtime (PaymentResultActivity.watchPaymentStatus)
 *      sẽ tự động nhận được thay đổi và chuyển sang màn "Thanh toán thành công"
 *
 * CÀI ĐẶT:
 *   npm install express firebase-admin
 *
 * CHẠY THỬ LOCAL (dùng ngrok để có URL public cho SePay gọi vào):
 *   node sepay_webhook.js
 *   ngrok http 3000
 *   → copy URL https://xxxx.ngrok-free.app/sepay-webhook dán vào SePay dashboard
 *
 * DEPLOY THẬT (miễn phí, chạy 24/7): Render.com / Railway.app — xem hướng dẫn cuối file.
 *
 * Yêu cầu: service-account.json (Firebase Console → Project settings →
 *          Service accounts → Generate new private key), đặt cùng thư mục với file này.
 *          KHÔNG commit file này lên Git — thêm vào .gitignore.
 */

const express = require('express');
// firebase-admin v14 dùng modular import (không còn admin.credential.cert / admin.firestore() kiểu cũ)
const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore, FieldValue } = require('firebase-admin/firestore');

// ── Xác thực Firebase Admin bằng service account ───────────────────────────
const sa = require('./service-account.json');
initializeApp({ credential: cert(sa) });
const db = getFirestore();

// ── Cấu hình ─────────────────────────────────────────────────────────────
const SEPAY_API_KEY = process.env.SEPAY_API_KEY || 'DAN_API_KEY_SEPAY_VAO_DAY';
const BANK_ACCOUNT   = '0977780173';

const app = express();
app.use(express.json());

app.post('/sepay-webhook', async (req, res) => {
  try {
    // Xác thực request thật sự đến từ SePay (Authorization: Apikey <key>)
    const auth = req.headers['authorization'] || '';
    if (SEPAY_API_KEY !== 'DAN_API_KEY_SEPAY_VAO_DAY' && auth !== `Apikey ${SEPAY_API_KEY}`) {
      console.warn('[sepay-webhook] Sai API key, từ chối request');
      return res.status(401).json({ success: false, message: 'Unauthorized' });
    }

    const body = req.body || {};
    // Payload SePay (tham khảo docs.sepay.vn): content, transferAmount, accountNumber, transferType
    const content        = String(body.content || body.description || '');
    const transferAmount = Number(body.transferAmount || 0);
    const accountNumber  = String(body.accountNumber || '');
    const transferType   = body.transferType; // "in" = tiền vào, "out" = tiền ra

    console.log('[sepay-webhook] Nhận giao dịch:', { content, transferAmount, accountNumber, transferType });

    // Chỉ xử lý tiền VÀO đúng tài khoản MB đang dùng cho app
    if (transferType && transferType !== 'in') {
      return res.json({ success: true, message: 'Bỏ qua giao dịch tiền ra' });
    }
    if (accountNumber && accountNumber !== BANK_ACCOUNT) {
      return res.json({ success: true, message: 'Không phải TK của app' });
    }

    // ── Lớp 1 — khớp theo MÃ ĐƠN trong nội dung CK (chính xác nhất) ──────────
    // Lưu ý: hệ thống ngân hàng (MB, ...) thường TỰ XÓA dấu gạch ngang trong nội
    // dung chuyển khoản — "GLS-260708-6724" về tới đây thành "GLS2607086724"
    // (dính liền, không dấu). Regex khớp cả 2 dạng, rồi tự chèn lại dấu "-".
    let doc = null;
    const match = content.match(/GLS-?(\d{6})-?(\d{3,6})/i);
    if (match) {
      const orderCode = `GLS-${match[1]}-${match[2]}`.toUpperCase();
      const snap = await db.collection('orders')
          .where('orderCode', '==', orderCode)
          .limit(1)
          .get();
      if (!snap.empty) doc = snap.docs[0];
      else console.warn('[sepay-webhook] Có mã trong nội dung nhưng không tìm thấy order:', orderCode);
    } else {
      console.warn('[sepay-webhook] Không tìm thấy mã đơn trong nội dung:', content);
    }

    // ── Lớp 2 — dự phòng: người chuyển khoản lỡ sửa/xóa mã đơn trong nội dung.
    // Dò các đơn BANK_TRANSFER đang PENDING có đúng số tiền này. Chỉ tự xác nhận
    // khi khớp DUY NHẤT 1 đơn — nếu trùng nhiều đơn cùng số tiền thì không đoán
    // bừa (tránh xác nhận nhầm tiền của người khác), để lại xử lý thủ công.
    if (!doc && transferAmount > 0) {
      const fallbackSnap = await db.collection('orders')
          .where('paymentMethod', '==', 'BANK_TRANSFER')
          .where('paymentStatus', '==', 'PENDING')
          .where('totalAmount', '==', transferAmount)
          .get();

      if (fallbackSnap.size === 1) {
        doc = fallbackSnap.docs[0];
        console.log('[sepay-webhook] Khớp theo số tiền (fallback):', doc.data().orderCode);
      } else if (fallbackSnap.size > 1) {
        console.warn(`[sepay-webhook] ${fallbackSnap.size} đơn cùng khớp số tiền ${transferAmount} — không thể tự xác định, cần kiểm tra thủ công`);
        return res.json({ success: true, message: 'Nhiều đơn trùng số tiền — cần đối soát thủ công' });
      }
    }

    if (!doc) {
      return res.json({ success: true, message: 'Không khớp được đơn hàng nào' });
    }

    const order = doc.data();

    if (order.paymentStatus === 'PAID') {
      return res.json({ success: true, message: 'Đơn đã xác nhận từ trước' });
    }

    // Đối soát số tiền lần cuối (trường hợp khớp qua mã đơn ở Lớp 1) — tránh xác nhận
    // nhầm khi khách chuyển thiếu/thừa dù nội dung có mã đúng
    if (transferAmount > 0 && Math.round(order.totalAmount) !== Math.round(transferAmount)) {
      console.warn('[sepay-webhook] Số tiền không khớp:', order.totalAmount, 'vs', transferAmount);
      return res.json({ success: true, message: 'Số tiền không khớp — cần kiểm tra thủ công' });
    }

    await doc.ref.update({
      paymentStatus: 'PAID',
      paidAt: FieldValue.serverTimestamp(),
    });

    console.log('[sepay-webhook] ✅ Xác nhận thanh toán:', order.orderCode);
    return res.json({ success: true, message: 'Đã xác nhận thanh toán ' + order.orderCode });

  } catch (err) {
    console.error('[sepay-webhook] Lỗi:', err);
    return res.status(500).json({ success: false, message: err.message });
  }
});

app.get('/', (_req, res) => res.send('SePay webhook server đang chạy'));

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`SePay webhook đang lắng nghe tại port ${PORT}`));

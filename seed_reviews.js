// seed_reviews.js — gán review từ tài khoản khách thật cho mọi sản phẩm (1–3 review/SP)
const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore, Timestamp } = require('firebase-admin/firestore');

initializeApp({ credential: cert(require('./service-account.json')) });
const db = getFirestore();

const C5 = [
  'Kính đẹp lắm, đeo lên rất hợp mặt, giao hàng nhanh nữa. Sẽ ủng hộ shop dài dài!',
  'Chất lượng vượt mong đợi so với tầm giá. Gọng chắc chắn, màu y hình.',
  'Mua tặng bạn mà bạn khen quá trời, shop tư vấn nhiệt tình. 10 điểm!',
  'Đóng gói cẩn thận, kèm hộp và khăn lau xịn. Đeo nhẹ tênh, rất ưng.',
  'Form kính ôm mặt, không bị trượt khi đeo lâu. Rất đáng tiền.',
  'Lần thứ hai mua ở Glassity, lần nào cũng ưng. Màu ngoài đời còn đẹp hơn hình.',
  'Kính xịn, nhìn sang hẳn. Bạn bè hỏi mua ở đâu suốt.',
];
const C4 = [
  'Kính đẹp, giao hơi lâu xíu nhưng chất lượng ổn. Hài lòng.',
  'Màu hơi khác hình một chút nhưng đeo lên vẫn đẹp, gọng nhẹ.',
  'Đáng tiền, chỉ tiếc hộp đựng hơi đơn giản. Kính thì ổn áp.',
  'Đeo thoải mái, không bị cấn tai. Trừ 1 sao vì ship chậm.',
  'Sản phẩm tốt, đúng mô tả. Nếu có thêm size nhỏ hơn thì tuyệt.',
  'Ưng cái thiết kế, chụp hình lên rất có style. Giá hợp lý.',
];
const C3 = [
  'Kính ổn trong tầm giá, gọng hơi rộng so với mặt mình.',
  'Tạm được, màu nhạt hơn hình chút. Dùng ổn.',
  'Chất lượng bình thường, được cái mẫu mã đẹp.',
];
const ACC5 = [
  'Phụ kiện nhỏ mà chất lượng tốt, đóng gói đẹp. Recommend!',
  'Dùng rất tiện, đáng đồng tiền. Giao nhanh.',
  'Hàng xịn, đúng mô tả. Mua kèm kính rất hợp lý.',
];
const ACC4 = [
  'Ổn trong tầm giá, dùng tốt.',
  'Sản phẩm như mô tả, giao hơi chậm chút.',
];

const rand = a => a[Math.floor(Math.random() * a.length)];
const randInt = (lo, hi) => lo + Math.floor(Math.random() * (hi - lo + 1));

function pickRating() {
  const r = Math.random();
  return r < 0.55 ? 5 : r < 0.85 ? 4 : 3;
}
function pickComment(rating, isAcc) {
  if (isAcc) return rating >= 5 ? rand(ACC5) : rand(ACC4);
  return rating >= 5 ? rand(C5) : rating === 4 ? rand(C4) : rand(C3);
}

(async () => {
  const custSnap = await db.collection('customers').get();
  const customers = custSnap.docs
    .map(d => ({ uid: d.id, name: (d.data().name || '').trim() }))
    .filter(c => c.name);
  if (!customers.length) { console.error('Không có customer nào!'); process.exit(1); }
  console.log(`${customers.length} khách hàng thật:`, customers.map(c => c.name).join(', '));

  const prodSnap = await db.collection('products').get();
  console.log(`${prodSnap.size} sản phẩm.`);

  // tránh trùng: xóa review seed cũ? — không, chỉ thêm cho SP chưa có review
  const revSnap = await db.collection('reviews').get();
  const hasReview = new Set(revSnap.docs.map(d => d.data().productId));

  let total = 0;
  const batchWriter = db.bulkWriter();
  for (const doc of prodSnap.docs) {
    const p = doc.data();
    if (hasReview.has(doc.id)) { console.log(`bỏ qua (đã có review): ${p.name}`); continue; }
    const isAcc = p.category === 'phu_kien';
    const n = randInt(1, 3);
    // chọn n khách khác nhau
    const pool = [...customers].sort(() => Math.random() - 0.5).slice(0, n);
    for (const cust of pool) {
      const rating = isAcc ? (Math.random() < 0.7 ? 5 : 4) : pickRating();
      const daysAgo = randInt(1, 60);
      const created = new Date(Date.now() - daysAgo * 86400000 - randInt(0, 86399) * 1000);
      batchWriter.create(db.collection('reviews').doc(), {
        productId: doc.id,
        customerId: cust.uid,
        userName: cust.name,
        rating,
        comment: pickComment(rating, isAcc),
        createdAt: Timestamp.fromDate(created),
      });
      total++;
    }
  }
  await batchWriter.close();
  console.log(`\nXong: đã tạo ${total} review.`);
  process.exit(0);
})().catch(e => { console.error(e); process.exit(1); });

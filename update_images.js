/**
 * update_images.js — Cập nhật field `images` cho từng product sau khi đã upload ảnh
 *
 * CÁCH DÙNG:
 * 1. Upload ảnh lên Firebase Storage (Console → Storage → products/<productId>/)
 * 2. Copy download URL của từng ảnh
 * 3. Điền vào mảng IMAGE_MAP bên dưới (key = productId từ Firestore)
 * 4. node update_images.js
 */

const admin = require('firebase-admin');
const serviceAccount = require('./service-account.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  projectId: 'glassity-770c5',
});

const db = admin.firestore();

// Điền productId (lấy từ Firestore console) và URL ảnh tương ứng
// Ví dụ:
// const IMAGE_MAP = {
//   'abc123xyz': ['https://firebasestorage.googleapis.com/v0/b/glassity-770c5.firebasestorage.app/o/products%2Fabc123xyz%2F1.png?alt=media&token=...'],
// };
const IMAGE_MAP = {
  // 'PRODUCT_ID': ['URL_ANH_1', 'URL_ANH_2'],
};

async function updateImages() {
  const entries = Object.entries(IMAGE_MAP);
  if (entries.length === 0) {
    console.log('IMAGE_MAP trong — hay dien product ID va URL anh roi chay lai.');
    process.exit(0);
  }
  for (const [productId, images] of entries) {
    await db.collection('products').doc(productId).update({ images });
    console.log('[OK] cap nhat anh cho', productId);
  }
  console.log('Xong!');
  process.exit(0);
}

updateImages().catch(err => { console.error(err); process.exit(1); });

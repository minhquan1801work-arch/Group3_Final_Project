/**
 * update_products.js
 * - Cập nhật categoryId, faceShapes, gender cho 34 products hiện có
 * - Thêm 3 phụ kiện mới (images trống, chờ điền Cloudinary)
 * - Cập nhật/tạo 4 categories theo Figma
 *
 * node update_products.js
 */
const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore, FieldValue } = require('firebase-admin/firestore');
const sa = require('./service-account.json');
initializeApp({ credential: cert(sa) });
const db = getFirestore();

// ── CATEGORY UPDATES ──────────────────────────────────────────────────────
const CATEGORIES = [
  { id: 'kinh_mat', name: 'Kính Mát',    order: 1 },
  { id: 'kinh_can', name: 'Kính Cận',    order: 2 },
  { id: 'phu_kien', name: 'Phụ Kiện',   order: 3 },
  { id: 'bst',      name: 'Bộ Sưu Tập', order: 4 },
];

// ── PRODUCT UPDATES ───────────────────────────────────────────────────────
// Mỗi entry: { id, categoryId, collection?, faceShapes, gender }
const UPDATES = [

  // ── BST — Sunlight Studio (kính mát cao cấp sang trọng) ────────────────
  { id: 'M2sx3gWyBYmc2t90mU1e', // gong kim loai anh kim 2190k
    categoryId: 'bst', collection: 'Sunlight Studio',
    faceShapes: ['trai_xoan', 'kim_cuong'], gender: 'unisex' },
  { id: 'MmwFlg1vDSb6RA1cMtV6', // gong kim loai cao cap 2290k
    categoryId: 'bst', collection: 'Sunlight Studio',
    faceShapes: ['trai_xoan', 'tron'], gender: 'unisex' },
  { id: 'OnK8VI1jZefyWs1S87xH', // tong ho phanh sang trong 1890k
    categoryId: 'bst', collection: 'Sunlight Studio',
    faceShapes: ['trai_xoan', 'trai_tim'], gender: 'unisex' },
  { id: '0kEUgYM180xXcmYstnvF', // mat meo sang trong 1990k
    categoryId: 'bst', collection: 'Sunlight Studio',
    faceShapes: ['trai_tim', 'trai_xoan'], gender: 'nu' },

  // ── BST — Monochrome Collection (tối giản, đơn sắc) ────────────────────
  { id: 'woJCWW5AmKRhE4KypSSH', // trong tron toi gian 890k
    categoryId: 'bst', collection: 'Monochrome Collection',
    faceShapes: ['vuong', 'kim_cuong'], gender: 'unisex' },
  { id: 'Au6U6Hh1DpOS5uO7R6qE', // kim loai manh 890k
    categoryId: 'bst', collection: 'Monochrome Collection',
    faceShapes: ['tron', 'trai_xoan'], gender: 'nam' },
  { id: 'ORt2PGmL1uP4sR5CMl3v', // vuong kim loai toi gian 990k
    categoryId: 'bst', collection: 'Monochrome Collection',
    faceShapes: ['tron', 'trai_xoan', 'trai_tim'], gender: 'unisex' },

  // ── BST — Essential Acetate (cổ điển, retro) ───────────────────────────
  { id: 'bwpg4MKg1ZwOUKXa7Iw2', // mat meo retro 1090k
    categoryId: 'bst', collection: 'Essential Acetate',
    faceShapes: ['trai_tim', 'trai_xoan'], gender: 'nu' },
  { id: 'aSr7fIggGMgOEukx5gPz', // oval hoai co 990k
    categoryId: 'bst', collection: 'Essential Acetate',
    faceShapes: ['tron', 'trai_xoan', 'vuong'], gender: 'unisex' },

  // ── Kính Mát (sunglasses thông thường) ────────────────────────────────
  { id: '9Igd0vnlrw4hrvAySNvC', // trong tron ho phanh 990k
    categoryId: 'kinh_mat', collection: null,
    faceShapes: ['vuong', 'kim_cuong'], gender: 'unisex' },
  { id: 'AT0iEshvG4bcPEu5u1MK', // Avant-Garde 1190k
    categoryId: 'kinh_mat', collection: null,
    faceShapes: ['kim_cuong', 'trai_xoan'], gender: 'unisex' },
  { id: '8h6VgbkEp0knMUcb7zSv', // gong vuong ca tinh 1090k
    categoryId: 'kinh_mat', collection: null,
    faceShapes: ['tron', 'trai_xoan', 'trai_tim'], gender: 'nam' },
  { id: 'Fxwc9USwGyaWbg7KnrEF', // Aero Future 1190k
    categoryId: 'kinh_mat', collection: null,
    faceShapes: ['kim_cuong', 'vuong'], gender: 'nam' },
  { id: 'LRrO9vgi7IDyBUCSOGot', // Cyber 1290k
    categoryId: 'kinh_mat', collection: null,
    faceShapes: ['vuong', 'kim_cuong'], gender: 'nam' },
  { id: 'Od3ZYeznsAzuaNgIkYje', // kim loai duong pho 990k
    categoryId: 'kinh_mat', collection: null,
    faceShapes: ['tron', 'trai_xoan'], gender: 'nam' },
  { id: 'UuQybzHEp56no6OWTtKM', // thiet ke doc dao 1090k
    categoryId: 'kinh_mat', collection: null,
    faceShapes: ['trai_tim', 'kim_cuong'], gender: 'unisex' },
  { id: 'PzCAzwHRlYA9gyOb1JGs', // vuong hien dai 950k
    categoryId: 'kinh_mat', collection: null,
    faceShapes: ['tron', 'trai_xoan', 'trai_tim'], gender: 'unisex' },
  { id: 'Vk1QFnG6ykg6VuWLru9m', // oval hien dai 950k
    categoryId: 'kinh_mat', collection: null,
    faceShapes: ['tron', 'trai_xoan', 'vuong'], gender: 'unisex' },
  { id: 'aDWvIsMrXbBpx3SQliTz', // vuong duong pho 990k
    categoryId: 'kinh_mat', collection: null,
    faceShapes: ['tron', 'trai_xoan', 'trai_tim'], gender: 'nam' },
  { id: 'iBrS2JgEaUw2zdPtwU3t', // tuong lai 1190k
    categoryId: 'kinh_mat', collection: null,
    faceShapes: ['kim_cuong', 'vuong'], gender: 'nam' },
  // Glassity brand — kính mát
  { id: 'HYTcksxvq1l4QVTj2UUZ', // Urban 590k
    categoryId: 'kinh_mat', collection: null,
    faceShapes: ['vuong', 'kim_cuong'], gender: 'nam' },
  { id: 'fsDL5tY2Q1IyUYhNR9zm', // Vintage 750k
    categoryId: 'kinh_mat', collection: null,
    faceShapes: ['trai_tim', 'tron'], gender: 'nu' },
  { id: 'j5Zqn8atwBnOEAecc2Mg', // Wave 610k
    categoryId: 'kinh_mat', collection: null,
    faceShapes: ['trai_xoan', 'trai_tim'], gender: 'unisex' },
  { id: 'vkbMgTCwV8qruapB0VDY', // Edge 680k
    categoryId: 'kinh_mat', collection: null,
    faceShapes: ['kim_cuong', 'trai_xoan'], gender: 'unisex' },
  { id: 'za38HPh3AKzU6L23hzVY', // Pilot 720k
    categoryId: 'kinh_mat', collection: null,
    faceShapes: ['tron', 'trai_xoan', 'trai_tim'], gender: 'unisex' },

  // ── Kính Cận (prescription frames) ────────────────────────────────────
  { id: 'THH7gUCEb9kQhQNvMGB1', // Vision Round 380k
    categoryId: 'kinh_can', collection: null,
    faceShapes: ['vuong', 'kim_cuong'], gender: 'unisex' },
  { id: 'ayZFiOp7NLpXaZtTa4Xb', // Vision Square 420k
    categoryId: 'kinh_can', collection: null,
    faceShapes: ['tron', 'trai_xoan', 'trai_tim'], gender: 'unisex' },
  { id: 'uMoKYFGzN90wg5qQc9fa', // Vision Air 350k
    categoryId: 'kinh_can', collection: null,
    faceShapes: ['tron', 'trai_xoan', 'trai_tim', 'kim_cuong', 'vuong'], gender: 'unisex' },
  { id: 'Alznmd5frBFo8AZCGlWp', // Soft Mini 490k
    categoryId: 'kinh_can', collection: null,
    faceShapes: ['trai_tim', 'trai_xoan'], gender: 'nu' },
  { id: 'YyfbrGrV8HSflhkdvxgi', // Oval Frame 520k
    categoryId: 'kinh_can', collection: null,
    faceShapes: ['tron', 'vuong', 'kim_cuong'], gender: 'unisex' },
  { id: 'vCmSCRy4zyej35UZGnyk', // Classic 449k
    categoryId: 'kinh_can', collection: null,
    faceShapes: ['tron', 'trai_xoan'], gender: 'unisex' },

  // ── Phụ Kiện (giữ nguyên, chỉ fix categoryId) ─────────────────────────
  { id: 'PyWNRLIRakyhWcT16CRb', // Dây Đeo Kính Silicon 55k
    categoryId: 'phu_kien', collection: null,
    faceShapes: [], gender: 'unisex' },
  { id: 'tjNVBpHwpeZoIM2zZQri', // Khăn Lau Kính Microfiber 35k
    categoryId: 'phu_kien', collection: null,
    faceShapes: [], gender: 'unisex' },
  { id: 'v1e3jefAMj0svtCC19Fk', // Hộp Đựng Kính Glassity Box 120k
    categoryId: 'phu_kien', collection: null,
    faceShapes: [], gender: 'unisex' },
];

// ── NEW PHỤ KIỆN PRODUCTS (images trống, chờ Cloudinary) ─────────────────
const NEW_PHU_KIEN = [
  {
    name: 'Hộp Đựng Kính Da Cao Cấp',
    description: 'Hộp da PU cứng bảo vệ kính toàn diện, khóa nam châm, lót nhung bên trong',
    price: 185000,
    stock: 30,
    categoryId: 'phu_kien',
    collection: null,
    faceShapes: [],
    gender: 'unisex',
    colors: ['#1A1614', '#8B4513'],
    images: [], // ← điền Cloudinary URL vào đây
  },
  {
    name: 'Nước Lau Kính Chuyên Dụng 30ml',
    description: 'Dung dịch vệ sinh kính chuyên nghiệp, an toàn cho tròng phủ AR và Photochromic',
    price: 79000,
    stock: 50,
    categoryId: 'phu_kien',
    collection: null,
    faceShapes: [],
    gender: 'unisex',
    colors: [],
    images: [], // ← điền Cloudinary URL vào đây
  },
  {
    name: 'Túi Đựng Kính Nhung Mềm',
    description: 'Túi nhung 2 lớp chống xước tròng, dùng được làm khăn lau khô',
    price: 49000,
    stock: 80,
    categoryId: 'phu_kien',
    collection: null,
    faceShapes: [],
    gender: 'unisex',
    colors: ['#1A1614', '#8B0000', '#1E3A5F'],
    images: [], // ← điền Cloudinary URL vào đây
  },
];

// ── RUN ───────────────────────────────────────────────────────────────────
async function run() {
  console.log('=== Update Firestore bắt đầu ===\n');

  // 1. Ghi categories
  console.log('--- Categories ---');
  for (const cat of CATEGORIES) {
    await db.collection('categories').doc(cat.id).set(cat, { merge: true });
    console.log(`  [OK] ${cat.id}`);
  }

  // 2. Update existing products
  console.log('\n--- Update existing products ---');
  for (const u of UPDATES) {
    const data = {
      categoryId: u.categoryId,
      faceShapes: u.faceShapes,
      gender: u.gender,
    };
    if (u.collection !== undefined) data.collection = u.collection;
    await db.collection('products').doc(u.id).update(data);
    console.log(`  [OK] ${u.id} → ${u.categoryId}${u.collection ? '/' + u.collection : ''}`);
  }

  // 3. Tạo phụ kiện mới
  console.log('\n--- Tạo phụ kiện mới ---');
  for (const p of NEW_PHU_KIEN) {
    const ref = await db.collection('products').add({
      ...p,
      createdAt: FieldValue.serverTimestamp(),
    });
    console.log(`  [OK] ${p.name} → ID: ${ref.id}`);
  }

  console.log('\n=== Xong! ===');
  process.exit(0);
}

run().catch(e => { console.error('LOI:', e.message); process.exit(1); });

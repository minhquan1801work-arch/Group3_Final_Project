/**
 * seed_v2.js — Seed categories + products theo Figma LA.Categories
 *
 * Categories (Figma — đồng cấp):
 *   kinh_mat  — Kính Mát (sunglasses)
 *   kinh_can  — Kính Cận (prescription, có gọng nhựa/kim loại + shape tròn/oval/mắt mèo/vuông)
 *   phu_kien  — Phụ Kiện (hộp đựng, khăn lau, nước lau)
 *   bst       — Bộ Sưu Tập (Monochrome | Essential Acetate | Sunlight Studio)
 *
 * Product fields:
 *   categoryId  : string (một trong 4 IDs trên)
 *   collection  : string | null  (chỉ dùng khi categoryId == "bst")
 *   faceShapes  : string[]  (tron | trai_xoan | trai_tim | kim_cuong | vuong)
 *   gender      : "nam" | "nu" | "unisex"
 *   colors      : string[]  (hex)
 *   images      : string[]  (Cloudinary URLs — thêm vào sau khi có URL thật)
 *
 * CÁCH DÙNG:
 *   npm install firebase-admin
 *   node seed_v2.js
 *
 * Yêu cầu: service-account.json HOẶC Firestore rules allow write: if true
 */

const admin = require('firebase-admin');

// Cách 1 — service-account.json (nếu có):
// const sa = require('./service-account.json');
// admin.initializeApp({ credential: admin.credential.cert(sa) });

// Cách 2 — Firestore rules đã set allow write: if true:
admin.initializeApp({ projectId: 'glassity-770c5' });

const db = admin.firestore();

// ─── CATEGORIES ────────────────────────────────────────────────────────────
const CATEGORIES = [
  { id: 'kinh_mat', name: 'Kính Mát',     icon: 'ic_sunglasses', order: 1 },
  { id: 'kinh_can', name: 'Kính Cận',     icon: 'ic_glasses',    order: 2 },
  { id: 'phu_kien', name: 'Phụ Kiện',     icon: 'ic_accessory',  order: 3 },
  { id: 'bst',      name: 'Bộ Sưu Tập',  icon: 'ic_collection', order: 4 },
];

// ─── PRODUCTS ──────────────────────────────────────────────────────────────
const PRODUCTS = [

  // ── Kính Mát ──────────────────────────────────────────────────────────
  {
    name: 'Kính Mát Vuông Cổ Điển',
    description: 'Gọng vuông mạnh mẽ, tròng chống UV400, phong cách đường phố',
    price: 549000,
    stock: 15,
    categoryId: 'kinh_mat',
    collection: null,
    faceShapes: ['tron', 'trai_xoan', 'trai_tim'],
    gender: 'nam',
    colors: ['#1A1614', '#8B4513'],
    images: [],
  },
  {
    name: 'Kính Mát Phi Công',
    description: 'Aviator vàng classic, tròng gradient, unisex mọi phong cách',
    price: 620000,
    stock: 20,
    categoryId: 'kinh_mat',
    collection: null,
    faceShapes: ['tron', 'trai_xoan', 'trai_tim'],
    gender: 'unisex',
    colors: ['#D4AF37', '#C0C0C0'],
    images: [],
  },
  {
    name: 'Kính Mát Mắt Mèo Retro',
    description: 'Cat-eye cá tính tôn nét nữ tính, tròng smoke',
    price: 489000,
    stock: 12,
    categoryId: 'kinh_mat',
    collection: null,
    faceShapes: ['trai_tim', 'trai_xoan', 'tron'],
    gender: 'nu',
    colors: ['#1A1614', '#8B0000', '#FFD700'],
    images: [],
  },
  {
    name: 'Kính Mát Tròn Bohemian',
    description: 'Gọng tròn nhỏ vintage, thấu kính màu amber ấm áp',
    price: 459000,
    stock: 18,
    categoryId: 'kinh_mat',
    collection: null,
    faceShapes: ['vuong', 'kim_cuong', 'trai_xoan'],
    gender: 'unisex',
    colors: ['#D4AF37', '#8B4513'],
    images: [],
  },
  {
    name: 'Kính Mát Wrap-Around Sport',
    description: 'Ôm sát mặt cho hoạt động ngoài trời, tròng phân cực',
    price: 750000,
    stock: 10,
    categoryId: 'kinh_mat',
    collection: null,
    faceShapes: ['tron', 'trai_xoan', 'vuong', 'kim_cuong'],
    gender: 'nam',
    colors: ['#1A1614', '#C0C0C0'],
    images: [],
  },

  // ── Kính Cận ──────────────────────────────────────────────────────────
  {
    name: 'Gọng Nhựa Tròn Thanh Lịch',
    description: 'Gọng nhựa tròn mỏng nhẹ, màu đen huyền bí',
    price: 420000,
    stock: 20,
    categoryId: 'kinh_can',
    collection: null,
    faceShapes: ['vuong', 'kim_cuong', 'trai_xoan'],
    gender: 'unisex',
    colors: ['#1A1614', '#8B4513'],
    images: [],
  },
  {
    name: 'Gọng Nhựa Oval Tối Giản',
    description: 'Oval nhẹ nhàng, màu sắc trung tính dễ phối đồ',
    price: 380000,
    stock: 25,
    categoryId: 'kinh_can',
    collection: null,
    faceShapes: ['tron', 'trai_xoan', 'vuong'],
    gender: 'nu',
    colors: ['#C0C0C0', '#F5DEB3', '#1A1614'],
    images: [],
  },
  {
    name: 'Gọng Kim Loại Vuông Trí Thức',
    description: 'Gọng kim loại mỏng kiểu vuông, cổ điển và học thuật',
    price: 460000,
    stock: 18,
    categoryId: 'kinh_can',
    collection: null,
    faceShapes: ['tron', 'trai_xoan', 'trai_tim'],
    gender: 'nam',
    colors: ['#D4AF37', '#C0C0C0', '#1A1614'],
    images: [],
  },
  {
    name: 'Gọng Kim Loại Mắt Mèo Hiện Đại',
    description: 'Cat-eye kim loại tinh tế, phong cách công sở năng động',
    price: 490000,
    stock: 14,
    categoryId: 'kinh_can',
    collection: null,
    faceShapes: ['trai_tim', 'trai_xoan', 'tron'],
    gender: 'nu',
    colors: ['#D4AF37', '#8B0000'],
    images: [],
  },

  // ── Bộ Sưu Tập: Monochrome Collection ──────────────────────────────
  {
    name: 'Monochrome Shadow',
    description: 'Đen tuyệt đối — kính tối giản mạnh mẽ cho mọi phong cách',
    price: 850000,
    stock: 10,
    categoryId: 'bst',
    collection: 'Monochrome Collection',
    faceShapes: ['tron', 'trai_xoan', 'vuong', 'kim_cuong'],
    gender: 'unisex',
    colors: ['#1A1614'],
    images: [],
  },
  {
    name: 'Monochrome Mist',
    description: 'Xám khói thanh lịch, gọng nhựa cao cấp phong cách Á Đông',
    price: 790000,
    stock: 12,
    categoryId: 'bst',
    collection: 'Monochrome Collection',
    faceShapes: ['trai_xoan', 'trai_tim', 'tron'],
    gender: 'unisex',
    colors: ['#808080', '#1A1614'],
    images: [],
  },

  // ── Bộ Sưu Tập: Essential Acetate ──────────────────────────────────
  {
    name: 'Acetate Havana',
    description: 'Acetate vân đồi mồi ấm áp, cổ điển Ý sang trọng',
    price: 980000,
    stock: 8,
    categoryId: 'bst',
    collection: 'Essential Acetate',
    faceShapes: ['tron', 'trai_xoan', 'vuong'],
    gender: 'unisex',
    colors: ['#8B4513', '#D4AF37'],
    images: [],
  },
  {
    name: 'Acetate Ivory',
    description: 'Acetate ngà trắng nhẹ nhàng, tinh tế cho phái nữ',
    price: 920000,
    stock: 10,
    categoryId: 'bst',
    collection: 'Essential Acetate',
    faceShapes: ['trai_tim', 'trai_xoan', 'tron'],
    gender: 'nu',
    colors: ['#FFFFF0', '#C0C0C0'],
    images: [],
  },

  // ── Bộ Sưu Tập: Sunlight Studio ────────────────────────────────────
  {
    name: 'Sunlight Amber',
    description: 'Tròng hổ phách đẹp dưới ánh mặt trời, lọc UV400',
    price: 1100000,
    stock: 6,
    categoryId: 'bst',
    collection: 'Sunlight Studio',
    faceShapes: ['tron', 'trai_xoan', 'trai_tim', 'vuong'],
    gender: 'unisex',
    colors: ['#D4AF37', '#8B4513'],
    images: [],
  },
  {
    name: 'Sunlight Ocean',
    description: 'Tròng xanh biển phân cực, lý tưởng cho hoạt động ngoài trời',
    price: 1050000,
    stock: 8,
    categoryId: 'bst',
    collection: 'Sunlight Studio',
    faceShapes: ['kim_cuong', 'vuong', 'trai_xoan'],
    gender: 'nam',
    colors: ['#1E90FF', '#1A1614'],
    images: [],
  },

  // ── Phụ Kiện ──────────────────────────────────────────────────────
  {
    name: 'Hộp Đựng Kính Da Cao Cấp',
    description: 'Hộp da PU bảo vệ kính tốt, có khóa nam châm sang trọng',
    price: 180000,
    stock: 30,
    categoryId: 'phu_kien',
    collection: null,
    faceShapes: [],
    gender: 'unisex',
    colors: ['#1A1614', '#8B4513'],
    images: [],
  },
  {
    name: 'Khăn Lau Kính Microfiber',
    description: 'Khăn siêu mịn lau sạch không để lại vệt, kèm túi đựng',
    price: 45000,
    stock: 100,
    categoryId: 'phu_kien',
    collection: null,
    faceShapes: [],
    gender: 'unisex',
    colors: ['#AAAAAA', '#1A1614'],
    images: [],
  },
  {
    name: 'Nước Lau Kính Chuyên Dụng',
    description: 'Dung dịch vệ sinh kính chuyên nghiệp, an toàn cho tròng phủ AR',
    price: 75000,
    stock: 50,
    categoryId: 'phu_kien',
    collection: null,
    faceShapes: [],
    gender: 'unisex',
    colors: [],
    images: [],
  },
];

// ─── SEED ──────────────────────────────────────────────────────────────────
async function seed() {
  console.log('=== Seed v2 bắt đầu ===\n');

  console.log('--- Ghi categories ---');
  for (const cat of CATEGORIES) {
    await db.collection('categories').doc(cat.id).set(cat);
    console.log(`  [OK] ${cat.id} — ${cat.name}`);
  }

  console.log('\n--- Ghi products ---');
  const batch = db.batch();
  for (const p of PRODUCTS) {
    const ref = db.collection('products').doc();
    batch.set(ref, {
      ...p,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });
    console.log(`  [OK] ${p.name} (${p.categoryId}${p.collection ? ' / ' + p.collection : ''})`);
  }
  await batch.commit();

  console.log(`\n=== Xong! ${CATEGORIES.length} categories, ${PRODUCTS.length} products ===`);
  process.exit(0);
}

seed().catch(err => {
  console.error('LOI:', err.message);
  process.exit(1);
});

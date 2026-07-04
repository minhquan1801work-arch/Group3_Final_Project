/**
 * add_face_shapes.js — Thêm field faceShapes vào từng product trong Firestore
 *
 * CACH DUNG:
 * 1. npm install firebase-admin  (nếu chưa có)
 * 2. Đặt service-account.json vào cùng thư mục
 * 3. node add_face_shapes.js
 *
 * Mapping dựa trên tên sản phẩm (theo seed_firestore.js):
 *   tron       = mặt tròn
 *   trai_xoan  = mặt trái xoan
 *   trai_tim   = mặt trái tim
 *   kim_cuong  = mặt kim cương
 *   vuong      = mặt vuông
 */

const admin = require('firebase-admin');
const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const serviceAccount = require('./service-account.json');

initializeApp({
  credential: cert(serviceAccount),
  projectId: 'glassity-770c5',
});

const db = getFirestore();

// Map tên product → faceShapes phù hợp (khớp theo keyword trong tên)
const shapeMappings = [
  { keywords: ['trong tron ho phanh', 'trong tron toi gian'], shapes: ['tron', 'trai_xoan'] },
  { keywords: ['Avant-Garde'],                                 shapes: ['kim_cuong', 'trai_xoan'] },
  { keywords: ['mat meo retro'],                               shapes: ['trai_tim', 'tron'] },
  { keywords: ['oval hoai co'],                                shapes: ['tron', 'trai_xoan'] },
  { keywords: ['oval hien dai'],                               shapes: ['tron', 'trai_xoan'] },
  { keywords: ['vuong kim loai toi gian', 'vuong hien dai', 'vuong duong pho', 'vuong ca tinh'], shapes: ['vuong', 'kim_cuong'] },
  { keywords: ['kim loai manh'],                               shapes: ['trai_xoan', 'trai_tim'] },
  { keywords: ['Aero Future', 'tuong lai'],                    shapes: ['kim_cuong', 'vuong'] },
  { keywords: ['Cyber'],                                       shapes: ['vuong', 'kim_cuong'] },
  { keywords: ['mat meo sang trong'],                          shapes: ['trai_tim', 'tron'] },
  { keywords: ['tong ho phanh sang trong'],                    shapes: ['trai_xoan', 'trai_tim'] },
  { keywords: ['kim loai anh kim', 'kim loai cao cap'],        shapes: ['trai_xoan', 'kim_cuong'] },
  { keywords: ['thiet ke doc dao'],                            shapes: ['trai_tim', 'kim_cuong'] },
  { keywords: ['gong kim loai duong pho'],                     shapes: ['tron', 'trai_xoan'] },
];

function getShapesForProduct(name) {
  const lower = name.toLowerCase();
  for (const mapping of shapeMappings) {
    for (const kw of mapping.keywords) {
      if (lower.includes(kw.toLowerCase())) return mapping.shapes;
    }
  }
  // Default: tất cả shapes (kính phổ quát)
  return ['tron', 'trai_xoan', 'trai_tim', 'kim_cuong', 'vuong'];
}

async function run() {
  console.log('=== Thêm faceShapes vào products ===\n');
  const snapshot = await db.collection('products').get();
  let count = 0;
  for (const doc of snapshot.docs) {
    const data = doc.data();
    if (data.faceShapes && data.faceShapes.length > 0) {
      console.log(`  [SKIP] ${data.name} — đã có faceShapes`);
      continue;
    }
    const shapes = getShapesForProduct(data.name);
    await doc.ref.update({ faceShapes: shapes });
    console.log(`  [OK] ${data.name} → [${shapes.join(', ')}]`);
    count++;
  }
  console.log(`\n=== Xong! Đã cập nhật ${count} products ===`);
  process.exit(0);
}

run().catch(err => {
  console.error('LOI:', err.message);
  process.exit(1);
});

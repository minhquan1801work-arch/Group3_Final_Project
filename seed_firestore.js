/**
 * seed_firestore.js — Tu dong tao 20 san pham + 4 categories vao Firestore
 *
 * CACH DUNG:
 * 1. npm install firebase-admin
 * 2. Tai service-account.json tu Firebase Console:
 *    Project Settings -> Service accounts -> Generate new private key -> Download
 * 3. Dat file service-account.json vao cung thu muc voi file nay (Group3_Final_Project/)
 * 4. node seed_firestore.js
 */

// Compatible voi ca firebase-admin v11 lan v12+
const admin = require('firebase-admin');
const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const serviceAccount = require('./service-account.json');

initializeApp({
  credential: cert(serviceAccount),
  projectId: 'glassity-770c5',
});

const db = getFirestore();

// CATEGORIES
const categories = [
  { id: 'retro',   name: 'Retro',   description: 'Phong cach hoai co, dam net vintage' },
  { id: 'minimal', name: 'Minimal', description: 'Toi gian, tinh te, hien dai' },
  { id: 'bold',    name: 'Bold',    description: 'Ca tinh, noi bat, phong cach duong pho' },
  { id: 'luxury',  name: 'Luxury',  description: 'Sang trong, chat lieu cao cap' },
];

// PRODUCTS (images: [] - cap nhat sau khi upload len Storage)
const products = [
  { name: 'Kinh mat trong tron ho phanh',       price: 990000,  stock: 20, description: 'Kinh mat trang tron trong suot mau ho phanh, phong cach vintage.', categoryId: 'retro',   colors: ['#C88B3A', '#1A1614'], images: [] },
  { name: 'Kinh mat Avant-Garde',               price: 1190000, stock: 15, description: 'Thiet ke avant-garde doc dao, phu hop nhung ai yeu phong cach nghe thuat.', categoryId: 'retro',   colors: ['#1A1614', '#AC9C8D'], images: [] },
  { name: 'Kinh mat mat meo retro',             price: 1090000, stock: 18, description: 'Gong mat meo phong cach nhung nam 60, sang trong va quyen ru.',     categoryId: 'retro',   colors: ['#1A1614', '#72383D'], images: [] },
  { name: 'Kinh mat oval hoai co',              price: 990000,  stock: 20, description: 'Oval hoai co ket hop hoan hao giua phong cach co dien va hien dai.', categoryId: 'retro',   colors: ['#C88B3A', '#AC9C8D'], images: [] },
  { name: 'Kinh mat trong tron toi gian',       price: 890000,  stock: 25, description: 'Thiet ke toi gian tong trang tran, phu hop voi moi trang phuc.',    categoryId: 'minimal', colors: ['#FFFFFF', '#1A1614'], images: [] },
  { name: 'Kinh mat vuong kim loai toi gian',   price: 990000,  stock: 20, description: 'Gong kim loai mong manh, trong vuong goc can, toi gian nhung sang trong.', categoryId: 'minimal', colors: ['#C0C0C0', '#C8A96E'], images: [] },
  { name: 'Kinh mat oval hien dai',             price: 950000,  stock: 22, description: 'Kieu dang oval hien dai, nhe nhang va thoai mai khi deo.',           categoryId: 'minimal', colors: ['#1A1614', '#C0C0C0'], images: [] },
  { name: 'Kinh mat vuong hien dai',            price: 950000,  stock: 22, description: 'Gong vuong hien dai, phu hop ca nam lan nu, linh hoat phong cach.',  categoryId: 'minimal', colors: ['#1A1614', '#AC9C8D'], images: [] },
  { name: 'Kinh mat kim loai manh',             price: 890000,  stock: 20, description: 'Gong kim loai sieu manh, thiet ke tinh te nhu tuong nhu vo.',        categoryId: 'minimal', colors: ['#C8A96E', '#C0C0C0'], images: [] },
  { name: 'Kinh mat Aero Future',               price: 1190000, stock: 15, description: 'Phong cach tuong lai voi thiet ke khi dong hoc, noi bat trong dam dong.', categoryId: 'bold',    colors: ['#1A1614', '#4A90D9'], images: [] },
  { name: 'Kinh mat gong vuong ca tinh',        price: 1090000, stock: 18, description: 'Gong vuong day ban, manh me va ca tinh, danh cho nguoi dam khac biet.', categoryId: 'bold',    colors: ['#1A1614', '#72383D'], images: [] },
  { name: 'Kinh mat phong cach Cyber',          price: 1290000, stock: 12, description: 'Cam hung tu the gioi cyberpunk, tao bao va doc dao nhat bo suu tap.',  categoryId: 'bold',    colors: ['#1A1614', '#4A4A4A'], images: [] },
  { name: 'Kinh mat tuong lai',                 price: 1190000, stock: 15, description: 'Thiet ke futuristic voi duong net sac ben, phong cach manh me.',      categoryId: 'bold',    colors: ['#C0C0C0', '#1A1614'], images: [] },
  { name: 'Kinh mat thiet ke doc dao',          price: 1090000, stock: 18, description: 'Phong cach thiet ke khong the nham lan, danh cho nguoi yeu nghe thuat.', categoryId: 'bold',    colors: ['#72383D', '#1A1614'], images: [] },
  { name: 'Kinh mat gong kim loai duong pho',   price: 990000,  stock: 20, description: 'Chat street style voi gong kim loai, sang trong pha nang dong.',     categoryId: 'bold',    colors: ['#C0C0C0', '#C8A96E'], images: [] },
  { name: 'Kinh mat vuong duong pho',           price: 990000,  stock: 20, description: 'Vuong goc ca tinh, style duong pho than thiet, phu hop moi dip.',    categoryId: 'bold',    colors: ['#1A1614', '#AC9C8D'], images: [] },
  { name: 'Kinh mat tong ho phanh sang trong',  price: 1890000, stock: 10, description: 'Mau ho phanh am ap ket hop chat lieu cao cap, bieu tuong sang trong.', categoryId: 'luxury',  colors: ['#C88B3A', '#8B6914'], images: [] },
  { name: 'Kinh mat mat meo sang trong',        price: 1990000, stock: 8,  description: 'Gong mat meo voi duong net menh mai, chat lieu acetate cao cap nhap khau.', categoryId: 'luxury',  colors: ['#1A1614', '#C8A96E'], images: [] },
  { name: 'Kinh mat gong kim loai anh kim',     price: 2190000, stock: 8,  description: 'Gong kim loai anh kim sang loang, xuat xu tu xuong san xuat hang dau.', categoryId: 'luxury',  colors: ['#C8A96E', '#C0C0C0'], images: [] },
  { name: 'Kinh mat gong kim loai cao cap',     price: 2290000, stock: 6,  description: 'Dinh cao su tinh te, gong kim loai cao cap thi cong tay tu cac nghe nhan.', categoryId: 'luxury',  colors: ['#C8A96E', '#1A1614'], images: [] },
];

async function seed() {
  console.log('=== Glassity Firestore Seeder ===\n');

  console.log('Dang tao categories...');
  for (const cat of categories) {
    await db.collection('categories').doc(cat.id).set(cat);
    console.log('  [OK] category:', cat.id);
  }

  console.log('\nDang tao products...');
  for (const prod of products) {
    const ref = await db.collection('products').add(prod);
    console.log('  [OK]', ref.id, '--', prod.name);
  }

  console.log('\n=== Xong! ===');
  console.log('- 4 categories da tao');
  console.log('- 20 products da tao (images: [] -- cap nhat sau khi upload len Storage)');
  process.exit(0);
}

seed().catch(err => {
  console.error('LOI:', err.message);
  process.exit(1);
});

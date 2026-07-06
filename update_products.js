/**
 * update_products.js — cap nhat color/faceShapes/gender/price/description/collection
 * cho 20 san pham da upload len Firestore.
 * Chay: node update_products.js
 */

const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore }        = require('firebase-admin/firestore');
const sa = require('./service-account.json');

initializeApp({ credential: cert(sa) });
const db = getFirestore();

const PRODUCTS = [
  {
    name: 'Bold Square Frame Sunglasses',
    variantColors: ['#7B3F1A','#7B3F1A','#1A1A1A','#E8B84B','#8B4513'],
    stock: 10, faceShapes: ['tron','trai_xoan'], gender: 'unisex', price: 890000,
    description: 'Gong vuong day dan voi thiet ke bold co dien, trong toi mau mang phong cach thoi trang duong pho manh me.',
    collection: 'Essential Acetate',
  },
  {
    name: 'Modern Square Sunglasses Style',
    variantColors: ['#B5A898','#7B3F1A','#1A1A1A','#D4C5BB','#6B8FA3'],
    stock: 10, faceShapes: ['tron','trai_xoan'], gender: 'unisex', price: 790000,
    description: 'Gong vuong hien dai thanh lich, da dang mau sac tu trung tinh den xanh duong, phu hop voi nhieu phong cach.',
    collection: 'Essential Acetate',
  },
  {
    name: 'Retro Oval Sunglasses Style',
    variantColors: ['#C0C0C0','#C0C0C0','#D4A017','#2D2D2D'],
    stock: 10, faceShapes: ['tron','vuong'], gender: 'unisex', price: 750000,
    description: 'Gong oval retro voi chat lieu kim loai bong, tao net co dien sang trong cho moi trang phuc.',
    collection: 'Essential Acetate',
  },
  {
    name: 'Amber Clear Round Sunglasses Style',
    variantColors: ['#7B3F1A','#1A1A1A','#E8B84B','#7B3F1A','#C4B59A'],
    stock: 10, faceShapes: ['vuong','trai_xoan'], gender: 'unisex', price: 750000,
    description: 'Gong tron trong suot tong amber am ap, kieu dang nhe nhang lang man phu hop ca nam lan nu.',
    collection: 'Sunlight Studio',
  },
  {
    name: 'Minimal Metal Square Sunglasses',
    variantColors: ['#C0C0C0','#2D2D2D','#D4A017','#D4A017'],
    stock: 10, faceShapes: ['tron','trai_xoan'], gender: 'unisex', price: 690000,
    description: 'Gong vuong kim loai toi gian, thiet ke mong nhe, de dang ket hop voi nhieu phong cach khac nhau.',
    collection: 'Essential Acetate',
  },
  {
    name: 'Modern Oval Sunglasses Design',
    variantColors: ['#1A1A1A','#7B3F1A','#6B1A1A','#E8E0CC'],
    stock: 10, faceShapes: ['tron','vuong'], gender: 'nu', price: 850000,
    description: 'Gong oval hien dai danh cho nu, duong cong tinh te noi bat, gam mau da dang tu den den be kem nha nhan.',
    collection: 'Sunlight Studio',
  },
  {
    name: 'Urban Metal Frame Sunglasses',
    variantColors: ['#D4A017','#2D2D2D','#D4A017','#D4A017'],
    stock: 10, faceShapes: ['tron','trai_xoan'], gender: 'unisex', price: 690000,
    description: 'Gong kim loai mong phong cach do thi, guong vang anh kim noi bat, ly tuong cho phong cach streetwear.',
    collection: 'Essential Acetate',
  },
  {
    name: 'Unique Design Fashion Sunglasses',
    variantColors: ['#F0F0F0','#1A1A1A','#7B4A2D','#2D4A6B'],
    stock: 10, faceShapes: ['tron','trai_xoan'], gender: 'unisex', price: 790000,
    description: 'Thiet ke doc dao khong theo loi mon, tao diem nhan an tuong voi 4 tong mau ca tinh tu trang den xanh navy.',
    collection: 'Monochrome Collection',
  },
  {
    name: 'Futuristic Fashion Sunglasses',
    variantColors: ['#F0F0F0','#1A1A1A','#4A5C3E','#1A1A1A'],
    stock: 10, faceShapes: ['tron','trai_xoan'], gender: 'unisex', price: 890000,
    description: 'Kieu dang tuong lai tao bao voi gong oversized, phu hop cho nhung ai yeu thich phong cach avant-garde.',
    collection: 'Monochrome Collection',
  },
  {
    name: 'Slim Metal Fashion Sunglasses',
    variantColors: ['#C0C0C0','#D4A017','#C0C0C0','#D4A017'],
    stock: 10, faceShapes: ['tron','trai_xoan'], gender: 'unisex', price: 650000,
    description: 'Gong kim loai sieu mong thanh thoat, hai tong bac va vang gold sang trong, phu hop voi phong cach toi gian.',
    collection: 'Essential Acetate',
  },
  {
    name: 'Luxury Amber Tone Sunglasses Style',
    variantColors: ['#1A1A1A','#2D1A00','#C17A3C','#B5A898','#E8D48A','#2D7BA6'],
    stock: 10, faceShapes: ['vuong','tron'], gender: 'nu', price: 1290000,
    description: 'Kinh xa xi tong amber vang am, 6 mau sac da dang tu co dien den hien dai, danh rieng cho phai nu tinh te.',
    collection: 'Sunlight Studio',
  },
  {
    name: 'Premium Metal Frame Sunglasses',
    variantColors: ['#D4A017','#2D2D2D','#D4A017','#C0C0C0'],
    stock: 10, faceShapes: ['tron','trai_xoan'], gender: 'unisex', price: 890000,
    description: 'Gong kim loai cao cap voi hoan thien be mat sac sao, ket hop mau vang gold va bac tao ve dang cap.',
    collection: 'Essential Acetate',
  },
  {
    name: 'Retro Cat Eye Sunglasses Style',
    variantColors: ['#1A1A1A','#4A2D1A','#F0EDE0','#E8A87A'],
    stock: 10, faceShapes: ['tron','trai_xoan'], gender: 'nu', price: 790000,
    description: 'Gong mat meo retro quyen ru danh cho nu, duong cong vut nhon goi cam, 4 mau tu den huyen den cam dat.',
    collection: 'Sunlight Studio',
  },
  {
    name: 'Urban Square Fashion Sunglasses',
    variantColors: ['#7B3F1A','#1A1A1A','#8B5A2B','#E8E8E8','#4A6B8A'],
    stock: 10, faceShapes: ['tron','trai_xoan'], gender: 'unisex', price: 750000,
    description: 'Gong vuong urban voi 5 tuy chon mau sac phong phu, tu nau dat den xanh thep, de dang mix & match.',
    collection: 'Essential Acetate',
  },
  {
    name: 'Avant Garde Sunglasses Style',
    variantColors: ['#1A1A1A','#1A1A1A','#1A1A1A'],
    stock: 10, faceShapes: ['tron','trai_xoan'], gender: 'unisex', price: 1190000,
    description: 'Gong wrap cyberpunk sieu co all-black tao bao, lay cam hung tu thoi trang cao cap, danh cho nhung ai dam noi bat.',
    collection: 'Monochrome Collection',
  },
  {
    name: 'Aero Future Sunglasses Look',
    variantColors: ['#F5F5F5','#F5F5F5'],
    stock: 10, faceShapes: ['tron','trai_xoan'], gender: 'unisex', price: 1190000,
    description: 'Gong wrap trang bong phong cach tuong lai, thiet ke khi dong hoc om sat khuon mat, cuc ky an tuong.',
    collection: 'Monochrome Collection',
  },
  {
    name: 'Cyber Fashion Sunglasses',
    variantColors: ['#1A1A1A','#1A1A1A'],
    stock: 10, faceShapes: ['tron','trai_xoan'], gender: 'unisex', price: 1090000,
    description: 'Gong angular den bong voi hinh hoc doc la, phong cach cyber manh me cho cac fashion-forward trendsetter.',
    collection: 'Monochrome Collection',
  },
  {
    name: 'Metallic Future Frame Sunglasses',
    variantColors: ['#C0C0C0','#C0C0C0','#C0C0C0'],
    stock: 10, faceShapes: ['tron','trai_xoan'], gender: 'unisex', price: 1190000,
    description: 'Gong wrap kim loai bac phan chieu anh sang, thiet ke tuong lai cuc an tuong voi trong guong chrome.',
    collection: 'Monochrome Collection',
  },
  {
    name: 'Luxury Cat Eye Sunglasses Design',
    variantColors: ['#6B1A2A','#1A1A1A','#F5F5F5'],
    stock: 10, faceShapes: ['trai_tim','tron'], gender: 'nu', price: 1390000,
    description: 'Gong mat meo cao cap day dan voi 3 tong mau sang trong: do bordeaux, den va trang — bieu tuong cua su quyen ru.',
    collection: 'Sunlight Studio',
  },
  {
    name: 'Minimal Clear Round Sunglasses Design',
    variantColors: ['#2D6B4A','#7B6B4A','#7A8B7A','#7B6040','#B0B8C0','#7B5030','#1A1A1A'],
    stock: 10, faceShapes: ['tron','trai_xoan'], gender: 'unisex', price: 890000,
    description: 'Gong tron trong suot acetate voi 7 mau phong phu, phong cach toi gian tri thuc tinh te cho moi khuon mat.',
    collection: 'Essential Acetate',
  },
];

async function main() {
  const snapshot = await db.collection('products').get();
  const docsMap = {};
  for (const doc of snapshot.docs) {
    const d = doc.data();
    if (d.name) docsMap[d.name] = { ref: doc.ref, data: d };
  }

  let updated = 0, notFound = 0;

  for (const p of PRODUCTS) {
    const entry = docsMap[p.name];
    if (!entry) {
      console.log('NOT FOUND: ' + p.name);
      notFound++;
      continue;
    }

    const existingVariants = entry.data.variants || [];
    const newVariants = existingVariants.map((v, i) => ({
      ...v,
      color: p.variantColors[i] !== undefined
        ? p.variantColors[i]
        : p.variantColors[p.variantColors.length - 1],
      stock: p.stock,
    }));

    await entry.ref.update({
      variants:    newVariants,
      faceShapes:  p.faceShapes,
      gender:      p.gender,
      price:       p.price,
      description: p.description,
      collection:  p.collection,
    });

    console.log('OK [' + newVariants.length + ' variants]: ' + p.name);
    updated++;
  }

  console.log('\nDone. ' + updated + ' updated, ' + notFound + ' not found.');
}

main().catch(err => { console.error('Fatal:', err); process.exit(1); });
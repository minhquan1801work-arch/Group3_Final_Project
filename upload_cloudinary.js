/**
 * upload_cloudinary.js — Upload anh len Cloudinary + update Firestore
 * Chay: node upload_cloudinary.js
 */

const { v2: cloudinary } = require('cloudinary');
const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const fs = require('fs');
const path = require('path');

// Cloudinary config
cloudinary.config({
  cloud_name: 'aa1g9udv',
  api_key: '552433829675378',
  api_secret: 'tMSO733g0Di0nMqq4f7_vTIJgYE',
});

// Firebase config
initializeApp({
  credential: cert(require('./service-account.json')),
  projectId: 'glassity-770c5',
});
const db = getFirestore();

// Map: ten folder Assets_final -> ten san pham trong Firestore
const FOLDER_TO_NAME = {
  'AMBER CLEAR ROUND SUNGLASSES STYLE':    'Kinh mat trong tron ho phanh',
  'AVANT GARDE SUNGLASSES STYLE':          'Kinh mat Avant-Garde',
  'RETRO CAT EYE SUNGLASSES STYLE':        'Kinh mat mat meo retro',
  'RETRO OVAL SUNGLASSES STYLE':           'Kinh mat oval hoai co',
  'MINIMAL CLEAR ROUND SUNGLASSES DESIGN': 'Kinh mat trong tron toi gian',
  'MINIMAL METAL SQUARE SUNGLASSES':       'Kinh mat vuong kim loai toi gian',
  'MODERN OVAL SUNGLASSES DESIGN':         'Kinh mat oval hien dai',
  'MODERN SQUARE SUNGLASSES STYLE':        'Kinh mat vuong hien dai',
  'SLIM METAL FASHION SUNGLASSES':         'Kinh mat kim loai manh',
  'AERO FUTURE SUNGLASSES LOOK':           'Kinh mat Aero Future',
  'BOLD SQUARE FRAME SUNGLASSES':          'Kinh mat gong vuong ca tinh',
  'CYBER FASHION SUNGLASSES':              'Kinh mat phong cach Cyber',
  'FUTURISTIC FASHION SUNGLASSES':         'Kinh mat tuong lai',
  'UNIQUE DESIGN FASHION SUNGLASSES':      'Kinh mat thiet ke doc dao',
  'URBAN METAL FRAME SUNGLASSES':          'Kinh mat gong kim loai duong pho',
  'URBAN SQUARE FASHION SUNGLASSES':       'Kinh mat vuong duong pho',
  'LUXURY AMBER TONE SUNGLASSES STYLE':    'Kinh mat tong ho phanh sang trong',
  'LUXURY CAT EYE SUNGLASSES DESIGN':      'Kinh mat mat meo sang trong',
  'METALLIC FUTURE FRAME SUNGLASSES':      'Kinh mat gong kim loai anh kim',
  'PREMIUM METAL FRAME SUNGLASSES':        'Kinh mat gong kim loai cao cap',
};

// Thu muc anh (thu tieng Viet truoc, fallback ASCII)
function findAssetsDir() {
  const candidates = [
    path.join(__dirname, '..', 'Assets_final', 'kính-20260630T042449Z-3-001', 'kính'),
    path.join(__dirname, '..', 'Assets_final', 'kinh-20260630T042449Z-3-001', 'kinh'),
  ];
  for (const p of candidates) {
    if (fs.existsSync(p)) return p;
  }
  return null;
}

function uploadToCloudinary(localPath, publicId) {
  return new Promise((resolve, reject) => {
    cloudinary.uploader.upload(localPath, {
      public_id: publicId,
      folder: 'glassity/products',
      overwrite: true,
      resource_type: 'image',
    }, (err, result) => {
      if (err) reject(err);
      else resolve(result.secure_url);
    });
  });
}

async function run() {
  console.log('=== Glassity Cloudinary Uploader ===\n');

  const assetsDir = findAssetsDir();
  if (!assetsDir) {
    console.error('Khong tim thay thu muc Assets_final!');
    process.exit(1);
  }
  console.log('Assets dir:', assetsDir, '\n');

  // Lay products tu Firestore
  const snapshot = await db.collection('products').get();
  const productMap = {};
  snapshot.forEach(doc => {
    productMap[doc.data().name] = { id: doc.id, ...doc.data() };
  });
  console.log(`Tim thay ${Object.keys(productMap).length} products trong Firestore\n`);

  let successCount = 0;
  const folders = fs.readdirSync(assetsDir)
    .filter(f => fs.statSync(path.join(assetsDir, f)).isDirectory());

  for (const folder of folders) {
    const productName = FOLDER_TO_NAME[folder];
    if (!productName) { console.log(`[SKIP] Khong co mapping: ${folder}`); continue; }

    const product = productMap[productName];
    if (!product) { console.log(`[SKIP] Khong co trong Firestore: "${productName}"`); continue; }

    const folderPath = path.join(assetsDir, folder);
    const pngFiles = fs.readdirSync(folderPath)
      .filter(f => f.toLowerCase().endsWith('.png'))
      .sort((a, b) => parseInt(a) - parseInt(b))
      .slice(0, 2);

    if (pngFiles.length === 0) { console.log(`[SKIP] Khong co PNG: ${folder}`); continue; }

    console.log(`[UPLOAD] ${folder}`);
    const urls = [];
    for (let i = 0; i < pngFiles.length; i++) {
      const localPath = path.join(folderPath, pngFiles[i]);
      const publicId = `${product.id}_${i + 1}`;
      try {
        const url = await uploadToCloudinary(localPath, publicId);
        urls.push(url);
        console.log(`  -> img${i + 1}: OK`);
      } catch (e) {
        console.log(`  -> img${i + 1} LOI: ${e.message}`);
      }
    }

    if (urls.length > 0) {
      await db.collection('products').doc(product.id).update({ images: urls });
      console.log(`  -> Firestore updated\n`);
      successCount++;
    }
  }

  console.log(`\n=== Xong! ${successCount}/${folders.length} san pham da co anh ===`);
  process.exit(0);
}

run().catch(err => { console.error('LOI:', err.message); process.exit(1); });

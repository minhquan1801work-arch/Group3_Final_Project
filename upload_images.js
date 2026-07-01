/**
 * upload_images.js — Tu dong upload anh len Firebase Storage + cap nhat Firestore
 *
 * CACH DUNG:
 *   npm install firebase-admin uuid
 *   node upload_images.js
 *
 * Script se:
 *   1. Doc tung folder anh trong Assets_final/kinh-.../kinh/
 *   2. Upload toi da 2 anh dau tien len Storage tai products/<productId>/
 *   3. Lay download URL (vinh vien, co token)
 *   4. Update field `images` trong Firestore cho dung product
 */

const admin = require('firebase-admin');
const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const { getStorage } = require('firebase-admin/storage');
const { v4: uuidv4 } = require('uuid');
const fs = require('fs');
const path = require('path');

initializeApp({
  credential: cert(require('./service-account.json')),
  projectId: 'glassity-770c5',
  storageBucket: 'glassity-770c5.firebasestorage.app', // neu loi thi doi thanh: 'glassity-770c5.appspot.com'
});

const db = getFirestore();
const bucket = getStorage().bucket();

// Map: ten folder Assets_final -> ten san pham (khop voi ten da seed vao Firestore)
const FOLDER_TO_NAME = {
  'AMBER CLEAR ROUND SUNGLASSES STYLE':   'Kinh mat trong tron ho phanh',
  'AVANT GARDE SUNGLASSES STYLE':         'Kinh mat Avant-Garde',
  'RETRO CAT EYE SUNGLASSES STYLE':       'Kinh mat mat meo retro',
  'RETRO OVAL SUNGLASSES STYLE':          'Kinh mat oval hoai co',
  'MINIMAL CLEAR ROUND SUNGLASSES DESIGN':'Kinh mat trong tron toi gian',
  'MINIMAL METAL SQUARE SUNGLASSES':      'Kinh mat vuong kim loai toi gian',
  'MODERN OVAL SUNGLASSES DESIGN':        'Kinh mat oval hien dai',
  'MODERN SQUARE SUNGLASSES STYLE':       'Kinh mat vuong hien dai',
  'SLIM METAL FASHION SUNGLASSES':        'Kinh mat kim loai manh',
  'AERO FUTURE SUNGLASSES LOOK':          'Kinh mat Aero Future',
  'BOLD SQUARE FRAME SUNGLASSES':         'Kinh mat gong vuong ca tinh',
  'CYBER FASHION SUNGLASSES':             'Kinh mat phong cach Cyber',
  'FUTURISTIC FASHION SUNGLASSES':        'Kinh mat tuong lai',
  'UNIQUE DESIGN FASHION SUNGLASSES':     'Kinh mat thiet ke doc dao',
  'URBAN METAL FRAME SUNGLASSES':         'Kinh mat gong kim loai duong pho',
  'URBAN SQUARE FASHION SUNGLASSES':      'Kinh mat vuong duong pho',
  'LUXURY AMBER TONE SUNGLASSES STYLE':   'Kinh mat tong ho phanh sang trong',
  'LUXURY CAT EYE SUNGLASSES DESIGN':     'Kinh mat mat meo sang trong',
  'METALLIC FUTURE FRAME SUNGLASSES':     'Kinh mat gong kim loai anh kim',
  'PREMIUM METAL FRAME SUNGLASSES':       'Kinh mat gong kim loai cao cap',
};

// Duong dan thu muc anh (tuong doi tu vi tri script)
const ASSETS_DIR = path.join(__dirname, '..', 'Assets_final',
  'kinh-20260630T042449Z-3-001', 'kinh');

// Fallback: neu thu muc ten tieng Viet
const ASSETS_DIR_VN = path.join(__dirname, '..', 'Assets_final',
  'kính-20260630T042449Z-3-001', 'kính');

async function uploadFile(localPath, storagePath) {
  const token = uuidv4();
  const buffer = fs.readFileSync(localPath);
  await bucket.file(storagePath).save(buffer, {
    metadata: {
      contentType: 'image/png',
      metadata: { firebaseStorageDownloadTokens: token },
    },
  });
  // URL vinh vien co token (giong URL Firebase Console)
  const encodedPath = encodeURIComponent(storagePath).replace(/%2F/g, '%2F');
  return `https://firebasestorage.googleapis.com/v0/b/${bucket.name}/o/${encodedPath}?alt=media&token=${token}`;
}

async function run() {
  console.log('=== Glassity Image Uploader ===\n');

  // Xac dinh thu muc assets
  let assetsDir = null;
  if (fs.existsSync(ASSETS_DIR_VN)) assetsDir = ASSETS_DIR_VN;
  else if (fs.existsSync(ASSETS_DIR)) assetsDir = ASSETS_DIR;
  else {
    console.error('Khong tim thay thu muc Assets_final. Kiem tra lai duong dan.');
    process.exit(1);
  }
  console.log('Tim thay assets tai:', assetsDir, '\n');

  // Lay tat ca products tu Firestore
  const snapshot = await db.collection('products').get();
  const productMap = {}; // name -> { id, ... }
  snapshot.forEach(doc => {
    productMap[doc.data().name] = { id: doc.id, ...doc.data() };
  });
  console.log(`Tim thay ${Object.keys(productMap).length} products trong Firestore\n`);

  let successCount = 0;
  const folders = fs.readdirSync(assetsDir).filter(f =>
    fs.statSync(path.join(assetsDir, f)).isDirectory()
  );

  for (const folder of folders) {
    const productName = FOLDER_TO_NAME[folder];
    if (!productName) {
      console.log(`[SKIP] Khong co mapping cho folder: ${folder}`);
      continue;
    }
    const product = productMap[productName];
    if (!product) {
      console.log(`[SKIP] Khong tim thay product trong Firestore: "${productName}"`);
      continue;
    }

    // Lay toi da 2 file PNG, sap xep theo ten
    const folderPath = path.join(assetsDir, folder);
    const pngFiles = fs.readdirSync(folderPath)
      .filter(f => f.toLowerCase().endsWith('.png'))
      .sort((a, b) => parseInt(a) - parseInt(b))
      .slice(0, 2);

    if (pngFiles.length === 0) {
      console.log(`[SKIP] Khong co PNG trong: ${folder}`);
      continue;
    }

    console.log(`[UPLOAD] ${folder}`);
    const urls = [];
    for (let i = 0; i < pngFiles.length; i++) {
      const localPath = path.join(folderPath, pngFiles[i]);
      const storagePath = `products/${product.id}/${i + 1}.png`;
      try {
        const url = await uploadFile(localPath, storagePath);
        urls.push(url);
        console.log(`  -> ${storagePath} OK`);
      } catch (e) {
        console.log(`  -> LOI: ${e.message}`);
      }
    }

    // Cap nhat Firestore
    if (urls.length > 0) {
      await db.collection('products').doc(product.id).update({ images: urls });
      console.log(`  -> Firestore updated (${urls.length} anh)\n`);
      successCount++;
    }
  }

  console.log(`\n=== Xong! ${successCount}/${folders.length} san pham da co anh ===`);
  process.exit(0);
}

run().catch(err => {
  console.error('LOI:', err.message);
  process.exit(1);
});

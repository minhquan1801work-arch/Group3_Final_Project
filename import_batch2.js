/**
 * import_batch2.js — upload anh + tao/update products tu dot 2 (10/07/2026)
 * - 6 Glassity kinh_can: update doc cu (anh moi + field tu sheet)
 * - 32 san pham moi: tao doc moi
 * - 6 phu kien: update anh vao doc cu (giu gia/mo ta cu)
 * Mau variant (color/colorName): dien o buoc sau (xem anh).
 */
const { v2: cloudinary } = require('cloudinary');
const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const fs = require('fs');
const path = require('path');

cloudinary.config(require('./cloudinary-config.json'));
initializeApp({ credential: cert(require('./service-account.json')) });
const db = getFirestore();

const KINH_ROOT = 'D:/Bat tu o C qua/Downloads/kính-20260710T100700Z-2-001/kính';
const PK_ROOT   = 'D:/Bat tu o C qua/Downloads/PHU KIEN';

// Sheet data: name -> {price, cat, faceShapes, frameShape, material, gender}
// (frameShape/material chi ghi cho kinh_can — kinh_mat bo qua theo quyet dinh team)
const SHEET = {
  'SLIM SQUARE DUAL-TONE EYEGLASSES':      { price: 890000,  cat: 'kinh_can', fs: ['tron','trai_xoan'],      shape: 'oval',    mat: 'nhua',     g: 'unisex' },
  'ROUND TORTOISE METAL SUNGLASSES':       { price: 1090000, cat: 'kinh_mat', fs: ['kim_cuong','vuong'],     shape: null,      mat: null,       g: 'unisex' },
  'RECTANGLE TECH-POLY SUNGLASSES':        { price: 890000,  cat: 'kinh_mat', fs: ['tron','trai_tim'],       shape: null,      mat: null,       g: 'nam' },
  'URBAN PANTO EYEGLASSES':                { price: 790000,  cat: 'kinh_can', fs: ['tron','trai_tim'],       shape: 'oval',    mat: 'nhua',     g: 'unisex' },
  'COLOR BLOCK SHAPE SUNGLASSES':          { price: 990000,  cat: 'kinh_mat', fs: ['tron'],                  shape: null,      mat: null,       g: 'unisex' },
  'CUT-OUT RECTANGULAR METAL EYEGLASSES':  { price: 1090000, cat: 'kinh_can', fs: ['vuong','trai_tim'],      shape: 'vuong',   mat: 'kim_loai', g: 'nu' },
  'RIVET SQUARE EYEGLASSES':               { price: 890000,  cat: 'kinh_can', fs: ['tron','trai_xoan'],      shape: 'vuong',   mat: 'nhua',     g: 'unisex' },
  'ROUNDED-SQUARE TR90 EYEGLASSES':        { price: 790000,  cat: 'kinh_can', fs: ['kim_cuong','trai_tim'],  shape: 'oval',    mat: 'nhua',     g: 'unisex' },
  'GEOMETRIC HYBRID METAL EYEGLASSES':     { price: 890000,  cat: 'kinh_can', fs: ['vuong','trai_tim'],      shape: 'tron',    mat: 'kim_loai', g: 'unisex' },
  'TRANSLUCENT CAT-EYE EYEGLASSES':        { price: 1190000, cat: 'kinh_can', fs: ['trai_tim','trai_xoan'],  shape: 'mat_meo', mat: 'nhua',     g: 'nu' },
  'POLYCARBONATE CAT-EYE EYEGLASSES':      { price: 790000,  cat: 'kinh_can', fs: ['kim_cuong','trai_tim'],  shape: 'mat_meo', mat: 'nhua',     g: 'nu' },
  'ANGULAR GEOMETRIC EYEGLASSES':          { price: 1090000, cat: 'kinh_can', fs: ['tron','trai_xoan'],      shape: 'vuong',   mat: 'nhua',     g: 'nu' },
  'HEXAGONAL WINDSOR RIM EYEGLASSES':      { price: 1290000, cat: 'kinh_can', fs: ['tron','trai_tim'],       shape: 'vuong',   mat: 'kim_loai', g: 'nu' },
  'CHIC OVERSIZED SQUARE SUNGLASSES':      { price: 990000,  cat: 'kinh_mat', fs: ['tron','trai_xoan'],      shape: null,      mat: null,       g: 'nu' },
  'SLEEK CAT-EYE TINTED SUNGLASSES':       { price: 889000,  cat: 'kinh_can', fs: ['tron','vuong'],          shape: 'mat_meo', mat: 'nhua',     g: 'nu' },
  'SOFT-CURVED OVAL CAT-EYE GLASSES':      { price: 920000,  cat: 'kinh_mat', fs: ['vuong','kim_cuong'],     shape: null,      mat: null,       g: 'nu' },
  'OLD CAT-EYE ACETATE SUNGLASSES':        { price: 780000,  cat: 'kinh_can', fs: ['tron','trai_tim'],       shape: 'mat_meo', mat: 'nhua',     g: 'unisex' },
  'CLASSIC SQUARE PLASTIC EYEGLASSES':     { price: 825000,  cat: 'kinh_can', fs: ['tron','trai_xoan'],      shape: 'vuong',   mat: 'nhua',     g: 'unisex' },
  'CLASSIC ROUND METAL EYEGLASSES':        { price: 1234000, cat: 'kinh_mat', fs: ['vuong','kim_cuong'],     shape: null,      mat: null,       g: 'unisex' },
  'SLIM OVAL METAL CAT-EYE EYEGLASSES':    { price: 675000,  cat: 'kinh_can', fs: ['vuong','trai_tim'],      shape: 'oval',    mat: 'kim_loai', g: 'nu' },
  'GEOMETRIC WIRE-FRAME METAL GLASSES':    { price: 799000,  cat: 'kinh_mat', fs: ['trai_xoan','kim_cuong'], shape: null,      mat: null,       g: 'unisex' },
  'OVERSIZED ROUNDED CAT-EYE SUNGLASSES':  { price: 989000,  cat: 'kinh_mat', fs: ['trai_tim','trai_xoan'],  shape: null,      mat: null,       g: 'unisex' },
  'MODERN OVAL CAT-EYE METAL EYEGLASSES':  { price: 899000,  cat: 'kinh_mat', fs: ['kim_cuong','vuong'],     shape: null,      mat: null,       g: 'nu' },
  'MINIMALIST WIRE-FRAME SUNGLASSES':      { price: 1099000, cat: 'kinh_mat', fs: ['kim_cuong'],             shape: null,      mat: null,       g: 'unisex' },
  'GLASSITY SOFT MINI':                    { price: 890000,  cat: 'kinh_can', fs: ['vuong','kim_cuong'],     shape: 'tron',    mat: 'nhua',     g: 'unisex' },
  'GLASSITY VISION ROUND':                 { price: 990000,  cat: 'kinh_can', fs: ['vuong','trai_tim'],      shape: 'tron',    mat: 'nhua',     g: 'unisex' },
  'GLASSITY OVAL FRAME':                   { price: 990000,  cat: 'kinh_can', fs: ['tron','trai_xoan'],      shape: 'oval',    mat: 'nhua',     g: 'unisex' },
  'GLASSITY VISION SQUARE':                { price: 990000,  cat: 'kinh_can', fs: ['tron','trai_xoan'],      shape: 'vuong',   mat: 'nhua',     g: 'nam' },
  'GLASSITY VISION AIR':                   { price: 790000,  cat: 'kinh_can', fs: ['vuong','kim_cuong'],     shape: 'tron',    mat: 'kim_loai', g: 'unisex' },
  'GLASSITY CLASSIC':                      { price: 890000,  cat: 'kinh_can', fs: ['kim_cuong','trai_tim'],  shape: 'tron',    mat: 'nhua',     g: 'unisex' },
  'SPARKLE BROWLINE EYEGLASSES':           { price: 1390000, cat: 'kinh_can', fs: ['tron','trai_xoan'],      shape: 'oval',    mat: 'kim_loai', g: 'nu' },
  'SLIM METAL CAT-EYE EYEGLASSES':         { price: 1490000, cat: 'kinh_can', fs: ['tron','vuong'],          shape: 'mat_meo', mat: 'kim_loai', g: 'nu' },
  'MODERN BUTTERFLY EYEGLASSES':           { price: 1290000, cat: 'kinh_can', fs: ['tron','vuong'],          shape: 'mat_meo', mat: 'nhua',     g: 'nu' },
  'SLIM SQUARE METAL EYEGLASSES':          { price: 790000,  cat: 'kinh_can', fs: ['tron','trai_xoan'],      shape: 'vuong',   mat: 'kim_loai', g: 'nam' },
  'FEMININE SQUARE METAL EYEGLASSES':      { price: 790000,  cat: 'kinh_can', fs: ['tron','trai_xoan'],      shape: 'vuong',   mat: 'kim_loai', g: 'nu' },
  'THICK SQUARE STAR EYEGLASSES':          { price: 1090000, cat: 'kinh_can', fs: ['tron','trai_tim'],       shape: 'vuong',   mat: 'nhua',     g: 'nam' },
  'SOFT SQUARE EYEGLASSES':                { price: 990000,  cat: 'kinh_can', fs: ['tron','kim_cuong'],      shape: 'vuong',   mat: 'nhua',     g: 'nam' },
  'URBAN SQUARE EYEGLASSES':               { price: 990000,  cat: 'kinh_can', fs: ['kim_cuong','trai_tim'],  shape: 'vuong',   mat: 'nhua',     g: 'unisex' },
};

// Phu kien: folder ASCII -> ten doc tieng Viet tren Firestore
const PK_MAP = {
  'DAY DEO KINH SILICON':       'Dây Đeo Kính Silicon',
  'HOP DUNG KINH DA CAO CAP':   'Hộp Đựng Kính Da Cao Cấp',
  'HOP DUNG KINH GLASSITY BOX': 'Hộp Đựng Kính Glassity Box',
  'KHAN LAU KINH MICROFIBER':   'Khăn Lau Kính Microfiber',
  'NUOC LAU KINH CHUYEN DUNG':  'Nước Lau Kính Chuyên Dụng 30ml',
  'TUI DUNG KINH NHUNG MEM':    'Túi Đựng Kính Nhung Mềm',
};

function toTitleCase(str) {
  return str.toLowerCase().replace(/(^|[\s-])\w/g, c => c.toUpperCase());
}
function randStock() { return 5 + Math.floor(Math.random() * 11); } // 5-15

function listImages(dir) {
  return fs.readdirSync(dir)
    .filter(f => /\.(png|jpg|jpeg|webp)$/i.test(f))
    .sort((a, b) => {
      const na = parseInt(a), nb = parseInt(b);
      if (!isNaN(na) && !isNaN(nb)) return na - nb;
      return a.localeCompare(b);
    });
}

async function upload(filePath, publicId) {
  const r = await cloudinary.uploader.upload(filePath, {
    public_id: publicId, overwrite: true, resource_type: 'image',
  });
  return r.secure_url;
}

async function main() {
  // ── KINH ──
  const folders = fs.readdirSync(KINH_ROOT).filter(f =>
      fs.statSync(path.join(KINH_ROOT, f)).isDirectory());

  for (const folder of folders) {
    const info = SHEET[folder.trim()];
    if (!info) { console.log('!! SHEET MISSING: ' + folder); continue; }

    const dir = path.join(KINH_ROOT, folder);
    const images = listImages(dir);
    const variants = [];
    for (let i = 0; i < images.length; i++) {
      const safe = folder.replace(/[^A-Za-z0-9 -]/g, '').trim();
      const url = await upload(path.join(dir, images[i]),
          `glassity/products/${safe}/${i + 1}`);
      variants.push({ color: '', colorName: '', stock: randStock(), images: [url] });
      process.stdout.write('.');
    }

    const name = toTitleCase(folder.trim());
    const data = {
      name, categoryId: info.cat, price: info.price,
      faceShapes: info.fs, gender: info.g,
      description: '', collection: '',
      variants,
    };
    if (info.cat === 'kinh_can') {
      data.frameShape = info.shape;
      data.material   = info.mat;
    }

    const existing = await db.collection('products')
        .where('name', '==', name).limit(1).get();
    if (!existing.empty) {
      await existing.docs[0].ref.update(data);
      console.log(` UPDATED: ${name} (${variants.length} variants)`);
    } else {
      await db.collection('products').add(data);
      console.log(` CREATED: ${name} (${variants.length} variants)`);
    }
  }

  // ── PHU KIEN: chi thay anh (giu gia/mo ta cu) ──
  for (const folder of Object.keys(PK_MAP)) {
    const dir = path.join(PK_ROOT, folder);
    const images = listImages(dir);
    const urls = [];
    for (let i = 0; i < images.length; i++) {
      urls.push(await upload(path.join(dir, images[i]),
          `glassity/products/${folder}/${i + 1}`));
      process.stdout.write('.');
    }
    const snap = await db.collection('products')
        .where('name', '==', PK_MAP[folder]).limit(1).get();
    if (snap.empty) { console.log(' !! DOC MISSING: ' + PK_MAP[folder]); continue; }
    await snap.docs[0].ref.update({ images: urls, stock: randStock() });
    console.log(` PK UPDATED: ${PK_MAP[folder]} (${urls.length} anh)`);
  }

  console.log('\nAll done!');
}
main().catch(e => { console.error(e); process.exit(1); });
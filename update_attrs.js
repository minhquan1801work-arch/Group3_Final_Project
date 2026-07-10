/**
 * update_attrs.js — them frameShape / material / accessoryType cho toan bo products.
 * frameShape: tron | oval | mat_meo | vuong
 * material:   nhua | kim_loai
 * accessoryType: hop_dung | khan_lau | nuoc_lau | tui_dung | day_deo
 */
const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
initializeApp({ credential: cert(require('./service-account.json')) });
const db = getFirestore();

// Kinh (mat + can): [frameShape, material]
const GLASSES = {
  'Bold Square Frame Sunglasses':          ['vuong',   'nhua'],
  'Modern Square Sunglasses Style':        ['vuong',   'nhua'],
  'Retro Oval Sunglasses Style':           ['oval',    'kim_loai'],
  'Amber Clear Round Sunglasses Style':    ['tron',    'nhua'],
  'Minimal Metal Square Sunglasses':       ['vuong',   'kim_loai'],
  'Modern Oval Sunglasses Design':         ['oval',    'nhua'],
  'Urban Metal Frame Sunglasses':          ['tron',    'kim_loai'],
  'Unique Design Fashion Sunglasses':      ['oval',    'nhua'],
  'Futuristic Fashion Sunglasses':         ['vuong',   'nhua'],
  'Slim Metal Fashion Sunglasses':         ['oval',    'kim_loai'],
  'Luxury Amber Tone Sunglasses Style':    ['vuong',   'nhua'],
  'Premium Metal Frame Sunglasses':        ['tron',    'kim_loai'],
  'Retro Cat Eye Sunglasses Style':        ['mat_meo', 'nhua'],
  'Urban Square Fashion Sunglasses':       ['vuong',   'nhua'],
  'Avant Garde Sunglasses Style':          ['oval',    'nhua'],
  'Aero Future Sunglasses Look':           ['oval',    'nhua'],
  'Cyber Fashion Sunglasses':              ['vuong',   'nhua'],
  'Metallic Future Frame Sunglasses':      ['oval',    'kim_loai'],
  'Luxury Cat Eye Sunglasses Design':      ['mat_meo', 'nhua'],
  'Minimal Clear Round Sunglasses Design': ['tron',    'nhua'],
  'Glassity Soft Mini':      ['tron',  'nhua'],
  'Glassity Vision Round':   ['tron',  'nhua'],
  'Glassity Oval Frame':     ['oval',  'nhua'],
  'Glassity Vision Square':  ['vuong', 'nhua'],
  'Glassity Vision Air':     ['tron',  'kim_loai'],
  'Glassity Classic':        ['tron',  'nhua'],
  'Glassity Urban':          ['vuong', 'nhua'],
  'Glassity Vintage':        ['tron',  'nhua'],
  'Glassity Wave':           ['oval',  'nhua'],
  'Glassity Edge':           ['vuong', 'nhua'],
  'Glassity Pilot':          ['oval',  'kim_loai'],
};

// Phu kien: accessoryType
const ACCESSORIES = {
  'Hộp Đựng Kính Da Cao Cấp':        'hop_dung',
  'Hộp Đựng Kính Glassity Box':      'hop_dung',
  'Khăn Lau Kính Microfiber':        'khan_lau',
  'Nước Lau Kính Chuyên Dụng 30ml':  'nuoc_lau',
  'Túi Đựng Kính Nhung Mềm':         'tui_dung',
  'Dây Đeo Kính Silicon':            'day_deo',
};

async function main() {
  const snap = await db.collection('products').get();
  let updated = 0;
  for (const doc of snap.docs) {
    const name = doc.data().name;
    if (GLASSES[name]) {
      const [frameShape, material] = GLASSES[name];
      await doc.ref.update({ frameShape, material });
      console.log(`GLASS  ${name} -> ${frameShape}/${material}`);
      updated++;
    } else if (ACCESSORIES[name]) {
      await doc.ref.update({ accessoryType: ACCESSORIES[name] });
      console.log(`ACC    ${name} -> ${ACCESSORIES[name]}`);
      updated++;
    } else {
      console.log(`SKIP   ${name}`);
    }
  }
  console.log(`\nDone. ${updated}/${snap.size} updated.`);
}
main().catch(e => { console.error(e); process.exit(1); });
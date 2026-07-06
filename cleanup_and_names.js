/**
 * cleanup_and_names.js
 * 1. Xoa 20 doc cu (ten tieng Viet, upload dot dau, khong co variants)
 * 2. Them colorName cho tung variant cua 20 san pham moi
 */
const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const sa = require('./service-account.json');
initializeApp({ credential: cert(sa) });
const db = getFirestore();

// 20 doc cu can xoa (da xac nhan: 0 variants, URL v1782926xxx)
const OLD_IDS = [
  '0kEUgYM180xXcmYstnvF', // Kinh mat mat meo sang trong
  '8h6VgbkEp0knMUcb7zSv', // Kinh mat gong vuong ca tinh
  '9Igd0vnlrw4hrvAySNvC', // Kinh mat trong tron ho phanh
  'AT0iEshvG4bcPEu5u1MK', // Kinh mat Avant-Garde
  'Au6U6Hh1DpOS5uO7R6qE', // Kinh mat kim loai manh
  'Fxwc9USwGyaWbg7KnrEF', // Kinh mat Aero Future
  'LRrO9vgi7IDyBUCSOGot', // Kinh mat phong cach Cyber
  'M2sx3gWyBYmc2t90mU1e', // Kinh mat gong kim loai anh kim
  'MmwFlg1vDSb6RA1cMtV6', // Kinh mat gong kim loai cao cap
  'ORt2PGmL1uP4sR5CMl3v', // Kinh mat vuong kim loai toi gian
  'Od3ZYeznsAzuaNgIkYje', // Kinh mat gong kim loai duong pho
  'OnK8VI1jZefyWs1S87xH', // Kinh mat tong ho phanh sang trong
  'PzCAzwHRlYA9gyOb1JGs', // Kinh mat vuong hien dai
  'UuQybzHEp56no6OWTtKM', // Kinh mat thiet ke doc dao
  'Vk1QFnG6ykg6VuWLru9m', // Kinh mat oval hien dai
  'aDWvIsMrXbBpx3SQliTz', // Kinh mat vuong duong pho
  'aSr7fIggGMgOEukx5gPz', // Kinh mat oval hoai co
  'bwpg4MKg1ZwOUKXa7Iw2', // Kinh mat mat meo retro
  'iBrS2JgEaUw2zdPtwU3t', // Kinh mat tuong lai
  'woJCWW5AmKRhE4KypSSH', // Kinh mat trong tron toi gian
];

// Ten mau cho tung variant (theo thu tu anh)
const COLOR_NAMES = {
  'Bold Square Frame Sunglasses':          ['Hổ phách', 'Hổ phách đậm', 'Đen', 'Vàng hổ phách', 'Nâu gỗ'],
  'Modern Square Sunglasses Style':        ['Be khói', 'Nâu hổ phách', 'Đen', 'Kem sữa', 'Xanh khói'],
  'Retro Oval Sunglasses Style':           ['Bạc', 'Bạc bóng', 'Vàng gold', 'Đen than'],
  'Amber Clear Round Sunglasses Style':    ['Hổ phách', 'Đen', 'Vàng mật ong', 'Nâu đồi mồi', 'Be trong'],
  'Minimal Metal Square Sunglasses':       ['Bạc', 'Đen than', 'Vàng gold', 'Gold bóng'],
  'Modern Oval Sunglasses Design':         ['Đen', 'Nâu hổ phách', 'Đỏ rượu', 'Kem ngà'],
  'Urban Metal Frame Sunglasses':          ['Vàng gold', 'Đen than', 'Gold gương', 'Gold bóng'],
  'Unique Design Fashion Sunglasses':      ['Trắng', 'Đen', 'Nâu đất', 'Xanh navy'],
  'Futuristic Fashion Sunglasses':         ['Trắng', 'Đen', 'Xanh rêu', 'Đen bóng'],
  'Slim Metal Fashion Sunglasses':         ['Bạc', 'Vàng gold', 'Bạc bóng', 'Gold bóng'],
  'Luxury Amber Tone Sunglasses Style':    ['Đen', 'Nâu đen', 'Cam hổ phách', 'Be khói', 'Vàng champagne', 'Xanh biển'],
  'Premium Metal Frame Sunglasses':        ['Vàng gold', 'Đen than', 'Gold bóng', 'Bạc'],
  'Retro Cat Eye Sunglasses Style':        ['Đen', 'Nâu socola', 'Kem ngà', 'Cam đào'],
  'Urban Square Fashion Sunglasses':       ['Hổ phách', 'Đen', 'Nâu caramel', 'Trắng sữa', 'Xanh thép'],
  'Avant Garde Sunglasses Style':          ['Đen bóng', 'Đen nhám', 'Đen toàn phần'],
  'Aero Future Sunglasses Look':           ['Trắng bóng', 'Trắng ngọc'],
  'Cyber Fashion Sunglasses':              ['Đen bóng', 'Đen nhám'],
  'Metallic Future Frame Sunglasses':      ['Bạc chrome', 'Bạc nhám', 'Bạc gương'],
  'Luxury Cat Eye Sunglasses Design':      ['Đỏ bordeaux', 'Đen', 'Trắng ngà'],
  'Minimal Clear Round Sunglasses Design': ['Xanh lục bảo', 'Nâu khói', 'Xanh rêu nhạt', 'Nâu gỗ', 'Xám trong', 'Đồi mồi', 'Đen'],
};

async function main() {
  // 1. Xoa doc cu
  for (const id of OLD_IDS) {
    await db.collection('products').doc(id).delete();
    console.log('DELETED: ' + id);
  }

  // 2. Them colorName vao variants
  const snap = await db.collection('products').get();
  for (const doc of snap.docs) {
    const d = doc.data();
    const names = COLOR_NAMES[d.name];
    if (!names || !d.variants) continue;

    const newVariants = d.variants.map((v, i) => ({
      ...v,
      colorName: names[i] || names[names.length - 1],
    }));
    await doc.ref.update({ variants: newVariants });
    console.log('NAMED [' + newVariants.length + ']: ' + d.name);
  }

  console.log('\nDone.');
}

main().catch(e => { console.error(e); process.exit(1); });
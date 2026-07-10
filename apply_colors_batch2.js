// apply_colors_batch2.js — gán màu variant + mô tả tiếng Việt cho sản phẩm đợt 2
const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const COLORS = require('./colors_batch2.js');

initializeApp({ credential: cert(require('./service-account.json')) });
const db = getFirestore();

const SHAPE_VI = { tron: 'gọng tròn', oval: 'gọng oval', mat_meo: 'gọng mắt mèo', vuong: 'gọng vuông' };
const MAT_VI = { nhua: 'nhựa cao cấp', kim_loai: 'kim loại' };

function makeDescription(p) {
  const isSun = p.category === 'kinh_mat';
  const shape = SHAPE_VI[p.frameShape] || '';
  const mat = MAT_VI[p.material] || '';
  const colorNames = (p.variants || []).map(v => v.colorName).filter(Boolean);
  const colorTxt = colorNames.length ? ` Có ${colorNames.length} lựa chọn màu sắc: ${colorNames.join(', ').toLowerCase()}.` : '';
  if (isSun) {
    return `${p.name} — mẫu kính mát thời trang của Glassity, tròng kính chống tia UV400 bảo vệ mắt tối ưu dưới ánh nắng. Thiết kế hiện đại, dễ phối cùng nhiều phong cách từ dạo phố đến du lịch.${colorTxt} Trọng lượng nhẹ, đeo êm ái cả ngày dài.`;
  }
  const detail = [shape, mat && `chất liệu ${mat}`].filter(Boolean).join(', ');
  return `${p.name} — mẫu kính cận ${detail || 'thiết kế hiện đại'} thuộc bộ sưu tập Glassity. Tròng kính hỗ trợ chống ánh sáng xanh, phù hợp làm việc lâu với màn hình.${colorTxt} Gọng nhẹ, ôm mặt thoải mái, phù hợp đeo hằng ngày đi học, đi làm.`;
}

(async () => {
  const snap = await db.collection('products').get();
  const byName = new Map();
  snap.docs.forEach(d => byName.set((d.data().name || '').trim(), d));

  let colored = 0, described = 0;
  const misses = [];

  for (const [name, colors] of Object.entries(COLORS)) {
    const doc = byName.get(name);
    if (!doc) { misses.push(name); continue; }
    const p = doc.data();
    const variants = p.variants || [];
    if (variants.length !== colors.length) {
      console.log(`⚠ ${name}: ${variants.length} variants vs ${colors.length} màu`);
    }
    variants.forEach((v, i) => {
      if (colors[i]) { v.color = colors[i][0]; v.colorName = colors[i][1]; }
    });
    const update = { variants };
    if (!p.description || !p.description.trim()) {
      update.description = makeDescription({ ...p, variants });
      described++;
    }
    await doc.ref.update(update);
    colored++;
    console.log(`✔ ${name} (${variants.length} màu${update.description ? ' + mô tả' : ''})`);
  }

  // mô tả cho các sản phẩm còn thiếu (không nằm trong COLORS, vd phụ kiện)
  for (const d of snap.docs) {
    const p = d.data();
    if ((!p.description || !p.description.trim()) && !COLORS[(p.name || '').trim()]) {
      const desc = p.category === 'phu_kien'
        ? `${p.name} — phụ kiện chính hãng từ Glassity, giúp bảo quản và chăm sóc kính của bạn bền đẹp như mới. Chất liệu chọn lọc, thiết kế tiện dụng, nhỏ gọn dễ mang theo.`
        : makeDescription(p);
      await d.ref.update({ description: desc });
      described++;
      console.log(`✔ mô tả: ${p.name}`);
    }
  }

  console.log(`\nXong: ${colored} SP gán màu, ${described} SP thêm mô tả.`);
  if (misses.length) console.log('KHÔNG TÌM THẤY:', misses.join(' | '));
  process.exit(0);
})().catch(e => { console.error(e); process.exit(1); });

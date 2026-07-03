/**
 * Cập nhật mô tả ~40 chữ (tiếng Việt có dấu) cho 20 sản phẩm — match theo name.
 * Chạy: node update_descriptions.js   (cần service-account.json ở root)
 */
const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
initializeApp({ credential: cert(require('./service-account.json')) });
const db = getFirestore();

// name (đúng như đã seed) → description ~40 chữ
const DESCRIPTIONS = {
  'Kinh mat trong tron ho phanh':
    'Kính mắt tròng tròn màu hổ phách ấm áp, gợi nhớ phong cách vintage thập niên 70. Tròng chống tia UV400, gọng nhựa cao cấp nhẹ và bền. Phù hợp khuôn mặt trái xoan, dễ phối cùng trang phục dạo phố lẫn đi làm hằng ngày.',
  'Kinh mat Avant-Garde':
    'Thiết kế avant-garde phá cách dành cho người yêu nghệ thuật và cá tính riêng. Đường nét bất đối xứng độc đáo, tròng chống chói hiệu quả. Gọng acetate bền nhẹ, ôm mặt thoải mái suốt ngày dài. Điểm nhấn hoàn hảo cho mọi bộ trang phục nổi bật.',
  'Kinh mat mat meo retro':
    'Gọng mắt mèo phong cách thập niên 60 tôn đường nét gương mặt, sang trọng và quyến rũ. Tròng polarized giảm lóa, chống tia UV400 tối ưu. Bản lề kim loại chắc chắn, đệm mũi êm ái. Lựa chọn hoàn hảo cho phái đẹp yêu nét cổ điển.',
  'Kinh mat oval hoai co':
    'Kính oval hoài cổ cân bằng tinh tế giữa cổ điển và hiện đại. Tròng xanh rêu chống UV400, giảm mỏi mắt khi ra nắng. Gọng nhựa dẻo bền, trọng lượng siêu nhẹ chỉ 18 gram. Hợp khuôn mặt vuông và trái xoan, nam nữ đều dùng được.',
  'Kinh mat trong tron toi gian':
    'Thiết kế tối giản với tròng tròn trong suốt, tinh tế và thanh lịch trong mọi hoàn cảnh. Chất liệu nhựa cao cấp không gây kích ứng da. Tròng chống ánh sáng xanh, bảo vệ mắt khi dùng máy tính. Phối đẹp cùng phong cách công sở lẫn thường ngày.',
  'Kinh mat vuong kim loai toi gian':
    'Gọng kim loại mảnh với tròng vuông góc cạnh, tối giản nhưng vẫn sang trọng cuốn hút. Khung thép không gỉ mạ màu bền đẹp theo thời gian. Đệm mũi silicon mềm, đeo lâu không đau. Dành cho người theo đuổi phong cách hiện đại, chỉn chu.',
  'Kinh mat oval hien dai':
    'Kiểu dáng oval hiện đại ôm nhẹ gương mặt, tạo cảm giác thoải mái suốt cả ngày. Tròng chống tia UV400 và chống trầy xước hiệu quả. Gọng nhựa dẻo khó gãy, màu sắc trang nhã. Phù hợp cả nam lẫn nữ, dễ phối mọi phong cách.',
  'Kinh mat vuong hien dai':
    'Gọng vuông hiện đại mạnh mẽ, cân đối cho cả nam và nữ yêu sự linh hoạt. Tròng phân cực giảm chói khi lái xe, chống UV400. Chất liệu nhựa cao cấp nhẹ bền, bản lề lò xo êm. Item không thể thiếu cho mùa hè năng động.',
  'Kinh mat kim loai manh':
    'Gọng kim loại siêu mảnh tinh tế như tưởng như vô hình trên gương mặt bạn. Trọng lượng chỉ 15 gram, đeo cả ngày không mỏi. Tròng chống tia UV400, hạn chế bám vân tay. Vẻ đẹp tối giản dành cho người yêu sự nhẹ nhàng, thanh thoát.',
  'Kinh mat Aero Future':
    'Cảm hứng khí động học tương lai với đường cong liền mạch ôm trọn tầm nhìn. Tròng tráng gương chống chói vượt trội, bảo vệ mắt tối đa. Gọng TR90 siêu bền, chịu va đập tốt. Nổi bật trong mọi hoạt động thể thao và dạo phố cá tính.',
  'Kinh mat gong vuong ca tinh':
    'Gọng vuông dày bản mạnh mẽ dành cho người dám khác biệt và khẳng định chất riêng. Tròng chống UV400, giảm lóa hiệu quả dưới nắng gắt. Acetate cao cấp giữ màu bền đẹp. Kết hợp hoàn hảo cùng phong cách streetwear và âm nhạc đường phố.',
  'Kinh mat phong cach Cyber':
    'Cảm hứng cyberpunk táo bạo nhất bộ sưu tập, thiết kế góc cạnh như bước ra từ phim viễn tưởng. Tròng liền khối chống chói và tia UV400. Gọng nhựa siêu nhẹ ôm sát mặt. Dành cho người tiên phong xu hướng, yêu công nghệ và khác biệt.',
  'Kinh mat tuong lai':
    'Thiết kế futuristic với đường nét sắc bén, thể hiện tinh thần mạnh mẽ và hiện đại. Tròng tráng phủ nano chống nước, chống bám bụi. Khung kim loại phối nhựa bền chắc, nhẹ nhàng. Điểm nhấn hoàn hảo cho những buổi tiệc và sự kiện nổi bật.',
  'Kinh mat thiet ke doc dao':
    'Phong cách thiết kế không thể nhầm lẫn dành riêng cho tâm hồn yêu nghệ thuật tự do. Đường nét điêu khắc thủ công tỉ mỉ từng chi tiết. Tròng chống UV400, màu sắc thời thượng. Món phụ kiện khiến bạn tỏa sáng giữa đám đông mọi lúc.',
  'Kinh mat gong kim loai duong pho':
    'Chất street style bụi bặm hòa cùng gọng kim loại sang trọng, năng động khó cưỡng. Tròng chống tia UV400, giảm chói khi di chuyển ngoài trời. Bản lề linh hoạt, gập mở êm ái bền bỉ. Người bạn đồng hành lý tưởng cho mọi chuyến đi.',
  'Kinh mat vuong duong pho':
    'Dáng vuông góc cá tính đậm chất đường phố, dễ phối đồ cho mọi dịp gặp gỡ. Tròng nhựa cứng chống trầy, bảo vệ mắt khỏi tia UV400. Gọng nhẹ ôm mặt thoải mái. Lựa chọn kinh tế mà vẫn thời trang cho giới trẻ năng động.',
  'Kinh mat tong ho phanh sang trong':
    'Tông hổ phách ấm áp kết hợp chất liệu acetate cao cấp, biểu tượng của sự sang trọng vượt thời gian. Tròng polarized cản chói tuyệt đối, chống UV400. Hoàn thiện thủ công từng đường nét. Đẳng cấp quý ông, quý cô trong mọi khoảnh khắc quan trọng.',
  'Kinh mat mat meo sang trong':
    'Gọng mắt mèo thanh thoát với đường nét mềm mại, chế tác từ acetate cao cấp nhập khẩu Ý. Tròng chống UV400, phủ lớp chống phản quang cao cấp. Bản lề mạ vàng tinh xảo, bền đẹp lâu dài. Tuyệt tác dành cho phái đẹp yêu sự hoàn mỹ.',
  'Kinh mat gong kim loai anh kim':
    'Gọng kim loại ánh kim sáng loáng từ xưởng sản xuất hàng đầu, tôn vinh vẻ đẹp thượng lưu. Tròng chống chói, chống tia UV400 chuẩn châu Âu. Đệm mũi titan siêu nhẹ không gây dị ứng. Phụ kiện khẳng định đẳng cấp trong giới sành thời trang.',
  'Kinh mat gong kim loai cao cap':
    'Đỉnh cao sự tinh tế với gọng kim loại cao cấp thi công thủ công bởi nghệ nhân lành nghề. Tròng khoáng chất trong suốt tuyệt đối, chống UV400. Từng chi tiết được đánh bóng hoàn hảo. Món đầu tư xứng đáng cho phong cách vĩnh cửu của bạn.',
};

(async () => {
  const snap = await db.collection('products').get();
  let updated = 0, missed = [];
  for (const doc of snap.docs) {
    const name = doc.get('name');
    const desc = DESCRIPTIONS[name];
    if (desc) {
      // CHÚ Ý: chỉ update field description — KHÔNG ghi field productId (@DocumentId crash!)
      await doc.ref.update({ description: desc });
      updated++;
      console.log('✔', name);
    } else {
      missed.push(name);
    }
  }
  console.log(`\nĐã cập nhật ${updated}/${snap.size} sản phẩm.`);
  if (missed.length) console.log('Không khớp tên:', missed);
  process.exit(0);
})().catch(e => { console.error(e); process.exit(1); });

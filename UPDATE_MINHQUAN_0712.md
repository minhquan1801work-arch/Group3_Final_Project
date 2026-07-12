# GLASSITY — GHI CHÚ UPDATE (Minh Quân, phiên làm việc 12/07/2026)

> **Dành cho:** Hồng Phúc / AI hỗ trợ merge
> **Branch:** `feature/person-MinhQuan`
> **Trạng thái tại thời điểm viết file này:** các thay đổi bên dưới **CHƯA COMMIT** — còn nằm ở working tree. File này để AI merge hiểu **cái gì đã đổi, tại sao, và phải giữ lại đoạn nào** khi có conflict.
> **Phạm vi:** toàn bộ nằm trong `ui/account` + `ui/order` (đúng phân công cũ), không đụng `ui/catalog` của Phúc lần này.

---

## DANH SÁCH FILE ĐÃ ĐỔI

### File MỚI

| File | Mục đích |
|---|---|
| `app/src/main/java/com/FinalProject/group3/utils/CloudinaryUploader.java` | Upload ảnh/video đánh giá (review) lên Cloudinary bằng unsigned upload preset (`glassity_reviews`, cloud `aa1g9udv` — cùng cloud đang host ảnh sản phẩm). Xem mục "Đánh giá sản phẩm" bên dưới. |
| `app/src/main/res/drawable/bg_btn_gray_filled.xml` | Nền xám (`brand_gray_light` #D9D9D9) cho nút bị vô hiệu hóa, VD "Đã đánh giá". |

### File SỬA — theo khu vực

#### 1. Đánh giá sản phẩm (`ui/order/ReviewActivity.java`, `utils/ReviewViewBinder.java`)

| File | Thay đổi |
|---|---|
| `ui/order/ReviewActivity.java` | (a) Chặn đánh giá 2 lần: `loadProductFromOrder()` giờ check field `reviewed` trên order document trước, nếu `true` thì toast + `finish()` ngay, không cho vào form nữa. (b) **Đổi toàn bộ upload ảnh/video từ Firebase Storage sang Cloudinary** (`CloudinaryUploader`) — Firebase Storage cần gói Blaze (trả phí) nên mọi lệnh upload trước đây đều fail âm thầm, chỉ có comment text là gửi được (đi qua Firestore, không qua Storage). |
| `utils/ReviewViewBinder.java` | Trước đây chỉ hiển thị `imageUrls`, bỏ qua hoàn toàn `videoUrl` đã lưu trong Firestore. Giờ thêm thumbnail video (đổi đuôi `.mp4→.jpg`, Cloudinary tự trả frame đầu) + icon play, bấm mở trình phát video hệ thống. |

**Cần Phúc/ai đó có quyền Cloudinary xác nhận:** preset `glassity_reviews` (Signing Mode: **Unsigned**) đã được tạo trong đúng cloud `aa1g9udv` — **không phải** cloud cá nhân khác. Không có preset này thì upload sẽ báo lỗi rõ ràng qua Toast (không còn silent-fail như bug cũ).

#### 2. Lịch sử đơn hàng (`ui/order/OrderHistoryActivity.java`, `item_order.xml`)

| File | Thay đổi |
|---|---|
| `ui/order/OrderHistoryActivity.java` | (a) Thêm `onResume()` gọi lại `loadOrders()` — trước đây quay lại từ `ReviewActivity` xong trạng thái "Đã đánh giá" không tự cập nhật vì chỉ load 1 lần ở `onCreate`. (b) Nút "Đã đánh giá" (disabled) đổi từ nền wine-outline nhạt (nhìn như hồng, không rõ là bị khóa) sang `bg_btn_gray_filled` + chữ `color_text_secondary` — rõ ràng là trạng thái đã khóa. (c) Thêm click listener cho ảnh/tên/màu sản phẩm trong mỗi dòng đơn → mở thẳng `ProductDetailActivity` của sản phẩm đó. |
| `app/src/main/res/layout/item_order.xml` | Đổi thứ tự nút: **"Xem chi tiết" bên trái, "Đã nhận được hàng"/"Đánh giá" bên phải** (trước đó ngược lại). |

#### 3. Thanh toán (`ui/order/CheckoutActivity.java`, `activity_checkout.xml`)

| File | Thay đổi |
|---|---|
| `ui/order/CheckoutActivity.java` | (a) **Fix hard-code sai:** dòng tóm tắt voucher trước đây in cứng "Miễn phí vận chuyển" cho **bất kỳ** voucher ship nào miễn giảm > 0đ, kể cả `SHIP50` (chỉ giảm 50%) — sai sự thật. Giờ chỉ hiện "Miễn phí vận chuyển" khi giảm đúng 100% phí ship, còn lại hiện đúng số tiền thực giảm (VD "-17.500đ phí ship"), giống cách voucher giảm giá hiển thị. (b) Tách dòng giảm giá + phí ship thành 2 dòng riêng (`\n`) thay vì nối bằng dấu `·`. |
| `app/src/main/res/layout/activity_checkout.xml` | (a) Xóa hẳn dải badge ngân hàng hard-code (VCB/CTG/AGR/BIDV/MB/+44) nằm dưới "Tổng thanh toán" — không có ý nghĩa, không liên kết logic gì. (b) Fix dòng "Thanh toán qua ngân hàng hoặc ví điện tử" bị giãn chữ để lấp đầy chiều ngang → ép 1 dòng + ellipsize, không giãn nữa. (c) Thêm 1 `View` spacer vô hình vào hàng icon ví điện tử (3 icon) để chia đều theo 4 ô như hàng icon ngân hàng (4 icon) — trước đó icon ví bị to hơn icon ngân hàng do `layout_weight` chia cho số ô khác nhau. |

#### 4. Sản phẩm yêu thích (`ui/account/FavoriteActivity.java`, `item_favorite.xml`)

| File | Thay đổi |
|---|---|
| `ui/account/FavoriteActivity.java` | **Fix hard-code:** rating từng sản phẩm trước đây in cứng `"4.5 ★"` cho mọi item (tự comment sẵn "demo — chưa có rating"). Giờ query thật collection `reviews` theo `productId`, tính trung bình y hệt cách `ProductDetailActivity` làm (dùng chung `ReviewViewBinder.ratingOf()`). Không có đánh giá nào → hiện "Chưa có đánh giá" thay vì số giả. |
| `app/src/main/res/layout/item_favorite.xml` | Nút "Mua ngay" đổi từ viền wine (`bg_btn_wine_outline`) sang nền đen chữ trắng (`bg_btn_black_filled`) — đồng bộ style nút đen dùng ở các trang khác trong app. |

#### 5. Đăng nhập / Đăng ký (`activity_login.xml`, `activity_signup.xml`, `activity_forgot_password.xml`, `SignupActivity.java`)

| File | Thay đổi |
|---|---|
| `activity_login.xml`, `activity_signup.xml`, `activity_forgot_password.xml` | Toàn bộ ô nhập liệu (Tên/Email/Mật khẩu/Xác nhận mật khẩu) không set `android:textColor` → rơi về màu mặc định gần giống hệt màu hint (`#B3AAA2`), nhìn như chữ bị mờ. Ép rõ `android:textColor="@color/color_text_primary"` (đen) cho tất cả. |
| `ui/account/SignupActivity.java` | (a) Thêm validate email ngay khi rời ô (blur), không đợi tới lúc bấm "Tạo tài khoản" — giống cơ chế real-time đã có sẵn cho mật khẩu. (b) Đổi từ `Patterns.EMAIL_ADDRESS` (regex chuẩn RFC, khá chặt) sang check đơn giản `contains("@")` — tránh chặn nhầm email doanh nghiệp/tên miền lạ không theo dạng phổ biến. Áp dụng đồng bộ ở cả blur-check lẫn submit-check để không mâu thuẫn nhau. |
| `activity_signup.xml` | Thêm `app:errorIconDrawable="@null"` cho `tilEmail`/`tilPassword`/`tilConfirmPassword` — icon chấm than đỏ mặc định của Material khi có lỗi đè lên icon con mắt (password toggle), khiến không bấm được nút xem mật khẩu khi ô đang báo lỗi. |

---

## CHECKLIST SAU KHI MERGE

- [ ] Tạo upload preset `glassity_reviews` (Unsigned) trong cloud Cloudinary `aa1g9udv` nếu chưa có — không có thì upload ảnh/video review sẽ báo lỗi
- [ ] Test đánh giá sản phẩm kèm ảnh + video → phải gửi thành công, hiện đúng trong `ProductDetailActivity`
- [ ] Test đánh giá xong quay lại "Đơn đã mua" → tab Hoàn thành phải thấy ngay nút xám "Đã đánh giá", không cần thoát app
- [ ] Test không thể đánh giá lại đơn đã đánh giá (mở lại `ReviewActivity` từ link cũ nếu có)
- [ ] Test áp voucher `SHIP50` (đơn ≥200K) → dòng tóm tắt phải hiện đúng số tiền giảm, KHÔNG được ghi "Miễn phí vận chuyển"
- [ ] Test trang Yêu thích → rating mỗi sản phẩm phải khác nhau theo review thật, không còn đồng loạt "4.5 ★"
- [ ] Test đăng ký tài khoản → chữ nhập vào ô phải rõ màu đen, không mờ như hint
- [ ] Test ô mật khẩu lúc đang báo lỗi (VD nhập sai định dạng) → vẫn bấm được icon con mắt để xem/ẩn mật khẩu

---

## FILE RÁC Ở ROOT — đề xuất dọn (chưa xóa, cần Phúc xác nhận trước)

Root project có khá nhiều script Node.js một-lần (import/seed/migrate dữ liệu Firestore) và vài file `.md` ghi chú cũ, gây rối khi AI review code. Đề xuất:

**Nhóm nên xóa (đã chạy xong nhiệm vụ, không còn tác dụng lại — trừ khi cần seed lại DB mới):**
- `import_batch2.js`
- `list_products.js`
- `migrate_variants.js`
- `read_products.js`
- `seed_reviews.js`
- `seed_v2.js`
- `update_attrs.js`
- `update_products.js`

**Giữ lại (đang dùng thật):**
- `sepay_webhook.js` — server webhook xác nhận thanh toán VietQR, vẫn chạy (xem `MERGE_NOTES_MINHQUAN_0708.md`)
- `package.json`, `package-lock.json`, `settings.gradle.kts` — cấu hình dự án
- `service-account.json` — key Firebase Admin (đã bị `.gitignore` chặn, không lên git)

**Nhóm `.md` ghi chú — cân nhắc gộp lại thay vì xóa** (nhiều file rải rác cùng mục đích "note tiến độ/merge"):
- `MERGE_NOTES_MINHQUAN_0708.md`, `MINHHQUAN_UPDATE.md`, `MINHQUAN_VER2_MERGE_GUIDE.md`, `PROGRESS_MINHQUAN.md`, `UPDATE_MINHQUAN_0712.md` (file này) → có thể gộp thành 1 `PROGRESS_MINHQUAN.md` duy nhất sau khi merge xong, đỡ rối. `SPEC_CAMERA_TRYON.md` và `TODO_PHAN_CONG.md` giữ nguyên (còn giá trị tham chiếu tính năng/phân công).

**Lưu ý khi xóa:** các script `.js` ở nhóm "nên xóa" từng thao tác trực tiếp lên Firestore (ghi dữ liệu thật) — xóa file không ảnh hưởng dữ liệu đã ghi, chỉ mất khả năng chạy lại script đó. Nếu còn nghi ngờ script nào sẽ cần dùng lại (VD `migrate_variants.js` nếu còn sản phẩm cũ chưa migrate), nên hỏi lại trước khi xóa thay vì xóa hàng loạt.

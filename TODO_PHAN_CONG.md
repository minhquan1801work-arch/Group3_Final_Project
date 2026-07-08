# GLASSITY — PHÂN CÔNG TODO TIẾP THEO (08/07/2026)

> Dựa trên: `HONGPHUC_PROGRESS.md`, `PROGRESS_MINHQUAN.md`, `MERGE_NOTES_MINHQUAN_0708.md` + đối chiếu 2 sơ đồ BPMN (Auth flow, Cart/Checkout flow) do team gửi.
> Link Figma (`NpoAatjJ7ywG3YlFTZd66W`) yêu cầu đăng nhập nên AI chưa đọc được trực tiếp — phần UI dưới đây dựa trên ảnh Figma đã chia sẻ trong quá trình làm việc (Home, Collection, Product List) + BPMN. Nếu có màn hình nào chưa khớp Figma khi review, báo lại để chỉnh.

---

## Đối chiếu BPMN → đã có / còn thiếu

| Bước trong BPMN | Trạng thái | Ghi chú |
|---|---|---|
| Auth: Signup/Signin, check trùng tài khoản, sai mật khẩu | ✅ Đã có | `SignupActivity`, `LoginActivity` |
| Auth: Forgot password (PIN code) | ⚠️ Khác BPMN nhưng **giữ nguyên** | Team quyết định: dùng link reset qua email của Firebase thay vì flow PIN trong BPMN — không đưa vào todo |
| Cart: add/buy now/favourite, adjust qty/variant | ✅ Đã có | |
| Voucher/coupon | ✅ Đã có | `CheckoutVoucherActivity` |
| Checkout: COD + Bank Transfer QR | ✅ Đã có | |
| Webhook xác nhận thanh toán tự động | ✅ Đã có | `sepay_webhook.js` (Quân, phiên 08/07) |
| **Admin Pool** (nhận đơn, tạo receipt, gửi notify, xác nhận đơn) | ❌ **Chưa có** | Team xác nhận cần làm — web admin nhẹ, xem mục Quân bên dưới |
| Search sản phẩm | ⚠️ **Chưa hoàn chỉnh** | File `SearchActivity.java` đã tồn tại nhưng hành vi/UI chưa đúng — Phúc sửa tiếp |
| Viết đánh giá (Review) | ✅ Đã có, ghi Firestore thật | `ReviewActivity` (Quân) — chụp/chọn ảnh, sao, comment, lưu vào `reviews` collection, đánh dấu `orders.reviewed=true`. **Hoạt động đúng.** |
| **Hiển thị đánh giá trên trang sản phẩm** | ❌ **Chưa có, đang demo** | `ProductDetailActivity.bindDemoReviews()` (dòng 386) dùng mảng `DEMO_REVIEWS` hardcode 2 review giả — **chưa query collection `reviews` thật**. Giao Phúc, xem mục bên dưới |
| Danh sách yêu thích (Favorite) | ✅ Đã có, hoạt động đầy đủ | `FavoriteActivity` + toggle tim ở `ProductDetailActivity` và `ProductAdapter` (card list) đều ghi/đọc Firestore thật, không còn placeholder |
| Lịch sử đơn hàng (Order History/Detail) | ✅ Đã có, hoạt động đầy đủ | Tabs theo status, nút Xem chi tiết/Đánh giá/Mua lại đều wire thật, nút "Đánh giá" gọi đúng `ReviewActivity` |

---

## PHẦN HỒNG PHÚC — `ui/catalog`

| # | Task | Ưu tiên | Ghi chú |
|---|---|:---:|---|
| 1 | Fix hoàn chỉnh `SearchActivity` | ⭐⭐⭐ | File có sẵn nhưng chưa hoạt động đúng. Kiểm tra: kết quả search có ra đúng sản phẩm không (`ProductRepository.searchProducts` dùng prefix + ``), lịch sử tìm kiếm lưu/xoá đúng, giao diện khớp Figma, bấm kết quả → vào đúng `ProductDetailActivity` |
| 2 | Review Firestore Rules | ⭐⭐⭐ | Đang `allow read, write: if true` — lỗ hổng bảo mật, cần review trước khi nộp bài (tối thiểu: chỉ cho user đã login ghi vào `orders`/`customers` của chính họ) |
| 3 | **Hiển thị review thật trên trang sản phẩm** | ⭐⭐⭐ | `ProductDetailActivity.bindDemoReviews()` đang dùng `DEMO_REVIEWS` hardcode. Đổi sang query `FirebaseHelper.getDb().collection(COL_REVIEWS).whereEqualTo("productId", productId)`, map sang `ItemReviewBinding` (tên người đánh giá lấy `customerId`→tra `customers/{uid}.name`, ngày từ `createdAt`, nội dung `comment`, số sao `rating` — hiện code đang fix cứng 5 sao full, cần đổi động theo `rating`). Cũng nên hiện luôn `imageUrls` nếu có ảnh review. Tính lại `tvRating`/`tvRatingCount` từ dữ liệu thật thay vì fix cứng "4.9" |
| 4 | Fix hoàn chỉnh `SearchActivity` | ⭐⭐⭐ | File có sẵn nhưng chưa hoạt động đúng. Kiểm tra: kết quả search có ra đúng sản phẩm không (`ProductRepository.searchProducts` dùng prefix + ``), lịch sử tìm kiếm lưu/xoá đúng, giao diện khớp Figma, bấm kết quả → vào đúng `ProductDetailActivity` |
| 5 | Review Firestore Rules | ⭐⭐⭐ | Đang `allow read, write: if true` — lỗ hổng bảo mật, cần review trước khi nộp bài (tối thiểu: chỉ cho user đã login ghi vào `orders`/`customers`/`reviews` của chính họ) |
| 6 | Ảnh thật cho 3 phụ kiện | ⭐⭐ | `Hộp Đựng Kính Da Cao Cấp`, `Nước Lau Kính Chuyên Dụng 30ml`, `Túi Đựng Kính Nhung Mềm` đang dùng ảnh demo Cloudinary — cần ảnh thật hoặc nhờ thành viên khác tìm theo `HUONG_DAN_THEM_SAN_PHAM.md` |
| 7 | Test toàn bộ luồng catalog sau merge | ⭐⭐⭐ | Home → Collection carousel → BST detail → Xem tất cả → ProductList lọc đúng → ProductDetail hiển thị đúng ảnh/màu/giá/review cho cả 20 SP mới lẫn SP cũ (kính cận, phụ kiện) |
| 8 | Đối chiếu lại toàn bộ UI với Figma | ⭐⭐ | Cần bạn gửi tôi export ảnh từng màn (hoặc cấp quyền xem) vì link hiện bị chặn đăng nhập — ưu tiên các màn: ProductDetail, ProductList, SearchActivity, DrawerLayout |
| 9 | Dọn `HONGPHUC_PROGRESS.md` | ⭐ | Đánh dấu B7 SearchActivity là "đang sửa lại", tránh gây hiểu nhầm như lần này |

---

## PHẦN MINH QUÂN — `ui/account`, `ui/order` + **Web Admin mới**

| # | Task | Ưu tiên | Ghi chú |
|---|---|:---:|---|
| 1 | Notification click → mark READ | ⭐⭐⭐ | Đã note sẵn trong `PROGRESS_MINHQUAN.md`: `NotificationAdapter` cần callback → `update("status","READ")` → ẩn chấm đỏ |
| 2 | Xóa nút `[DEBUG] Tạo 5 đơn test` | ⭐⭐⭐ | `fragment_profile.xml` + `ProfileFragment.java` — **bắt buộc trước khi nộp** |
| 3 | Test A→Z tài khoản mới | ⭐⭐⭐ | Đăng ký → thêm địa chỉ → mua → xem đơn → đánh giá (đã note sẵn) |
| 4 | **Web Admin (mới, đáp ứng BPMN Admin Pool)** | ⭐⭐⭐ | Trang web nhẹ (có thể HTML tĩnh + Firebase SDK, hoặc React đơn giản) để admin: xem danh sách đơn mới → xác nhận/từ chối đơn → cập nhật `status` (`PENDING`→`PROCESSING`→`SHIPPED`→`DELIVERED`) → trigger gửi notification cho khách. Đây là phần **hiện chưa tồn tại** trong repo, tương ứng đúng "Admin Pool" trong BPMN checkout. Phạm vi tối thiểu để chấm BPMN đủ: 1 trang danh sách đơn + nút xác nhận, không cần responsive đẹp |
| 5 | Sync `MERGE_NOTES_MINHQUAN_0708.md` | ⭐ | File ghi "CHƯA COMMIT" — xác nhận đã commit/push vào `main` sau merge chưa, xoá note tạm nếu xong |

---

## Cần xác nhận thêm với BA/giảng viên (không giao ai làm ngay)

- Không có mục nào khác đang treo — 2 điểm mơ hồ trước đó (Forgot Password PIN, Admin Pool) đã được team quyết định ở trên.

---

## Việc chung (cả hai, trước khi nộp)

- [ ] Build release, test trên máy thật/emulator hợp lệ (emulator hiện lỗi thiếu system image — xem ghi chú trong lịch sử chat, cần cài qua Android Studio SDK Manager)
- [ ] Kiểm tra `.gitignore` không lọt `service-account.json`, `cloudinary-config.json`, `node_modules/`
- [ ] Review lại toàn bộ luồng theo đúng 2 sơ đồ BPMN, tick từng bước

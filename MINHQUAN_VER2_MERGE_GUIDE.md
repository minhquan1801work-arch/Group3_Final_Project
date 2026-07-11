# GLASSITY — GUIDE MERGE `minhquanver2` (dành cho AI hỗ trợ Hồng Phúc)

> **Nguồn:** nhánh `minhquanver2` (dựa trên `main` tại commit `c360e7a`, cộng thêm các thay đổi bên dưới)
> **Người tạo:** Minh Quân (qua AI hỗ trợ), phiên làm việc 11/07/2026
> **Mục tiêu file này:** liệt kê chính xác các luồng/tính năng **đã hoàn thiện và đã test** trong `minhquanver2` — khi gộp vào nhánh của Phúc (hoặc merge chung về `main`), **PHẢI GIỮ NGUYÊN các luồng này**, không được revert/ghi đè mất. Nội dung UI/text có thể điều chỉnh theo ý Phúc, nhưng **hành vi/logic** thì không đổi.

---

## ⚠️ QUAN TRỌNG NHẤT — đọc trước khi merge

`minhquanver2` chỉ sửa các file thuộc phạm vi `ui/account`, `ui/order`, `MainActivity.java`, `adapter/CartAdapter.java`, `repository/CartRepository.java`, `utils/CartQuickActions.java` + `admin_web/index.html`. **Không đụng vào `ui/catalog`** (trừ 1 dòng nhỏ trong `ProductDetailActivity.java` — xem mục 6). Nếu Phúc có sửa các file trùng tên bên dưới song song → **merge cả 2 phần, đừng chọn 1 bên** (accept theirs/ours mù quáng sẽ mất 1 trong 2 bộ thay đổi).

---

## 1. Giỏ hàng (`ui/order/CartFragment.java`, `adapter/CartAdapter.java`, `repository/CartRepository.java`)

### 1a. Sản phẩm mới thêm luôn nằm ĐẦU danh sách
- `CartRepository.getCartItems()`: sort theo `addedAt` **giảm dần** (mới → cũ), KHÔNG được đổi lại thành tăng dần.
- `CartAdapter.submitListWithAutoSelect()`: khi không có `autoSelectId` cụ thể, auto-select **item đầu danh sách** (`newItems.get(0)`) — vì list đã sort mới→cũ nên item đầu = item mới nhất.
- **Giữ nguyên logic auto-select đã có từ trước**: nếu có `lastAddedId` (đọc từ SharedPreferences `cart_prefs`/`last_added_id`) → ưu tiên chọn đúng item đó.

### 1b. Chế độ "Sửa" — xóa nhiều sản phẩm cùng lúc
- `fragment_cart.xml`: có `TextView id="tvEdit"` (chữ "Sửa"/"Xong", màu wine) ở góc phải header, chỉ hiện khi giỏ có hàng.
- Bấm "Sửa" → `editMode = true`: nút MUA HÀNG (`btnBuy`) ẩn đi, thay bằng nút đỏ `btnDeleteSelected` ("XÓA (n)").
- Người dùng tick từng sản phẩm (checkbox có sẵn) hoặc dùng "Chọn tất cả" (`cbSelectAll`) như bình thường.
- Bấm nút XÓA → `confirmDeleteSelected()`: hiện `AlertDialog` xác nhận (nếu chọn hết → text "...xóa TẤT CẢ sản phẩm..."), xác nhận thì xóa hàng loạt qua `cartRepo.removeFromCart()` cho từng item, xong tự thoát chế độ Sửa + reload giỏ.
- **Không được xóa bỏ 2 nút này hay đổi cách hoạt động** nếu Phúc rework lại UI giỏ hàng — có thể đổi vị trí/màu sắc nhưng hành vi giữ nguyên.

### 1c. Nút back trong giỏ hàng
- `CartFragment`: `btnBack` gọi `NavController.popBackStack()`; nếu **không pop được** (tức giỏ đang là màn duy nhất trên stack — trường hợp mở từ `ProductDetailActivity`/`FavoriteActivity` qua `EXTRA_OPEN_CART`) thì gọi `requireActivity().finish()`.
- **Lý do:** trước đây back luôn nhảy về Home dù người dùng vào giỏ từ trang chi tiết sản phẩm — sai UX. Giờ back phải trả về đúng màn trước đó.
- Đi kèm: `MainActivity.handleOpenCartIntent()` dùng `NavOptions.popUpTo(homeFragment, true)` khi mở giỏ qua intent; `ProductDetailActivity`/`FavoriteActivity` mở giỏ **KHÔNG** dùng `FLAG_ACTIVITY_CLEAR_TOP` nữa (để giữ activity gọi dưới stack).

---

## 2. Thêm vào giỏ ở trang danh sách sản phẩm (`utils/CartQuickActions.java`)

- Nút "Thêm vào giỏ" ở `ProductListActivity`/`SearchActivity`/`HomeFragment` (dùng chung `CartQuickActions.wire()`) đã hoạt động đúng — **trước đây không có phản hồi gì khi bấm** khiến người dùng tưởng bị lỗi.
- Đã thêm `Toast.makeText(context, "Đã thêm vào giỏ hàng", ...)` ngay khi thêm thành công (trong `wire()` callback `onSuccess`).
- Nếu Phúc rework `ProductAdapter`/`ProductListActivity` → nhớ giữ lại dòng Toast này (hoặc thay bằng animation/snackbar tương đương), đừng để mất phản hồi.

---

## 3. Đăng nhập / Đăng ký (`ui/account/LoginActivity.java`, `SignupActivity.java`, `WelcomeActivity.java`)

### 3a. Link "Chưa có tài khoản? Đăng ký ngay" ở màn Đăng nhập
- `activity_login.xml`: có `TextView id="tvGoSignup"` dưới nút Google, bấm mở `SignupActivity`.
- Strings: `login_no_account`, `login_signup_now`.

### 3b. Đề xuất mật khẩu mạnh (màn Đăng ký)
- `activity_signup.xml`: `TextView id="tvSuggestPassword"` ("✨ Đề xuất mật khẩu mạnh") nằm dưới dòng quy tắc mật khẩu, trên ô Xác nhận mật khẩu.
- `SignupActivity.generateStrongPassword()`: sinh mật khẩu 12 ký tự (4 thường + 3 HOA + 3 số + 2 ký tự đặc biệt `@#$%&*`, loại bỏ ký tự dễ nhầm `l/I/O/0/1`), điền vào **cả 2 ô** mật khẩu, bỏ ẩn để khách đọc được, kèm Toast nhắc lưu lại.
- Giữ nguyên hàm này khi Phúc chỉnh sửa UI Signup.

### 3c. UI đăng nhập Facebook (placeholder, CHƯA kích hoạt thật)
- 3 màn `activity_welcome.xml`, `activity_login.xml`, `activity_signup.xml`: thêm nút `btnFacebook` (icon `ic_facebook.xml`, 52dp tròn) đặt bên trái nút Google, packed chain.
- Bấm vào → Toast "Đăng nhập/Đăng ký bằng Facebook đang được phát triển" (chưa có Facebook App ID thật — xem lý do trong lịch sử chat, cần tài khoản Facebook Developer để làm thật).
- Nếu sau này có ai làm Facebook Login thật → thay listener trong `WelcomeActivity`/`LoginActivity`/`SignupActivity`, giữ nguyên vị trí UI.

### 3d. Overlay thẻ thành viên cho khách (`fragment_profile.xml`, `ProfileFragment.java`)
- Trước đây: khách chưa đăng nhập → ẩn hẳn thẻ barcode (`cardMember.setVisibility(GONE)`).
- Giờ: **vẫn hiện thẻ** nhưng phủ lớp trắng mờ `guestOverlay` (nền `#E6FFFFFF`) với chữ "Đăng ký / đăng nhập để sử dụng tích điểm". Bấm vào overlay → `LoginRequiredDialog` (không tự động nhảy thẳng sang màn Login).
- Đừng quay lại cách ẩn hẳn thẻ — mục đích là cho khách thấy trước giá trị (tích điểm) rồi mới mời đăng nhập.

---

## 4. Thanh toán — Voucher cho khách (`ui/order/CheckoutActivity.java`, `activity_checkout.xml`)

- Trước đây: khách bấm vào ô "Mã giảm giá" hay ô nhập mã đều bị chặn, bắt đăng nhập hết.
- Giờ: **tách 2 block riêng biệt**:
  1. Dòng trên: đổi title thành "**Lựa chọn mã giảm giá dành cho thành viên**" — bấm vào → `LoginRequiredDialog` yêu cầu đăng nhập (giữ nguyên, đây là kho voucher cá nhân qua `CheckoutVoucherActivity`).
  2. Dòng dưới (cách nhau bằng dải xám `gapVoucherGuest` + label `tvGuestVoucherLabel` "Nhập mã giảm giá hiện có"): khách **tự nhập mã** vào ô `etVoucher` + bấm "Áp dụng" (`btnApplyVoucherInline`) — vẫn gọi `applyVoucherCode()` như thành viên, được giảm giá nếu mã hợp lệ.
- Với **thành viên đã đăng nhập**: layout giữ y nguyên như cũ (không hiện gap/label, title vẫn là "Mã giảm giá" bình thường) — code check `isGuest` để quyết định hiện block nào.
- **Không được khóa hoàn toàn ô nhập mã của khách như bản cũ.**

### Voucher "Dùng ngay" ở `VoucherActivity.java` (kho voucher thành viên)
- Trước: bấm "Dùng ngay" chỉ hiện Toast nhắc nhập mã, không làm gì thêm.
- Giờ: bấm "Dùng ngay" → Toast nhắc **và** mở luôn tab Giỏ hàng (`MainActivity` với `EXTRA_OPEN_CART`) để khách chọn sản phẩm mua ngay.

---

## 5. Đơn hàng — luồng "Đã nhận được hàng" (`ui/order/OrderHistoryActivity.java`, admin web)

### 5a. Trạng thái đơn mới: `COMPLETED`
- Luồng cũ: `PENDING → PROCESSING → SHIPPED → DELIVERED` (DELIVERED = xong luôn, có nút Đánh giá ngay).
- Luồng mới: `... → SHIPPED → DELIVERED → COMPLETED`.
  - **DELIVERED** = shop đã giao xong, đơn hiện ở tab **"Đã giao"**, nút hành động là nút đen **"Đã nhận được hàng"** (không phải nút Đánh giá nữa).
  - Khách bấm nút đó → `confirmReceived()`: update Firestore `orderStatus = "COMPLETED"`, tự động nhảy sang tab **"Hoàn thành"**, gửi thông báo mới nhắc đánh giá qua `NotificationHelper.pushOrder()`.
  - **COMPLETED** = tab "Hoàn thành", có đủ 2 nút: **Mua lại** + **Đánh giá** (hoặc "Đã đánh giá" nếu đã review).
- **Tuyệt đối không được gộp lại DELIVERED và COMPLETED thành 1 trạng thái** — đây là yêu cầu rõ ràng của khách hàng (thật): phải có bước xác nhận nhận hàng riêng biệt trước khi đánh giá.

### 5b. Fix hiển thị variant trong danh sách đơn
- Trước: `tvProductColor` hiện thẳng mã hex (`#1A1A1A`) và ảnh luôn lấy `product.getImages().get(0)` (sai màu/ảnh trắng).
- Giờ: tra đúng `variants[]` theo `first.getColor()` (mã màu đã mua) → lấy `colorName` tiếng Việt + `images[0]` của đúng variant đó. Fallback về field cũ nếu không tìm thấy variant khớp.
- Đây là fix áp dụng logic tương tự `CartAdapter`/`ProductDetailActivity` đã làm — **giữ nguyên cách tra variant này**, đừng quay lại đọc field `images` gốc của product.

### 5c. Admin web (`admin_web/index.html`) đồng bộ trạng thái COMPLETED
- `STATUS_LABEL`, `NEXT_STATUS`, filter chip "Hoàn thành", CSS badge `.st-COMPLETED`, thống kê doanh thu — tất cả đã cập nhật để nhận diện `COMPLETED` như một trạng thái hợp lệ (đơn khách đã xác nhận nhận hàng), KHÔNG phải trạng thái admin tự chuyển (`NEXT_STATUS.COMPLETED = []` — admin không có nút chuyển sang COMPLETED, chỉ khách tự làm trong app).
- Message thông báo khi admin chuyển DELIVERED: đổi thành "...Vui lòng xác nhận đã nhận hàng." (không còn "Hãy đánh giá" ở bước này — lời nhắc đánh giá giờ do app gửi SAU KHI khách xác nhận nhận hàng).
- Đã thêm 3 tab quản trị mới: **Sản phẩm** (tồn kho theo variant, cảnh báo sắp hết ≤5), **Khách hàng** (tên/email/SĐT/điểm), **Đánh giá** (rating trung bình + list review) + stat "Đơn hôm nay". Các tab này chỉ load data khi bấm vào lần đầu (không tốn read Firestore thừa).
- **Đã deploy** lên `https://glassity-770c5.web.app` — nếu Phúc sửa thêm `admin_web/index.html`, nhớ deploy lại (`firebase deploy --only hosting`) sau khi merge.

---

## 6. Thông báo — điều hướng đúng loại (`ui/account/NotificationFragment.java`)

- Trước: mọi thông báo `type=ORDER` đều mở `OrderDetailActivity` (hoặc `OrderHistoryActivity` tab 2 nếu không có orderId).
- Giờ: phân loại theo **nội dung message** (lowercase, kiểm tra `contains`):
  - Chứa "đánh giá" + có `orderId` → mở thẳng `ReviewActivity.intent(context, orderId)` (ví dụ thông báo "Hãy đánh giá sản phẩm bạn vừa nhận...").
  - Chứa "đã giao" → mở `OrderHistoryActivity.intentWithTab(context, 2)` (tab "Đã giao").
  - Còn lại (ORDER khác, có orderId) → `OrderDetailActivity` như cũ.
- Nếu Phúc/ai đổi nội dung message trong `NotificationHelper.java` hoặc admin web, **phải giữ các từ khóa "đánh giá"/"đã giao" trong message** để routing này còn hoạt động đúng — hoặc đổi routing sang check theo field riêng (khuyến nghị nâng cấp sau: thêm field `subType` thay vì check text, nhưng hiện tại đang hoạt động ổn theo cách match text).

---

## 7. Logo Glassity thay icon mặc định

- File mới `res/drawable/ic_logo_glassity.xml` (2 tròng kính + cầu nối, màu `brand_dark`) dùng để thay các icon/emoji generic:
  - **App icon** (launcher): `ic_launcher_background.xml` (nền cream `#EFE9E1`) + `ic_launcher_foreground.xml` (logo kính) — thay hẳn robot Android mặc định.
  - **Dialog yêu cầu đăng nhập** (`dialog_login_required.xml`): thay emoji 🛍 bằng logo trong vòng tròn cream.
  - **Empty state màn Thông báo** (`fragment_notification.xml`): thêm logo mờ (alpha 0.35) phía trên chữ "Chưa có thông báo nào".
- Giữ nguyên các icon ngữ nghĩa khác (giỏ hàng, tim yêu thích, túi đơn hàng ở các empty state khác) — **không thay bằng logo**, vì icon ngữ nghĩa dễ hiểu hơn cho từng màn cụ thể.

---

## CHECKLIST SAU KHI MERGE

- [ ] Test giỏ hàng: thêm sản phẩm mới → xuất hiện đúng ở đầu danh sách, tự động được tick chọn
- [ ] Test giỏ hàng: bấm "Sửa" → tick nhiều sản phẩm → XÓA → dialog xác nhận → xóa đúng, thoát chế độ Sửa
- [ ] Test giỏ hàng: vào giỏ từ trang chi tiết sản phẩm → bấm back → phải quay lại đúng trang chi tiết đó (không nhảy Home)
- [ ] Test trang danh sách sản phẩm: bấm "Thêm vào giỏ" → có Toast phản hồi, giỏ hàng cập nhật đúng
- [ ] Test đăng ký: bấm "Đề xuất mật khẩu mạnh" → 2 ô mật khẩu tự điền, hiện rõ, đạt yêu cầu validate
- [ ] Test đăng nhập: bấm "Đăng ký ngay" → mở đúng SignupActivity
- [ ] Test khách (chưa đăng nhập) vào trang cá nhân → thấy thẻ barcode mờ + chữ mời đăng nhập (không phải ẩn hẳn)
- [ ] Test khách vào Thanh toán → tự nhập mã giảm giá công khai (VD `FREESHIP`) → được giảm đúng; bấm "Lựa chọn mã giảm giá dành cho thành viên" → bị chặn, yêu cầu đăng nhập
- [ ] Test đơn hàng: sau khi admin chuyển đơn sang "Đã giao" → app hiện nút "Đã nhận được hàng" ở tab Đã giao → bấm vào → chuyển tab Hoàn thành, có nút Mua lại + Đánh giá
- [ ] Test màu sắc/ảnh sản phẩm trong Đơn hàng của bạn khớp đúng variant đã mua (không còn hiện mã hex hay ảnh sai màu)
- [ ] Test thông báo: bấm thông báo "đã giao" → vào đúng tab Đã giao; bấm thông báo nhắc đánh giá → vào đúng trang ReviewActivity của đơn đó
- [ ] Kiểm tra app icon đã đổi thành logo Glassity (có thể cần gỡ cài đặt lại app để icon cũ hết cache)
- [ ] Admin web: mở `glassity-770c5.web.app`, kiểm tra 4 tab (Đơn hàng/Sản phẩm/Khách hàng/Đánh giá) hoạt động, đơn COMPLETED hiện đúng badge xanh "Khách đã nhận"

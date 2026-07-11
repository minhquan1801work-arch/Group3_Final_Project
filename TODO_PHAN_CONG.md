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
| 1 | ~~Fix hoàn chỉnh `SearchActivity`~~ ✅ **XONG 09/07** | — | Đã viết lại: in-memory search (contains, bỏ dấu tiếng Việt, khớp cả tên màu variant), type-ahead debounce 250ms, lịch sử per-uid (guest ẩn hẳn), gợi ý cá nhân hóa (SP đã mua + yêu thích + lịch sử search → chấm điểm collection/faceShape/category/giá), UI camera giữ chỗ. **Còn lại (làm sau):** tính năng tìm bằng hình ảnh thật cho nút camera |
| 2 | ~~Hiển thị review thật trên trang sản phẩm~~ ✅ **XONG 10/07** | — | Đã bỏ `DEMO_REVIEWS`; query `reviews` theo productId (sort client-side theo createdAt desc), sao động theo rating, ngày dd/MM/yyyy, thumbnails `imageUrls`, rating trung bình thật + đếm thật, empty state "Chưa có đánh giá". `ReviewActivity` giờ lưu kèm `userName`. Đã seed 122 review tiếng Việt từ 11 tài khoản khách thật cho đủ 64 SP (script `seed_reviews.js`) |
| 3 | Review Firestore Rules | ⭐⭐⭐ | Đang `allow read, write: if true` — lỗ hổng bảo mật, cần review trước khi nộp (tối thiểu: chỉ user đã login ghi vào `orders`/`customers`/`reviews` của chính họ) |
| 4 | ~~Ảnh thật cho phụ kiện + kính đợt 2~~ ✅ **XONG 10/07** | — | Import đợt 2 (`import_batch2.js`): 32 SP kính mới + 6 Glassity + 6 phụ kiện cập nhật ảnh thật từ Cloudinary → tổng 64 SP. Đã gán màu variant (xem ảnh từng SP) + mô tả tiếng Việt (`apply_colors_batch2.js`) |
| 5 | Test toàn bộ luồng catalog sau merge | ⭐⭐⭐ | Home → Collection → BST detail → Xem tất cả → ProductList → ProductDetail đúng ảnh/màu/giá/review cho cả SP mới lẫn cũ. Test Search: guest không thấy lịch sử, user login có lịch sử riêng, gợi ý đổi theo hành vi mua/thích |
| 6 | Đối chiếu lại toàn bộ UI với Figma | ⭐⭐ | Gửi AI export ảnh từng màn (link Figma bị chặn đăng nhập) — ưu tiên: ProductDetail, ProductList, DrawerLayout |
| 7 | Tìm bằng hình ảnh (nút camera Search) | ⭐ | UI đã giữ chỗ (Toast "sắp ra mắt"). Khi làm thật: chụp/chọn ảnh → tìm SP tương tự. Gợi ý: ML Kit Image Labeling hoặc so màu chủ đạo |

---

## PHẦN MINH QUÂN — `ui/account`, `ui/order` + **Web Admin mới**

| # | Task | Ưu tiên | Ghi chú |
|---|---|:---:|---|
| 1 | ~~Notification click → mark READ~~ ✅ **XONG** | — | Kiểm tra code 09/07: `NotificationAdapter` đã có click → mark READ + ẩn chấm đỏ |
| 2 | ~~Xóa nút `[DEBUG] Tạo 5 đơn test`~~ ✅ **XONG** | — | Kiểm tra code 09/07: không còn trong `fragment_profile.xml` lẫn `ProfileFragment.java` |
| 3 | Test A→Z tài khoản mới | ⭐⭐⭐ | Đăng ký → thêm địa chỉ → mua → xem đơn → đánh giá (đã note sẵn) |
| 4 | **Web Admin (mới, đáp ứng BPMN Admin Pool)** | ⭐⭐⭐ | Trang web nhẹ (có thể HTML tĩnh + Firebase SDK, hoặc React đơn giản) để admin: xem danh sách đơn mới → xác nhận/từ chối đơn → cập nhật `status` (`PENDING`→`PROCESSING`→`SHIPPED`→`DELIVERED`) → trigger gửi notification cho khách. **Kiểm tra 09/07: chưa có trong repo.** Phạm vi tối thiểu: 1 trang danh sách đơn + nút xác nhận |
| 5 | Sync `MERGE_NOTES_MINHQUAN_0708.md` | ⭐ | File ghi "CHƯA COMMIT" — xác nhận đã commit/push vào `main` sau merge chưa, xoá note tạm nếu xong |
| 6 | Các mục Profile còn Toast giữ chỗ | ⭐ | `ProfileFragment` một số mục menu (hỗ trợ/chính sách...) đang Toast — nối dần hoặc chấp nhận trước khi nộp |

---

## Cần xác nhận thêm với BA/giảng viên (không giao ai làm ngay)

- Không có mục nào khác đang treo — 2 điểm mơ hồ trước đó (Forgot Password PIN, Admin Pool) đã được team quyết định ở trên.

---

## Điểm treo còn lại toàn app (rà code 09/07)

| Chỗ | Trạng thái | Ai |
|---|---|---|
| Review trên trang SP | ✅ XONG 10/07 — query thật + seed 122 review | Phúc |
| `HERO_PRODUCT_IDS` | ⏳ 3 slot rỗng — bấm hero Toast "Sắp ra mắt" (HomeFragment đầu file) | Phúc điền ID |
| Drawer "Về Glassity" | ✅ XONG 11/07 — trước chỉ đóng drawer, giờ mở `AboutActivity` | Phúc |
| Home chỉ hiện 2/3 blog card | ✅ XONG 11/07 — thêm `blogCard3`, sửa tiêu đề 2 card cũ khớp đúng bài | Phúc |
| Footer About/Contact/Policy | ✅ XONG 11/07 — About mở `AboutActivity` (trang riêng theo Figma LA.About Glassity), Contact/Policy vẫn `ContactActivity`/`PolicyActivity` | Phúc |
| Tìm bằng ảnh (camera Search) | ⏳ Toast giữ chỗ | Phúc (ưu tiên thấp) |
| "Về Glassity" trong BlogActivity | ✅ XONG 11/07 — mở `AboutActivity` | Phúc |
| 12 SP ảnh demo (6 kính cận + 6 phụ kiện) | ✅ XONG 10/07 — đã thay ảnh thật (import đợt 2) | Phúc |
| Web Admin | ❌ chưa có | Quân |
| Firestore Rules | ❓ cần kiểm tra trên Console (`allow read, write: if true`?) | Phúc |
| Một số mục Profile | ⏳ Toast giữ chỗ | Quân |

## Footer dùng chung (11/07) — lưu ý cho Quân nếu thêm màn hình mới

Khối footer đầy đủ (social icon Twitter/Instagram/YouTube → liên hệ → link About/Contact/Policy → dòng bản quyền)
đã tách ra `app/src/main/res/layout/layout_footer_full.xml`, không copy-paste tay nữa.

- **Đang dùng ở:** `fragment_home.xml` (HomeFragment) và `activity_about.xml` (AboutActivity).
- **Cách dùng ở màn mới:**
  ```xml
  <include
      android:id="@+id/incFooter"
      android:layout_width="match_parent"
      android:layout_height="wrap_content"
      layout="@layout/layout_footer_full" />
  ```
  ⚠️ Bắt buộc phải có `android:id` trên thẻ `<include>` — layout gốc dùng `<merge>` nên
  ViewBinding cần id này để lộ các view con (`footerAbout`, `footerContact`, `footerPolicy`,
  `footerTw`, `footerIg`, `footerYt`) ra ngoài qua `binding.incFooter.<id>`, không có id thì
  build lỗi "cannot find symbol".
- **Wire click trong Java** (xem `HomeFragment.setupClickListeners()` hoặc `AboutActivity.setupFooter()` làm mẫu):
  ```java
  binding.incFooter.footerAbout.setOnClickListener(v -> AboutActivity.start(requireContext()));
  binding.incFooter.footerContact.setOnClickListener(v -> startActivity(ContactActivity.intent(...)));
  binding.incFooter.footerPolicy.setOnClickListener(v -> startActivity(PolicyActivity.intent(..., TYPE_PRIVACY)));
  ```
- **Cập nhật 11/07 (đợt sau):** `BlogActivity` (`activity_blog.xml`) đã gộp vào layout chung luôn (trước đó dòng
  note này nói còn tách riêng — giờ hết đúng rồi), nên hiện tại Home + About + Blog đều dùng chung 1 footer.

## Logo thương hiệu — app icon + Toast (11/07) — lưu ý cho Quân

Đã đổi bộ nhận diện thương hiệu, có 1 điểm quan trọng liên quan tới **mọi Toast trong toàn app** kể cả code của Quân:

- **App icon** (`mipmap-*dpi/ic_launcher*`, `mipmap-anydpi-v26/ic_launcher*.xml`) đã đổi từ robot Android mặc định
  sang logo Glassity thật. `drawable/ic_launcher_background.xml` giờ là nền trắng phẳng (trước là pattern xanh lá
  của template mặc định).
- **Vì sao liên quan tới Toast:** từ Android 11+ hệ thống **tự động chèn icon app** vào cạnh mọi `Toast.makeText(...)`
  — hành vi này do OS kiểm soát, không có API nào để app tự chọn icon riêng cho Toast. Nếu trước đây bạn thấy Toast
  hiện con robot Android (vd Toast "tính năng chưa có sẵn"), đó là vì app icon lúc đó vẫn là robot mặc định —
  **không phải bug trong code Toast**, và **không cần sửa gì thêm ở chỗ gọi `Toast.makeText`** — đổi app icon là
  tự động fix hết mọi Toast trong app cùng lúc.
- ⚠️ Nếu bạn build lại mà vẫn thấy robot: **uninstall app cũ trước khi cài lại** — Android cache icon theo
  package, cài đè không luôn refresh icon.
- Asset `drawable/logo_submark.png` (hình kính, không chữ, nền trong suốt) đã chuẩn bị sẵn — dành cho
  `NotificationCompat.Builder.setSmallIcon()` nếu sau này bạn làm push notification thật (FCM), lúc đó mới có
  chỗ dùng thật sự vì hiện app chưa có hệ thống push/notification nào.

## Việc chung (cả hai, trước khi nộp)

- [ ] Build release, test trên máy thật/emulator hợp lệ (emulator hiện lỗi thiếu system image — xem ghi chú trong lịch sử chat, cần cài qua Android Studio SDK Manager)
- [ ] Kiểm tra `.gitignore` không lọt `service-account.json`, `cloudinary-config.json`, `node_modules/`
- [ ] Review lại toàn bộ luồng theo đúng 2 sơ đồ BPMN, tick từng bước

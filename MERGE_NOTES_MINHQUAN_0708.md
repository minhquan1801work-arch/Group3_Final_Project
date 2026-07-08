# GLASSITY — GHI CHÚ MERGE (Minh Quân, phiên làm việc 08/07/2026)

> **Dành cho:** Hồng Phúc / AI hỗ trợ merge
> **Branch:** `feature/person-MinhQuan`
> **Trạng thái tại thời điểm viết file này:** các thay đổi bên dưới **CHƯA COMMIT** — còn nằm ở working tree. Quân sẽ tự commit/push riêng, file này chỉ để AI merge hiểu **cái gì đã đổi, tại sao, và phải giữ lại đoạn nào** khi có conflict.
> **Mục tiêu:** merge xong không được làm mất/revert bất kỳ thay đổi nào liệt kê dưới đây.

---

## ⚠️ QUAN TRỌNG NHẤT — đọc trước khi merge

Theo phân công cũ (`PROGRESS_MINHQUAN.md`): `ui/catalog` là lãnh thổ của **Phúc**, Quân chỉ đụng `ui/account` + `ui/order`. Nhưng phiên này Quân **có sửa vào một số file trong `ui/catalog`, `adapter/`, `model/`** — KHÔNG PHẢI đi lấn phần việc của Phúc, mà là **fix bug ảnh sản phẩm bị đen** do chính data mới (`variants[]`) Phúc thêm vào Firestore nhưng `ProductAdapter`/`ProductDetailActivity` chưa đọc field đó. Xem mục "Bug ảnh đen" bên dưới để hiểu rõ, và **giữ nguyên phần sửa này** khi merge dù nó nằm trong file của Phúc.

Nếu Phúc cũng có sửa `ProductAdapter.java` / `Product.java` / `HomeFragment.java` / `ProductDetailActivity.java` song song → **merge cả 2 phần, đừng chọn 1 bên** (accept theirs/ours mù quáng sẽ mất 1 trong 2 bộ thay đổi).

---

## DANH SÁCH FILE ĐÃ ĐỔI

### File MỚI (giữ nguyên, không xoá)

| File | Mục đích |
|---|---|
| `app/src/main/java/com/FinalProject/group3/model/ProductVariant.java` | Model biến thể sản phẩm (color, colorName, images, stock). **Bắt buộc phải có** — nhiều file cũ (`CartAdapter`, `CheckoutActivity`, `FavoriteActivity`, `CartFragment`) đã gọi `product.getVariants()` từ trước nhưng class này chưa tồn tại → **lỗi biên dịch toàn bộ app** nếu thiếu file này. |
| `app/src/main/java/com/FinalProject/group3/utils/SessionManager.java` | Quản lý phiên đăng nhập/khách, dùng ở `WelcomeActivity` (xem mục bên dưới). |
| `app/src/main/res/drawable/bg_card_vietqr.xml` | Nền card hiển thị QR chuyển khoản (tông wine/cream). |
| `sepay_webhook.js` (root project, **không phải Android**) | Server Node.js nhận webhook từ SePay để tự động xác nhận thanh toán VietQR → set `paymentStatus=PAID` trên Firestore. Xem mục "Backend webhook" bên dưới — **không liên quan tới build Android**, an toàn nếu Phúc không đụng tới. |

### File SỬA — theo khu vực

#### 1. Fix bug ảnh sản phẩm đen (chạm vào phần của Phúc — xem cảnh báo ở đầu file)

| File | Thay đổi |
|---|---|
| `model/Product.java` | Thêm field `variants: List<ProductVariant>` + getter/setter `getVariants()`/`setVariants()`. |
| `adapter/ProductAdapter.java` | Thêm hàm `resolveThumbnailUrl(product)`: ưu tiên đọc `variants[0].images[0]`, fallback `images[0]` cũ. Áp dụng cho card ở Home/ProductList/Search/gợi ý sau thanh toán. |
| `ui/catalog/ProductDetailActivity.java` | Thêm hàm `resolveGalleryImages()`: nếu `images` rỗng thì gộp ảnh từ tất cả `variants[]` làm gallery. Đồng thời thêm `currentVariantColor()`/`currentVariantStock()` (lấy màu/kho theo variant đang chọn — 2 hàm này được gọi ở `addToCart()` từ trước nhưng bị thiếu, gây lỗi biên dịch). |

**Nguyên nhân gốc:** 20 sản phẩm mới Phúc thêm (commit "anh SP + variant colorName...") lưu ảnh trong `variants[].images` thay vì field `images` cũ. `ProductAdapter`/`ProductDetailActivity` cũ chỉ đọc field cũ → hiện placeholder đen (`bg_product_placeholder` = màu `#1A1614`, nhìn giống ảnh lỗi/đen). Sau fix, cả 2 kiểu sản phẩm (cũ và mới) đều hiển thị ảnh đúng.

#### 2. Giỏ hàng (`ui/order`)

| File | Thay đổi |
|---|---|
| `adapter/CartAdapter.java` | Thêm `getItemAt(position)` — phục vụ swipe-to-delete. |
| `ui/order/CartFragment.java` | (a) Thêm nút back ở header (`popBackStack()`). (b) Thêm swipe-to-delete (kéo trái item → dialog xác nhận xoá, tái dùng `onDeleteClick()` có sẵn). |
| `app/src/main/res/layout/fragment_cart.xml` | Thêm `ImageView btnBack` ở header; xoá `paddingBottom="84dp"` dư thừa (trước đó để chừa chỗ cho footer pill, nhưng footer đã tự ẩn khi ở tab giỏ hàng nên gây khoảng trắng thừa cuối trang). |

#### 3. Sổ địa chỉ (`ui/order`)

| File | Thay đổi |
|---|---|
| `ui/order/AddressListActivity.java` | Thêm `getSwipeThreshold()` = 0.3f cho `ItemTouchHelper` (giảm ngưỡng kéo để dễ trigger swipe-to-delete trên emulator — tính năng xoá vốn đã có sẵn từ trước, chỉ chỉnh ngưỡng). |

#### 4. Thanh toán / VietQR (`ui/order`)

| File | Thay đổi |
|---|---|
| `ui/order/PaymentResultActivity.java` | (a) Ẩn khối "Có thể bạn cũng thích" khi đang ở trạng thái chờ chuyển khoản (`llSuggest.GONE`), chỉ hiện khi COD hoặc đã xác nhận thanh toán. (b) Thêm `watchPaymentStatus()` — lắng nghe Firestore realtime field `paymentStatus`; khi đổi thành `"PAID"` (do webhook `sepay_webhook.js` set) → tự chuyển UI sang "Thanh toán thành công", không cần thao tác gì thêm. (c) Nút "Về trang chủ" giờ gửi kèm `MainActivity.EXTRA_OPEN_HOME=true`. |
| `app/src/main/res/layout/activity_payment_result.xml` | Đổi thứ tự hiển thị: **Số tài khoản → Số tiền → Nội dung CK** (trước đó là Số tiền → Nội dung → STK). Đổi màu card VietQR sang tông wine/cream (`bg_card_vietqr`). Bọc khối gợi ý sản phẩm vào `llSuggest` để control visibility. Thêm dòng trạng thái "Đang tự động kiểm tra giao dịch...". |

#### 5. Trang chủ + điều hướng (`ui/catalog`, root `MainActivity`)

| File | Thay đổi |
|---|---|
| `ui/catalog/HomeFragment.java` | Thêm `scrollToTop()` — cuộn `NestedScrollView` root về đầu. |
| `MainActivity.java` | (a) Thêm `EXTRA_OPEN_HOME` — xử lý tương tự cơ chế `EXTRA_OPEN_CART` có sẵn: khi nhận extra này, ép `NavController` về `homeFragment` (xoá back stack cũ) **và luôn gọi `scrollToTop()`** trên instance Home hiện tại (kể cả khi đã đang đứng sẵn ở tab Home — trước đó bug là nếu đã ở Home thì bỏ qua, không cuộn). (b) `handleOpenHomeIntent()` được gọi cả ở `onCreate()` lẫn `onNewIntent()`. |

#### 6. Đăng nhập / phiên (`ui/account`)

| File | Thay đổi |
|---|---|
| `ui/account/WelcomeActivity.java` | Đầu `onCreate()`: nếu `SessionManager.hasSession()` (đã đăng nhập HOẶC đã chọn "Khách" trước đó) → vào thẳng `MainActivity`, không hiện lại màn chọn Đăng nhập/Đăng ký/Khách mỗi lần mở app. Nút "Tiếp tục với vai trò Khách" giờ gọi `SessionManager.setGuestMode(true)` trước khi vào Main. |
| `ui/account/ProfileFragment.java` | Nút Đăng xuất đổi từ `FirebaseHelper.signOut()` → `SessionManager.logout()` (xoá cả cờ guest, tránh bị Welcome tự nhảy ngược vào Main sau khi đăng xuất). |
| `ui/account/AccountInfoActivity.java` | Tương tự — đổi sang `SessionManager.logout()`. |

**Lý do:** trước đây `WelcomeActivity` là LAUNCHER activity nhưng không check phiên cũ, nên mỗi lần app bị kill và mở lại đều bắt chọn lại dù đã đăng nhập từ trước — không đúng hành vi app di động chuẩn.

#### 7. `.gitignore`

Mở rộng pattern chặn file bí mật:
```diff
- service-account.json
+ service-account*.json
+ service-account*.js
+ cloudinary-config.json
+ *.pem
+ *.p12
```
**Lý do:** phát hiện 1 file `service-account.json.json` (do Windows ẩn đuôi file gây đặt tên lặp đuôi) **đã từng bị commit + push lên GitHub** ở commit cũ (`63fb292`, sau đó bị xoá ở `5591dd6` nhưng vẫn còn trong lịch sử git). Key đó đã được rotate (tạo key mới, key cũ coi như vô hiệu). Pattern mới chặn triệt để mọi biến thể tên file tương tự trong tương lai.

⚠️ **`node_modules/` hiện KHÔNG có trong `.gitignore`** (vấn đề có từ trước, không phải do phiên này) — & đã có **5618 file** trong `node_modules` bị track vào git từ trước. Phiên này có cài thêm `express` (cho `sepay_webhook.js`) nên sẽ có thêm vài chục package mới nằm trong `node_modules`, hiện đang **untracked**. Khuyến nghị: thêm `node_modules/` vào `.gitignore` và chạy `git rm -r --cached node_modules` ở 1 commit dọn dẹp riêng — Quân chưa làm việc này vì ảnh hưởng lịch sử chung, để cả nhóm thống nhất trước.

#### 8. `package.json`

Thêm dependency `express` (dùng cho `sepay_webhook.js`, không ảnh hưởng app Android).

---

## BACKEND WEBHOOK (`sepay_webhook.js`) — ngoài phạm vi Android

File này là 1 server Node.js độc lập, **không phải một phần của app Android**, dùng để tự động xác nhận thanh toán VietQR khi tiền về tài khoản MB Bank thật. Nó:
1. Nhận webhook từ dịch vụ SePay khi có giao dịch vào TK `0977780173`
2. Đối chiếu mã đơn (`GLS-xxxxxx-xxxx`, tự xử lý cả trường hợp ngân hàng xoá dấu gạch ngang trong nội dung CK) + số tiền
3. Set `paymentStatus: "PAID"` trên Firestore document `orders/{orderId}`
4. App Android (`PaymentResultActivity.watchPaymentStatus()`) lắng nghe realtime field này → tự chuyển UI khi có thay đổi

**Cần để chạy được (không nằm trong git, mỗi người tự cấu hình):**
- `service-account.json` (Firebase Admin SDK key) — đặt ở root project, đã bị `.gitignore` chặn, **mỗi máy phải tự tải riêng** từ Firebase Console → Project settings → Service accounts.
- `ngrok` (hoặc host thật như Render/Railway) để có public URL cho SePay gọi vào.
- Cấu hình webhook trong SePay dashboard trỏ về URL đó + `/sepay-webhook`.

Nếu Phúc không cần test phần tự động xác nhận thanh toán thì **bỏ qua toàn bộ mục này**, không ảnh hưởng gì tới việc build/chạy app Android bình thường.

---

## CHECKLIST SAU KHI MERGE

- [ ] Build lại app, kiểm tra ảnh sản phẩm hiện đúng ở Home + Detail + gợi ý sau thanh toán (cả sản phẩm cũ và 20 sản phẩm mới)
- [ ] Test giỏ hàng: nút back hoạt động, swipe trái để xoá item
- [ ] Test sổ địa chỉ: swipe trái để xoá
- [ ] Test luồng thanh toán chuyển khoản → bấm "Về trang chủ" → phải vào đúng tab Home **và cuộn lên đầu trang**
- [ ] Test mở app sau khi đã đăng nhập/chọn khách từ trước → phải vào thẳng Home, không bắt chọn lại
- [ ] Đăng xuất → phải quay đúng về màn Welcome (không tự nhảy lại Main)
- [ ] Nếu Phúc đã sửa `ProductAdapter.java`/`Product.java`/`HomeFragment.java` song song → merge tay, đừng chọn "theirs" hoặc "ours" toàn bộ file

# Cập nhật của Minh Quân — Session 2 (10/07/2026)

Tài liệu này ghi lại toàn bộ thay đổi cần merge vào nhánh chính.  
Sau khi merge, chạy **Build → Rebuild Project** và test từng luồng theo mục "Kiểm tra sau merge".

---

## 1. File mới tạo

| File | Mục đích |
|------|----------|
| `app/.../utils/NotificationHelper.java` | Gửi/seed thông báo Firestore (push, pushOrder, sendWelcome, sendOrderPlaced, seedDemoCampaigns) |
| `app/res/layout/dialog_media_chooser.xml` | Bottom sheet chọn ảnh/video khi đánh giá sản phẩm |
| `app/res/drawable/ic_video.xml` | Icon camera quay video (Material) |
| `app/res/drawable/ic_play_circle.xml` | Icon play trắng dùng overlay thumbnail video |
| `app/res/drawable/bg_btn_orange_outline.xml` | Nền trắng + viền cam #E65100 (dùng cho nút "Mua lại") |

---

## 2. File đã sửa

### 2a. Caching địa chỉ
**`utils/AddressApiHelper.java`**
- Thêm `volatile cachedProvinces`, `ConcurrentHashMap cachedDistricts/cachedWards`
- `fetchProvinces/fetchDistricts/fetchWards` check cache trước khi gọi API
- Thêm `preload()` — fire-and-forget, gọi khi app khởi động

**`MainActivity.java`** — trong `onCreate()`:
```java
AddressApiHelper.preload();
NotificationHelper.seedDemoCampaigns(this);
```

---

### 2b. Xóa nút DEBUG
**`fragment_profile.xml`** — xóa `btnSeedOrders` TextView  
**`ProfileFragment.java`** — xóa click listener, setVisibility, và method `seedTestOrders()`

---

### 2c. Hệ thống thông báo (Notification)

**`model/Notification.java`** — thêm field `private String orderId` + getter/setter

**`adapter/NotificationAdapter.java`**
- Thêm: `getItemAt(int)`, `removeAt(int)`, `isEmpty()`, `getUnreadItems()`
- `onBindViewHolder`: background xám nhạt `#F3F3F3` nếu UNREAD, trắng `#FFFFFF` nếu READ

**`fragment_notification.xml`** — thêm nút "Đọc tất cả" (`btnMarkAllRead`) cạnh tiêu đề

**`NotificationFragment.java`** — viết lại hoàn toàn:
- Swipe trái → xóa thông báo (đỏ giống giỏ hàng)
- Bấm → `markRead()` cập nhật local + Firestore, rồi điều hướng theo `type`:
  - `ORDER` + có `orderId` → `OrderDetailActivity`
  - `ORDER` không có `orderId` → `OrderHistoryActivity` tab "Đã giao" (index 2)
  - `PROMOTION` → `VoucherActivity`
  - `SYSTEM` → không điều hướng
- `btnMarkAllRead` → `WriteBatch` update tất cả UNREAD lên Firestore
- Lọc bỏ thông báo `message == null/empty` (dọn data cũ sai format)

**`SignupActivity.java`** — sau đăng ký thành công:
```java
NotificationHelper.sendWelcome(FirebaseHelper.getCurrentUserId());
```

**`CheckoutActivity.java`** — sau tạo đơn thành công (logged-in user):
```java
String productSummary = items.get(0).getProduct().getName();
if (items.size() > 1) productSummary += " (+" + (items.size()-1) + " san pham)";
NotificationHelper.sendOrderPlaced(uid, orderId, orderCode, method, productSummary);
```

---

### 2d. Trang Yêu thích
**`activity_favorite.xml`** — thêm `btnCart` (ImageView) + `tvCartBadge` (TextView badge) ở toolbar  
**`FavoriteActivity.java`**
- `btnCart` click → mở tab giỏ hàng
- `onResume()` → `CartQuickActions.refreshBadge(binding.tvCartBadge)`
- Nút "Thêm vào giỏ" → `CartQuickActions.flyToCart()` animation + toast

---

### 2e. Lịch sử đơn hàng (UX)
**`item_order.xml`** — đổi `<Button>` → `<TextView>` (tránh MaterialComponents đè `backgroundTint`):
- `btnReorder` ("Mua lại"): `bg_btn_orange_outline` + chữ `#E65100` + `foreground="?selectableItemBackground"`
- `btnReview` ("Đánh giá"): `bg_btn_wine_outline` + foreground ripple
- `btnDetail` ("Xem chi tiết"): `bg_btn_black_filled` + chữ trắng + foreground ripple

**`OrderHistoryActivity.java`**
- `statusLabel(Order)`: `DELIVERED` + chưa review → `"Đã giao - Chờ đánh giá"`, đã review → `"Đã giao"`
- Unreviewed `btnReview`: `bg_btn_black_filled` + `Color.WHITE` (trước là viền đỏ)
- `btnReorder`: gọi `reorder(orderId)` thay vì toast
- Method `reorder(orderId)`:
  1. Fetch `orderDetails` subcollection → lấy `productId` đầu tiên
  2. Fetch product từ `ProductRepository`
  3. Nếu tồn tại → `ProductDetailActivity.start(context, productId)`
  4. Nếu không tồn tại/lỗi → toast "Sản phẩm không còn kinh doanh..." + về trang chủ (`reorderFallback()`)

---

### 2f. Trang đánh giá (ReviewActivity)
**`activity_review.xml`** — label section media: `"Ảnh & video · Tối đa 5 ảnh, 1 video"`

**`ReviewActivity.java`** — viết lại:
- **Star error**: không còn Toast, hiển thị chữ đỏ trong `tvStarLabel`: `"Vui lòng chọn đánh giá sao"`
- **Media picker**: `showMediaChooser()` → `BottomSheetDialog` với 4 option (chụp ảnh / thư viện ảnh / quay video / thư viện video); photo-full hoặc video-full thì option bị dim + disabled
- **Video support**: field `Uri selectedVideo`, `videoCaptureLauncher`, `videoGalleryLauncher`; thumbnail hiển thị trong media row cùng ảnh; overlay play icon; nút X để xóa
- **Upload flow**: nếu có video → upload video trước → lấy `videoUrl` → upload ảnh → `saveReview()` lưu `videoUrl` vào Firestore
- **Giới hạn**: tối đa 5 ảnh + 1 video; placeholder ẩn khi đã đủ cả hai

---

### 2g. Nav bar — chấm đỏ thông báo
**`layout_bottom_nav.xml`** — `dotNotification`: `marginStart="16dp"` + `marginBottom="16dp"` (từ 24dp → 16dp để dot bám đúng góc chuông)

---

### 2h. Admin web (Firebase Hosting)
**`admin_web/index.html`** — đã deploy lên `https://glassity-770c5.web.app`

`notifyCustomer(order, newStatus)`:
- Fetch `orderDetails` subcollection → `productId` → fetch `products/{id}` → lấy `.name`
- Gắn tên vào message: `"Đơn GLS-xxx - Tên kính: đã giao thành công..."`
- Fields Firestore: `customerId`, `message`, `type: "ORDER"`, `orderId: order.id`, `status: "UNREAD"`, `createdAt: serverTimestamp()`

---

## 3. File đã xóa (rác)

| File | Lý do |
|------|-------|
| `app/res/drawable/bg_btn_review_attention.xml` | Không còn được tham chiếu ở đâu sau khi đổi btnReview sang `bg_btn_black_filled` |

---

## 4. Kiểm tra sau merge

Chạy lại app trên emulator/thiết bị thật, test tuần tự:

| # | Luồng | Kết quả mong đợi |
|---|-------|-----------------|
| 1 | Mở app → vào Địa chỉ | Tỉnh/thành load ngay (đã cache từ startup), không lag |
| 2 | Đăng ký tài khoản mới | Nhận 2 thông báo: "Chào mừng" (SYSTEM) + voucher 10% (PROMOTION) |
| 3 | Đặt hàng thành công | Thông báo "Đặt hàng thành công! Đơn GLS-xxx - TÊN KÍNH đang chờ xác nhận" |
| 4 | Admin web đổi trạng thái → DELIVERED | App nhận thông báo "Đơn GLS-xxx - Tên kính đã giao thành công. Hãy đánh giá nhé!" |
| 5 | Tab Thông báo — unread | Nền xám nhạt; đã đọc nền trắng |
| 6 | Kéo trái thông báo | Xóa thông báo (nền đỏ chữ "Xóa") |
| 7 | Nút "Đọc tất cả" | Tất cả về trắng, Firestore cập nhật batch |
| 8 | Bấm thông báo ORDER | Vào `OrderDetailActivity` của đúng đơn đó |
| 9 | Bấm thông báo PROMOTION | Vào `VoucherActivity` |
| 10 | Tab Đã giao → nút "Đánh giá" | Nền đen chữ trắng; vào `ReviewActivity` |
| 11 | ReviewActivity — chọn sao | Chưa chọn + nhấn Gửi → chữ đỏ dưới sao (không toast) |
| 12 | ReviewActivity — icon + | Bottom sheet xuất hiện: 4 option ảnh/video |
| 13 | Tab Đã giao → nút "Mua lại" | Nền trắng viền cam chữ cam; ấn → vào `ProductDetailActivity` |
| 14 | Mua lại sản phẩm đã gỡ | Toast + về trang chủ |
| 15 | Trang Yêu thích | Icon giỏ hàng ở toolbar + badge số lượng; bấm "Thêm vào giỏ" → animation bay |
| 16 | Chấm đỏ nav bar | Nằm sát góc phải-trên icon chuông (không bị xa) |

---

## 5. Lưu ý merge

- `NotificationHelper.sendOrderPlaced` đã thêm param `productSummary` → cần update tất cả nơi gọi (hiện chỉ có `CheckoutActivity.java` line ~724)
- `item_order.xml` đổi Button → TextView: nếu Hồng Phúc có code nào cast `binding.btnReorder` thành `Button` thì sẽ crash — đổi sang `TextView` hoặc `View`
- `bg_btn_review_attention.xml` đã xóa: nếu branch khác còn tham chiếu drawable này sẽ lỗi compile — tìm kiếm và xóa reference
- Admin web đã deploy live; merge code không ảnh hưởng đến web

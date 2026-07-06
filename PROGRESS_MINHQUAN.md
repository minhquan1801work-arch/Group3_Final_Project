# GLASSITY — TIẾN ĐỘ PERSON A (MINH QUÂN)

> **Cập nhật:** 06/07/2026 | **Deadline:** ~09/07/2026
> **Branch:** `feature/person-MinhQuan`
> **Phân công:** Quân = `ui/account` + `ui/order` | Phúc = `ui/catalog`

---

## HƯỚNG DẪN MERGE (Cho Hồng Phúc)

```
git checkout main
git pull
git merge feature/person-MinhQuan
# Giải quyết conflict nếu có (xem mục "File dễ conflict" bên dưới)
git push
```

**File dễ conflict với Person B:**
- `AndroidManifest.xml` — Quân thêm nhiều `<activity>`. Khi conflict: GIỮ cả hai `<activity>` block, không xoá cái nào.
- `MainActivity.java` — nếu Phúc có sửa nav graph. Giữ NavHostFragment, không đổi R.id.
- `build.gradle` — nếu khác dependency: merge thủ công, không dùng "accept theirs/ours" mù quáng.

**File KHÔNG được đụng (của Quân):**
- Toàn bộ `ui/account/` và `ui/order/`
- `adapter/VoucherAdapter.java`, `adapter/NotificationAdapter.java`
- `model/Order.java` (có thêm 6 field mới — xem mục Schema bên dưới)

---

## TECH STACK & QUY TẮC BẮT BUỘC

| Quy tắc | Chi tiết |
|---|---|
| Ngôn ngữ | Java thuần — **không Kotlin** |
| Binding | ViewBinding — không `findViewById` |
| Pattern | Repository + Callback — không MVVM/LiveData |
| Activity mới | Phải gọi `InsetsUtil.applySystemBarsPadding(binding.getRoot())` sau `setContentView` |
| `@DocumentId` | **KHÔNG** ghi field id trở lại Firestore. Ví dụ: khi `add(data)` dùng `Map<String,Object>`, không dùng object model có field id |
| Firestore boolean | Getter `isXxx()` → Firestore map thành key `xxx`. Ví dụ `isDefault()` → `"default"` (reserved word). Ghi thủ công bằng Map nếu cần |

---

## MÀN HÌNH ĐÃ HOÀN THÀNH

### Auth & Welcome
| File | Trạng thái | Ghi chú |
|---|---|---|
| `WelcomeActivity` | ✅ | 3 nút: Đăng nhập / Đăng ký / Khách |
| `LoginActivity` | ✅ | Email+pass, Google Sign-In |
| `SignupActivity` | ✅ | Tạo `customers/{uid}` khi đăng ký |
| `ForgotPasswordActivity` | ✅ | Reset qua Firebase |

### Profile & Tài khoản (`ui/account`)
| File | Trạng thái | Ghi chú |
|---|---|---|
| `ProfileFragment` | ✅ | Thẻ thành viên, điểm realtime từ Firestore, grid menu, logout. Có nút `[DEBUG] Tạo 5 đơn test` — **XÓA TRƯỚC KHI NỘP** |
| `AccountInfoActivity` | ✅ | Load/save: tên, SĐT, giới tính, ngày sinh |
| `VoucherActivity` | ✅ | Tabs Tất cả/Giảm giá/Vận chuyển; kho trống; BottomSheet nhận mã (FREESHIP/GIAM10/GIAM50K) |
| `PointHistoryActivity` | ✅ | Hiển thị tất cả đơn hàng + điểm tích lũy realtime |
| `SettingsActivity` | ✅ | 4 dòng → mở Settings hệ thống (thông báo/vị trí/ngôn ngữ/sinh trắc học) |
| `ContactActivity` | ✅ | Thông tin liên hệ Glassity |
| `NotificationFragment` | ✅ | List thông báo theo uid, chấm đỏ UNREAD. **TODO còn lại:** bấm → mark READ |

### Đơn hàng (`ui/order`)
| File | Trạng thái | Ghi chú |
|---|---|---|
| `CartFragment` | ✅ | 3 trạng thái (khách/đăng nhập/trống), checkbox, stepper, tính tiền |
| `CheckoutActivity` | ✅ | Ship 35k/50k, chọn địa chỉ, voucher giảm giá + ship, toggle điểm, COD/Bank, đặt hàng |
| `PaymentResultActivity` | ✅ | COD thành công / Bank: thông tin chuyển khoản. Nối `OrderHistoryActivity` |
| `AddressListActivity` | ✅ | CRUD địa chỉ, swipe-to-delete, địa chỉ mặc định, 34 tỉnh thành |
| `AddAddressActivity` | ✅ | Form thêm/sửa địa chỉ |
| `CheckoutVoucherActivity` | ✅ | Màn chọn voucher trong Checkout (radio button) |
| `OrderHistoryActivity` | ✅ | Tabs: Tất cả/Chờ giao hàng/Đã giao/Đã hủy. Nút: Xem chi tiết / Đánh giá / Mua lại |
| `OrderDetailActivity` | ✅ | Chi tiết đơn: sản phẩm, địa chỉ, breakdown thanh toán đầy đủ |
| `ReviewActivity` | ✅ | Tối đa 5 ảnh (camera+gallery), chọn sao 1–5 (bắt buộc), comment tuỳ chọn, upload Storage |

---

## SCHEMA FIRESTORE (phần của Person A)

### `customers/{uid}`
```
{
  name: string,
  email: string,
  phone: string,
  gender: string,         // "Nam" | "Nữ" | "Khác"
  birthday: string,       // "dd/MM/yyyy"
  points: number,         // FieldValue.increment — KHÔNG ghi trực tiếp
  createdAt: timestamp
}
```

### `customers/{uid}/addresses/{addrId}`
```
{
  name: string,
  phone: string,
  province: string,
  address: string,
  isDefault: boolean      // Firestore lưu là key "isDefault" (dùng Map, không dùng model)
}
```

### `customers/{uid}/vouchers/{voucherId}`
```
{
  code: string,           // "FREESHIP" | "GIAM10" | "GIAM50K"
  title: string,
  type: string,           // "SHIPPING" | "DISCOUNT"
  minOrder: number,
  expireAt: timestamp
}
```

### `orders/{orderId}`
```
{
  customerId: string,
  addressId: string,
  status: string,         // "PENDING" | "PROCESSING" | "SHIPPED" | "DELIVERED" | "CANCELLED"
  paymentMethod: string,  // "COD" | "BANK_TRANSFER"
  totalAmount: number,    // tổng cuối cùng khách trả
  shippingFee: number,    // 35000 | 50000
  shipDiscount: number,   // giảm từ voucher SHIPPING (0 nếu không dùng)
  voucherDiscount: number,// giảm từ voucher DISCOUNT (0 nếu không dùng)
  usedPoints: number,     // điểm đã tiêu (0 nếu không dùng)
  earnedPoints: number,   // điểm tích sau đơn này
  reviewed: boolean,      // false → true sau khi đánh giá
  createdAt: timestamp
}
```

### `orders/{orderId}/orderDetails/{detailId}`
```
{
  productId: string,
  productName: string,
  color: string,
  quantity: number,
  price: number
}
```

### `reviews/{reviewId}`
```
{
  productId: string,
  orderId: string,
  customerId: string,
  rating: number,         // 1–5
  comment: string,
  imageUrls: string[],    // tối đa 5 URL từ Firebase Storage
  createdAt: timestamp
}
```
> Ảnh review lưu tại Storage path: `reviews/{uid}/{timestamp}_{index}.jpg`

### `payments/{paymentId}`
```
{
  orderId: string,
  method: string,
  status: string,         // "PENDING" | "COMPLETED"
  amount: number,
  createdAt: timestamp
}
```

### `notifications/{notifId}`
```
{
  customerId: string,
  title: string,
  body: string,
  status: string,         // "UNREAD" | "READ"
  createdAt: timestamp
}
```

---

## LOGIC ĐIỂM TÍCH LŨY

- **Tích điểm:** `earnedPoints = floor(totalAmount / 10000)` — tính trong `CheckoutActivity.rewardPointsEarned()`
- **Tiêu điểm:** 100 điểm = 10.000đ — tính trong `CheckoutActivity.pointsDiscount()`
- **Ghi Firestore:** `FieldValue.increment(earned - used)` sau khi đặt hàng thành công
- **Đọc điểm:** luôn đọc từ `customers/{uid}.points` — **không tính lại từ lịch sử đơn**

---

## LƯU Ý KỸ THUẬT CHO AI / NGƯỜI MERGE

### Khi thêm Activity mới
```java
// Bước 1: khai báo trong AndroidManifest.xml
<activity android:name=".ui.xxx.XxxActivity" android:exported="false" />

// Bước 2: gọi trong onCreate() sau setContentView
InsetsUtil.applySystemBarsPadding(binding.getRoot());
```

### Khi ghi Firestore (tránh @DocumentId)
```java
// ĐÚNG — dùng Map
Map<String, Object> data = new HashMap<>();
data.put("field1", value1);
db.collection("col").add(data);

// SAI — model có @DocumentId hoặc id field sẽ bị ghi id vào document
db.collection("col").add(myModel); // model.id = null → lỗi
```

### VoucherAdapter — phân biệt màu theo loại
- `SHIPPING` → nền xanh `#1E6B4A`
- `DISCOUNT` → nền wine `#72383D`

### ReviewActivity — đặc điểm
- `selectedStar = 0` mặc định (không chọn sao)
- Bắt buộc: `selectedStar >= 1`
- Không bắt buộc: comment, ảnh
- Lưu `imageUrls: [...]` (List, không phải String đơn)
- Upload tuần tự từng ảnh trước khi `saveReview()`

---

## CÒN LẠI CỦA MINH QUÂN

| Task | Ưu tiên | Chi tiết |
|---|---|---|
| Notification click → mark READ | ⭐ cao | `NotificationAdapter` cần click callback → `update("status","READ")` → ẩn chấm đỏ |
| Xóa nút `[DEBUG] Tạo 5 đơn test` | ⭐ cao | Trong `fragment_profile.xml` và `ProfileFragment.java` trước khi nộp |
| Test A→Z account mới | ⭐ cao | Đăng ký → thêm địa chỉ → mua → xem đơn → đánh giá |
| SearchActivity | thấp | Chưa có file. Toast "sắp ra mắt" đang giữ chỗ |

---

## TÀI KHOẢN TEST

| Email | Mật khẩu | Dùng để test |
|---|---|---|
| test1@glassity.com | Test@123 | Đơn nhiều trạng thái, giỏ hàng |
| test2@glassity.com Test@123| Test@123 | Đơn bank transfer |
| test3@glassity.com | Test@123 | Đơn cancelled/processing |
| test4@glassity.com | Test@123 | Giỏ + favorites |
| test5@glassity.com | Test@123 | Empty states |
| minhquan1801.work (Google) | — | Account của Quân — có 5 đơn test đủ trạng thái |

---

## EMULATOR / CHẠY DỰ ÁN

1. Sau pull: **File → Reload All from Disk → Sync Gradle → Rebuild**
2. Emulator mất mạng: khởi động kèm `-dns-server 8.8.8.8,1.1.1.1`
3. Google Sign-In lỗi mã 10: thiếu SHA-1 — vào Firebase Console → Project Settings → thêm SHA-1 của debug keystore
4. Lỗi mã 7: mạng emulator. Lỗi 12500: Play Services cũ → cập nhật Play Store trên emulator

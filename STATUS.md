# Glassity — Project Status & Phân Công Việc

> Cập nhật: 2026-07-02 | Deadline: ~07-09 | Team: 2 người (Person A + Person B)

---

## 🔧 Tech Stack

| Thành phần | Công nghệ |
|---|---|
| Language | Java thuần (KHÔNG Kotlin) |
| Architecture | Repository + Callback (KHÔNG MVVM/LiveData) |
| UI Binding | ViewBinding (KHÔNG findViewById) |
| Auth | Firebase Auth (Email/Password + Google Sign-In) |
| Database | Cloud Firestore |
| Images | Cloudinary (cloud: aa1g9udv) |
| Navigation | BottomNavigationView + NavHostFragment |
| Image Loading | Glide |

---

## ✅ ĐÃ HOÀN THÀNH

### Infrastructure
- [x] Firebase project setup (glassity-770c5)
- [x] google-services.json (đã fix lỗi null bytes — file hợp lệ)
- [x] SHA-1 fingerprint đã add vào Firebase Console
- [x] Cloudinary thay Firebase Storage (Spark plan không dùng Storage được)
- [x] **20 sản phẩm** đã seed vào Firestore với ảnh Cloudinary
- [x] **4 categories**: retro / minimal / bold / luxury

### Models (tất cả đã có)
- [x] Product, Category, Cart, CartDetail, Customer
- [x] Order, OrderDetail, Payment, Favorite, Notification

### Repositories (tất cả đã có — chỉ cần gọi, KHÔNG cần viết thêm)
- [x] `ProductRepository` — getAllProducts, getProductsByCategory, getProductById, getBestSellerProducts, searchProducts
- [x] `CartRepository` — getCartItems, addToCart, updateQuantity, removeFromCart, clearCart
- [x] `OrderRepository` — createOrder, getMyOrders, getOrderById, cancelOrder
- [x] `CategoryRepository` — getAllCategories
- [x] `AuthRepository` — login, signup, googleSignIn, forgotPassword, getCurrentUser

### Screens đã code
- [x] **WelcomeActivity** — màn chọn Đăng nhập / Đăng ký / Khách
- [x] **LoginActivity** — Email/Password + Google Sign-In (UI xong, logic OK)
- [x] **SignupActivity** — đăng ký email
- [x] **ForgotPasswordActivity** — reset mật khẩu
- [x] **HomeFragment** (LA.Homepage) — full layout theo Figma: hero, promo banner, sản phẩm bán chạy (RecyclerView ngang), chọn kính theo dạng mắt, bộ sưu tập, lợi ích, sản phẩm nổi bật (grid 2 cột)
- [x] **CategoryFragment** — hiển thị 4 danh mục, click → ProductListActivity
- [x] **ProductListActivity** (DL.Product) — load tất cả / theo category, grid 2 cột, filter chip (UI only)
- [x] **CartFragment** — STUB (chỉ inflate layout, chưa có UI thật)
- [x] **ProfileFragment** — STUB (chỉ hiện tên/email + nút Đăng xuất)

### Adapters
- [x] ProductAdapter — card sản phẩm với Glide, màu dot, btnAddToCart, btnBuyNow, ivFavorite
- [x] FeaturedProductAdapter — card ngang nhỏ cho HomeFragment
- [x] CategoryAdapter — danh mục

### Drawables / Colors
- [x] brand_dark #322D29, brand_wine #72383D, brand_taupe #AC9C8D, brand_beige #D1C7BD, brand_cream #EFE9E1
- [x] bg_btn_wine_filled, bg_btn_wine_outline, bg_circle, bg_face_circle, bg_product_placeholder, bg_rounded_card
- [x] ic_search, ic_menu, ic_arrow_back, ic_filter, ic_heart_outline, ic_google, ic_chevron_right

---

## 🚧 CÒN LẠI (chưa làm)

### Screens chưa có
| Màn hình | Figma frame | Package | Độ ưu tiên |
|---|---|---|---|
| ProductDetailActivity | DL_Product Detail_Layout2 | ui/catalog | 🔴 CAO |
| CartFragment (real) | DL_Cart | ui/order | 🔴 CAO |
| CheckoutActivity | DL_Checkout | ui/order | 🟠 TRUNG |
| PaymentActivity | DL_Payment | ui/order | 🟠 TRUNG |
| OrderHistoryActivity | LA_Orders | ui/order | 🟡 THẤP |
| OrderDetailActivity | DL_Order Detail | ui/order | 🟡 THẤP |
| ProfileFragment (full) | LA_Profile | ui/account | 🟠 TRUNG |
| SearchActivity | LA_Search | ui/catalog | 🟡 THẤP |

### Features còn thiếu
- [ ] `btnFilter` trong ProductListActivity chưa có click handler (filter theo giá/loại)
- [ ] Yêu thích (Favorite) — ivFavorite trong ProductAdapter chưa ghi Firestore
- [ ] Badge số lượng trên icon Cart ở BottomNav / HomeFragment header
- [ ] ProductDetailActivity.start() — HomeFragment và ProductAdapter đang Toast tạm

---

## 🐛 ĐANG BỊ STUCK

### Google Sign-In (error: "communicating with Google servers")
**Nguyên nhân:** Emulator Google Play Store cần đăng nhập Google account trước khi dùng Google Sign-In.

**Fix:** Trên emulator mới (Google Play Store):
1. Vào **Settings → Accounts → Add account → Google**
2. Đăng nhập bằng tài khoản Google cá nhân
3. Sau đó test Google Sign-In trong app

**Hoặc bỏ qua Google Sign-In** — test bằng Email/Password trước (vào Firebase Console → Authentication → Add user → tạo email test).

### Build warning (không block)
- `processDebugGoogleServices` đã fix xong (google-services.json valid)
- Rebuild lại: **Build → Clean Project → Rebuild Project**

---

## 👥 PHÂN CÔNG VIỆC (tránh conflict)

> **Quy tắc:** Mỗi người làm trên branch riêng. Merge vào `main` sau khi xong từng task lớn. KHÔNG cùng sửa 1 file cùng lúc.

---

### 🅰️ PERSON A — Package `ui/account` + `ui/order`

**Branch:** `feature/person-a`

Person A sở hữu toàn bộ luồng **Tài khoản** và **Mua hàng**. Các file dưới đây KHÔNG đụng đến Person B.

#### Tuần này làm theo thứ tự:

**[A1] CartFragment — URGENT**
- File: `ui/order/CartFragment.java` + `fragment_cart.xml`
- Xem Figma frame DL_Cart trước khi code
- Dùng `CartRepository.getCartItems()` để load
- RecyclerView item: ảnh + tên + giá + tăng/giảm quantity + xóa
- Tổng tiền ở dưới + nút "Đặt hàng"
- Cần tạo thêm: `ItemCartBinding` (item_cart.xml)

**[A2] CheckoutActivity**
- File mới: `ui/order/CheckoutActivity.java` + `activity_checkout.xml`
- Xem Figma frame DL_Checkout
- Form: địa chỉ giao hàng, phương thức thanh toán (radio: COD / chuyển khoản)
- Nút "Xác nhận đặt hàng" → gọi `OrderRepository.createOrder()` → `CartRepository.clearCart()`

**[A3] ProfileFragment (full)**
- File: `ui/account/ProfileFragment.java` + `fragment_profile.xml`
- Xem Figma frame LA_Profile
- Hiển thị: avatar (placeholder), tên, email, số điện thoại
- Menu items: Đơn hàng của tôi, Địa chỉ, Đổi mật khẩu, Đăng xuất
- "Đơn hàng của tôi" → OrderHistoryActivity

**[A4] OrderHistoryActivity** (nếu còn thời gian)
- File mới: `ui/order/OrderHistoryActivity.java` + `activity_order_history.xml`
- Xem Figma frame LA_Orders
- RecyclerView: list đơn hàng, mỗi item: mã đơn, ngày, tổng tiền, trạng thái

**Files Person A được phép sửa:**
```
ui/account/ProfileFragment.java
ui/account/fragment_profile.xml  (trong res/layout/)
ui/order/CartFragment.java
ui/order/CheckoutActivity.java   (tạo mới)
ui/order/OrderHistoryActivity.java (tạo mới)
layout/fragment_cart.xml
layout/activity_checkout.xml     (tạo mới)
layout/activity_order_history.xml (tạo mới)
layout/item_cart.xml             (tạo mới)
AndroidManifest.xml              (chỉ thêm <activity> mới, KHÔNG xóa cái cũ)
```

---

### 🅱️ PERSON B — Package `ui/catalog`

**Branch:** `feature/person-b`

Person B sở hữu toàn bộ luồng **Duyệt sản phẩm**.

#### Tuần này làm theo thứ tự:

**[B1] ProductDetailActivity — URGENT**
- File mới: `ui/catalog/ProductDetailActivity.java` + `activity_product_detail.xml`
- Xem Figma frame DL_Product Detail_Layout2 trước khi code
- Hiển thị: ViewPager ảnh, tên, giá, mô tả, chọn màu (RadioButton / chip), số lượng +/-, nút "Thêm vào giỏ" + "Mua ngay"
- `static void start(Context ctx, String productId)` để các screen khác gọi
- Dùng `ProductRepository.getProductById()` để load
- Dùng `CartRepository.addToCart()` khi nhấn "Thêm vào giỏ"
- Sau khi xong: **báo Person A** để thay Toast trong ProductAdapter + CartFragment

**[B2] Kết nối ProductDetailActivity vào HomeFragment + ProductAdapter**
- Sửa `HomeFragment.java`: thay Toast bằng `ProductDetailActivity.start()`
- Sửa `ProductAdapter.java`: thay Toast ở `onProductClick` bằng `ProductDetailActivity.start()`

**[B3] ProductListActivity — bổ sung btnFilter**
- File: `ui/catalog/ProductListActivity.java`
- Xem Figma filter panel
- `btnFilter` → hiện BottomSheetDialog: lọc theo giá, loại kính
- Filter xong → gọi lại `getAllProducts()` với điều kiện thêm

**[B4] HomeFragment — wire kính theo dạng mắt** (nếu còn thời gian)
- 5 circle trong section "CHỌN KÍNH THEO DẠNG MẮT" hiện chưa clickable
- Click → `ProductListActivity.start()` với filter dạng mặt

**Files Person B được phép sửa:**
```
ui/catalog/HomeFragment.java
ui/catalog/CategoryFragment.java
ui/catalog/ProductListActivity.java
ui/catalog/ProductDetailActivity.java  (tạo mới)
layout/fragment_home.xml
layout/fragment_category.xml
layout/activity_product_list.xml
layout/activity_product_detail.xml    (tạo mới)
adapter/ProductAdapter.java
adapter/FeaturedProductAdapter.java
AndroidManifest.xml  (chỉ thêm <activity> ProductDetailActivity)
```

---

## 🔀 QUY TRÌNH MERGE (tránh conflict)

```
main
 ├── feature/person-a   ← Person A làm ở đây
 └── feature/person-b   ← Person B làm ở đây
```

**Khi nào merge:**
- Xong [A1] CartFragment → Person A merge vào main → Person B pull main
- Xong [B1] ProductDetailActivity → Person B merge vào main → Person A pull main
- Merge mỗi ngày cuối buổi để sync sớm

**File dùng chung (cả 2 cẩn thận):**
- `AndroidManifest.xml` — mỗi người chỉ ADD <activity> mới, không xóa dòng cũ
- `strings.xml` — add string mới, không sửa string của người kia
- `themes.xml`, `colors.xml` — đã đủ, hạn chế sửa

**Tuyệt đối KHÔNG sửa cùng lúc:**
- `MainActivity.java` — không ai cần sửa cái này
- `main_nav_graph.xml` — chỉ sửa khi thêm Fragment mới vào BottomNav

---

## 📋 MASTER CHECKLIST (tick theo thứ tự)

```
[ ] Fix Google Sign-In: đăng nhập Google account vào emulator Settings
[ ] Test email/password login (tạo user test trong Firebase Console)
[ ] [B1] ProductDetailActivity — xem Figma → code → test
[ ] [A1] CartFragment — xem Figma → code → test
[ ] [B2] Wire ProductDetailActivity vào Home + ProductAdapter
[ ] [A2] CheckoutActivity + tạo Order
[ ] [A3] ProfileFragment full
[ ] [B3] ProductListActivity filter
[ ] [A4] OrderHistoryActivity (nice to have)
[ ] [B4] HomeFragment dạng mặt (nice to have)
[ ] Demo cuối: chạy full flow: Browse → Detail → Add Cart → Checkout → Xem đơn
```

---

## 💡 LƯU Ý KHI CODE

1. **Vòng đời Fragment** — mọi callback Firestore phải guard `if (binding == null) return;`
2. **Activity** — guard `if (isFinishing() || isDestroyed()) return;`
3. **Comment lifecycle** — viết bằng tiếng Việt trong mỗi file
4. **Viết file lớn** — dùng Python qua terminal, KHÔNG dùng Edit/Write của AI (dễ bị cắt ngắn)
5. **ViewBinding** — KHÔNG dùng `findViewById` ở bất kỳ đâu
6. **Xem Figma trước** — trước khi code màn nào, mở đúng frame Figma tương ứng

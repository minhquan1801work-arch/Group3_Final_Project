# Glassity — Tiến độ Person B (HongPhuc)

> Branch: `feature/person-HongPhuc`  
> Package phụ trách: `ui/catalog`

---

## Tóm tắt nhanh

| Task | Trạng thái | File chính |
|------|-----------|------------|
| [B1] ProductDetailActivity | ✅ Xong (Long làm) | `ui/catalog/ProductDetailActivity.java` |
| [B2] Wire Home → ProductDetail | ✅ Xong (Long làm) | `ui/catalog/HomeFragment.java` |
| [B3] ProductListActivity — Filter & Sort | ✅ Xong | `ui/catalog/ProductListActivity.java` |
| [B4] HomeFragment — Dạng mặt clickable | ✅ Xong | `ui/catalog/HomeFragment.java` |

---

## Data Model (v3 — sau refactor)

### 2 chiều phân loại độc lập

| Trường | Kiểu | Ý nghĩa |
|--------|------|---------|
| `categoryId` | String | **Loại sản phẩm** — kính mắt, kính cận, phụ kiện |
| `faceShapes` | Array<String> | **Dạng mặt phù hợp** — có thể nhiều giá trị cùng lúc |

### Category IDs (`categories` collection)

| Document ID | Tên | Ghi chú |
|------------|-----|---------|
| `cat_kinh_mat` | Kính mắt | Kính râm, thời trang |
| `cat_kinh_can` | Kính cận | Kính số, kính đọc sách |
| `cat_phu_kien` | Phụ kiện | Hộp, dây, khăn lau |

> Không có category nào cho dạng mặt — dạng mặt dùng `faceShapes` array trong product.

### faceShapes Values

| Giá trị | Dạng mặt |
|---------|---------|
| `"tron"` | Mặt tròn |
| `"trai_xoan"` | Mặt trái xoan |
| `"trai_tim"` | Mặt trái tim |
| `"kim_cuong"` | Mặt kim cương |
| `"vuong"` | Mặt vuông |

Ví dụ product document:
```json
{
  "name": "Glassity Classic",
  "price": 449000,
  "categoryId": "cat_kinh_mat",
  "faceShapes": ["tron", "trai_xoan"],
  "colors": ["#1A1614", "#C0C0C0"],
  "images": ["...url..."]
}
```

Hằng số public tập trung tại `ProductListActivity.CAT_*` và `ProductListActivity.SHAPE_*`.

---

## [B3] ProductListActivity — Filter, Sort & Chip ✅

### Files thay đổi
- `app/.../ui/catalog/ProductListActivity.java`
- `app/.../res/layout/bottom_sheet_filter.xml` (tạo mới)
- `app/.../repository/ProductRepository.java` — thêm `getProductsByFaceShape()`, `getProductsByFaceShapeAll()`

### Cách hoạt động
1. **Chip "Kính mắt"** → `where("categoryId","==","cat_kinh_mat")`
2. **Chip "Shape kính"** → `whereArrayContainsAny("faceShapes", [5 shape values])`
3. **Chip "Phụ kiện"** → `where("categoryId","==","cat_phu_kien")`
4. **Nút filter** → `BottomSheetDialog` sort giá tăng/giảm (client-side)

### Mở màn hình từ nơi khác
```java
// Theo loại sản phẩm
ProductListActivity.start(context, ProductListActivity.CAT_KINH_MAT, null, "Kính mắt");
ProductListActivity.start(context, ProductListActivity.CAT_PHU_KIEN, null, "Phụ kiện");

// Theo dạng mặt (từ HomeFragment)
ProductListActivity.start(context, null, ProductListActivity.SHAPE_TRON, "Kính mặt tròn");
ProductListActivity.start(context, null, ProductListActivity.SHAPE_TRAI_XOAN, "Kính mặt trái xoan");
ProductListActivity.start(context, null, ProductListActivity.SHAPE_TRAI_TIM, "Kính mặt trái tim");
ProductListActivity.start(context, null, ProductListActivity.SHAPE_KIM_CUONG, "Kính mặt kim cương");
ProductListActivity.start(context, null, ProductListActivity.SHAPE_VUONG, "Kính mặt vuông");

// Tất cả / tất cả shape
ProductListActivity.startAll(context);
ProductListActivity.startAllShapes(context);
```

---

## [B4] HomeFragment — Dạng mặt clickable ✅

### Files thay đổi
- `app/.../res/layout/fragment_home.xml` — thêm `android:id` và `android:clickable` cho 5 LinearLayout dạng mặt
- `app/.../ui/catalog/HomeFragment.java` — 5 click listener dùng `SHAPE_*`

### IDs trong XML
| ID | Dạng mặt | SHAPE constant |
|----|---------|----------------|
| `faceTron` | Tròn | `SHAPE_TRON = "tron"` |
| `faceTraiXoan` | Trái xoan | `SHAPE_TRAI_XOAN = "trai_xoan"` |
| `faceTraiTim` | Trái tim | `SHAPE_TRAI_TIM = "trai_tim"` |
| `faceKimCuong` | Kim cương | `SHAPE_KIM_CUONG = "kim_cuong"` |
| `faceVuong` | Vuông | `SHAPE_VUONG = "vuong"` |

---

## [B1] ProductDetailActivity ✅ (Long làm)

- ViewPager2 ảnh + thumbnail (Cloudinary URL qua Glide)
- Chọn màu (dot động từ `product.colors`)
- Stepper số lượng (min 1, max stock)
- **Thêm vào giỏ**: `CartRepository.addToCart()`
- **Mua ngay**: `addToCartReturningId()` → sang `CheckoutActivity`
- Sản phẩm liên quan (cùng `categoryId`)

```java
ProductDetailActivity.start(context, productId);
```

---

## [B2] HomeFragment ✅ (Long làm)

- Adapter đã wire → `ProductDetailActivity.start()`
- Cart icon → switch tab Cart

---

## Seed Data

File: `app/.../utils/SeedDataHelper.java`  
Gọi từ: `WelcomeActivity.onCreate()`  
Key SharedPreferences: `seed_done_v3`

**Cần làm trước khi seed hoạt động:**
1. Firebase Console → Firestore → Rules → set `allow read, write: if true`
2. Xóa data app trên emulator (Settings → Apps → Clear Data) để reset SharedPreferences
3. Chạy lại app → seed tự động

**Data mẫu được tạo:**
- 3 categories: `cat_kinh_mat`, `cat_kinh_can`, `cat_phu_kien`
- 14 products: 8 kính mắt, 3 kính cận, 3 phụ kiện

---

## Lưu ý cho teammate (Long)

1. Khi thêm product thật vào Firestore, set đúng 2 trường:
   - `categoryId`: `"cat_kinh_mat"` / `"cat_kinh_can"` / `"cat_phu_kien"`
   - `faceShapes`: array các shape phù hợp, ví dụ `["tron", "trai_xoan"]`
2. Phụ kiện để `faceShapes: []` (mảng rỗng)
3. `CartRepository.addToCartReturningId()` cần tồn tại — đang dùng trong "Mua ngay"
4. Firestore index: `whereArrayContainsAny` không cần composite index trừ khi combine thêm `where` khác

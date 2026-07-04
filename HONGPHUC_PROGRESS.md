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

## Category IDs toàn bộ (⚠️ phải khớp với document ID trong Firestore)

| categoryId | Tên hiển thị | Dùng ở đâu |
|-----------|-------------|-----------|
| `cat_kinh_mat` | Kính mắt | Chip "Kính mắt" trong ProductList |
| `cat_phu_kien` | Phụ kiện | Chip "Phụ kiện" trong ProductList |
| `cat_shape_tron` | Kính mặt tròn | Dạng mặt TRON ở Home |
| `cat_shape_trai_xoan` | Kính mặt trái xoan | Dạng mặt TRAI XOAN ở Home |
| `cat_shape_trai_tim` | Kính mặt trái tim | Dạng mặt TRAI TIM ở Home |
| `cat_shape_kim_cuong` | Kính mặt kim cương | Dạng mặt KIM CUONG ở Home |
| `cat_shape_vuong` | Kính mặt vuông | Dạng mặt VUONG ở Home |

Hằng số public tập trung tại `ProductListActivity.CAT_*` — khi đổi ID chỉ cần sửa 1 chỗ.

---

## [B3] ProductListActivity — Filter, Sort & Chip ✅

### Files thay đổi
- `app/src/main/java/.../ui/catalog/ProductListActivity.java`
- `app/src/main/res/layout/bottom_sheet_filter.xml` (tạo mới)
- `app/src/main/java/.../repository/ProductRepository.java` — thêm `getProductsByCategories()`

### Cách hoạt động
1. **Chip "Kính mắt"** → `getProductsByCategory("cat_kinh_mat")`
2. **Chip "Shape kính"** → `getProductsByCategories(5 shape IDs)` (Firestore `whereIn`)
3. **Chip "Phụ kiện"** → `getProductsByCategory("cat_phu_kien")`
4. **Nút filter** (icon góc phải) → `BottomSheetDialog` với 2 radio:
   - "Giá từ thấp đến cao" → sort ASC client-side
   - "Giá từ cao đến thấp" → sort DESC client-side
5. Sort giữ nguyên khi chuyển chip (không reset)

### Mở màn hình từ nơi khác
```java
ProductListActivity.startAll(context);                                         // Tất cả SP
ProductListActivity.startAllShapes(context);                                   // Tất cả 5 shape
ProductListActivity.start(context, ProductListActivity.CAT_KINH_MAT, "Kính mắt");
ProductListActivity.start(context, ProductListActivity.CAT_SHAPE_TRON, "Kính mặt tròn");
// ... tương tự cho 4 shape còn lại
```

---

## [B4] HomeFragment — Dạng mặt clickable ✅

### Files thay đổi
- `app/src/main/res/layout/fragment_home.xml` — thêm `android:id` và `android:clickable` cho 5 LinearLayout dạng mặt
- `app/src/main/java/.../ui/catalog/HomeFragment.java` — thêm 5 click listener

### IDs trong XML
| ID | Dạng mặt | Mở màn hình |
|----|---------|------------|
| `faceTron` | TRON | `CAT_SHAPE_TRON` |
| `faceTraiXoan` | TRAI XOAN | `CAT_SHAPE_TRAI_XOAN` |
| `faceTraiTim` | TRAI TIM | `CAT_SHAPE_TRAI_TIM` |
| `faceKimCuong` | KIM CUONG | `CAT_SHAPE_KIM_CUONG` |
| `faceVuong` | VUONG | `CAT_SHAPE_VUONG` |

---

## [B1] ProductDetailActivity ✅ (Long làm)

### Tính năng đã có
- ViewPager2 ảnh + thumbnail (Cloudinary URL qua Glide)
- Chọn màu (dot động từ `product.colors`)
- Stepper số lượng (min 1, max stock)
- **Thêm vào giỏ**: `CartRepository.addToCart()`
- **Mua ngay**: `addToCartReturningId()` → sang `CheckoutActivity`
- Sản phẩm liên quan (cùng `categoryId`)
- Review demo static
- Guest → redirect `LoginActivity`

```java
ProductDetailActivity.start(context, productId);
```

---

## [B2] HomeFragment ✅ (Long làm)

- Adapter đã wire → `ProductDetailActivity.start()`
- Cart icon → switch tab Cart

---

## Lưu ý cho teammate (Long / người seed data)

1. Khi tạo Firestore collection `categories`, dùng **document ID** theo bảng trên (ví dụ: document `cat_shape_tron`, field `name: "Kính mặt tròn"`).
2. Khi tạo sản phẩm, set `categoryId` đúng theo bảng trên.
3. Chip "Shape kính" trong ProductList dùng `whereIn` — Firestore cần index composite nếu kết hợp với filter khác (hiện chưa cần).
4. `CartRepository.addToCartReturningId()` cần tồn tại (Long implement) — đang dùng trong "Mua ngay".

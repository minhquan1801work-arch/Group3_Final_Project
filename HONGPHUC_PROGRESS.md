# Glassity — Tiến độ Person B (HongPhuc)

> Branch: `feature/person-HongPhuc`  
> Package phụ trách: `ui/catalog`

---

## Tóm tắt nhanh

| Task | Trạng thái | File chính |
|------|-----------|------------|
| [B1] ProductDetailActivity | ✅ Xong (Long làm) | `ui/catalog/ProductDetailActivity.java` |
| [B2] Wire Home → ProductDetail | ✅ Xong | `ui/catalog/HomeFragment.java` |
| [B3] ProductListActivity — Filter & Sort | ✅ Xong | `ui/catalog/ProductListActivity.java` |
| [B4] HomeFragment — Dạng mặt clickable | ✅ Xong | `ui/catalog/HomeFragment.java` |
| [B5] Hamburger DrawerLayout | ✅ Xong | `MainActivity.java`, `layout_nav_drawer.xml` |
| [B6] HomeFragment — Hero carousel, BST tile 3, Blog, Footer | ✅ Xong | `fragment_home.xml`, `HomeFragment.java` |
| [B7] SearchActivity | ✅ Xong | `ui/catalog/SearchActivity.java` |
| [B8] CollectionActivity | ✅ Xong | `ui/catalog/CollectionActivity.java` |
| [B9] Fix chip "Shape Kính" → BottomSheet | ✅ Xong | `ProductListActivity.java` |

---

## Data Model (v3 — sau refactor)

### 2 chiều phân loại độc lập

| Trường | Kiểu | Ý nghĩa |
|--------|------|---------|
| `categoryId` | String | **Loại sản phẩm** — kinh_mat, kinh_can, phu_kien, bst |
| `faceShapes` | Array\<String\> | **Dạng mặt phù hợp** — có thể nhiều giá trị cùng lúc |
| `collection` | String | **Bộ sưu tập** (chỉ BST): "Monochrome Collection" / "Essential Acetate" / "Sunlight Studio" |
| `gender` | String | `"nam"` / `"nu"` / `"unisex"` |

### Category IDs (`categories` collection) — theo Figma LA.Categories

| Document ID | Tên hiển thị | Chip |
|------------|-------------|------|
| `kinh_mat` | Kính Mát (sunglasses) | chipKinhMat (chip 1) |
| `kinh_can` | Kính Cận (prescription) | chipShape (chip 2) — text "Shape Kính ⌄" |
| `phu_kien` | Phụ Kiện    | chipPhuKien (chip 3) |
| `bst`      | Bộ Sưu Tập  | chipLuxury (chip 4) |

### faceShapes Values

| Giá trị | Dạng mặt |
|---------|---------|
| `"tron"` | Mặt tròn |
| `"trai_xoan"` | Mặt trái xoan |
| `"trai_tim"` | Mặt trái tim |
| `"kim_cuong"` | Mặt kim cương |
| `"vuong"` | Mặt vuông |

---

## [B5] Hamburger DrawerLayout ✅

### Files thay đổi
- `app/.../MainActivity.java` — `openDrawer()`, `setupDrawer()`, wire toàn bộ menu items
- `app/.../res/layout/activity_main.xml` — wrap CoordinatorLayout trong DrawerLayout
- `app/.../res/layout/layout_nav_drawer.xml` — nội dung menu: Nam/Nữ tabs, category tree

### Hành vi drawer
- **Kính Mát / Kính Cận / Phụ Kiện / BST** → `ProductListActivity.start(ctx, catId, null, title)`
- **Shape items** (Tròn/Oval/Mắt Mèo/Vuông) → `ProductListActivity.start(ctx, null, shape, title)`
- **BST / sub-collection** → `CollectionActivity.start(ctx, collectionName)`
- Drawer IDs: `drawerLayout` (DrawerLayout), `btnDrawerClose`, `tabNam`, `tabNu`, `tabIndicator`, `menuKinhMat`, `menuGongNhua`, `menuGongKimLoai`, `menuShapeTron`, `menuShapeOval`, `menuShapeMatMeo`, `menuShapeVuong`, `menuPhuKien`, `menuHopDung`, `menuKhanLau`, `menuNuocLau`, `menuBST`, `menuMonochrome`, `menuEssential`, `menuSunlight`, `menuVeGlassity`, `menuBlogChonKinh`

---

## [B6] HomeFragment — Hero + BST + Blog + Footer ✅

### Files thay đổi
- `app/.../res/layout/fragment_home.xml` — toàn bộ rebuild
- `app/.../ui/catalog/HomeFragment.java` — ViewPager2 auto-scroll, dots, click listeners
- `app/.../adapter/HeroBannerAdapter.java` — mới tạo
- `app/.../res/drawable/dot_active.xml`, `dot_inactive.xml` — mới tạo

### Sections trong Home
1. Header bar (hamburger, logo, search, cart)
2. **Hero carousel** — ViewPager2, 3 slides, auto-scroll 3.5s, dot indicators
   - Điền URL: `HomeFragment.HERO_URLS` (3 string rỗng chờ Cloudinary)
3. Promo banner
4. Sản phẩm bán chạy (horizontal scroll)
5. Chọn kính theo dáng mặt (5 circles)
6. Bộ sưu tập — 3 tiles (Monochrome + Essential 2 cột, Sunlight full-width)
   - **IDs tile:** `tileMonochrome`, `tileEssential`, `tileSunlight`
   - Điền ảnh: `imgMonochrome`, `imgEssential`, `imgSunlight` (ImageView src)
7. Lợi ích (4 icon)
8. Sản phẩm nổi bật (grid 2 cột)
9. **Blog** — 2 static cards (horizontal scroll), `blogCard1`, `blogCard2`
10. **Banner "Khám phá Glassity"** — `bannerKhamPha`, ảnh `imgKhamPha`
11. **Footer** — dark bg, social links, copyright

### Cloudinary URLs cần điền (từ HomeFragment.java)
| Vị trí | Cách điền |
|--------|-----------|
| Hero slides 1-3 | `HERO_URLS` list trong `HomeFragment.java` |
| Tile Monochrome | `binding.imgMonochrome` → Glide (nếu muốn dynamic) hoặc `android:src` |
| Tile Essential | `binding.imgEssential` |
| Tile Sunlight | `binding.imgSunlight` |
| Banner Khám phá | `binding.imgKhamPha` |

---

## [B7] SearchActivity ✅

### Files
- `app/.../ui/catalog/SearchActivity.java`
- `app/.../res/layout/activity_search.xml`
- `app/.../res/drawable/bg_search_bar.xml`

### Tính năng
- Lịch sử tìm kiếm lưu `SharedPreferences` (`search_history` / `history`), tối đa 8 từ
- Xóa từng chip lịch sử, nút "Xóa tất cả"
- Kết quả search: `ProductRepository.searchProducts(keyword)` → prefix match Firestore
- Grid 2 cột kết quả, empty state text
- Keyboard tự hiện khi mở

---

## [B8] CollectionActivity ✅

### Files
- `app/.../ui/catalog/CollectionActivity.java`
- `app/.../res/layout/activity_collection.xml`

### Hành vi
- **Không có EXTRA**: hiển thị 3 section (Monochrome + Essential + Sunlight), mỗi section có ảnh placeholder + "Hàng mới về" horizontal RecyclerView
- **Có EXTRA_COLLECTION**: chỉ hiển thị 1 section tương ứng
- Data: `ProductRepository.getProductsByCollection(collection)` query `where("collection","==",value)`
- IDs ảnh: `imgMonochrome`, `imgEssential`, `imgSunlight` (220dp, src sẽ được điền Cloudinary)
- Launch: `CollectionActivity.start(ctx)` hoặc `CollectionActivity.start(ctx, "Monochrome Collection")`

---

## [B9] Fix chip "Shape Kính" ✅

### Thay đổi
- `activity_product_list.xml`: chip text `"Kính Cận"` → `"Shape Kính ⌄"`
- `ProductListActivity.java`: `chipShape` listener → `showShapeSheet()` (không còn `switchToCategory(CAT_KINH_CAN)`)
- `bottom_sheet_shape.xml`: BottomSheet với 5 lựa chọn Tròn/Oval/Mắt Mèo/Vuông/Tất cả shape
- Khi chọn shape: chip text cập nhật `"Shape: [Tên] ⌄"`, query `whereArrayContains("faceShapes", shape)`

---

## [B3] ProductListActivity — Filter, Sort & Chip ✅

### Cách mở từ nơi khác
```java
ProductListActivity.start(context, ProductListActivity.CAT_KINH_MAT, null, "Kính Mát");
ProductListActivity.start(context, null, ProductListActivity.SHAPE_TRON, "Kính mặt tròn");
ProductListActivity.startAll(context);
ProductListActivity.startAllShapes(context);
```

---

## Data Firestore (đã sync)

| Collection | Nội dung |
|-----------|---------|
| `categories` | 4 docs: kinh_mat, kinh_can, phu_kien, bst |
| `products` | 37 docs: 16 kinh_mat, 6 kinh_can, 9 bst, 6 phu_kien |

**3 phụ kiện cần điền ảnh Cloudinary:**
| ID | Tên |
|----|-----|
| `Q6aMez9S8Xi0t6cM5PnI` | Hộp Đựng Kính Da Cao Cấp |
| `SfTyfUdfdgagHyK8aSA5` | Nước Lau Kính Chuyên Dụng 30ml |
| `FNUWhKMCn2Vr1KBr3JJx` | Túi Đựng Kính Nhung Mềm |

---

## Lưu ý cho teammate (Long)

1. Khi thêm product vào Firestore: set `categoryId` + `faceShapes` array đúng format
2. Phụ kiện để `faceShapes: []` (mảng rỗng)
3. `CartRepository.addToCartReturningId()` cần tồn tại (dùng trong ProductDetail "Mua ngay")
4. Firestore index: `whereArrayContainsAny` không cần composite index trừ khi combine thêm `where` khác
5. `ProductRepository.getProductsByCollection(collection)` → cần field `collection` đúng trên Firestore docs

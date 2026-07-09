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

## [B7] SearchActivity ✅ (hoàn thiện 09/07/2026)

### Files
- `app/.../ui/catalog/SearchActivity.java` — viết lại toàn bộ
- `app/.../res/layout/activity_search.xml` — viết lại: thêm section "Gợi ý tìm kiếm" + icon camera
- `app/.../adapter/SearchSuggestAdapter.java` — mới: card gợi ý (ảnh + tên đậm)
- `app/.../res/layout/item_search_suggest.xml` — mới

### Cách hoạt động
- **Search in-memory**: load toàn bộ products 1 lần → tìm contains, bỏ dấu tiếng Việt, không phân biệt hoa thường, khớp tên + mô tả + tên màu variant ("hổ phách" ra được SP có variant Hổ phách). Không dùng Firestore prefix query nữa (hàm `searchProducts` cũ trong repo vẫn giữ cho nơi khác)
- **Type-ahead**: gõ ≥2 ký tự → debounce 250ms → hiện grid sản phẩm khớp ngay. Bấm Enter/chip lịch sử mới lưu lịch sử (không lưu rác từng ký tự)
- **Lịch sử**: chỉ user đã đăng nhập, lưu SharedPreferences key `history_<uid>` (mỗi tài khoản riêng), max 8, mới nhất trước, guest ẩn hẳn section
- **Gợi ý tìm kiếm** (cá nhân hóa): hồ sơ sở thích = SP đã mua (orders→orderDetails) + yêu thích + khớp lịch sử search → chấm điểm SP chưa mua: +3 trùng collection, +2/faceShape trùng, +1 trùng category, +1 cùng khoảng giá ±30% → top 6, điểm bằng thì random. Guest/chưa có dữ liệu → random 6
- **Camera trong search bar**: UI giữ chỗ cho tính năng tìm bằng hình ảnh (Toast "sắp ra mắt") — sẽ bổ sung sau. Icon camera ↔ nút X hoán đổi theo trạng thái đang gõ

---

## [B8] CollectionActivity ✅ (rework 08/07/2026 theo Figma)

### Files
- `app/.../ui/catalog/CollectionActivity.java` — viết lại toàn bộ, 2 chế độ
- `app/.../res/layout/activity_collection.xml` — viết lại toàn bộ
- `app/.../adapter/CollectionProductAdapter.java` — mới: card SP nhỏ (ảnh 110dp + tên + giá)
- `app/.../res/layout/item_collection_product.xml` — mới
- `app/.../res/drawable/bg_sheet_top_rounded.xml` — mới: sheet trắng bo góc trên

### Chế độ A — danh sách BST (không EXTRA, Figma "March Collection")
- Nền tối `#4A4443`, header chữ "March" (cursive) + "COLLECTION" letterspaced
- 3 section xếp dọc: ảnh full-width 380dp + hàng `01 ─── MONOCHROME COLLECTION`
- Bấm section → mở lại activity với EXTRA của BST đó
- IDs: `layoutAll`, `btnBackAll`, `sectionAllMonochrome/Essential/Sunlight`, `imgAllMonochrome/Essential/Sunlight`

### Chế độ B — chi tiết 1 BST (có EXTRA_COLLECTION)
- Hero full-bleed 440dp + title serif overlay (`imgHero`, `tvHeroTitle`) + back trắng
- Sheet trắng bo góc trên (margin -24dp đè lên hero): "HÀNG MỚI VỀ" + mũi tên `btnPrev`/`btnNext` (smoothScroll), RecyclerView `rvProducts` card nhỏ ngang
- Nút `btnViewAll` "Xem tất cả" → `ProductListActivity.startCollection(ctx, collection)`: trang danh sách sản phẩm chỉ hiện SP thuộc BST đó (title = tên BST, vẫn có filter/sort)
- `ProductListActivity` thêm `EXTRA_COLLECTION` + `loadByCollection()`; bấm chip category/shape khác sẽ reset filter collection

---

## Merge vào main (08/07/2026) ✅

Đã merge `feature/person-HongPhuc` → `main`, resolve 9 file conflict:

| File | Cách resolve |
|------|--------------|
| `CollectionActivity.java`, `Product.java`, `ProductVariant.java` | Lấy bản HongPhuc (rewrite Figma + colorName + getTotalStock) |
| `CartAdapter.java` | Giữ bản main: `onVariantClick(item)` 1 tham số (khớp CartFragment) |
| `ProductDetailActivity.java` | Ảnh/variants lấy bản HongPhuc; **guest-flow lấy bản main** (guest Mua ngay → Checkout trực tiếp, Thêm giỏ → LoginRequiredDialog). Xóa method trùng `currentVariantColor/Stock` (bản dùng `selectedColorIndex` không tồn tại) |
| `ProductAdapter.java` | Kết hợp: `resolveThumbnailUrl()` của main + color dots từ variants |
| `.gitignore` | Union: `service-account*`, `*.pem`, `cloudinary-config.json`, `node_modules/` |
| `package.json` / lock | Union 3 deps: cloudinary + express + firebase-admin, regen lock |

Build OK sau merge. Lưu ý cho lần merge sau: file `adapter/` + `model/` là vùng chạm nhau giữa 2 người — luôn kiểm tra interface signature (`onVariantClick`) và guest-flow trước khi lấy đè.

### Home (fragment_home.xml) — BST đổi thành carousel ngang
- 3 tile cũ (2+1 grid) → `HorizontalScrollView` card 200dp: ảnh dọc 265dp + tên BST bên dưới (letterspaced) — lướt ngang như Figma
- IDs giữ nguyên: `tileMonochrome/tileEssential/tileSunlight`, `imgMonochrome/imgEssential/imgSunlight`, `btnViewAllCollections` → HomeFragment.java không phải sửa

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

## Data Firestore (đã sync — 06/07/2026)

| Collection | Nội dung |
|-----------|---------|
| `categories` | 4 docs: kinh_mat, kinh_can, phu_kien, bst |
| `products` | 20 docs kính mát — Cloudinary + đầy đủ data |

### 20 sản phẩm kính mát (categoryId: `kinh_mat`)

Tất cả đã có: `name`, `categoryId`, `variants[{color,stock,images}]`, `faceShapes`, `gender`, `price`, `description`, `collection`.

- Ảnh lưu trên Cloudinary: `cloud_name: aa1g9udv`, folder `glassity/products/<TÊN SP>/`
- Mỗi ảnh = 1 variant riêng (color hex theo màu gọng, stock: 10 mỗi variant)
- Script tạo data: `upload_products.js` + `update_products.js` ở root repo

### faceShapes values thực tế trong Firestore (khớp app)

| Giá trị | Hiển thị trong app |
|---------|---------|
| `"tron"` | Tròn |
| `"trai_xoan"` | Oval |
| `"trai_tim"` | Mắt Mèo (cat-eye) |
| `"vuong"` | Vuông |

> **Lưu ý:** BottomSheet "Oval" → query `"trai_xoan"`, "Mắt Mèo" → query `"trai_tim"`

---

## Fix: Data cũ trùng lặp + ảnh không hiện + tên màu (06/07/2026)

### Vấn đề phát hiện
1. **20 doc trùng lặp**: đợt seed trước đã tạo 20 sản phẩm tên tiếng Việt (vd "Kinh mat Avant-Garde", "Kinh mat trong tron ho phanh") dùng chung ảnh Cloudinary với 20 sản phẩm mới (tên tiếng Anh, có variants đầy đủ). Đã **xóa 20 doc cũ này** bằng script `cleanup_and_names.js`.
2. **Ảnh sản phẩm không hiện** trong `ProductAdapter` và `FeaturedProductAdapter`: 2 adapter này đang đọc `product.getImages()` (field cũ, rỗng với data mới lưu trong `variants[i].images`). Đã sửa: ưu tiên đọc `variants[0].images[0]`, fallback `images` cũ nếu không có variants.
3. **Color dots trong `ProductAdapter`**: cũng đang đọc `product.getColors()` cũ → sửa lấy màu từ `variants[i].color`.

### Field mới: `ProductVariant.colorName`
- Thêm field `colorName` (String) vào `ProductVariant.java` — tên màu tiếng Việt hiển thị (vd "Hổ phách", "Đen bóng", "Xanh lục bảo"), tách biệt với `color` (hex dùng cho ô dot).
- Đã điền `colorName` cho toàn bộ 20 sản phẩm kính mát qua `cleanup_and_names.js`.
- `ProductDetailActivity.refreshColorDots()` giờ ưu tiên hiển thị `colorName`, fallback map hex cũ (`colorName()` method) nếu variant không có tên.
- `CartAdapter` — chip "Màu: X ▾" tra `colorName` theo variant khớp hex; thumbnail giỏ hàng cũng lấy ảnh đúng variant đã chọn (trước đó luôn lấy `images[0]` chung chung).

### Fallback tương thích ngược
- `Product.getImages()` giờ tự fallback về `variants.get(0).getImages()` khi field `images` cũ rỗng — nhờ vậy các màn hình của Quân (`OrderDetailActivity`, `CheckoutActivity`, `ReviewActivity`, `OrderHistoryActivity`) **không cần sửa gì** vẫn hiện đúng ảnh.

### Files đã sửa
- `model/ProductVariant.java` — thêm `colorName` + getter/setter
- `model/Product.java` — `getImages()` fallback về variant đầu tiên
- `adapter/ProductAdapter.java` — ảnh + color dots đọc từ variants
- `adapter/FeaturedProductAdapter.java` — ảnh đọc từ variants
- `adapter/CartAdapter.java` — ảnh + tên màu đọc theo variant khớp
- `ui/catalog/ProductDetailActivity.java` — hiển thị `colorName` thay vì hex

### Scripts đã chạy (root repo, không cần chạy lại)
- `cleanup_and_names.js` — xóa 20 doc cũ + set `colorName` cho 20 sản phẩm mới
- `list_products.js` — script debug liệt kê toàn bộ `products` collection (giữ lại để tiện kiểm tra sau này)

---

## Điền ảnh giao diện tĩnh (Hero, BST tile, blog, dáng mặt) — 06/07/2026

Nguồn ảnh: `D:\Glassity\photo-20260706T105430Z-3-001\photo\`. Đã upload 11 ảnh lên Cloudinary folder `glassity/site/` qua script `upload_site_images.js`.

### Mapping ảnh → vị trí

| Vị trí | File gốc | Public ID Cloudinary |
|--------|---------|----------------------|
| Hero slide 1 | `bg1.png` (nữ blazer đen, cat-eye) | `glassity/site/hero_bg1` |
| Hero slide 2 | `james.heic` → jpg (wrap shades) | `glassity/site/hero_james` |
| Hero slide 3 | `juhoon.jpg` (sunglasses đôi) | `glassity/site/hero_juhoon` |
| **Promo banner "Giảm 20%"** | `sasalele.png` (chevron + lấp lánh, khớp Figma) | `glassity/site/promo_sasalele` |
| Tile Monochrome (Home + Collection) | `bg1.png` | `glassity/site/hero_bg1` |
| Tile Essential (Home + Collection) | `background.png` (flatlay be) | `glassity/site/flatlay_background` |
| Tile Sunlight (Home + Collection) | `bg2.png` (nắng + cầu vồng) | `glassity/site/hero_bg2` |
| Banner "Khám phá Glassity" | `1.jpg` (chàng trai áo đỏ cầm hoa, khớp Figma) | `glassity/site/kham_pha_flowers` |
| Blog card 1 "Cách chọn kính..." | `guide.png` (sơ đồ Tròng/Gọng/Càng kính) | `glassity/site/guide_diagram` |
| Blog card 2 "Xu hướng 2025..." | `login_signup.jpg` (kính trên đá thạch cao) | `glassity/site/blog_login_signup` |
| Vòng dáng mặt TRÒN | `shape1.png` (mask tròn có sẵn) | `glassity/site/shape_tron` |
| Vòng dáng mặt TRÁI XOAN | `Shape2.png` (mask oval có sẵn) | `glassity/site/shape_trai_xoan` |
| Vòng dáng mặt TRÁI TIM | `shape3.png` (mask tim có sẵn) | `glassity/site/shape_trai_tim` |
| Vòng dáng mặt KIM CƯƠNG | `shape4.png` (mask kim cương có sẵn) | `glassity/site/shape_kim_cuong` |
| Vòng dáng mặt VUÔNG | `shpe5.png` (mask vuông bo góc có sẵn) | `glassity/site/shape_vuong` |

### Files đã sửa
- `HomeFragment.java` — `HERO_URLS` điền 3 URL thật; thêm hằng số `URL_MONOCHROME/ESSENTIAL/SUNLIGHT/KHAM_PHA/BLOG_GUIDE/BLOG_TREND/SHAPE_*`; thêm method `setupStaticImages()` load Glide cho tất cả ImageView tĩnh, gọi trong `onViewCreated()`
- `CollectionActivity.java` — thêm cùng 3 hằng số Monochrome/Essential/Sunlight, load Glide ngay trong `onCreate()`
- `fragment_home.xml` — đổi `<View>` → `<ImageView>` cho `blogCard1` (`imgBlog1`), `blogCard2` (`imgBlog2`), và 5 `<View>` dáng mặt bên trong `faceTron/faceTraiXoan/faceTraiTim/faceKimCuong/faceVuong` (id mới: `imgFaceTron`, `imgFaceTraiXoan`, `imgFaceTraiTim`, `imgFaceKimCuong`, `imgFaceVuong`) — id LinearLayout cha giữ nguyên nên click listener trong `HomeFragment.java` không đổi; thêm `imgPromo` (ImageView full-bleed) vào `layoutPromoBanner`, clip bo góc bằng `setClipToOutline(true)` trong Java

### Còn thiếu — KHÔNG đụng vì thuộc phạm vi Quân (`ui/account`, `ui/order`)
Các ảnh còn lại trong thư mục `photo/` chưa dùng vì không có placeholder trong `ui/catalog` và đụng vào sẽ động tới màn hình của Quân:
- Logo ngân hàng: `bidv.webp`, `mbbank.png`, `vietcombank.webp`, `vietinbank.png`, `momo.png`, `vnpay.png`, `zalopay.png` — có thể dùng cho màn chọn ngân hàng trong `CheckoutActivity`/`PaymentResultActivity` (file của Quân)
- Ảnh chân dung: `james.heic`, `juhoon.jpg`, `karina.jpg`, `keonho.jpg`, `martin.jpg`, `sasalele.png`, `1.jpg` — có thể dùng làm avatar review demo (`ReviewActivity`, file của Quân) hoặc testimonial
- `login_signup.jpg` — có thể dùng làm background `WelcomeActivity`/`LoginActivity` (file của Quân)
- 11 ảnh `glass2.png`–`glass12.png` còn dư, chưa dùng — có thể dùng thêm cho blog/carousel nếu cần mở rộng sau

Nếu Quân muốn dùng các ảnh này, đã upload sẵn nguồn tại `D:\Glassity\photo-20260706T105430Z-3-001\photo\`, chưa lên Cloudinary.

### Collections phân bổ

| Collection | Sản phẩm |
|-----------|---------|
| `Monochrome Collection` | Unique Design, Futuristic, Avant Garde, Aero Future, Cyber Fashion, Metallic Future |
| `Sunlight Studio` | Amber Clear Round, Modern Oval, Luxury Amber Tone, Retro Cat Eye, Luxury Cat Eye |
| `Essential Acetate` | Bold Square, Modern Square, Retro Oval, Minimal Metal, Urban Metal, Slim Metal, Premium Metal, Retro Cat Eye Style, Urban Square, Minimal Clear Round |

---

## Lưu ý cho teammate (Long)

1. Khi thêm product vào Firestore: set `categoryId` + `faceShapes` array đúng format
2. Phụ kiện để `faceShapes: []` (mảng rỗng)
3. `CartRepository.addToCartReturningId()` cần tồn tại (dùng trong ProductDetail "Mua ngay")
4. Firestore index: `whereArrayContainsAny` không cần composite index trừ khi combine thêm `where` khác
5. `ProductRepository.getProductsByCollection(collection)` → cần field `collection` đúng trên Firestore docs

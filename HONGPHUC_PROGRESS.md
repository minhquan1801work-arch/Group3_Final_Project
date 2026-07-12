# Glassity — Tiến độ Person B (HongPhuc)

> Branch: `feature/person-HongPhuc`  
> Package phụ trách: `ui/catalog`

---

## 📋 TÓM TẮT TOÀN BỘ CÔNG VIỆC (dùng báo cáo giảng viên)

> Chi tiết kỹ thuật từng mục nằm trong các phần bên dưới (tìm theo ngày/tên mục). Phần này chỉ liệt kê **đã làm gì và tại sao**, không đi sâu code.

### A. Trang chủ (Home)
- Xây dựng lại toàn bộ trang chủ khớp Figma: header (logo, tìm kiếm, giỏ hàng), hero carousel 3 sản phẩm nổi bật (peek carousel — card giữa to, 2 bên ló ra, tự phóng to/thu nhỏ theo lượt, nút "XEM NGAY" dạng pill xám mờ), banner khuyến mãi "Mua 2 giảm 20%", danh sách sản phẩm bán chạy, chọn kính theo 5 dáng mặt, 3 bộ sưu tập (carousel ngang), 6 tile lợi ích, 3 bài blog, banner "Khám phá Glassity", footer đầy đủ (mạng xã hội, liên hệ, About/Contact/Policy)
- Gắn ID sản phẩm thật vào 3 slide hero (trước rỗng, bấm không có gì)
- Gắn banner khuyến mãi → mở trang tất cả sản phẩm (trước không bấm được)
- Fix contrast chữ mờ trên card blog (nền be trùng màu chữ) + chiều cao 3 card lệch nhau

### B. Trang chi tiết sản phẩm (ProductDetail)
- Hiển thị đánh giá (review) thật từ Firestore thay vì data giả cứng — sao động theo rating thật, ngày tháng, ảnh review (xem mục D)
- Dời khối "Màu sắc + Số lượng" lên ngay sau thông tin sản phẩm (trước nằm dưới phần đánh giá, bất tiện)
- Thêm ảnh sơ đồ kích thước (Tròng/Gọng/Càng kính) vào đầu phần "Kích thước sản phẩm"
- Đặt sẵn nút "Thử kính ảo" đúng vị trí theo mockup (góc dưới-phải ảnh chính) — **UI xong, logic AR thật chưa làm** (xem `SPEC_CAMERA_TRYON.md`)

### C. Trang Blog (3 bài viết đầy đủ)
- Viết đầy đủ nội dung 3 bài blog (Gọng kính Hot Trend 2026, Sự trở lại gọng kính Retro, Bí kíp chọn kính mát mùa hè) kèm ảnh thật, dáng mặt, thumbnail xu hướng
- Gắn đủ 3 bài vào trang chủ + drawer menu (trước thiếu 2/3 bài)
- Icon nhà trong header Blog → về thẳng trang chủ

### D. Đánh giá sản phẩm (Reviews)
- Bỏ data giả cứng (`DEMO_REVIEWS`), query thật từ Firestore, tính rating trung bình + số lượng thật
- Trang "Tất cả đánh giá" riêng (trước bấm "Xem tất cả" không có gì)
- Ảnh đính kèm review: hiển thị đúng, **bấm vào xem full-size được** (pinch-zoom, vuốt qua lại nếu nhiều ảnh — dùng thư viện PhotoView)
- Seed dữ liệu: 122 review text (từ 11 khách hàng thật) + 6 review có ảnh minh họa để test UI

### E. Trang About (Giới thiệu), Contact (Liên hệ), Chính sách/FAQ
- Trang "Về Glassity" riêng theo đúng mockup (sứ mệnh, tầm nhìn, chứng nhận, thành tựu)
- Viết lại trang Liên hệ khớp mockup: hotline/email/địa chỉ đúng icon, đúng số điện thoại và địa chỉ thật (data cũ bị sai/lỗi thời), thêm nút mở Google Maps
- Fix chữ mờ khó đọc ở 4 trang chính sách (FAQ, Bảo hành, Bảo mật, Giao hàng & Kiểm tra) — cùng nguyên nhân màu chữ nhạt như blog

### F. Thương hiệu (Logo, App icon)
- Thay toàn bộ chữ "Glassity" trong header (Home, Blog, Drawer, ProductDetail, Profile, Favorite) bằng logo thật, đều gắn nút bấm về trang chủ (rà soát phát hiện 4/6 chỗ quên gắn click, đã fix hết)
- Đổi app icon từ robot Android mặc định sang logo thương hiệu thật (nền cream)

### G. Hiệu năng (Performance)
- Phát hiện nguyên nhân ảnh tải chậm: ảnh gốc quá nặng (800KB-2MB), không nén/resize
- Thêm tối ưu Cloudinary tự động (đổi định dạng + nén + resize đúng kích thước hiển thị) cho ~25 chỗ tải ảnh toàn app
- Thêm kỹ thuật "blur-up" (hiện ảnh mờ nhẹ trong lúc chờ) + preload ảnh hero ngay khi mở app — giảm cảm giác chờ

### H. Sự cố đã xử lý khẩn
- WebView nhúng Google Maps gây văng ra Chrome + app không ổn định — phát hiện nguyên nhân (thiếu WebViewClient) và gỡ bỏ ngay, thay bằng giải pháp tĩnh an toàn hơn
- Lỗi header/nút bị đè bởi thanh trạng thái (status bar) ở drawer và trang xem ảnh — phát hiện và fix theo đúng pattern chuẩn của app

### I. Dữ liệu sản phẩm (Data)
- Import đợt 2: 32 sản phẩm mới + cập nhật ảnh thật cho 12 sản phẩm cũ còn dùng ảnh demo → tổng **64 sản phẩm thật** trên Firestore
- Gán màu sắc + mô tả tiếng Việt cho toàn bộ sản phẩm mới

### J. Hợp tác nhóm
- Merge nhánh của Quân vào main, xử lý 6 file xung đột (giữ đúng phần mới hơn, đồng bộ app icon theo hướng nhóm chọn)
- Ghi hướng dẫn kỹ thuật cho Quân (footer dùng chung, cách tái sử dụng) trong `TODO_PHAN_CONG.md`

### 🔜 Việc tiếp theo (chưa làm, đã có kế hoạch chi tiết)
- **Tìm sản phẩm bằng camera/gallery** + **Thử kính ảo (AR)** — xem file **`SPEC_CAMERA_TRYON.md`** (kế hoạch kỹ thuật đầy đủ: thư viện cần dùng, luồng xử lý, điểm nghẽn đã bàn với user)

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

## [B12] Filter v2 — 5 chip khớp hamburger + 3 field Firestore mới (09/07/2026) ✅

### Data: 3 field mới trong `products` (đã chạy `update_attrs.js`, 37/37 doc)
| Field | Giá trị | Ý nghĩa |
|---|---|---|
| `frameShape` | `tron` / `oval` / `mat_meo` / `vuong` | **Hình dáng GỌNG kính** — KHÁC `faceShapes` (dáng khuôn mặt phù hợp) |
| `material` | `nhua` / `kim_loai` | Chất liệu gọng |
| `accessoryType` | `hop_dung` / `khan_lau` / `nuoc_lau` / `tui_dung` / `day_deo` | Loại phụ kiện |

`Product.java` đã thêm 3 field + getter/setter. `HUONG_DAN_THEM_SAN_PHAM.md` đã cập nhật cột mới.

### Logic filter mới (thay bản multi-filter AND tự do của B11)
**5 chip = 4 nhóm LOẠI TRỪ nhau** (chọn nhóm này tắt nhóm kia), duy nhất Gọng+Shape combine:
1. `Kính Mát` — toggle trực tiếp
2. `Gọng Kính ⌄` — sheet Nhựa/Kim Loại/Bỏ chọn → **ép Kính Cận**
3. `Shape Kính ⌄` — sheet Tròn/Oval/Mắt Mèo/Vuông/Bỏ chọn (lọc `frameShape`) → **ép Kính Cận**; Gọng+Shape chọn đồng thời được
4. `Phụ Kiện ⌄` — sheet Tất cả/Hộp/Khăn/Nước/Bỏ chọn (lọc `accessoryType`)
5. `Bộ Sưu Tập ⌄` — sheet Tất cả/3 BST/Bỏ chọn

**Ngữ cảnh dáng mặt từ trang chủ**: vào bằng nút "Chọn kính theo dáng mặt" → title "Kính hợp mặt Tròn" **giữ nguyên suốt phiên** (không chip nào sáng, không bỏ được — để khách biết đang chọn trong tập đó), các chip lọc thêm bên trong. Title ví dụ: "Kính hợp mặt Tròn · Kính Mát". **Chip Phụ Kiện bị ẨN trong ngữ cảnh này** (phụ kiện không có faceShapes → giao 2 tập luôn rỗng, phi logic).

### Drawer (MainActivity)
- Shape items → `startFrameShape()` (hình dáng gọng — trước đây trỏ nhầm faceShapes)
- Phụ Kiện + 3 mục con → `startAccessory(type)`
- Launcher mới: `startFrameShape(ctx, shape)`, `startAccessory(ctx, type|null)`

### Files
- `ProductListActivity.java` — viết lại state machine Mode {KINH_MAT, KINH_CAN, PHU_KIEN, BST} + fixedFaceShape; field mới có fallback heuristic cho doc cũ
- `bottom_sheet_material/accessory/collection.xml` — mới; `bottom_sheet_shape.xml` — đổi title + "Bỏ chọn"
- `activity_product_list.xml` — 5 chip
- `update_attrs.js` — script điền field (root repo)

---

## [B11] ProductListActivity rework — multi-filter (09/07/2026) — ⚠️ ĐÃ THAY BẰNG B12

### Vấn đề đã fix
1. **Ô sản phẩm lệch chiều cao theo tên** → `item_product.xml` tvName `minLines=2 + maxLines=2 + ellipsize=end`: tên luôn chiếm đúng 2 dòng, dài quá hiện "..."
2. **Filter không chọn nhiều loại cùng lúc** → viết lại toàn bộ filter thành in-memory (load products 1 lần): các chiều filter độc lập AND với nhau — loại (Kính Mát/Phụ Kiện) + chất liệu (Gọng Nhựa/Kim Loại) + shape + BST. Bấm chip đang active = bỏ filter đó
3. **Empty-text đè lên danh sách** → hết race giữa 2 query Firestore (giờ chỉ filter RAM, đồng bộ); text ẩn khi đang loading
4. **Thiếu Gọng Nhựa / Gọng Kim Loại** → thêm 2 chip (đủ 6 khớp 5 danh mục hamburger + shape). Data chưa có field material nên phân loại heuristic theo tên+mô tả (`metal/metallic/kim loai/titan/thep` → kim loại, còn lại → nhựa). **Nâng cấp sau:** thêm field `material` vào Firestore
5. **Title dính chữ cũ** → `updateTitle()` tính lại từ filter đang bật, nối bằng " · " (vd "Kính Mát · Gọng Nhựa · Shape Tròn"), không filter nào = "Tất cả sản phẩm"
6. **Rà soát thêm — 2 bug tự phát hiện:** (a) nút "Thêm vào giỏ"/"Mua ngay" ở trang này chưa wire `CartQuickActions` (chỉ Toast giả) → đã wire; (b) drawer "Gọng Nhựa"/"Gọng Kim Loại" đều trỏ nhầm `CAT_KINH_CAN` → giờ dùng `startMaterial()`
7. Chip "Bộ Sưu Tập" trước query `categoryId=="bst"` (không còn SP nào như vậy → luôn rỗng) → giờ filter SP có field `collection` khác rỗng

### API mới cho teammate
- `ProductListActivity.startMaterial(ctx, MAT_NHUA | MAT_KIM_LOAI)`
- Launchers cũ (`start/startAll/startAllShapes/startCollection`) giữ nguyên chữ ký; riêng tham số `title` giờ bị bỏ qua (title tự tính theo filter)

---

## [B10] Homepage khớp Figma 100% (09/07/2026) ✅

### Thay đổi lớn theo Figma
| Section | Trước | Sau (Figma) |
|---|---|---|
| Hero carousel | Full-width + dots | **Peek carousel**: slide giữa to, 2 bên ló ra, scale animation, mỗi slide có label "XEM NGAY" gạch chân. Bỏ dots |
| Hero click | Không có | Bấm slide → `ProductDetailActivity`. **ID sản phẩm điền tại `HERO_PRODUCT_IDS` trong `HomeFragment.java`** (đang rỗng → Toast "Sắp ra mắt"). Điền document ID Firestore theo đúng thứ tự slide |
| Promo banner | Text trái trên, nút wine | Cao 200dp, text IN HOA căn giữa dưới, nút đen `bg_btn_black_filled` căn giữa |
| Card "Bán chạy" | Tên 2 dòng + giá | Tên 1 dòng + icon tim góc ảnh, **ẩn giá** (`tvPrice` gone, giữ id cho adapter) |
| Dáng mặt | 64dp, label đen, hàng 2 lệch trái | 84dp, label **màu riêng từng shape** (đỏ/teal/vàng/xanh/cam, bold), hàng 2 căn giữa |
| SẢN PHẨM NỔI BẬT | Grid 2 cột | **Xóa hẳn** (Figma không có) — `productAdapter`/`rvProducts`/`tvEmptyProducts` đã gỡ khỏi HomeFragment, badge giỏ hàng giờ refresh qua `CartQuickActions.refreshBadge()` |
| Lợi ích | 4 dòng text | **6 tile 2 cột** có viền (styles `BenefitTile/Icon/Text` trong themes.xml): vận chuyển, đo mắt, đổi trả, hỗ trợ 24/7, chính hãng, thanh toán |
| Thứ tự section | BST→Lợi ích→Nổi bật→Blog | BST→**Blog→Lợi ích** (khớp Figma) |
| Khám phá Glassity | Banner overlay chữ giữa | Nền trắng: ảnh + **chữ dọc "GLASSITY" serif xoay 90°** + tagline dọc + link "KHÁM PHÁ GLASSITY" gạch chân (`tvKhamPhaLink`) |
| Footer | Nền đen | **Nền sáng #F7F5F2 căn giữa**: 3 icon MXH (vector mới `ic_social_x/ig/yt`), ornament ─◇─, email/SĐT/giờ mở cửa (serif), hàng About/Contact/Policy (id `footerAbout/footerContact/footerPolicy` — **trang đích bổ sung sau**), dòng bản quyền |

### Files
- `fragment_home.xml` — sửa lớn toàn bộ các section trên
- `HomeFragment.java` — hero peek transformer + `HERO_PRODUCT_IDS` + gỡ section nổi bật
- `HeroBannerAdapter.java` — dùng layout `item_hero_banner.xml` (mới) thay ImageView trần
- `item_product_featured.xml` — card bán chạy mới
- `themes.xml` — 3 style BenefitTile/BenefitIcon/BenefitText
- Drawable mới: `bg_benefit_tile`, `ic_social_x`, `ic_social_ig`, `ic_social_yt`
- `item_hero_banner.xml` — mới

### Chờ điền sau
- [ ] `HERO_PRODUCT_IDS` (3 document ID) — `HomeFragment.java` gần đầu file
- [ ] Trang đích About / Contact / Policy cho footer (id đã có sẵn, chưa gắn listener)

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
| `products` | **32 docs** (cập nhật 09/07): 20 kính mát ảnh thật + 6 kính cận seed + 6 phụ kiện seed. Đã xóa 5 kính mát seed trùng vai trò (Glassity Urban/Vintage/Wave/Edge/Pilot — ảnh demo). Kính cận + phụ kiện vẫn dùng ảnh demo, chờ ảnh thật |

### ⚠️ 12 sản phẩm CẦN TÌM ẢNH THẬT (đang dùng ảnh demo Cloudinary)

**Kính cận (6)** — khi tìm ảnh chú ý khớp mô tả gọng có sẵn:

| Tên | Hình dáng gọng | Chất liệu | Ghi chú mô tả hiện tại |
|---|---|---|---|
| Glassity Soft Mini | tron | nhua | Gọng tròn nhỏ, hợp trán rộng |
| Glassity Vision Round | tron | nhua | Gọng tròn nhẹ, cận số thấp→cao |
| Glassity Oval Frame | oval | nhua | Gọng chữ nhật góc vuông (mô tả seed hơi lệch tên — tìm ảnh oval rồi sửa lại mô tả) |
| Glassity Vision Square | vuong | nhua | Gọng chữ nhật thanh mảnh, trẻ trung |
| Glassity Vision Air | tron | kim_loai | Gọng siêu nhẹ titanium |
| Glassity Classic | tron | nhua | Gọng nhựa cao cấp, tròng UV400 |

**Phụ kiện (6)**:

| Tên | accessoryType |
|---|---|
| Hộp Đựng Kính Da Cao Cấp | hop_dung |
| Hộp Đựng Kính Glassity Box | hop_dung |
| Khăn Lau Kính Microfiber | khan_lau |
| Nước Lau Kính Chuyên Dụng 30ml | nuoc_lau |
| Túi Đựng Kính Nhung Mềm | tui_dung |
| Dây Đeo Kính Silicon | day_deo |

> Chuẩn bị ảnh theo format `HUONG_DAN_THEM_SAN_PHAM.md` (thư mục tên IN HOA + ảnh đánh số). Vì các sản phẩm này **đã tồn tại trên Firestore**, chỉ cần thay ảnh — gửi Phúc kèm ghi chú "thay ảnh cho SP có sẵn" để chạy script update thay vì tạo mới.

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

---

## Cập nhật 10/07/2026 — Import sản phẩm đợt 2 + Review thật

### Import đợt 2 (script `import_batch2.js`)
- Nguồn: sheet "Thông tin sản phẩm.xlsx" (38 SP) + folder ảnh kính (38 folder) + folder PHU KIEN (6 folder)
- Kết quả: **32 SP mới tạo, 6 Glassity + 6 phụ kiện cập nhật ảnh thật → tổng 64 sản phẩm** trong Firestore
- Ảnh upload Cloudinary path `glassity/products/{tên}/{n}`; tồn kho random 5–15/variant
- Mục "12 SP ảnh demo" cũ đã giải quyết xong — không còn SP dùng ảnh demo

### Màu variant + mô tả (script `apply_colors_batch2.js` + `colors_batch2.js`)
- Xem thủ công từng ảnh của 38 folder để gán hex + tên màu tiếng Việt cho từng variant
- Sinh mô tả tiếng Việt cho toàn bộ SP thiếu description (38 SP kính + phụ kiện)

### Review thật trên trang sản phẩm ✅
- `ProductDetailActivity`: bỏ `DEMO_REVIEWS`, query `reviews` where productId (sort client-side createdAt desc), sao động theo rating, ngày dd/MM/yyyy, thumbnails ảnh review (`item_review.xml` thêm HorizontalScrollView), rating trung bình + số lượng thật, empty state "Chưa có đánh giá"
- `ReviewActivity`: lưu thêm `userName` (lấy từ customers/{uid}.name) khi gửi review
- Seed dữ liệu (`seed_reviews.js`): **122 review tiếng Việt** từ 11 tài khoản khách thật, 1–3 review/SP, rating thiên 4–5 sao, ngày random trong 60 ngày — SP đã có review thật thì bỏ qua
- Build assembleDebug PASS

### Script chạy 1 lần (không commit secrets)
`import_batch2.js`, `colors_batch2.js`, `apply_colors_batch2.js`, `seed_reviews.js` — cần `service-account.json` + `cloudinary-config.json` (gitignored)

### Sửa trang "Về Glassity" theo phản hồi (11/07/2026)
- Banner "Khám phá Glassity" ở Home giờ mở `AboutActivity` thay vì `ProductListActivity` (đúng theo Figma)
- Bỏ carousel tự động (Handler/Runnable) ở hero — thay bằng 2 ảnh TĨNH: banner "Khám phá Glassity" (dùng chung ảnh với Home) + ảnh Mickey thương hiệu, đúng bản mockup tĩnh gửi kèm
- Sửa lỗi ảnh certification bị viền đen: bỏ `background="@drawable/bg_product_placeholder"` (nền đen) trên ImageView + đổi sang `wrap_content` height thay vì height cố định ép fitCenter
- Tách footer (social icon + liên hệ + link About/Contact/Policy + copyright) dùng chung giữa Home và About ra `layout_footer_full.xml`, include bằng id `incFooter`, truy cập qua `binding.incFooter.footerXxx` (ViewBinding nested-binding pattern cho include có id)
- Build assembleDebug PASS, không còn cảnh báo

### Trang "Tất cả đánh giá" (11/07/2026)
- `AllReviewsActivity` mới: header "< Đánh giá sản phẩm ... X.X★" + RecyclerView đầy đủ review (không giới hạn số lượng), empty state khi chưa có review
- Bấm "Tất cả" ở `ProductDetailActivity` giờ mở trang này (trước đây Toast placeholder)
- Tách logic gán 1 dòng review vào `ReviewViewBinder` (utils) dùng chung giữa `ProductDetailActivity` (mini list) và `ReviewAdapter` (RecyclerView của AllReviewsActivity) — tránh trùng code
- Build assembleDebug PASS

### Footer dùng chung — tách reuse (11/07/2026)
- Tạo `app/src/main/res/layout/layout_footer_full.xml`: social icon (Tw/Ig/Yt) → liên hệ → link About/Contact/Policy → dòng bản quyền, gộp lại từ 2 bản copy-paste y hệt trước đó ở `fragment_home.xml` và `activity_about.xml`
- Include bằng `<include android:id="@+id/incFooter" layout="@layout/layout_footer_full" />` — bắt buộc có `android:id` vì layout gốc dùng `<merge>`, ViewBinding cần id đó mới lộ được các view con (`footerAbout`, `footerContact`, `footerPolicy`, `footerTw`, `footerIg`, `footerYt`) ra qua `binding.incFooter.<id>`
- Đã cập nhật `HomeFragment.setupClickListeners()` và `AboutActivity.setupFooter()` sang cú pháp `binding.incFooter.footerXxx`
- `BlogActivity` vẫn giữ bản footer rút gọn riêng (không social icon) — không gộp vì khác cấu trúc, không phải thiếu sót
- Đã ghi hướng dẫn chi tiết cho Quân trong `TODO_PHAN_CONG.md` (mục "Footer dùng chung") phòng khi thêm màn hình mới cần tái sử dụng
- Build assembleDebug PASS

### 3 bài Blog — thêm ảnh thật (11/07/2026)
- Nội dung chữ 3 bài (`buildBlog1/2/3`) đã khớp sẵn với mockup Figma, chỉ thiếu ảnh — upload 11 ảnh từ `D:\Bat tu o C qua\Downloads\blog` lên Cloudinary (`glassity/site/blog/...`)
- Hero 3 bài thay bằng ảnh thật: `hero_trend2026`, `hero_retro`, `hero_summer` (thay placeholder chung `guide_diagram.png`/`blog_login_signup.jpg` cũ)
- Thêm 3 helper dựng nội dung trong `BlogActivity`: `addImage()` (ảnh full-width chèn giữa bài), `addTrendRow()` (dãy 5 thumbnail vuông + caption — Blog 1 mục "Gọng kính hot trend 2026"), `addFaceRow()` (ảnh vuông trái + đoạn văn phải — Blog 1 mục 5 dáng mặt, dùng lại đúng link ảnh dáng mặt của HomeFragment)
- Chèn ảnh minh họa đúng vị trí: Blog 2 có 2 ảnh (nhóm 90s, street style celeb), Blog 3 có 1 ảnh (chợ kính giá rẻ)
- `HomeFragment` — 2 ảnh thẻ Blog ở trang chủ (`imgBlog1/2`) đổi sang đúng hero Blog 1/2 thay vì ảnh không liên quan
- Footer `BlogActivity` gộp vào `layout_footer_full.xml` dùng chung (trước đây có bản riêng, giờ đồng bộ với Home/About)
- Build assembleDebug PASS

### Blog: chữ đậm hơn + nút Home nhanh (11/07/2026)
- Thêm màu `color_text_body` (#5C544C, đậm hơn `color_text_secondary` #8A8079) — áp dụng cho toàn bộ đoạn văn trong Blog (`addParagraph`, `addFaceRow`), dễ đọc hơn
- Bàn với user về việc gắn nguyên thanh nav pill (hide-on-scroll) như Home vào BlogActivity — quyết định KHÔNG làm vì pill nav hiện chỉ tồn tại trong MainActivity, gắn thêm vào Blog sẽ lệch chuẩn so với mọi trang chi tiết khác (ProductDetail, Search, Collection, About...)
- Thay bằng: thêm icon "Trang chủ" ở góc phải header Blog (cạnh "Quay lại"), bấm vào về thẳng `MainActivity` tab Home — tái dùng cơ chế `MainActivity.EXTRA_OPEN_HOME` đã có sẵn (giống nút "Về trang chủ" sau khi thanh toán ở `PaymentResultActivity`)
- Build assembleDebug PASS

### Logo thương hiệu: header, app icon, submark (11/07/2026)
- 3 asset logo từ team: `logo_simplified` (chữ "Glassity" serif, không hình), `logo_primary` (hình kính + chữ), `logo_submark` (chỉ hình kính) — đã crop bỏ viền trắng + tách nền trong suốt bằng script Python/PIL, lưu vào `res/drawable/logo_simplified.png` và `logo_submark.png`
- **Header trang chủ** (`fragment_home.xml`): `tvLogo` (TextView "Glassity") đổi thành `imgLogo` (ImageView dùng `logo_simplified`), bấm vào cuộn lên đầu trang (đã ở Home)
- **Header Blog** (`activity_blog.xml`): bỏ chữ "Quay lại", chỉ còn mũi tên to bên trái; icon nhà bên phải đổi thành logo `logo_simplified` đặt giữa header (bấm về thẳng MainActivity tab Home qua `EXTRA_OPEN_HOME`, giống PaymentResultActivity)
- **App icon**: thay toàn bộ `mipmap-*dpi/ic_launcher(.round)` (mdpi→xxxhdpi) bằng icon vuông nền trắng + logo `logo_primary` (hình kính + chữ) căn giữa; adaptive icon foreground (`mipmap-xxxhdpi/ic_launcher_foreground.png`) + background trắng phẳng (`drawable/ic_launcher_background.xml`) — bỏ hẳn robot Android mặc định của Android Studio template
- **Submark cho Toast/thông báo — ĐÃ RÕ NGUYÊN NHÂN (không cần code thêm)**: user báo trên máy thật (Pixel 5) các Toast kiểu "tính năng chưa có sẵn" vẫn hiện con robot Android. Rà code xác nhận app hiện KHÔNG có hệ thống push/system notification nào (`NotificationCompat.Builder`, `FirebaseMessagingService` — không file nào có) và toàn bộ Toast dùng `Toast.makeText` thuần, không có custom layout/icon riêng.
  - **Nguyên nhân thật**: từ Android 11+ (Pixel 5 chắc chắn ≥ Android 11), hệ thống **tự động chèn icon launcher của app** vào cạnh mọi Toast — đây là hành vi OS-controlled, không phải app tự vẽ, và **không có API nào để app chỉ định icon khác cho riêng Toast** (khác với icon app). Vì icon app trước đó vẫn là robot mặc định của template Android Studio (mục "App icon" bên trên) nên mọi Toast đều hiện robot.
  - **Đã tự hết** sau khi đổi app icon (mục trên) — build lại + cài lại app (uninstall bản cũ trước khi cài, Android cache icon theo package) là Toast sẽ tự hiện logo mới, không cần thêm dòng code nào.
  - `drawable/logo_submark.png` (hình kính, không chữ) vẫn giữ lại — dùng được cho `NotificationCompat.Builder.setSmallIcon()` nếu sau này Quân làm push notification thật (icon submark hợp cho status bar vì đơn sắc, gọn), lúc đó mới thật sự cần đến asset này.
- Build assembleDebug PASS

### Fix: drawer "Về Glassity" + Home chỉ hiện 2/3 blog (11/07/2026)
- `MainActivity` — `menuVeGlassity` trong hamburger drawer trước chỉ đóng drawer, không điều hướng đi đâu → giờ mở `AboutActivity`
- `fragment_home.xml` — Home chỉ có `blogCard1`/`blogCard2` (2 thẻ), thiếu Blog 3 (Bí kíp chọn kính mát mùa hè) → thêm `blogCard3`/`imgBlog3`. Đồng thời sửa lại tiêu đề card 1 và 2 cho khớp đúng nội dung bài (trước đó tiêu đề card 2 ghi "Xu hướng kính mắt 2025" là text cũ để sót, không khớp bài Retro thật)
- `HomeFragment.java` — đổi tên hằng `URL_BLOG_GUIDE/TREND` → `URL_BLOG1/2/3`, thêm `URL_BLOG3` (hero_summer), wire Glide + click listener cho card 3 → `BlogActivity.start(ctx, 3)`

### Rà soát overlap status bar / camera cutout (11/07/2026) — KHÔNG có bug
- Kiểm tra `InsetsUtil.applySystemBarsPadding()` (đệm padding theo `systemBars() + displayCutout()`, tự né notch/punch-hole camera + status bar mọi máy) — xác nhận **mọi Activity đều gọi đúng** trong `onCreate` sau `setContentView`
- 5 Fragment (Home/Cart/Notification/Profile/Category) KHÔNG tự gọi riêng — **đúng chủ ý**, vì cả 4 đều sống trong `NavHostFragment` của `MainActivity`, và `MainActivity` đã áp padding 1 lần cho root `CoordinatorLayout` bao ngoài — áp lại ở từng fragment con sẽ bị đệm lố (padding cộng dồn 2 lần)
- Kết luận: không có màn hình nào bị header/thanh trạng thái đè lên trên bất kỳ máy nào (kể cả máy có notch/punch-hole camera) — không cần sửa gì thêm ở phần này
- Build assembleDebug PASS

### Fix: 3 card blog Home (contrast + lệch chiều cao) + drawer thiếu 2 blog (11/07/2026)
- **Nguyên nhân chữ mờ:** nền thẻ `bg_rounded_card` = `brand_beige` (#D1C7BD), dòng phụ đề lại dùng `color_hint` (#B3AAA2) — 2 màu gần như trùng nhau nên chữ "chìm" vào nền. Đổi phụ đề sang `color_text_secondary` (#8A8079, đậm hơn hẳn) cho cả 3 card
- **Nguyên nhân lệch chiều cao:** tiêu đề card chỉ set `maxLines="2"` (không set `minLines`) nên card có tiêu đề 1 dòng sẽ thấp hơn card có tiêu đề 2 dòng → thêm `android:minLines="2"` để mọi card luôn giữ đúng 2 dòng cho tiêu đề, chiều cao 3 card khớp nhau
- **Drawer thiếu 2 mục Blog:** trước chỉ có `menuBlogChonKinh` (Blog 1), thêm `menuBlogRetro` (Blog 2) + `menuBlogMuaHe` (Blog 3) trong `layout_nav_drawer.xml`, wire trong `MainActivity.java`
- Build assembleDebug PASS

### Fix: drawer bị đè status bar/camera + logo header (11/07/2026)
- **Nguyên nhân thật** (khác kết luận lần trước): `layout_nav_drawer.xml` là 1 view **sibling** của `CoordinatorLayout id="main"` trong `activity_main.xml` (nằm cùng cấp trong `DrawerLayout`, không phải con của `main`) — nên `InsetsUtil.applySystemBarsPadding(findViewById(R.id.main))` gọi ở `onCreate` KHÔNG áp dụng cho drawer, khiến header drawer (nút X, "Glassity", search/cart) vẽ đè lên status bar/camera cutout
- Fix: thêm `InsetsUtil.applySystemBarsPadding(drawerView)` riêng trong `setupDrawer()` — 1 dòng, dùng đúng utility đã chạy ổn ở mọi màn khác
- Tiện thể đổi luôn TextView "Glassity" trong header drawer thành `ImageView` dùng `logo_simplified` (đồng bộ Home header + Blog header đã đổi trước đó)
- Build assembleDebug PASS

### Nút "Thử kính ảo" — giữ chỗ vị trí (11/07/2026)
- Đặt đúng vị trí theo mockup Figma: nút tròn nền đen mờ, góc dưới-phải ảnh sản phẩm chính, icon camera trắng
- `activity_product_detail.xml`: bọc `vpImages` (ViewPager2) trong `FrameLayout`, thêm `ImageButton btnTryOn` (`bg_circle_translucent_dark` mới + `ic_camera` có sẵn)
- `ProductDetailActivity.java`: wire click → Toast "sắp ra mắt" (giữ chỗ, giống pattern camera search ở SearchActivity)
- **Chưa làm phần AR thật** (ML Kit Face Detection + CameraX + Canvas overlay gọng kính) — đợi user chuẩn bị 1 ảnh gọng kính PNG nền trong suốt, thẳng mặt, dùng chung demo cho mọi sản phẩm (đã thống nhất trong chat, xem plan trước đó)
- Build assembleDebug PASS

### Hero carousel khớp mockup (11/07/2026)
- **Vấn đề:** card giữa chiếm gần hết màn hình, 2 bên gần như không thấy — mockup cần card giữa ~55-60%, 2 bên ló ra rõ, cùng chiều cao (không scale nhỏ lại)
- Tăng `peekPx` từ 48dp → 72dp (padding 2 bên RecyclerView bên trong ViewPager2 — quyết định độ ló của card kề)
- Bỏ hẳn transformer scale-down 0.8 cho card 2 bên (trước đây làm card bên nhỏ hơn cả chiều cao lẫn chiều rộng — mockup không có hiệu ứng này, chỉ crop theo mép màn hình, mọi card cùng kích thước)
- Thêm bo góc cho từng card hero (`item_hero_banner.xml`: `background="@drawable/bg_rounded_card"` + `clipToOutline="true"`, bán kính 12dp giống card blog)
- Build assembleDebug PASS — tỉ lệ tính toán theo mockup, chưa test trực tiếp trên máy thật (emulator project đang lỗi thiếu system image, xem TODO chung) — báo lại nếu tỉ lệ chưa khớp 100% để tinh chỉnh thêm `peekPx`

### Hero carousel — nút "XEM NGAY" dạng button + hiệu ứng scale theo lượt (11/07/2026)
- `item_hero_banner.xml`: "XEM NGAY" trước là text + gạch chân, giờ đổi thành nút bấm thật (nền đen bo góc `bg_btn_black_filled`, cùng style nút "Mua ngay" ở promo banner) — bỏ gạch chân
- `HomeFragment.setupHeroCarousel()`: thêm lại hiệu ứng scale qua PageTransformer — card đang hiển thị (giữa) full-size, card 2 bên tự thu nhỏ còn 85% khi lệch khỏi tâm, phóng to dần khi tới lượt hiển thị (kết hợp với peek 72dp đã chỉnh trước đó)
- Build assembleDebug PASS

### Fix: nút "XEM NGAY" đúng mockup (11/07/2026)
- Lần trước lấy nhầm style nút "Mua ngay" (đen đặc, chữ trắng, góc dưới-trái) — không khớp mockup thật
- Sửa lại đúng: nền xám mờ giảm opacity (`bg_pill_gray_translucent.xml` mới, #B3D9D9D9), bo góc 6dp, chữ đen (#1A1A1A), canh giữa theo chiều ngang ở đáy card (`bottom|center_horizontal`) thay vì góc dưới-trái
- Build assembleDebug PASS

### Promo banner khớp mockup (11/07/2026)
- Chữ "MUA SẢN PHẨM THỨ HAI GIẢM NGAY 20%": trắng có đổ bóng → đen đậm (#1A1A1A), tăng cỡ 18sp → 24sp, bỏ shadow (nền không quá rối nên không cần)
- Nút "Mua ngay": bo góc 4dp (rectangle) → bo tròn hết cỡ dạng viên thuốc (`bg_btn_black_pill.xml` mới, radius 20dp), hạ xuống sát đáy card (marginBottom 28dp → 14dp)
- Build assembleDebug PASS

### Điền HERO_PRODUCT_IDS — 3 hero card trang chủ (11/07/2026)
- Slide 1: Cyber Fashion Sunglasses (`7dxSOPmpL0hiPeqw4FaE`)
- Slide 2: Unique Design Fashion Sunglasses (`D6FYz7iLHMH8OiHFL84g`)
- Slide 3: Modern Square Sunglasses Style (`C1wvHijLlWYEF9W99C5j`)
- Bấm "XEM NGAY" ở mỗi card giờ mở đúng `ProductDetailActivity` của SP tương ứng thay vì Toast "sắp ra mắt"
- Build assembleDebug PASS

### Replace toàn bộ text "Glassity" còn sót thành logo (11/07/2026)
- Rà toàn bộ `res/layout`, tìm được 5 chỗ còn dùng `android:text="Glassity"` (ngoài Home/Blog/Drawer đã đổi trước đó):
  - `activity_favorite.xml` (header, chữ đen) → đổi logo
  - `activity_product_detail.xml` (`tvHeaderTitle`, chữ đen) → đổi logo, đổi id thành `imgHeaderLogo` (không còn tham chiếu Java/XML nào tới `tvHeaderTitle` sau khi sửa luôn constraint tham chiếu ở `dividerHeader`)
  - `fragment_profile.xml` — 2 chỗ: header (`tvLogo`→`imgLogo`) + chữ "Glassity" trang trí phía trên barcode thẻ thành viên, cả 2 đều chữ đen → đổi logo
  - `activity_collection.xml` — chữ TRẮNG (`#FFFFFF`) → **giữ nguyên text**, đúng yêu cầu (logo đen sẽ không thấy trên nền tối/ảnh của Collection)
- Build assembleDebug PASS

### Banner promo "Mua sản phẩm thứ hai" → mở tất cả sản phẩm (11/07/2026)
- `layoutPromoBanner` trước không có click listener nào — thêm `ProductListActivity.startAll()`, kèm `clickable/focusable + selectableItemBackground` để có hiệu ứng ripple khi bấm
- Nút "Mua ngay" con bên trong không có listener riêng nên không chặn touch — bấm bất kỳ đâu trên banner (kể cả nút) đều mở đúng trang tất cả sản phẩm
- Build assembleDebug PASS

### ProductDetail: dời khối Màu sắc/Số lượng lên + thêm ảnh sơ đồ kích thước (11/07/2026)
- `activity_product_detail.xml`: khối "Màu sắc" + "Số lượng" dời từ dưới phần review lên ngay sau `tvVariant` (ngay sau card sản phẩm: giá/tên/kiểu dáng), trước tabs Đánh giá/Chi tiết/Dành cho bạn
- Thêm `ImageView imgSizeDiagram` ở đầu section "Kích thước sản phẩm" (trước bảng số liệu), load ảnh sơ đồ tên bộ phận kính (Tròng/Gọng/Càng kính) từ Cloudinary có sẵn (`guide_diagram.png`) qua Glide trong `ProductDetailActivity.onCreate()`
- Build assembleDebug PASS

### Merge nhánh Quân (minhquanver2) vào main (11/07/2026)
- 6 file conflict: `HomeFragment.java` (7), `fragment_home.xml` (4), `item_hero_banner.xml` (2), `themes.xml` (1), `ic_launcher_background.xml`, `ic_launcher_foreground.xml` (deleted by us)
- Nguyên nhân: nhánh Quân tách trước khi làm loạt việc tối 11/07 (hero carousel, promo banner, blog ảnh, About link, footer dùng chung) → hầu hết theirs chỉ là bản cũ của đúng những chỗ vừa sửa
- **Giữ Ours** cho: HomeFragment.java, fragment_home.xml, item_hero_banner.xml, themes.xml (theirs không có gì mới/đã lỗi thời)
- **Giữ Theirs** cho app icon (theo lựa chọn của user): `ic_launcher_background.xml` (nền cream #EFE9E1) + `ic_launcher_foreground.xml` (vector 2 vòng tròn kính viền đen `#322D29`, tự vẽ bằng path, KHÔNG dùng ảnh `logo_primary.png` nữa)
- Đồng bộ theo hướng đã chọn: sửa `mipmap-anydpi-v26/ic_launcher(.round).xml` trỏ về `@drawable/ic_launcher_foreground` (vector), xóa PNG foreground mồ côi, **render lại toàn bộ icon legacy** (`mipmap-mdpi→xxxhdpi/ic_launcher(.round).png`, minSdk 24 cần bản non-adaptive) bằng script Python/PIL vẽ đúng hình học vector của Quân (2 vòng tròn tâm (39,54)/(69,54) bán kính 14, cầu nối, nền cream) để nhất quán giữa icon adaptive (Android 8+) và legacy (Android 7-)
- File `ic_logo_glassity.xml` (vector logo dùng ở Welcome/Login/Signup của Quân) giữ nguyên, không đụng
- Build assembleDebug PASS, 2 commit: merge commit + cleanup PNG mồ côi. **Chưa push** — cần bạn xác nhận trước khi push lên origin/main

### App icon: đổi từ vector tự vẽ sang logo thật submark (11/07/2026)
- Sau khi merge, app icon đang dùng vector tự vẽ của Quân (2 vòng tròn path) — user gửi lại đúng ảnh `logo_submark.png` (asset thật từ designer, đã có sẵn trong `drawable/` từ trước) và yêu cầu dùng ảnh này thay vector, vẫn giữ nền cream `#EFE9E1`
- Xóa `drawable/ic_launcher_foreground.xml` (vector), render lại toàn bộ icon (`mipmap-mdpi→xxxhdpi/ic_launcher(.round).png` + `mipmap-xxxhdpi/ic_launcher_foreground.png` cho adaptive icon) bằng `logo_submark.png` đặt giữa nền cream qua script Python/PIL
- 2 file `mipmap-anydpi-v26/ic_launcher(.round).xml` trỏ foreground về `@mipmap/ic_launcher_foreground` (bitmap) thay vì `@drawable/ic_launcher_foreground` (vector, đã xóa)
- Build assembleDebug PASS, đã stage — chưa commit

### CollectionActivity: ảnh hero detail khác ảnh preview (11/07/2026)
- Trước đó trang chi tiết 1 BST (showSingle) dùng chung ảnh hero với preview ngoài Home + danh sách BST (URL_MONOCHROME/ESSENTIAL/SUNLIGHT) — giờ tách riêng
- Thêm `URL_HERO_MONOCHROME/ESSENTIAL/SUNLIGHT` dùng `hero_bg1/hero_bg2/hero_bg3` có sẵn trên Cloudinary (`glassity/site/`), theo đúng thứ tự Monochrome=1, Essential=2, Sunlight=3
- Build assembleDebug PASS

### CollectionActivity: tên BST dời xuống cạnh dưới hero (11/07/2026)
- `tvHeroTitle` trước ở cạnh trên (`layout_gravity="top"`, marginTop 52dp) che mất phần trên ảnh chính — dời xuống cạnh dưới (`layout_gravity="bottom"`, marginBottom 48dp), vẫn nằm trên sheet trắng "HÀNG MỚI VỀ" bo góc đè lên hero (-24dp), không bị chồng lấn
- Build assembleDebug PASS

### Fix tốc độ tải ảnh + test review có ảnh (11/07/2026)

**A. Tối ưu ảnh Cloudinary (nguyên nhân chậm khi mở app):**
- Rà toàn bộ 46 chỗ gọi Glide — xác nhận không có blocking call/Firestore đồng bộ nào, chậm là do **ảnh tải nguyên bản gốc** (800KB-2MB) không có tham số resize/nén, và Home cold-start bắn ~13+ request ảnh cùng lúc
- Tạo `utils/CloudinaryUtil.java`: helper chèn `f_auto,q_auto[,w_x]` vào URL Cloudinary trước khi đưa Glide load (đổi định dạng WebP/AVIF tự động + nén chất lượng tự động + resize đúng kích thước hiển thị)
- Bake `f_auto,q_auto` thẳng vào hằng số `CLOUD` dùng chung ở 4 file (`HomeFragment`, `BlogActivity`, `CollectionActivity`, `AboutActivity`) — tự động fix toàn bộ hero/tile/blog/about, không cần sửa từng dòng
- Áp `CloudinaryUtil.optimize(url, width)` cho toàn bộ chỗ load ảnh sản phẩm từ Firestore: `ProductAdapter`, `FeaturedProductAdapter`, `CollectionProductAdapter`, `SearchSuggestAdapter` (w=400 — card/grid), `CartAdapter`, `FavoriteActivity`, `CartFragment`, `CheckoutActivity`, `OrderDetailActivity`, `OrderHistoryActivity`, `ReviewActivity` (w=250 — thumbnail nhỏ), `ProductDetailActivity` (ảnh chính w=800, thumbnail row w=200, size-diagram w=600), `ReviewViewBinder` (ảnh review w=200)
- KHÔNG đụng: ảnh QR VietQR (`PaymentResultActivity`, không phải Cloudinary), ảnh camera/gallery cục bộ trong `ReviewActivity` (URI máy, không phải URL Cloudinary — sửa vào sẽ hỏng)
- Build assembleDebug PASS

**B. Test review có ảnh (data thật để kiểm tra UI):**
- Rà `ReviewViewBinder.java` + `item_review.xml` kỹ — logic đúng, không bug, nhưng **122 review seed cũ không review nào có ảnh** → chưa từng test bằng mắt
- Tạo 6 review có ảnh (script tạm `seed_reviews_with_images.js`, đã xóa sau khi chạy), gán vào tài khoản khách thật, dùng ảnh sẵn có của chính sản phẩm làm ảnh minh họa (không phải ảnh khách thật — chỉ để test UI hiển thị đúng):
  - **Cyber Fashion Sunglasses**: Lê Minh Cường (1 ảnh), Hoàng Quốc Em (2 ảnh)
  - **Unique Design Fashion Sunglasses**: Quynh Nhu (1 ảnh), Quan Nguyen Minh (3 ảnh)
  - **Modern Square Sunglasses Style**: Lanion (1 ảnh), Quan Nguyen Minh (3 ảnh)
- User cần tự mở app kiểm tra 3 sản phẩm trên (đúng 3 sản phẩm đang gắn ở hero banner trang chủ) để xác nhận ảnh review hiển thị đúng

### Xem ảnh review full-size + preload/blur-up ảnh (12/07/2026)

**A. Xem ảnh review full-size (pinch-zoom):**
- Thêm thư viện `com.github.chrisbanes:PhotoView:2.3.0` (qua JitPack, thêm repo trong `settings.gradle.kts`)
- Mới: `PhotoViewerActivity` + `PhotoViewerAdapter` + `activity_photo_viewer.xml`/`item_photo_viewer.xml` — màn hình toàn màn hình nền đen, ViewPager2 chứa PhotoView (pinch-zoom + pan), đếm số ảnh góc trên "1/3", nút đóng, vuốt ngang qua lại nếu review có nhiều ảnh
- `ReviewViewBinder.java`: mỗi thumbnail ảnh review giờ có `OnClickListener` mở `PhotoViewerActivity` với đúng danh sách ảnh + vị trí đã bấm

**B. Preload + blur-up placeholder (giảm cảm giác chờ):**
- `CloudinaryUtil.blurPlaceholder(url)`: tạo URL ảnh siêu nhẹ + mờ (`w_40,e_blur:1000,q_1,f_auto`) hiện ngay lập tức trong lúc chờ tải bản đẹp — kỹ thuật "blur-up" giống Instagram/Medium
- `HeroBannerAdapter`: dùng `.thumbnail(...)` load bản mờ trước, giữ `diskCacheStrategy(AUTOMATIC)`
- `HomeFragment`: thêm `preloadHeroImages()` gọi `Glide.preload()` cho 3 URL hero ngay đầu `onViewCreated()` — bắn request tải song song với Firestore/layout thay vì đợi carousel bind xong mới tải
- `ProductAdapter`, `FeaturedProductAdapter`: cùng thêm `.thumbnail()` blur-up cho ảnh sản phẩm (grid Home + ProductList)
- Build assembleDebug PASS

### Fix: PhotoViewerActivity thiếu nút đóng do đè status bar (12/07/2026)
- Cùng loại lỗi với drawer trước đó — quên gọi `InsetsUtil.applySystemBarsPadding()` khi tạo `PhotoViewerActivity`, khiến nút đóng (top|start) núp dưới status bar, không bấm được
- Thêm dòng gọi InsetsUtil ngay sau `setContentView()`, đúng pattern chuẩn dùng ở mọi Activity khác trong app
- Build assembleDebug PASS

### Rà soát toàn bộ logo header — gắn nút về Trang chủ còn thiếu (12/07/2026)
User phát hiện logo ở ProductDetail bấm không có gì — rà lại cả 6 chỗ dùng `logo_simplified` trong app, phát hiện **4/6 chưa gắn click** (trước đó chỉ đổi text→ảnh, quên wire listener):

| File | Trạng thái trước | Đã sửa |
|---|---|---|
| `fragment_home.xml` (Home) | ✅ Đã có (cuộn lên đầu) | — |
| `activity_blog.xml` (Blog, btnHome) | ✅ Đã có (về Home) | — |
| `activity_product_detail.xml` | ❌ Thiếu | Thêm `imgHeaderLogo` click → `MainActivity` + `EXTRA_OPEN_HOME` |
| `fragment_profile.xml` (header) | ❌ Thiếu | Thêm `imgLogo` click → `NavController.navigate(R.id.homeFragment)` (cùng Activity, dùng Nav thay vì mở Intent mới) |
| `activity_favorite.xml` | ❌ Thiếu, còn chưa có `id` | Thêm `id="imgLogo"` + click → `MainActivity` + `EXTRA_OPEN_HOME` |
| `layout_nav_drawer.xml` | ❌ Thiếu, còn chưa có `id` | Thêm `id="imgDrawerLogo"` + click trong `MainActivity.setupDrawer()`: đóng drawer + `NavController.navigate(R.id.homeFragment)` |

Tiện thể thêm `background="?attr/selectableItemBackgroundBorderless"` cho các logo còn thiếu hiệu ứng ripple khi bấm (đồng bộ toàn app).
Build assembleDebug PASS

### Trang Contact viết lại khớp mockup (12/07/2026)
- Layout cũ dùng CardView + chevron (kiểu list điều hướng) — không khớp mockup (flat list, không card, số điện thoại kiểu link xanh gạch chân)
- Viết lại toàn bộ `activity_contact.xml`: bỏ CardView/chevron, icon 3 dòng (hotline/email/địa chỉ) dùng đúng ảnh icon outline user gửi (`ic_locate.png`, `ic_email.png`, `ic_hotline.png` — nền trong suốt, resize 128x128, đổi tên `ic_contact_location/email/phone.png`, tint được bình thường vì nền alpha=0)
- Cập nhật số hotline khớp mockup: `1800 1162` (đặt hàng), `1800 1160` (góp ý/khiếu nại) — trước là 1900 1182/1188 (sai/cũ)
- Cập nhật địa chỉ khớp mockup: "669 QL1A, Khu phố 6, Phường Linh Xuân, Thủ Đức, TP. Hồ Chí Minh" — trước là địa chỉ cũ "09B DL1A, Khu phố 5..."
- Thêm dòng footer bản quyền cuối trang (mockup có, bản cũ không có)
- `ContactActivity.java`: cập nhật số điện thoại dial đúng theo mockup
- Build assembleDebug PASS

### Fix chữ nhạt ở 4 trang chính sách/FAQ (12/07/2026)
- `activity_policy.xml` (dùng chung cho FAQ, Bảo hành, Bảo mật, Giao hàng & Kiểm tra qua `PolicyActivity`): `tvPolicyContent` đang dùng `color_text_secondary` (#8A8079, nhạt) — cùng lỗi đã gặp ở Blog trước đó — đổi sang `color_text_body` (#5C544C, đậm hơn hẳn)
- Ornament dưới tiêu đề trước là ký tự "⸻" thô — đổi thành gạch-◇-gạch đồng bộ với style About/Footer đã dùng ở nơi khác trong app
- Build assembleDebug PASS

### Trang Contact — thêm frame Google Maps (12/07/2026)
- Thêm `WebView` nhúng bản đồ ngay dưới địa chỉ, dùng URL nhúng cổ điển của Google Maps (`maps.google.com/maps?q=...&output=embed`) — **không cần API key/billing**, chỉ cần quyền INTERNET (đã có sẵn)
- WebView bị chặn touch riêng (`setOnTouchListener` trả `true`) — cả khối chỉ đóng vai trò 1 nút bấm duy nhất, không lo user vô tình kéo/zoom bản đồ dở dang
- Bấm vào bất kỳ đâu trong frame → mở app Google Maps thật (Intent `ACTION_VIEW` tới `google.com/maps/search`, tự tìm đúng địa chỉ Glassity)
- Build assembleDebug PASS

### ⚠️ FIX KHẨN: WebView Google Maps gây bay ra Chrome + app không ổn định (12/07/2026)
- **Nguyên nhân**: WebView nhúng bản đồ thêm trước đó KHÔNG gắn `WebViewClient` — khi trang Google Maps embed tự điều hướng (redirect/trang consent), Android mặc định coi là điều hướng ra ngoài WebView và bắn sang Chrome (hành vi mặc định của WebView khi thiếu WebViewClient). Việc app "chớp tắt" ở chỗ khác nhiều khả năng là hệ quả WebView làm tiến trình không ổn định
- **Đã bỏ hẳn WebView** (rủi ro nhất, không sửa nửa vời) — thay bằng khối tĩnh: icon vị trí + "Xem trên Google Maps" + mũi tên, bấm vào mở thẳng app Google Maps qua Intent `ACTION_VIEW` — đúng yêu cầu gốc "bấm vào bay qua app gg map", chỉ bỏ phần preview nhúng trực tiếp (rủi ro không đáng)
- Build assembleDebug PASS — cần bạn test lại gấp xem hết flicker/crash chưa

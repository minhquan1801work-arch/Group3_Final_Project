# Spec: Tìm bằng Camera + Thử kính ảo (Visual Try-On)

> Viết ngày 12/07/2026 để tiếp tục ở session mới không mất context. Plan này đã được user duyệt trong chat — chưa code phần logic thật, chỉ mới đặt UI giữ chỗ (nút bấm), xem `HONGPHUC_PROGRESS.md` mục liên quan.

---

## 1. Tìm sản phẩm bằng ảnh (camera/gallery)

### Hiện trạng
- UI đã có sẵn: icon camera trong `SearchActivity` (`activity_search.xml`), bấm vào hiện Toast "sắp ra mắt" — chưa có logic thật.
- **Hạ tầng camera/gallery đã có sẵn, tái dùng luôn**: `ReviewActivity.java` đã implement đầy đủ pattern chụp ảnh + chọn từ thư viện:
  - `ActivityResultLauncher<Uri> cameraLauncher` — chụp ảnh qua `FileProvider` (xem `cameraImageUri = FileProvider.getUriForFile(...)`)
  - `ActivityResultLauncher<String> galleryLauncher` — chọn ảnh từ thư viện
  - `ActivityResultLauncher<String> cameraPermLauncher` — xin quyền `Manifest.permission.CAMERA`
  - Quyền đã khai báo sẵn trong `AndroidManifest.xml`: `CAMERA`, `READ_MEDIA_IMAGES`, `READ_EXTERNAL_STORAGE`
  - → Copy nguyên logic 3 launcher này sang `SearchActivity`, đổi hành động sau khi có ảnh (thay vì thêm vào `selectedImages` như Review, gọi hàm search mới).

### Cách match sản phẩm — ĐÃ QUYẾT ĐỊNH: so màu chủ đạo (Palette API)
- **Không làm** reverse-image search thật (Cloud Vision API / Cloudinary Visual Search / embedding riêng) — quá tay so với scope đồ án, cần backend + trả phí.
- **MVP đã chốt**: dùng `androidx.palette:palette` (thư viện free, on-device, cực nhẹ — **chưa có trong `build.gradle.kts`, cần thêm dependency**):
  ```kotlin
  implementation("androidx.palette:palette-ktx:1.0.0")
  ```
- Luồng xử lý:
  1. User chụp/chọn ảnh → có `Bitmap`
  2. `Palette.from(bitmap).generate()` → lấy màu nổi bật nhất (`getDominantColor()` hoặc `getVibrantColor()`, có fallback nếu null)
  3. Load toàn bộ `products` từ Firestore (đã có `ProductRepository`, xem cách `SearchActivity` load in-memory hiện tại để tái dùng)
  4. Với mỗi sản phẩm, so khoảng cách màu giữa màu chủ đạo ảnh và `variant.color` (hex có sẵn) — dùng công thức Euclidean đơn giản trên RGB hoặc chuyển sang Lab rồi dùng CIE76 (chính xác hơn với mắt người, nhưng Euclidean RGB cũng đủ dùng cho demo)
  5. Sort theo khoảng cách tăng dần → hiển thị kết quả giống `ProductListActivity`/grid hiện có

### Nâng cao (tùy thời gian còn lại — không bắt buộc)
- Thêm ML Kit Image Labeling (`com.google.mlkit:image-labeling:17.0.9`, free/on-device) để xác nhận ảnh có chứa "glasses/sunglasses/eyewear" trước khi so màu — tránh kết quả rác nếu user chụp nhầm vật khác. Nếu không detect được nhãn liên quan, có thể vẫn chạy so màu nhưng show cảnh báo "Không chắc đây là ảnh kính, kết quả có thể không chính xác".

### File cần đụng
- `SearchActivity.java` — thêm 3 launcher (copy từ ReviewActivity), thêm hàm `searchByImage(Bitmap)`
- `build.gradle.kts` — thêm dependency `androidx.palette`
- Có thể cần adapter/layout mới để hiển thị kết quả (hoặc tái dùng `ProductAdapter` + `activity_product_list.xml` pattern)

---

## 2. Thử kính ảo (Visual Try-On)

### Hiện trạng
- Nút bấm đã đặt đúng vị trí (`ProductDetailActivity`, góc dưới-phải ảnh sản phẩm chính): `btnTryOn` trong `activity_product_detail.xml`, hiện wire Toast "Thử kính ảo — sắp ra mắt!" trong `ProductDetailActivity.java`.
- **Đây KHÔNG phải xoay 360° sản phẩm** (không có ảnh turntable, không khả thi) — mà là **AR đeo thử qua camera trước**, giống Warby Parker/EyeBuyDirect.

### Kỹ thuật đã chốt
1. **ML Kit Face Detection** (`com.google.mlkit:face-detection:16.1.6`, free, on-device) — lấy landmark 2 mắt + mũi từ camera trước
2. **CameraX** (`androidx.camera:camera-camera2/lifecycle/view`) — hiện preview camera trước real-time
3. **Canvas/custom View overlay** — vẽ ảnh gọng kính (PNG nền trong suốt) đè lên mặt, scale + xoay 2D theo khoảng cách/góc giữa 2 mắt (landmark trả về từ ML Kit)
4. Không dùng SDK AR trả phí (DeepAR, Banuba...) — làm tay bằng ML Kit + Canvas là đủ, miễn phí hoàn toàn

### ⚠️ Điểm nghẽn đã bàn và chốt hướng giải quyết
- Ảnh sản phẩm hiện tại (chụp nghiêng 3/4 studio) **không dùng được** để overlay lên mặt — cần ảnh gọng kính nhìn THẲNG, PNG nền trong suốt.
- Chuẩn bị ảnh riêng cho từng sản phẩm/màu (190 ảnh) **quá tốn công** — **đã quyết định dùng chung 1 ảnh gọng kính demo cho TẤT CẢ sản phẩm** (không khớp màu/dáng từng SP, chỉ để chứng minh cơ chế AR chạy được — chấp nhận được cho mục đích demo/bảo vệ đồ án).
- Ảnh demo cần: PNG nền trong suốt, chụp/vẽ thẳng mặt (không nghiêng — thuật toán chỉ scale+xoay 2D theo mặt phẳng, không xử lý phối cảnh), cân đối 2 bên, ~800-1000px ngang.
- **User đang chuẩn bị ảnh này** — khi có, đặt vào `res/drawable/glasses_tryon_demo.png` (nền trong suốt).

### Luồng xử lý dự kiến
1. Bấm `btnTryOn` → mở `TryOnActivity` mới (hoặc CameraX preview full-screen)
2. Xin quyền `CAMERA` nếu chưa có
3. CameraX bind preview vào `PreviewView`, đồng thời chạy ML Kit Face Detection trên từng frame (dùng `ImageAnalysis` use case)
4. Khi có kết quả detect (landmark 2 mắt), tính: khoảng cách 2 mắt → scale ảnh gọng kính; góc nghiêng đầu (roll) → xoay ảnh; điểm giữa 2 mắt → vị trí đặt overlay
5. Vẽ overlay lên 1 custom View đè lên `PreviewView` (không sửa trực tiếp frame camera, chỉ overlay UI)
6. Nút chụp ảnh lại kết quả (tùy chọn, không bắt buộc cho MVP)

### File cần tạo mới
- `ui/catalog/TryOnActivity.java` + `activity_try_on.xml`
- `utils/FaceOverlayView.java` (custom View vẽ overlay theo landmark)
- `build.gradle.kts` — thêm CameraX + ML Kit Face Detection dependencies

### Wire vào chỗ đã có
```java
// ProductDetailActivity.java — thay Toast placeholder hiện tại:
binding.btnTryOn.setOnClickListener(v ->
        Toast.makeText(this, "Thử kính ảo — sắp ra mắt!", Toast.LENGTH_SHORT).show());
// → đổi thành:
binding.btnTryOn.setOnClickListener(v -> TryOnActivity.start(this));
```

---

## Thứ tự làm đề xuất
1. Camera search (nhanh hơn, không cần asset mới, chỉ cần thêm Palette dependency)
2. Try-on (cần ảnh demo gọng kính trước khi bắt tay code phần overlay — hỏi user đã chuẩn bị ảnh chưa trước khi bắt đầu)

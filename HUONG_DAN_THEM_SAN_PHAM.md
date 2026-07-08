# HƯỚNG DẪN CHUẨN BỊ SẢN PHẨM MỚI — GLASSITY

> Dành cho thành viên tìm/sưu tầm sản phẩm. Chuẩn bị đúng format bên dưới rồi gửi cho Hồng Phúc,
> script sẽ tự upload ảnh lên Cloudinary + tạo sản phẩm trên Firestore, app hiện ngay không cần sửa code.

---

## 1. Cấu trúc thư mục ảnh (BẮT BUỘC)

Mỗi sản phẩm = **1 thư mục**, tên thư mục = **tên sản phẩm tiếng Anh, IN HOA**:

```
📁 kính/
 ├── 📁 BOLD SQUARE FRAME SUNGLASSES/
 │    ├── 1.png
 │    ├── 2.png
 │    ├── 3.png
 │    └── ...
 ├── 📁 RETRO CAT EYE SUNGLASSES STYLE/
 │    ├── 54.png
 │    └── ...
 └── ...
```

Quy tắc:
- **Tên thư mục** → thành tên sản phẩm trong app (tự đổi thành "Bold Square Frame Sunglasses")
- **Tên file ảnh**: số thứ tự (`1.png`, `2.png`...) — đánh số liên tục, không trùng với sản phẩm khác
- **Mỗi ảnh = 1 variant màu** trong app (khách bấm chấm màu → nhảy sang ảnh đó). Vậy mỗi màu của gọng chỉ cần **1 ảnh đại diện đẹp nhất**
- Định dạng: PNG/JPG/WEBP, nền sáng đồng nhất (trắng/xám nhạt), kính nằm giữa khung
- Tối thiểu 2 ảnh (2 màu), lý tưởng 3–5 ảnh/sản phẩm

## 2. File thông tin sản phẩm

Kèm theo thư mục ảnh, tạo **1 file Excel hoặc ghi chú** với các cột sau (mỗi dòng = 1 sản phẩm):

| Cột | Bắt buộc | Giá trị hợp lệ | Ví dụ |
|-----|:---:|----------------|-------|
| Tên sản phẩm | ✅ | Trùng tên thư mục | BOLD SQUARE FRAME SUNGLASSES |
| Giá (VND) | ✅ | Số | 890000 |
| Loại | ✅ | `kinh_mat` / `kinh_can` / `phu_kien` | kinh_mat |
| Dáng mặt phù hợp | ✅ | 1–2 giá trị: `tron`, `trai_xoan` (=Oval), `trai_tim` (=Mắt Mèo), `kim_cuong`, `vuong` | tron, trai_xoan |
| Giới tính | ✅ | `nam` / `nu` / `unisex` | unisex |
| Bộ sưu tập | ⭕ | `Monochrome Collection` / `Essential Acetate` / `Sunlight Studio` (hoặc bỏ trống) | Essential Acetate |
| Mô tả | ⭕ | 1–2 câu tiếng Việt (bỏ trống thì Phúc tự viết theo ảnh) | Gọng vuông dày dặn... |
| Màu từng ảnh | ⭕ | Theo thứ tự ảnh: tên màu + mã hex nếu biết (bỏ trống thì Phúc tự nhìn ảnh điền) | 1=Hổ phách #7B3F1A, 2=Đen #1A1A1A |
| Tồn kho | ⭕ | Số/variant, mặc định 10 | 10 |

**Chú ý các giá trị cột "Dáng mặt"** — phải dùng đúng mã bên trên, vì app query đúng chuỗi này:
- Mặt tròn → `tron` • Mặt oval/trái xoan → `trai_xoan` • Mắt mèo/trái tim → `trai_tim` • Kim cương → `kim_cuong` • Vuông → `vuong`

**Chú ý cột "Bộ sưu tập"** — chỉ 3 giá trị trên, gõ đúng từng chữ (app query `where collection == "..."`):
- `Monochrome Collection` = tông đen/trắng, futuristic, avant-garde
- `Essential Acetate` = gọng nhựa/kim loại cơ bản hằng ngày
- `Sunlight Studio` = tông ấm, amber, cat-eye nữ tính

## 3. Gửi như thế nào?

1. Nén thư mục `kính/` (chứa các thư mục sản phẩm) thành ZIP
2. Kèm file Excel/ghi chú thông tin ở mục 2
3. Gửi Phúc qua Drive/Zalo — **KHÔNG push ảnh lên git**

## 4. Quy trình phía Phúc (để ai cũng hiểu flow)

```
Ảnh + Excel → upload_products.js  → Cloudinary (glassity/products/<TÊN SP>/<số>.png)
                                  → Firestore doc trong collection "products"
            → update_products.js  → điền price/faceShapes/gender/description/collection
            → cleanup_and_names.js → điền colorName ("Hổ phách", "Đen bóng"...) cho từng variant
```

Script nằm ở root repo. Cần 2 file secret (Phúc giữ, gửi riêng khi cần): `service-account.json` (Firebase Admin) + `cloudinary-config.json` (Cloudinary key). Hai file này đã gitignore — **tuyệt đối không commit**.

## 5. Cấu trúc document Firestore tạo ra (tham khảo cho dev)

```js
products/{autoId} = {
  name:        "Bold Square Frame Sunglasses",
  categoryId:  "kinh_mat",
  price:       890000,
  description: "Gọng vuông dày dặn...",
  faceShapes:  ["tron", "trai_xoan"],
  gender:      "unisex",
  collection:  "Essential Acetate",     // "" nếu không thuộc BST
  variants: [                            // mỗi ảnh = 1 variant
    {
      color:     "#7B3F1A",             // hex cho chấm màu
      colorName: "Hổ phách",            // tên hiển thị chỗ "Màu sắc:"
      stock:     10,
      images:    ["https://res.cloudinary.com/aa1g9udv/image/upload/.../1.png"]
    },
    ...
  ]
}
```

App đọc ảnh + màu + tồn kho **từ variants** (không dùng field `images`/`colors`/`stock` cũ nữa — chúng chỉ còn để tương thích sản phẩm seed cũ).

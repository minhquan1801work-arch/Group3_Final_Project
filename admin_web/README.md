# Glassity — Web Quản trị đơn hàng (`admin_web/index.html`)

Web admin nhẹ 1 file HTML (Firebase JS SDK qua CDN, không cần build/cài gì),
đáp ứng "Admin Pool" trong BPMN checkout: xem đơn mới → xác nhận/hủy →
cập nhật trạng thái → tự gửi thông báo trong app cho khách.

## Chức năng (đủ CRUD trên đơn hàng)

| | Chức năng |
|---|---|
| **C**reate | Tạo đơn thủ công (nhận đơn qua điện thoại/Zalo) — form tên/SĐT/địa chỉ/tổng tiền |
| **R**ead | Danh sách đơn realtime (tự cập nhật khi có đơn mới), lọc theo trạng thái, tìm theo mã đơn/SĐT/tên; bấm vào đơn xem chi tiết sản phẩm + giao hàng + thanh toán; thống kê nhanh (số đơn theo trạng thái + doanh thu đơn hoàn thành) |
| **U**pdate | Chuyển trạng thái theo đúng luồng `PENDING → PROCESSING → SHIPPED → DELIVERED` (hoặc hủy); xác nhận đã nhận tiền chuyển khoản; sửa địa chỉ giao hàng. Mỗi lần đổi trạng thái tự ghi 1 thông báo vào collection `notifications` → hiện trong tab Thông báo của app khách |
| **D**elete | Xóa đơn (kèm xóa subcollection `orderDetails`, có confirm) |

## Setup lần đầu (bắt buộc, ~3 phút)

1. **Deploy rules mới**: copy toàn bộ nội dung `firestore.rules` (ở root repo)
   dán vào Firebase Console → Firestore Database → Rules → Publish.
   (Rules mới thêm hàm `isAdmin()` — thiếu bước này web sẽ báo lỗi permission.)

2. **Tạo tài khoản admin**:
   - Firebase Console → Authentication → Users → **Add user** → đặt email + mật khẩu
     (VD: `admin@glassity.com`). Copy **User UID** vừa tạo.
   - Firestore Database → collection `admins` (tạo mới nếu chưa có) →
     **Add document** → Document ID = dán đúng UID vừa copy → thêm field
     `role` (string) = `admin` → Save.

3. **Mở web**: mở file `admin_web/index.html` trực tiếp bằng trình duyệt
   (double-click là được, không cần server) → đăng nhập bằng tài khoản ở bước 2.

## Lưu ý

- `apiKey` trong file là public identifier của Firebase (không phải secret) —
  an toàn khi commit; toàn bộ bảo mật nằm ở Firestore Rules + check `admins/{uid}`.
- Tài khoản thường (khách trong app) đăng nhập vào web sẽ bị chặn ngay với
  thông báo "không có quyền quản trị" — check cả phía client lẫn Rules.
- Đơn `GUEST` (khách vãng lai) không nhận được thông báo trong app (không có
  tài khoản) — web tự bỏ qua bước gửi noti cho các đơn này.

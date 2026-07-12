Glassity — Ứng dụng mua sắm mắt kính trên di động
Về thương hiệu
Glassity là tên ghép từ Glasses (mắt kính) và City (đô thị) — đại diện cho phong cách sống hiện đại, năng động của người trẻ thành phố. Logo thương hiệu là 2 chữ cái G và C lồng vào nhau, tạo thành hình dáng một cặp mắt kính.

Glassity hướng đến nhóm khách hàng 18–35 tuổi — học sinh, sinh viên và người đi làm trẻ — những người cần một chiếc kính vừa đảm bảo chất lượng, vừa hợp thời trang, và muốn quy trình mua sắm diễn ra nhanh gọn ngay trên điện thoại.

Ứng dụng làm được gì
Thử kính ảo (AR Try-On) — Camera trước nhận diện khuôn mặt theo thời gian thực, hiển thị gọng kính lên đúng vị trí, tự động co giãn và xoay theo góc mặt. Xử lý hoàn toàn trên thiết bị (CameraX + ML Kit Face Detection), không gửi dữ liệu khuôn mặt lên máy chủ.

Duyệt và tìm sản phẩm — Trang chủ, danh mục theo kiểu gọng/dáng mặt/giới tính, tìm kiếm không dấu kèm gợi ý tự động, gợi ý sản phẩm cá nhân hóa theo lịch sử dùng app.

Yêu thích và đánh giá — Lưu sản phẩm quan tâm; đánh giá sản phẩm là dữ liệu thật từ khách đã mua, kèm ảnh/video, điểm trung bình tính tự động.

Giỏ hàng và thanh toán — Thanh toán COD hoặc chuyển khoản qua VietQR, hệ thống tự động xác nhận giao dịch thành công mà không cần thao tác thủ công. Hỗ trợ áp mã giảm giá, mã miễn/giảm phí vận chuyển, và tích/dùng điểm thành viên.

Quản lý đơn hàng — Theo dõi trạng thái đơn hàng theo từng giai đoạn, đánh giá và mua lại ngay sau khi xác nhận đã nhận hàng.

Tài khoản linh hoạt — Dùng thử ngay với vai trò khách, không bắt buộc đăng ký; hỗ trợ đăng nhập nhanh qua Google.

Công nghệ sử dụng
Thành phần	Công nghệ
Nền tảng	Android (Java), minSdk 24 – targetSdk 36
Dữ liệu & tài khoản	Firebase (Firestore, Authentication, Cloud Messaging)
Lưu trữ ảnh/video	Cloudinary, Glide
Thử kính ảo	CameraX, ML Kit Face Detection
Thanh toán	VietQR + webhook Node.js xác nhận giao dịch tự động
Đăng nhập	Google Sign-In
Hướng dẫn cài đặt
Yêu cầu: Android Studio (bản mới), JDK 11+, tài khoản Firebase, tài khoản Cloudinary.

Clone project

git clone https://github.com/minhquan1801work-arch/Group3_Final_Project.git
Mở bằng Android Studio, đợi Gradle sync.

Cấu hình Firebase — Tạo app Android với package com.FinalProject.group3 trên Firebase Console, bật Authentication (Email/Password + Google), Firestore, Storage. Tải file google-services.json và đặt vào thư mục app/.

Cấu hình Cloudinary — Tạo tài khoản, vào Settings → Upload → Upload Presets, tạo preset tên glassity_reviews, chọn chế độ Unsigned. Thiếu bước này thì ảnh/video đánh giá sẽ không tải lên được.

Chạy ứng dụng — Chọn thiết bị (emulator có Google Play Services hoặc máy thật đã bật USB debugging), bấm Run.

Thử tính năng AR — Vào trang chi tiết một sản phẩm bất kỳ, bấm icon thử kính, cấp quyền Camera. Nên kiểm thử trên máy thật để camera nhận diện khuôn mặt ổn định hơn emulator.

(Tùy chọn) Muốn kiểm thử luồng thanh toán VietQR tự động xác nhận bằng giao dịch ngân hàng thật — chạy thêm node sepay_webhook.js ở thư mục gốc (cần service-account.json riêng và ngrok). Bỏ qua bước này không ảnh hưởng đến việc chạy ứng dụng — thanh toán COD vẫn hoạt động bình thường.

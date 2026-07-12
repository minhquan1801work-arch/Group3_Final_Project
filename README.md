<div align="center">
👓 GLASSITY
Glasses meets City.
Ứng dụng mua sắm mắt kính trên di động — nơi mỗi ánh nhìn đều kể một câu chuyện.

</div>
Glassity là ai?
Glassity = Glasses (mắt kính) + City (thành phố, hiện đại). Cái tên gói gọn đúng tinh thần tụi mình muốn mang tới: những chiếc kính dành cho người trẻ sống giữa nhịp sống đô thị năng động — mỗi cặp mắt kính không chỉ để nhìn, mà còn để được nhìn thấy, để thể hiện gu thẩm mỹ và cá tính riêng.

Logo tối giản của Glassity là 2 chữ cái G và C lồng vào nhau, nhìn tổng thể lại y hệt một cặp mắt kính — vừa là chữ, vừa là hình, để ai lướt qua cũng biết ngay "à, đây là tiệm kính".

Tụi mình phục vụ ai? Chủ yếu là các bạn 18–35 tuổi, học sinh/sinh viên và dân văn phòng trẻ ở thành phố lớn — những người vừa cần kính để nhìn rõ, vừa muốn mẫu mã hợp thời, giá hợp lý, và quan trọng nhất: mua sắm phải nhanh, gọn, không rườm rà, xong hết trên một cái điện thoại. Đó cũng chính là lý do Glassity chọn làm app di động thay vì chỉ dừng ở website — để "lướt là mua", mọi lúc mọi nơi.

App có gì hay ho?
Glassity không chỉ là một cái shop online thêm-vào-giỏ-thanh-toán thông thường — mình cố nhét vào khá nhiều thứ để trải nghiệm mượt và thú vị hơn:

🕶️ Thử kính ảo bằng AR — tính năng "chất" nhất
Không cần ra tiệm, không cần tưởng tượng — bật camera trước lên là gọng kính tự "dán" lên mặt bạn theo thời gian thực, tự co giãn theo khoảng cách 2 mắt và xoay theo góc nghiêng đầu. Đứng sau hậu trường là CameraX + ML Kit Face Detection, xử lý hoàn toàn ngay trên máy — không gửi ảnh mặt bạn lên server nào cả, không cần SDK AR trả phí đắt đỏ.

🛍️ Mua sắm mượt như "lướt TikTok"
Trang chủ với banner, danh mục, sản phẩm nổi bật; lọc theo kiểu gọng (nhựa/kim loại), theo dáng mặt (tròn/oval/mắt mèo/vuông), theo giới tính. Tìm kiếm gõ không dấu vẫn ra kết quả, gợi ý tự động khi gõ, và còn "nhớ" bạn thích gì để gợi ý đúng gu hơn qua từng lần dùng app.

❤️ Yêu thích, đánh giá thật
Ưng mẫu nào cứ thả tim để dành. Mỗi sản phẩm có đánh giá thật 100% từ khách đã mua — kèm ảnh, video hẳn hoi (không phải placeholder giả), điểm trung bình tự tính chứ không phải số ai đó gõ tay.

💳 Thanh toán không cần đoán
COD (nhận hàng trả tiền) hoặc chuyển khoản quét VietQR — quét xong chuyển tiền là app tự động báo "Thanh toán thành công" ngay, không cần bấm xác nhận thủ công, không cần ngồi chờ admin duyệt tay.

🎟️ Giảm giá & tích điểm
Áp mã giảm giá đơn hàng và mã miễn/giảm phí ship cùng lúc, dùng điểm thành viên tích lũy để trừ bớt tiền — mua càng nhiều, lời càng nhiều.

📦 Theo dõi đơn hàng rõ ràng
Từ lúc đặt tới lúc nhận đều có trạng thái rõ ràng theo từng tab, bấm "Đã nhận được hàng" phát là mở khóa được đánh giá + mua lại ngay trong 1 chạm.

👤 Dùng thử không cần đăng nhập
Ngại tạo tài khoản? Cứ chọn "Tiếp tục với vai trò Khách" mà lượn app thoải mái, tới lúc thanh toán mới cần thông tin. Đăng nhập cũng có thể "đi tắt" qua Google, không cần nhớ thêm mật khẩu mới.

Bộ khung công nghệ đứng sau hậu trường
Mảng	Dùng gì
Nền tảng	Android (Java), minSdk 24 (Android 7.0+) → targetSdk 36
Dữ liệu & tài khoản	Firebase (Firestore, Authentication, Cloud Messaging)
Ảnh & video	Cloudinary (ảnh sản phẩm, ảnh/video đánh giá) + Glide (tải/hiển thị ảnh mượt)
AR thử kính	CameraX + ML Kit Face Detection (xử lý on-device, không cần cloud)
Thanh toán tự động	VietQR + webhook Node.js riêng lắng nghe giao dịch ngân hàng thật
Đăng nhập nhanh	Google Sign-In
Muốn tự chạy thử app? Làm theo đây
Cần chuẩn bị trước
Android Studio (bản mới), JDK 11+
Tài khoản Firebase (Firestore + Auth + Storage đã bật)
Tài khoản Cloudinary (để ảnh sản phẩm/đánh giá lên được)
Các bước
Clone về máy

git clone https://github.com/minhquan1801work-arch/Group3_Final_Project.git
Mở bằng Android Studio, đợi Gradle sync xong.

Nối Firebase — vào Firebase Console, tạo app Android với package com.FinalProject.group3, tải file google-services.json bỏ vào thư mục app/. Bật sẵn Authentication (Email/Password + Google), Firestore, Storage.

Nối Cloudinary — tạo tài khoản, vào Settings → Upload → Upload Presets, tạo preset tên glassity_reviews, chọn Unsigned. Thiếu bước này thì ảnh/video đánh giá sẽ không tải lên được.

Bấm Run ▶ — chọn emulator (nên chọn máy có Google Play Services để đăng nhập Google chạy được) hoặc điện thoại thật đã bật USB debugging.

Thử ngay tính năng AR — vào trang chi tiết 1 chiếc kính bất kỳ, bấm icon thử kính ở góc ảnh, cho phép quyền Camera. Nên test trên máy thật để camera nhận diện khuôn mặt mượt hơn emulator.

(Không bắt buộc) Muốn test luồng thanh toán VietQR tự động xác nhận thật bằng giao dịch ngân hàng — chạy thêm node sepay_webhook.js ở root project (cần service-account.json riêng + ngrok). Bỏ qua bước này thì thanh toán COD vẫn chạy bình thường không ảnh hưởng gì.

<div align="center">
Một chiếc kính không chỉ để nhìn — mà còn để thể hiện chính bạn. 👓✨

</div>

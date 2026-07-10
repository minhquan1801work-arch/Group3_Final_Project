package com.FinalProject.group3.ui.account;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;

import androidx.appcompat.app.AppCompatActivity;

import com.FinalProject.group3.databinding.ActivityPolicyBinding;

/**
 * DL_FAQ / DL_Warranty / DL_Policy (Figma) — 1 Activity dùng chung cho 4 trang:
 * Câu hỏi thường gặp, Chính sách bảo hành, Chính sách bảo mật,
 * Chính sách giao hàng và kiểm tra. Nội dung tĩnh, chọn theo EXTRA_TYPE.
 */
public class PolicyActivity extends AppCompatActivity {

    public static final String TYPE_FAQ = "faq";
    public static final String TYPE_WARRANTY = "warranty";
    public static final String TYPE_PRIVACY = "privacy";
    public static final String TYPE_SHIPPING = "shipping";
    public static final String TYPE_TERMS = "terms";
    public static final String TYPE_ABOUT = "about";

    private static final String EXTRA_TYPE = "policy_type";

    public static Intent intent(Context context, String type) {
        return new Intent(context, PolicyActivity.class).putExtra(EXTRA_TYPE, type);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityPolicyBinding binding = ActivityPolicyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        com.FinalProject.group3.utils.InsetsUtil.applySystemBarsPadding(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        String type = getIntent().getStringExtra(EXTRA_TYPE);
        if (type == null) type = TYPE_FAQ;

        String title, html;
        switch (type) {
            case TYPE_WARRANTY: title = "CHÍNH SÁCH BẢO HÀNH"; html = WARRANTY_HTML; break;
            case TYPE_PRIVACY:  title = "CHÍNH SÁCH BẢO MẬT";  html = PRIVACY_HTML;  break;
            case TYPE_SHIPPING: title = "CHÍNH SÁCH\nGIAO HÀNG VÀ KIỂM TRA"; html = SHIPPING_HTML; break;
            case TYPE_TERMS:    title = "ĐIỀU KHOẢN SỬ DỤNG";  html = TERMS_HTML;    break;
            case TYPE_ABOUT:    title = "VỀ GLASSITY";         html = ABOUT_HTML;    break;
            default:            title = "CÂU HỎI THƯỜNG GẶP";  html = FAQ_HTML;      break;
        }
        binding.tvPolicyTitle.setText(title);
        binding.tvPolicyContent.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT));
    }

    // ── Nội dung tĩnh (theo Figma DL_FAQ) ─────────────────────────────────────
    private static final String FAQ_HTML =
        "<b>1. Làm thế nào để đặt hàng tại Glassity?</b><br>"
        + "Chọn sản phẩm yêu thích, thêm vào giỏ hàng và hoàn tất thanh toán chỉ trong vài bước đơn giản.<br>"
        + "<b>2. Glassity có bán tròng kính cận không?</b><br>"
        + "Hiện tại, Glassity chỉ cung cấp gọng kính thời trang và không bán tròng kính cận.<br>"
        + "<b>3. Tôi có thể lắp tròng kính riêng vào gọng kính của Glassity không?</b><br>"
        + "Có. Quý khách có thể mang gọng kính đến cửa hàng kính mắt để lắp loại tròng phù hợp.<br>"
        + "<b>4. Làm thế nào để chọn kích thước gọng kính phù hợp?</b><br>"
        + "Thông tin kích thước được hiển thị trên từng trang sản phẩm để quý khách dễ dàng tham khảo và lựa chọn.<br>"
        + "<b>5. Gọng kính của Glassity được làm từ chất liệu gì?</b><br>"
        + "Sản phẩm được làm từ các chất liệu nhẹ, bền và mang lại cảm giác thoải mái khi sử dụng.<br>"
        + "<b>6. Thời gian giao hàng là bao lâu?</b><br>"
        + "Đơn hàng thường được xử lý trong 1–3 ngày làm việc và giao đến trong khoảng 5–10 ngày làm việc.<br>"
        + "<b>7. Tôi có thể theo dõi đơn hàng không?</b><br>"
        + "Có. Mã vận đơn sẽ được gửi sau khi đơn hàng được bàn giao cho đơn vị vận chuyển.<br>"
        + "<b>8. Tôi nên làm gì nếu nhận được sản phẩm bị lỗi hoặc sai mẫu?</b><br>"
        + "Vui lòng liên hệ với Glassity kèm hình ảnh sản phẩm để được hỗ trợ nhanh nhất.<br>"
        + "<b>9. Làm thế nào để liên hệ với Glassity?</b><br>"
        + "Quý khách có thể liên hệ với chúng tôi qua email hoặc tính năng chat trực tuyến trên ứng dụng.<br>"
        + "<b>10. Sản phẩm có đi kèm phụ kiện không?</b><br>"
        + "Có. Mỗi gọng kính đều được tặng kèm hộp đựng và khăn lau kính.";

    // ── Nội dung tĩnh (theo Figma DL_Warranty) ────────────────────────────────
    private static final String WARRANTY_HTML =
        "Glassity áp dụng bảo hành sản phẩm trong những trường hợp sau:<br>"
        + "• Hỗ trợ điều chỉnh thị lực miễn phí trong vòng 30 ngày nếu tròng kính mới gây cảm giác khó chịu như chóng mặt, đau đầu hoặc nhìn mờ.<br>"
        + "• Hỗ trợ giảm 50% chi phí thay gọng kính (tối đa 500.000 VNĐ) nếu kính của bạn bị gãy trong vòng 120 ngày kể từ ngày mua.<br>"
        + "• Hỗ trợ đổi mới 100% nếu gọng kính bị nứt hoặc gãy do lỗi sản phẩm trong vòng 07 ngày kể từ ngày mua.<br>"
        + "• Gọng kính thuộc các thương hiệu đối tác của Glassity được bảo hành 01 năm đối với các lỗi từ nhà sản xuất.<br>"
        + "• Miễn phí trọn đời các dịch vụ:<br>"
        + "&nbsp;&nbsp;&nbsp;• Vệ sinh kính<br>"
        + "&nbsp;&nbsp;&nbsp;• Thay đệm mũi<br>"
        + "&nbsp;&nbsp;&nbsp;• Siết, chỉnh ốc kính<br>"
        + "• Miễn phí khám mắt và kiểm tra thị lực.<br>"
        + "<b>Cảm ơn Quý khách đã tin tưởng và lựa chọn Glassity!</b>";

    // ── Nội dung tĩnh (theo Figma DL_Policy) ──────────────────────────────────
    private static final String PRIVACY_HTML =
        "<b>1. Mục đích thu thập thông tin cá nhân</b><br>"
        + "Việc thu thập thông tin cá nhân của khách hàng nhằm các mục đích sau:<br>"
        + "• Hỗ trợ khách hàng trong quá trình đặt hàng, thanh toán và giao nhận sản phẩm.<br>"
        + "• Cung cấp thông tin về sản phẩm, dịch vụ và các hỗ trợ theo yêu cầu của khách hàng.<br>"
        + "• Gửi thông báo về các chương trình khuyến mãi và sản phẩm mới.<br>"
        + "• Giải quyết các vấn đề phát sinh trong quá trình mua sắm.<br>"
        + "<b>2. Phạm vi thu thập thông tin</b><br>"
        + "Khi đặt hàng, Glassity có thể thu thập các thông tin cá nhân sau: Họ và tên; Địa chỉ email; Số điện thoại; Địa chỉ nhận hàng.<br>"
        + "<b>3. Thời gian lưu trữ thông tin</b><br>"
        + "Thông tin cá nhân của khách hàng sẽ được lưu trữ cho đến khi khách hàng yêu cầu xóa hoặc tự thực hiện việc xóa thông tin trên tài khoản của mình. "
        + "Trong các trường hợp khác, thông tin cá nhân sẽ được bảo mật và lưu trữ an toàn trên hệ thống máy chủ của Glassity.<br>"
        + "<b>4. Các cá nhân, tổ chức có thể tiếp cận thông tin</b><br>"
        + "Thông tin cá nhân của khách hàng có thể được chia sẻ với các bên sau trong phạm vi cần thiết:<br>"
        + "• Đơn vị vận chuyển: Nhận các thông tin cần thiết như họ tên, địa chỉ và số điện thoại để thực hiện giao hàng.<br>"
        + "• Nhân sự của công ty: Một số bộ phận được cấp quyền truy cập thông tin nhằm hỗ trợ khách hàng trong quá trình sử dụng sản phẩm và dịch vụ.<br>"
        + "• Đối tác hoặc đơn vị cung cấp dịch vụ: Tham gia thực hiện các chương trình hợp tác phục vụ các mục đích đã nêu và phải tuân thủ các yêu cầu bảo mật thông tin.<br>"
        + "• Cơ quan nhà nước có thẩm quyền: Trong trường hợp pháp luật yêu cầu hoặc cần thiết để thực hiện các thủ tục pháp lý.<br>"
        + "• Trường hợp chuyển giao doanh nghiệp: Khi có hoạt động sáp nhập hoặc chuyển nhượng một phần hoặc toàn bộ doanh nghiệp, đơn vị tiếp nhận có thể được quyền truy cập vào các thông tin đang được lưu trữ.<br>"
        + "<b>5. Thông tin của đơn vị thu thập và quản lý thông tin</b><br>"
        + "Tên doanh nghiệp: Glassity Eyewear<br>"
        + "Giấy chứng nhận đăng ký kinh doanh: Số 0316737985, cấp ngày 30/12/2025.<br>"
        + "Địa chỉ trụ sở: 669 Quốc lộ 1A, Khu phố 6, Thành phố Thủ Đức, Thành phố Hồ Chí Minh, Việt Nam.<br>"
        + "<b>6. Phương thức tiếp cận và chỉnh sửa thông tin cá nhân</b><br>"
        + "Khách hàng có thể yêu cầu truy cập, cập nhật hoặc chỉnh sửa thông tin cá nhân đã cung cấp bằng cách:<br>"
        + "• Gọi đến hotline: 1900 8386<br>"
        + "• Gửi email đến: glassity@gmail.com.vn<br>"
        + "<b>7. Cơ chế tiếp nhận và giải quyết khiếu nại</b><br>"
        + "Glassity cam kết bảo vệ tuyệt đối thông tin cá nhân của khách hàng. Mọi thông tin được cung cấp sẽ được bảo mật và không được chia sẻ, mua bán hoặc cho thuê với bất kỳ bên thứ ba nào ngoài các trường hợp được nêu trong chính sách này.<br>"
        + "Nếu có bất kỳ thắc mắc, góp ý hoặc khiếu nại nào liên quan đến Chính sách bảo mật, hoặc nhận thấy thông tin cá nhân của mình bị sử dụng không đúng mục đích đã cam kết, quý khách vui lòng liên hệ với Glassity qua hotline 1900 8386 để được hỗ trợ và giải quyết kịp thời.";

    // ── Nội dung tĩnh — Điều khoản sử dụng (gắn với Đăng ký + Thanh toán) ─────
    private static final String TERMS_HTML =
        "<b>1. Chấp nhận điều khoản</b><br>"
        + "Bằng việc tạo tài khoản hoặc đặt hàng trên ứng dụng Glassity, quý khách xác nhận đã đọc, hiểu và đồng ý tuân thủ toàn bộ nội dung trong Điều khoản sử dụng này.<br>"
        + "<b>2. Tài khoản người dùng</b><br>"
        + "• Quý khách chịu trách nhiệm bảo mật thông tin đăng nhập và mọi hoạt động phát sinh từ tài khoản của mình.<br>"
        + "• Thông tin đăng ký (họ tên, email, số điện thoại) phải chính xác và được cập nhật khi có thay đổi.<br>"
        + "• Glassity có quyền tạm khóa hoặc chấm dứt tài khoản vi phạm điều khoản sử dụng hoặc có dấu hiệu gian lận.<br>"
        + "<b>3. Đặt hàng và giá cả</b><br>"
        + "• Giá sản phẩm hiển thị trên ứng dụng đã bao gồm thuế (nếu có), chưa bao gồm phí vận chuyển.<br>"
        + "• Đơn hàng chỉ được xác nhận sau khi Glassity kiểm tra thành công thông tin đặt hàng và tồn kho.<br>"
        + "• Glassity có quyền từ chối hoặc hủy đơn hàng trong trường hợp phát hiện sai sót về giá, tồn kho hoặc thông tin gian lận.<br>"
        + "<b>4. Thanh toán</b><br>"
        + "• Quý khách có thể thanh toán khi nhận hàng (COD) hoặc chuyển khoản ngân hàng theo thông tin được cung cấp khi đặt hàng.<br>"
        + "• Đơn hàng chuyển khoản chỉ được xử lý sau khi Glassity xác nhận đã nhận đủ số tiền thanh toán.<br>"
        + "<b>5. Đổi trả và bảo hành</b><br>"
        + "Việc đổi trả, bảo hành sản phẩm được áp dụng theo Chính sách bảo hành và Chính sách giao hàng &amp; kiểm tra riêng của Glassity, xem chi tiết tại mục tương ứng trong ứng dụng.<br>"
        + "<b>6. Quyền sở hữu trí tuệ</b><br>"
        + "Toàn bộ nội dung, hình ảnh, logo và thương hiệu Glassity thuộc quyền sở hữu của Glassity Eyewear. Nghiêm cấm sao chép, sử dụng cho mục đích thương mại khi chưa được cho phép.<br>"
        + "<b>7. Giới hạn trách nhiệm</b><br>"
        + "Glassity không chịu trách nhiệm cho các thiệt hại phát sinh do lỗi kết nối mạng, gián đoạn dịch vụ ngoài tầm kiểm soát, hoặc do khách hàng cung cấp thông tin không chính xác.<br>"
        + "<b>8. Thay đổi điều khoản</b><br>"
        + "Glassity có quyền cập nhật, chỉnh sửa Điều khoản sử dụng bất kỳ lúc nào. Phiên bản mới nhất sẽ được đăng tải trên ứng dụng và có hiệu lực ngay khi công bố.<br>"
        + "<b>9. Liên hệ</b><br>"
        + "Mọi thắc mắc liên quan đến Điều khoản sử dụng vui lòng liên hệ hotline 1900 8386 hoặc email glassity@gmail.com.vn.";

    // ── Nội dung tĩnh — Về Glassity (footer "About") ──────────────────────────
    private static final String ABOUT_HTML =
        "<b>Glassity Eyewear</b><br>"
        + "Glassity là thương hiệu kính mắt thời trang, ra đời với mong muốn mang đến những mẫu gọng kính chất lượng, "
        + "thiết kế hiện đại và giá cả hợp lý cho mọi khách hàng Việt Nam.<br><br>"
        + "<b>Sứ mệnh</b><br>"
        + "Chúng tôi tin rằng một chiếc kính không chỉ giúp bạn nhìn rõ hơn mà còn là điểm nhấn thể hiện phong cách riêng. "
        + "Glassity không ngừng tìm kiếm những mẫu gọng kính mới, phù hợp với nhiều dáng khuôn mặt và cá tính khác nhau.<br><br>"
        + "<b>Cam kết</b><br>"
        + "• Sản phẩm chính hãng, chất liệu bền, nhẹ và an toàn cho người dùng.<br>"
        + "• Hỗ trợ đổi trả, bảo hành rõ ràng, minh bạch.<br>"
        + "• Dịch vụ chăm sóc khách hàng tận tâm, phản hồi nhanh chóng.<br><br>"
        + "<b>Thông tin liên hệ</b><br>"
        + "Địa chỉ: 669 Quốc lộ 1A, Khu phố 6, Thành phố Thủ Đức, Thành phố Hồ Chí Minh, Việt Nam.<br>"
        + "Hotline: 1900 8386 &nbsp;|&nbsp; Email: glassity@gmail.com.vn";

    // ── Nội dung tĩnh (theo Figma DL_Warranty — Giao hàng & Kiểm tra) ─────────
    private static final String SHIPPING_HTML =
        "<b>Chính sách vận chuyển</b><br>"
        + "<b>Phạm vi giao hàng – Thời gian giao hàng</b><br>"
        + "• Trong TP. Hồ Chí Minh (khu vực nội thành và ngoại thành): Khách hàng sẽ nhận được đơn hàng trong vòng 1–2 ngày.<br>"
        + "• Các tỉnh/thành phố khác trên toàn quốc: Đơn hàng của Glassity sẽ được vận chuyển thông qua các đối tác giao hàng, với thời gian giao hàng dự kiến từ 2–4 ngày làm việc.<br>"
        + "<b>Trách nhiệm của người bán và đơn vị vận chuyển</b><br>"
        + "• Đơn vị vận chuyển có trách nhiệm cung cấp bằng chứng giao hàng (bao gồm hình ảnh và chữ ký của người nhận) cho cả người mua và người bán khi có yêu cầu.<br>"
        + "• Lưu ý: Nếu bạn chưa nhận được đơn hàng sau 7 ngày, vui lòng liên hệ hotline Glassity: 1800 1162 để được hỗ trợ kịp thời.<br>"
        + "<b>Phí vận chuyển</b><br>"
        + "• Phí vận chuyển được tính dựa trên địa chỉ giao hàng và trọng lượng sản phẩm. Khách hàng sẽ nhận được thông tin chi tiết về phí vận chuyển trong quá trình thanh toán.<br><br>"
        + "<b>Chính sách kiểm tra sản phẩm</b><br>"
        + "<b>Tình trạng sản phẩm</b><br>"
        + "• Tất cả sản phẩm đều được kiểm tra cẩn thận trước khi giao hàng. Chúng tôi cam kết cung cấp các sản phẩm chất lượng cao, đúng với mô tả trên website tại thời điểm giao hàng.<br>"
        + "<b>Quy trình kiểm tra sản phẩm khi nhận hàng</b><br>"
        + "• Khi nhận được đơn hàng, vui lòng kiểm tra kỹ sản phẩm trước khi ký xác nhận nhận hàng.<br>"
        + "• Nếu có bất kỳ vấn đề nào về chất lượng sản phẩm, vui lòng liên hệ ngay qua hotline: 1800 1162 để chúng tôi hỗ trợ đổi hoặc trả hàng theo chính sách.";
}

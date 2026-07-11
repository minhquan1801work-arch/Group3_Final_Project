package com.FinalProject.group3.ui.catalog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.FinalProject.group3.R;
import com.FinalProject.group3.adapter.FeaturedProductAdapter;
import com.FinalProject.group3.databinding.ActivityBlogBinding;
import com.FinalProject.group3.model.Product;
import com.FinalProject.group3.repository.ProductRepository;
import com.FinalProject.group3.ui.account.ContactActivity;
import com.FinalProject.group3.ui.account.PolicyActivity;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * LA.Blog1/2/3 (Figma) — trang bài viết blog dùng chung, chọn bài theo EXTRA_BLOG_ID.
 *
 * Cấu trúc mỗi bài: hero banner → nội dung (tiêu đề section serif, đoạn văn,
 * ảnh minh họa) → khối gợi ý sản phẩm (RecyclerView ngang, data Firestore)
 * → 2 bài viết liên quan (link chéo 3 bài) → footer liên hệ.
 */
public class BlogActivity extends AppCompatActivity {

    private static final String EXTRA_BLOG_ID = "blog_id";
    private static final String CLOUD = "https://res.cloudinary.com/aa1g9udv/image/upload/f_auto,q_auto/";

    // Ảnh hero + card của 3 bài
    private static final String[] HERO = {
            CLOUD + "v1783753558/glassity/site/blog/hero_trend2026.png", // Blog 1
            CLOUD + "v1783753564/glassity/site/blog/hero_retro.png",     // Blog 2
            CLOUD + "v1783753566/glassity/site/blog/hero_summer.png"     // Blog 3
    };
    private static final String[] TITLES = {
            "Gọng Kính Hot Trend 2026",
            "Sự trở lại của gọng kính Retro",
            "Bí kíp chọn kính mát an toàn"
    };

    // Ảnh minh họa trong bài (đợt bổ sung 11/07)
    private static final String URL_RETRO_90S_GROUP =
            CLOUD + "v1783753569/glassity/site/blog/retro_90s_group.png";
    private static final String URL_RETRO_STREETSTYLE =
            CLOUD + "v1783753572/glassity/site/blog/retro_streetstyle.png";
    private static final String URL_CHEAP_SUNGLASSES_MARKET =
            CLOUD + "v1783753573/glassity/site/blog/cheap_sunglasses_market.png";

    // 5 thumbnail "Gọng kính hot trend 2026" (Blog 1)
    private static final String[][] TREND_ITEMS = {
            {CLOUD + "v1783753575/glassity/site/blog/trend_thick.png", "Gọng dày\ncá tính"},
            {CLOUD + "v1783753576/glassity/site/blog/trend_metal.png", "Gọng kim loại\ntối giản"},
            {CLOUD + "v1783753577/glassity/site/blog/trend_clear.png", "Gọng trong\nnhẹ nhàng"},
            {CLOUD + "v1783753578/glassity/site/blog/trend_cateye.png", "Mắt mèo\nnhẹ nhàng"},
            {CLOUD + "v1783753579/glassity/site/blog/trend_aviator.png", "Phi công\nthời thượng"},
    };

    // 5 ảnh dáng mặt (Blog 1) — dùng chung link với HomeFragment
    private static final String URL_SHAPE_TRON       = CLOUD + "v1783354487/glassity/site/shape_tron.png";
    private static final String URL_SHAPE_TRAI_XOAN  = CLOUD + "v1783354492/glassity/site/shape_trai_xoan.png";
    private static final String URL_SHAPE_TRAI_TIM   = CLOUD + "v1783354498/glassity/site/shape_trai_tim.png";
    private static final String URL_SHAPE_KIM_CUONG  = CLOUD + "v1783354502/glassity/site/shape_kim_cuong.png";
    private static final String URL_SHAPE_VUONG      = CLOUD + "v1783354507/glassity/site/shape_vuong.png";

    private ActivityBlogBinding binding;
    private final ProductRepository productRepo = new ProductRepository();
    private int blogId = 1;

    public static void start(Context context, int blogId) {
        context.startActivity(new Intent(context, BlogActivity.class)
                .putExtra(EXTRA_BLOG_ID, blogId));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBlogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        com.FinalProject.group3.utils.InsetsUtil.applySystemBarsPadding(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnHome.setOnClickListener(v -> {
            // Về thẳng MainActivity, xóa hết stack phía trên, ép về tab Trang chủ
            Intent intent = new Intent(this, com.FinalProject.group3.MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(com.FinalProject.group3.MainActivity.EXTRA_OPEN_HOME, true);
            startActivity(intent);
            finish();
        });

        blogId = getIntent().getIntExtra(EXTRA_BLOG_ID, 1);
        if (blogId < 1 || blogId > 3) blogId = 1;

        Glide.with(this).load(HERO[blogId - 1]).centerCrop().into(binding.imgHero);

        switch (blogId) {
            case 2:  buildBlog2(); break;
            case 3:  buildBlog3(); break;
            default: buildBlog1(); break;
        }

        setupProducts();
        setupRelated();
        setupFooter();
    }

    // ═══ Bài 1: Gọng kính Hot Trend 2026 + cách chọn theo dáng mặt ═══
    private void buildBlog1() {
        addParagraph("Năm 2026 đánh dấu sự lên ngôi của những thiết kế gọng kính tinh tế, "
                + "tối giản nhưng vẫn thể hiện cá tính riêng. Cùng Glassity khám phá các xu hướng "
                + "gọng kính hot nhất và cách chọn kính phù hợp với từng dáng mặt để tôn lên vẻ đẹp tự nhiên nhé!");

        addSectionTitle("GỌNG KÍNH HOT TREND 2026");
        addParagraph("<b>Gọng dày cá tính</b> — mạnh mẽ, nổi bật, phù hợp phong cách streetwear.<br>"
                + "<b>Gọng kim loại tối giản</b> — thanh lịch, nhẹ nhàng, dễ phối đồ.<br>"
                + "<b>Gọng trong nhẹ nhàng</b> — tự nhiên, hiện đại, không thể lỗi mốt.<br>"
                + "<b>Mắt mèo nhẹ nhàng</b> — nữ tính, quyến rũ, không bao giờ lỗi thời.<br>"
                + "<b>Phi công thời thượng</b> — phóng khoáng, thời trang, đậm chất cuốn hút.");
        addTrendRow(TREND_ITEMS);

        addSectionTitle("CÁCH CHỌN KÍNH\nPHÙ HỢP VỚI DÁNG MẶT");
        addFaceRow(URL_SHAPE_TRON, "<b>Mặt Tròn</b><br>Gương mặt tròn nên ưu tiên gọng vuông hoặc chữ nhật để "
                + "tạo hiệu ứng thanh thoát. Tránh các mẫu gọng tròn bản lớn vì chúng dễ làm khuôn mặt trông tròn hơn.<br>"
                + "<b>Phù hợp:</b> gọng vuông, chữ nhật, gọng wayfarer.");
        addFaceRow(URL_SHAPE_TRAI_XOAN, "<b>Mặt Trái Xoan</b><br>Mặt trái xoan có tỷ lệ cân đối nên phù hợp với hầu hết "
                + "các kiểu gọng kính. Bạn có thể thoải mái lựa chọn mẫu gọng theo phong cách và sở thích "
                + "cá nhân để thể hiện cá tính riêng.<br><b>Phù hợp:</b> hầu hết kiểu dáng vuông, tròn, oval…");
        addFaceRow(URL_SHAPE_TRAI_TIM, "<b>Mặt Trái Tim</b><br>Nên ưu tiên các mẫu gọng mảnh hoặc có phần dưới rộng để "
                + "cân đối khuôn mặt. Tránh gọng mắt mèo có đuôi quá nhọn vì dễ làm phần trán trông rộng hơn.<br>"
                + "<b>Phù hợp:</b> gọng oval, gọng tròn, gọng browline.");
        addFaceRow(URL_SHAPE_KIM_CUONG, "<b>Mặt Kim Cương</b><br>Các mẫu gọng oval hoặc không viền để cân bằng đường nét "
                + "khuôn mặt của người có dáng mặt kim cương. Tránh chọn gọng quá nhỏ hoặc quá hồ vì có thể "
                + "làm nổi bật phần gò má.<br><b>Phù hợp:</b> gọng oval, cat-eye nhẹ…");
        addFaceRow(URL_SHAPE_VUONG, "<b>Mặt Vuông</b><br>Nên chọn những mẫu gọng có đường nét bo tròn hoặc oval để "
                + "làm mềm các góc cạnh trên khuôn mặt. Hạn chế sử dụng gọng vuông dày vì sẽ khiến khuôn mặt "
                + "trông cứng hơn.<br><b>Phù hợp:</b> gọng tròn, oval, gọng bầu dục.");
    }

    // ═══ Bài 2: Sự trở lại của gọng kính Retro thập niên 90 ═══
    private void buildBlog2() {
        addSectionTitle("SỰ TRỖI DẬY CỦA NHỮNG GỌNG\nKÍNH THẬP NIÊN 90S");
        addParagraph("Thời trang là một vòng tuần hoàn, và năm nay, guồng quay ấy đang dừng lại ở "
                + "những năm 1990 — kỷ nguyên của sự tối giản nhưng đầy nổi loạn. Chiếm trọn tâm điểm trên "
                + "các sàn diễn và street style thế giới lúc này không gì khác ngoài sự trở lại đầy kiêu hãnh "
                + "của những chiếc gọng kính dáng Oval cổ điển.");
        addImage(URL_RETRO_90S_GROUP);

        addSectionTitle("DÁNG KÍNH OVAL RETRO \"HỒI SINH\"?");
        addParagraph("Không phải ngẫu nhiên mà dáng kính Oval mạ vàng từ thập niên 90s lại chiếm sóng "
                + "trở lại. Bí quyết nằm ở sự cân bằng hoàn hảo:<br>"
                + "• <b>Tôn dáng mặt:</b> đường bo tròn dẹt đặc trưng giúp làm mềm các góc cạnh, "
                + "tạo sự thanh thoát tự nhiên cho khuôn mặt.<br>"
                + "• <b>Chất liệu biểu tượng:</b> sự kết hợp giữa viền kim loại mạ vàng sang trọng và gọng "
                + "đồi mồi hoài cổ tạo nên một tổng thể không bao giờ lỗi mốt.<br>"
                + "• <b>Cân mọi phong cách:</b> từ Y2K tinh nghịch, bụi bặm cho đến lịch lãm công sở, "
                + "chiếc kính này là điểm nhấn nâng tầm mọi outfit.");

        addSectionTitle("TỪ SÀN DIỄN ĐẾN STREET STYLE —\nKHI CÁC IT-GIRLS ĐỒNG LOẠT \"LÊN ĐỒ\"");
        addParagraph("Không khó để bắt gặp những chiếc gọng kính retro này đang phủ sóng khắp các "
                + "trang mạng xã hội. Từ sàn catwalk cho đến phong cách đời thường của các siêu mẫu hàng đầu "
                + "như Bella Hadid, Kendall Jenner hay các fashionista Gen Z, gọng kính Oval cổ điển chính là "
                + "\"vũ khí bí mật\" để tạo nên diện mạo chic và lôi cuốn. Retro không có nghĩa là cũ kỹ — "
                + "retro chính là sự sành điệu vượt thời gian.");
        addImage(URL_RETRO_STREETSTYLE);

        addSectionTitle("TIÊU ĐIỂM SẢN PHẨM: ĐÓN ĐẦU XU HƯỚNG\nCÙNG GỌNG OVAL ĐỔI MỚI");
        addParagraph("<b>Khung gọng mạ vàng cao cấp:</b> đường viền mảnh mai, mang lại vẻ ngoài "
                + "thanh lịch và sang trọng.<br>"
                + "<b>Càng kính họa tiết đồi mồi:</b> điểm nhấn biểu tượng mang đậm tinh thần thập niên 90, "
                + "tạo chiều sâu và sự khác biệt.<br>"
                + "<b>Tròng màu xanh rêu dịu mắt:</b> vừa thời trang mà còn giúp bảo vệ thị lực tối ưu khi xuống phố.");
    }

    // ═══ Bài 3: Bí kíp chọn kính mát chuẩn chỉnh cho mùa hè ═══
    private void buildBlog3() {
        addSectionTitle("BÍ KÍP CHỌN KÍNH RÂM \"CHUẨN\nCHỈNH\" CHO CHUYẾN MÙA HÈ");
        addParagraph("Kính râm là item không thể thiếu vào mùa hè. Không chỉ là \"vũ khí\" chiếm "
                + "spotlight trong mọi bức ảnh mà còn là tấm khiên bảo vệ đôi mắt trước cái nắng gay gắt "
                + "của đại dương. Thế nhưng, bạn có biết chọn kính đi biển khác hoàn toàn với kính đeo thường ngày?");

        addSectionTitle("TÁC HẠI KHÔN LƯỜNG TỪ KÍNH RÂM GIÁ RẺ");
        addImage(URL_CHEAP_SUNGLASSES_MARKET);
        addParagraph("Nhiều người có thói quen mua đại những chiếc kính râm giá rẻ vài chục nghìn được "
                + "bày bán dọc các khu du lịch biển hoặc trên sàn TMĐT vì nghĩ \"chỉ đeo chụp ảnh rồi thôi\". "
                + "Đây là một sai lầm cực kỳ nguy hiểm:<br>"
                + "• <b>Đánh lừa thị giác:</b> tròng kính giá rẻ chỉ là những miếng nhựa nhuộm màu tối, "
                + "khi đeo vào đồng tử của bạn sẽ tự động mở rộng ra để đón ánh sáng.<br>"
                + "• <b>Bắt nắng gấp đôi:</b> vì không có lớp phủ chống tia cực tím (UV), những chiếc tròng "
                + "giá rẻ này tạo điều kiện cho các tia UV có hại lọt thẳng vào sâu trong mắt — gấp nhiều lần "
                + "so với khi không đeo kính.<br>"
                + "• <b>Hậu quả:</b> gây mỏi mắt, nhức đầu ngay lập tức, về lâu dài có thể dẫn đến bỏng võng mạc, "
                + "viêm giác mạc hoặc đục thủy tinh thể do những đợt phơi nắng tự mắt trần.");

        addSectionTitle("TIÊU CHÍ VÀNG KHI CHỌN KÍNH RÂM ĐI BIỂN");
        addParagraph("<b>1. Phải có chứng nhận UV400 (chống tia UV 100%)</b><br>"
                + "Đây là thông số bắt buộc. Lớp phủ UV400 sẽ ngăn chặn toàn bộ các tia cực tím UVA và UVB "
                + "có hại từ ánh nắng mặt trời chiếu trực tiếp hoặc phản xạ từ bãi cát.<br>"
                + "<b>2. Ưu tiên màu tròng dịu mắt</b><br>"
                + "Ánh nắng ngoài biển có cường độ rất mạnh. Những chiếc kính có tròng màu xanh rêu hoặc nâu "
                + "sẽ lọc chói tốt, đem lại thị giác dịu mắt tức thì nhưng không làm sai lệch màu sắc tự nhiên "
                + "của cảnh vật xung quanh.<br>"
                + "<b>3. Chất liệu gọng bền bỉ, chống ăn mòn từ muối biển</b><br>"
                + "Gió biển mang theo hơi muối rất dễ làm rỉ sét các loại gọng kém chất lượng. Bạn nên ưu tiên "
                + "các loại gọng hợp kim mạ cao cấp hoặc nhựa cứng chất lượng cao để kính luôn bền đẹp.");

        addSectionTitle("THAM KHẢO BST KÍNH RÂM TẠI GLASSITY!");
        addParagraph("Glassity cam kết mang đến cho bạn những chiếc kính râm chất lượng, bảo vệ mắt "
                + "tuyệt đối, kết hợp chất liệu cao cấp chống ăn mòn và chính sách 1 đổi 1 trong 10 ngày "
                + "để mùa hè của bạn an tâm trọn vẹn.");
    }

    // ── Khối sản phẩm gợi ý: nguồn data khác nhau theo bài ────────────────────
    private void setupProducts() {
        FeaturedProductAdapter adapter = new FeaturedProductAdapter(product ->
                ProductDetailActivity.start(this, product.getProductId()));
        binding.rvBlogProducts.setAdapter(adapter);

        ProductRepository.ProductListCallback callback = new ProductRepository.ProductListCallback() {
            @Override public void onSuccess(List<Product> products) { adapter.submitList(products); }
            @Override public void onFailure(String error) { /* section phụ — im lặng */ }
        };

        switch (blogId) {
            case 2:
                binding.tvProductsTitle.setText("TIÊU ĐIỂM SẢN PHẨM");
                productRepo.getProductsByCollection("Essential Acetate", callback);
                break;
            case 3:
                binding.tvProductsTitle.setText("BST KÍNH MÁT");
                productRepo.getProductsByCollection("Sunlight Studio", callback);
                break;
            default:
                binding.tvProductsTitle.setText("GỌNG KÍNH HOT TREND 2026");
                productRepo.getBestSellerProducts(6, callback);
                break;
        }
    }

    // ── 2 bài viết liên quan = 2 bài còn lại ──────────────────────────────────
    private void setupRelated() {
        int[] others = new int[2];
        int idx = 0;
        for (int i = 1; i <= 3; i++) if (i != blogId) others[idx++] = i;

        Glide.with(this).load(HERO[others[0] - 1]).centerCrop().into(binding.imgRelated1);
        binding.tvRelated1.setText(TITLES[others[0] - 1]);
        final int first = others[0];
        // Không finish() — back sẽ quay về đúng bài đang đọc dở
        binding.relatedCard1.setOnClickListener(v -> start(this, first));

        Glide.with(this).load(HERO[others[1] - 1]).centerCrop().into(binding.imgRelated2);
        binding.tvRelated2.setText(TITLES[others[1] - 1]);
        final int second = others[1];
        binding.relatedCard2.setOnClickListener(v -> start(this, second));
    }

    private void setupFooter() {
        binding.incFooter.footerAbout.setOnClickListener(v -> AboutActivity.start(this));
        binding.incFooter.footerContact.setOnClickListener(v ->
                startActivity(ContactActivity.intent(this)));
        binding.incFooter.footerPolicy.setOnClickListener(v ->
                startActivity(PolicyActivity.intent(this, PolicyActivity.TYPE_PRIVACY)));
    }

    // ── Helpers dựng nội dung ─────────────────────────────────────────────────

    /** Tiêu đề section: serif, in hoa, canh giữa (Figma) */
    private void addSectionTitle(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(15);
        tv.setTypeface(android.graphics.Typeface.SERIF);
        tv.setLetterSpacing(0.08f);
        tv.setGravity(android.view.Gravity.CENTER);
        tv.setTextColor(getColor(R.color.color_text_primary));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(20);
        lp.bottomMargin = dp(4);
        tv.setLayoutParams(lp);
        binding.llContent.addView(tv);
    }

    /** Đoạn văn: hỗ trợ <b>, <br> qua Html */
    private void addParagraph(String html) {
        TextView tv = new TextView(this);
        tv.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT));
        tv.setTextSize(13);
        tv.setLineSpacing(dp(3), 1f);
        tv.setTextColor(getColor(R.color.color_text_body));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(10);
        tv.setLayoutParams(lp);
        binding.llContent.addView(tv);
    }

    /** Ảnh minh họa full-width trong bài */
    private void addImage(String url) {
        ImageView iv = new ImageView(this);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(180));
        lp.topMargin = dp(12);
        iv.setLayoutParams(lp);
        Glide.with(this).load(url).centerCrop().into(iv);
        binding.llContent.addView(iv);
    }

    /** Dãy 5 thumbnail vuông + caption (Blog 1: "Gọng kính hot trend 2026") */
    private void addTrendRow(String[][] items) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowLp.topMargin = dp(12);
        row.setLayoutParams(rowLp);

        for (String[] item : items) {
            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            col.setLayoutParams(colLp);

            ImageView iv = new ImageView(this);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setLayoutParams(new LinearLayout.LayoutParams(dp(56), dp(56)));
            iv.setBackgroundResource(R.drawable.bg_product_placeholder_light);
            Glide.with(this).load(item[0]).centerCrop().into(iv);
            col.addView(iv);

            TextView caption = new TextView(this);
            caption.setText(item[1]);
            caption.setTextSize(9.5f);
            caption.setGravity(android.view.Gravity.CENTER);
            caption.setTextColor(getColor(R.color.color_text_secondary));
            LinearLayout.LayoutParams capLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            capLp.topMargin = dp(4);
            caption.setLayoutParams(capLp);
            col.addView(caption);

            row.addView(col);
        }
        binding.llContent.addView(row);
    }

    /** 1 dòng "dáng mặt": ảnh vuông bên trái + đoạn văn bên phải (Blog 1) */
    private void addFaceRow(String imageUrl, String html) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowLp.topMargin = dp(12);
        row.setLayoutParams(rowLp);

        ImageView iv = new ImageView(this);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LinearLayout.LayoutParams ivLp = new LinearLayout.LayoutParams(dp(64), dp(64));
        ivLp.setMarginEnd(dp(10));
        iv.setLayoutParams(ivLp);
        Glide.with(this).load(imageUrl).centerCrop().into(iv);
        row.addView(iv);

        TextView tv = new TextView(this);
        tv.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT));
        tv.setTextSize(13);
        tv.setLineSpacing(dp(3), 1f);
        tv.setTextColor(getColor(R.color.color_text_body));
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(tv);

        binding.llContent.addView(row);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}

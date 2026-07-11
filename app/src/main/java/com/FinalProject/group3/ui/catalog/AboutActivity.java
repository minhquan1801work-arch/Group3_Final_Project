package com.FinalProject.group3.ui.catalog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.FinalProject.group3.databinding.ActivityAboutBinding;
import com.FinalProject.group3.ui.account.ContactActivity;
import com.FinalProject.group3.ui.account.PolicyActivity;
import com.bumptech.glide.Glide;

/**
 * LA.About Glassity (Figma) — trang giới thiệu thương hiệu, mở từ banner
 * "Khám phá Glassity" và footer "About" ở Home/Blog.
 */
public class AboutActivity extends AppCompatActivity {

    private static final String CLOUD = "https://res.cloudinary.com/aa1g9udv/image/upload/f_auto,q_auto/";
    private static final String URL_HERO =
            CLOUD + "v1783747666/glassity/site/about/hero_mickey.png";
    private static final String URL_BRAND =
            CLOUD + "v1783747665/glassity/site/about/hero_editorial.jpg";
    private static final String URL_CERTIFICATION =
            CLOUD + "v1783747667/glassity/site/about/certification.png";
    private static final String URL_AWARDS =
            CLOUD + "v1783747668/glassity/site/about/awards.png";

    private ActivityAboutBinding binding;

    public static void start(Context context) {
        context.startActivity(new Intent(context, AboutActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        com.FinalProject.group3.utils.InsetsUtil.applySystemBarsPadding(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        Glide.with(this).load(URL_HERO).centerCrop().into(binding.imgHero);
        Glide.with(this).load(URL_BRAND).into(binding.imgBrand);
        Glide.with(this).load(URL_CERTIFICATION).into(binding.imgCertification);
        Glide.with(this).load(URL_AWARDS).into(binding.imgAwards);

        binding.tvIntro.setText(
                "Glassity là thương hiệu kính mắt hiện đại dành cho nhịp sống đô thị, nơi sự rõ nét " +
                "không chỉ nằm ở thị giác mà còn ở phong cách và sự tự tin. Kết hợp giữa thiết kế tối " +
                "giản, chất liệu cao cấp và độ chính xác, Glassity tạo nên những sản phẩm tinh tế, dễ " +
                "dàng đồng hành trong mọi khoảnh khắc thường ngày. Với tinh thần đương đại và sự chăm " +
                "chút trong từng chi tiết, chúng tôi không chỉ mang đến một chiếc kính mà còn là một " +
                "tuyên ngôn về cách bạn nhìn thế giới và cách thế giới nhìn bạn.");

        binding.tvMission.setText(
                "Mang đến những thiết kế kính mắt kết hợp giữa độ chính xác quang học, thẩm mỹ hiện " +
                "đại và sự thoải mái trong trải nghiệm hằng ngày. Thông qua chất liệu cao cấp, công " +
                "nghệ thông minh và sự tinh giản trong từng chi tiết, chúng tôi tạo nên những sản phẩm " +
                "giúp người dùng tự tin thể hiện phong cách sống của riêng mình.");

        binding.tvVision.setText(
                "Hướng tới trở thành thương hiệu kính mắt hàng đầu Châu Á, định hình phong cách sống " +
                "đô thị thông qua sự giao thoa giữa thiết kế tinh tế, công nghệ hiện đại và bản sắc cá " +
                "nhân. Mỗi thiết kế không chỉ mang lại sự rõ nét trong thị giác, mà còn là tuyên ngôn " +
                "cho phong cách sống và cá tính của thế hệ hiện đại.");

        binding.tvCert.setText(
                "EssilorLuxottica là một trong những tập đoàn lớn hàng đầu ngành thị lực và mắt kính, " +
                "gắn với nhiều thương hiệu kính tròng quen thuộc như Essilor, Varilux, Crizal, Eyezen " +
                "và Transitions. Các thương hiệu này thường được khách hàng quan tâm khi lựa chọn " +
                "tròng kính đa tròng, tròng kính đổi màu, tròng kính chống chói hoặc tròng kính hỗ trợ " +
                "người dùng màn hình.");

        binding.tvAwards.setText(
                "Glassity vinh dự đạt giải thưởng Top 10 Thương Hiệu Hàng Đầu ASEAN 2023, Top 10 " +
                "Thương Hiệu Nổi Tiếng Việt Nam 2023 và Top 10 Thương Hiệu Dẫn Đầu Việt Nam 2024 do " +
                "Viện nghiên cứu Kinh tế Châu Á – VEDSU bình chọn, tiếp tục vững vị thế dẫn đầu ngành " +
                "kính mắt tại Việt Nam.");

        setupFooter();
    }

    private void setupFooter() {
        binding.incFooter.footerAbout.setOnClickListener(v -> { /* đang ở trang này */ });
        binding.incFooter.footerContact.setOnClickListener(v ->
                startActivity(ContactActivity.intent(this)));
        binding.incFooter.footerPolicy.setOnClickListener(v ->
                startActivity(PolicyActivity.intent(this, PolicyActivity.TYPE_PRIVACY)));
    }
}

package com.FinalProject.group3.ui.catalog;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.FinalProject.group3.R;
import com.FinalProject.group3.adapter.FeaturedProductAdapter;
import com.FinalProject.group3.adapter.HeroBannerAdapter;
import com.FinalProject.group3.adapter.ProductAdapter;
import com.FinalProject.group3.databinding.FragmentHomeBinding;
import com.FinalProject.group3.model.Product;
import com.FinalProject.group3.repository.ProductRepository;

import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String CLOUD = "https://res.cloudinary.com/aa1g9udv/image/upload/f_auto,q_auto/";

    // Hero: 3 portrait thời trang đeo kính (khớp Figma)
    private static final List<String> HERO_URLS = Arrays.asList(
            CLOUD + "v1783502208/5714e927d794a24eb4e3f5ac9d35cec642484aca_bhnscg.png",   // nữ blazer đen, cat-eye
            CLOUD + "v1783502208/54bafc97ad1059dfd47aceb4ce98548693bf6b5c_xzhlb3.png", // james — wrap shades
            CLOUD + "v1783502209/04b56adec24d1b9c3c5e7043b002d25a723d0d8b_cuvcfg.png" // juhoon — sunglasses đôi
    );

    // ID sản phẩm cho từng hero slide (bấm "XEM NGAY" → ProductDetail).
    private static final List<String> HERO_PRODUCT_IDS = Arrays.asList(
            "7dxSOPmpL0hiPeqw4FaE",  // slide 1 — Cyber Fashion Sunglasses
            "D6FYz7iLHMH8OiHFL84g",  // slide 2 — Unique Design Fashion Sunglasses
            "C1wvHijLlWYEF9W99C5j"   // slide 3 — Modern Square Sunglasses Style
    );

    private static final String URL_PROMO       = CLOUD + "v1783355118/glassity/site/promo_sasalele.jpg";
    private static final String URL_MONOCHROME = CLOUD + "v1783502208/d21ee09b2dcb18b17af1ec5262d245334b74241b_lwh1kx.png";
    private static final String URL_ESSENTIAL   = CLOUD + "v1783502208/7aec1cc6374895c92464c3118255d38449be11ee_yzemoi.png";
    private static final String URL_SUNLIGHT    = CLOUD + "v1783502207/36566f6bfcef59072645817ac9273fc3824ad0c3_msnssy.png";
    private static final String URL_KHAM_PHA    = CLOUD + "v1783355119/glassity/site/kham_pha_flowers.jpg";
    private static final String URL_BLOG1 = CLOUD + "v1783753558/glassity/site/blog/hero_trend2026.png";
    private static final String URL_BLOG2 = CLOUD + "v1783753564/glassity/site/blog/hero_retro.png";
    private static final String URL_BLOG3 = CLOUD + "v1783753566/glassity/site/blog/hero_summer.png";

    private static final String URL_SHAPE_TRON       = CLOUD + "v1783354487/glassity/site/shape_tron.png";
    private static final String URL_SHAPE_TRAI_XOAN  = CLOUD + "v1783354492/glassity/site/shape_trai_xoan.png";
    private static final String URL_SHAPE_TRAI_TIM   = CLOUD + "v1783354498/glassity/site/shape_trai_tim.png";
    private static final String URL_SHAPE_KIM_CUONG  = CLOUD + "v1783354502/glassity/site/shape_kim_cuong.png";
    private static final String URL_SHAPE_VUONG      = CLOUD + "v1783354507/glassity/site/shape_vuong.png";

    private FragmentHomeBinding binding;
    private final ProductRepository productRepository = new ProductRepository();

    private FeaturedProductAdapter featuredAdapter;
    private HeroBannerAdapter heroBannerAdapter;

    private final Handler autoScrollHandler = new Handler(Looper.getMainLooper());
    private Runnable autoScrollRunnable;
    private static final long HERO_AUTO_SCROLL_MS = 3500;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preloadHeroImages();
        setupAdapters();
        setupHeroCarousel();
        setupStaticImages();
        setupClickListeners();
        loadProducts();
    }

    /** Bắn trước request tải 3 ảnh hero ngay từ đầu — song song với Firestore/layout,
     *  để lúc carousel hiển thị thì ảnh đã có sẵn/gần xong trong cache Glide. */
    private void preloadHeroImages() {
        for (String url : HERO_URLS) {
            com.bumptech.glide.Glide.with(this).load(url).preload();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startAutoScroll();
        if (binding != null)
            com.FinalProject.group3.utils.CartQuickActions.refreshBadge(binding.tvCartBadge);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoScroll();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAutoScroll();
        binding = null;
    }

    /** Cuộn NestedScrollView (root) về đầu — gọi khi quay lại Home từ màn khác (VD: sau thanh toán). */
    public void scrollToTop() {
        if (binding == null) return;
        binding.getRoot().smoothScrollTo(0, 0);
    }

    // ── Adapters ─────────────────────────────────────────────────────────────

    private void setupAdapters() {
        featuredAdapter = new FeaturedProductAdapter(product ->
                ProductDetailActivity.start(requireContext(), product.getProductId()));
        binding.rvFeaturedProducts.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvFeaturedProducts.setAdapter(featuredAdapter);

        // Badge số lượng giỏ hàng trên header (trước đây wire qua productAdapter đã bỏ)
        com.FinalProject.group3.utils.CartQuickActions.refreshBadge(binding.tvCartBadge);
    }

    // ── Hero carousel ─────────────────────────────────────────────────────────

    private void setupHeroCarousel() {
        heroBannerAdapter = new HeroBannerAdapter(HERO_URLS);
        heroBannerAdapter.setOnBannerClickListener(position -> {
            String productId = HERO_PRODUCT_IDS.get(position);
            if (productId != null && !productId.isEmpty()) {
                ProductDetailActivity.start(requireContext(), productId);
            } else {
                Toast.makeText(requireContext(), "Sản phẩm sắp ra mắt!", Toast.LENGTH_SHORT).show();
            }
        });
        binding.vpHero.setAdapter(heroBannerAdapter);

        // Peek carousel (Figma): slide giữa ~55% màn hình, 2 bên ló ra rõ + thu nhỏ khi lệch tâm
        binding.vpHero.setOffscreenPageLimit(3);
        int peekPx = dpToPx(72);
        androidx.recyclerview.widget.RecyclerView inner =
                (androidx.recyclerview.widget.RecyclerView) binding.vpHero.getChildAt(0);
        inner.setPadding(peekPx, 0, peekPx, 0);
        inner.setClipToPadding(false);

        androidx.viewpager2.widget.CompositePageTransformer transformer =
                new androidx.viewpager2.widget.CompositePageTransformer();
        transformer.addTransformer(new androidx.viewpager2.widget.MarginPageTransformer(dpToPx(10)));
        transformer.addTransformer((page, position) -> {
            // Card đang hiển thị (position=0) to hẳn lên; càng lệch khỏi tâm càng thu nhỏ
            float t = Math.min(1f, Math.abs(position));
            float scale = 1f - 0.15f * t; // card bên = 85% kích thước
            page.setScaleY(scale);
            page.setScaleX(scale);
        });
        binding.vpHero.setPageTransformer(transformer);
    }

    private void startAutoScroll() {
        stopAutoScroll();
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (binding == null) return;
                int next = (binding.vpHero.getCurrentItem() + 1) % HERO_URLS.size();
                binding.vpHero.setCurrentItem(next, true);
                autoScrollHandler.postDelayed(this, HERO_AUTO_SCROLL_MS);
            }
        };
        autoScrollHandler.postDelayed(autoScrollRunnable, HERO_AUTO_SCROLL_MS);
    }

    private void stopAutoScroll() {
        if (autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }

    // ── Ảnh tĩnh (tile BST, banner, blog, dáng mặt) ─────────────────────────────
    private void setupStaticImages() {
        // Promo banner: clip ảnh theo bo góc của bg_rounded_card
        binding.layoutPromoBanner.setClipToOutline(true);
        com.bumptech.glide.Glide.with(this).load(URL_PROMO).centerCrop().into(binding.imgPromo);

        com.bumptech.glide.Glide.with(this).load(URL_MONOCHROME).centerCrop().into(binding.imgMonochrome);
        com.bumptech.glide.Glide.with(this).load(URL_ESSENTIAL).centerCrop().into(binding.imgEssential);
        com.bumptech.glide.Glide.with(this).load(URL_SUNLIGHT).centerCrop().into(binding.imgSunlight);
        com.bumptech.glide.Glide.with(this).load(URL_KHAM_PHA).centerCrop().into(binding.imgKhamPha);
        com.bumptech.glide.Glide.with(this).load(URL_BLOG1).centerCrop().into(binding.imgBlog1);
        com.bumptech.glide.Glide.with(this).load(URL_BLOG2).centerCrop().into(binding.imgBlog2);
        com.bumptech.glide.Glide.with(this).load(URL_BLOG3).centerCrop().into(binding.imgBlog3);

        com.bumptech.glide.Glide.with(this).load(URL_SHAPE_TRON).centerCrop().into(binding.imgFaceTron);
        com.bumptech.glide.Glide.with(this).load(URL_SHAPE_TRAI_XOAN).centerCrop().into(binding.imgFaceTraiXoan);
        com.bumptech.glide.Glide.with(this).load(URL_SHAPE_TRAI_TIM).centerCrop().into(binding.imgFaceTraiTim);
        com.bumptech.glide.Glide.with(this).load(URL_SHAPE_KIM_CUONG).centerCrop().into(binding.imgFaceKimCuong);
        com.bumptech.glide.Glide.with(this).load(URL_SHAPE_VUONG).centerCrop().into(binding.imgFaceVuong);
    }

    // ── Click listeners ───────────────────────────────────────────────────────

    private void setupClickListeners() {
        binding.btnMenu.setOnClickListener(v -> {
            if (getActivity() instanceof com.FinalProject.group3.MainActivity) {
                ((com.FinalProject.group3.MainActivity) getActivity()).openDrawer();
            }
        });

        // Logo header → đã ở Home, cuộn lên đầu trang
        binding.imgLogo.setOnClickListener(v ->
                ((androidx.core.widget.NestedScrollView) binding.getRoot()).smoothScrollTo(0, 0));

        binding.btnSearch.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SearchActivity.class)));

        binding.btnCart.setOnClickListener(v ->
                androidx.navigation.fragment.NavHostFragment.findNavController(this)
                        .navigate(R.id.cartFragment));

        binding.btnViewAllFeatured.setOnClickListener(v ->
                ProductListActivity.startAll(requireContext()));

        // Banner "Mua sản phẩm thứ hai giảm 20%" → mở tất cả sản phẩm
        binding.layoutPromoBanner.setOnClickListener(v ->
                ProductListActivity.startAll(requireContext()));

        // Collection tiles
        binding.tileMonochrome.setOnClickListener(v ->
                CollectionActivity.start(requireContext(), "Monochrome Collection"));
        binding.tileEssential.setOnClickListener(v ->
                CollectionActivity.start(requireContext(), "Essential Acetate"));
        binding.tileSunlight.setOnClickListener(v ->
                CollectionActivity.start(requireContext(), "Sunlight Studio"));

        binding.btnViewAllCollections.setOnClickListener(v ->
                CollectionActivity.start(requireContext()));

        // Face shapes
        binding.faceTron.setOnClickListener(v ->
                ProductListActivity.start(requireContext(), null, ProductListActivity.SHAPE_TRON, "Kính mặt tròn"));
        binding.faceTraiXoan.setOnClickListener(v ->
                ProductListActivity.start(requireContext(), null, ProductListActivity.SHAPE_TRAI_XOAN, "Kính mặt trái xoan"));
        binding.faceTraiTim.setOnClickListener(v ->
                ProductListActivity.start(requireContext(), null, ProductListActivity.SHAPE_TRAI_TIM, "Kính mặt trái tim"));
        binding.faceKimCuong.setOnClickListener(v ->
                ProductListActivity.start(requireContext(), null, ProductListActivity.SHAPE_KIM_CUONG, "Kính mặt kim cương"));
        binding.faceVuong.setOnClickListener(v ->
                ProductListActivity.start(requireContext(), null, ProductListActivity.SHAPE_VUONG, "Kính mặt vuông"));

        // Banner "Khám phá Glassity" → mở trang giới thiệu thương hiệu; link gạch chân theo Figma
        binding.tvKhamPhaLink.setPaintFlags(
                binding.tvKhamPhaLink.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        binding.bannerKhamPha.setOnClickListener(v -> AboutActivity.start(requireContext()));

        // Blog cards → LA.Blog1 (chọn kính theo dáng mặt) / LA.Blog2 (retro)
        binding.blogCard1.setOnClickListener(v -> BlogActivity.start(requireContext(), 1));
        binding.blogCard2.setOnClickListener(v -> BlogActivity.start(requireContext(), 2));
        binding.blogCard3.setOnClickListener(v -> BlogActivity.start(requireContext(), 3));

        // Footer: About / Contact / Policy → gắn trang đích
        binding.incFooter.footerAbout.setOnClickListener(v -> AboutActivity.start(requireContext()));
        binding.incFooter.footerContact.setOnClickListener(v -> startActivity(
                com.FinalProject.group3.ui.account.ContactActivity.intent(requireContext())));
        binding.incFooter.footerPolicy.setOnClickListener(v -> startActivity(
                com.FinalProject.group3.ui.account.PolicyActivity.intent(
                        requireContext(), com.FinalProject.group3.ui.account.PolicyActivity.TYPE_PRIVACY)));
    }

    // ── Load data ─────────────────────────────────────────────────────────────

    private void loadProducts() {
        productRepository.getBestSellerProducts(10, new ProductRepository.ProductListCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                if (binding == null) return;
                featuredAdapter.submitList(products);
            }

            @Override
            public void onFailure(String error) {
                if (binding == null) return;
                Toast.makeText(requireContext(), "Lỗi tải sản phẩm: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int dpToPx(int dp) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}

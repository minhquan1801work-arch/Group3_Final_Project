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

    private static final String CLOUD = "https://res.cloudinary.com/aa1g9udv/image/upload/";

    // Hero: 3 portrait thời trang đeo kính (khớp Figma)
    private static final List<String> HERO_URLS = Arrays.asList(
            CLOUD + "v1783502208/5714e927d794a24eb4e3f5ac9d35cec642484aca_bhnscg.png",   // nữ blazer đen, cat-eye
            CLOUD + "v1783502208/54bafc97ad1059dfd47aceb4ce98548693bf6b5c_xzhlb3.png", // james — wrap shades
            CLOUD + "v1783502209/04b56adec24d1b9c3c5e7043b002d25a723d0d8b_cuvcfg.png" // juhoon — sunglasses đôi
    );

    private static final String URL_PROMO       = CLOUD + "v1783355118/glassity/site/promo_sasalele.jpg";
    private static final String URL_MONOCHROME = CLOUD + "v1783502208/d21ee09b2dcb18b17af1ec5262d245334b74241b_lwh1kx.png";
    private static final String URL_ESSENTIAL   = CLOUD + "v1783502208/7aec1cc6374895c92464c3118255d38449be11ee_yzemoi.png";
    private static final String URL_SUNLIGHT    = CLOUD + "v1783502207/36566f6bfcef59072645817ac9273fc3824ad0c3_msnssy.png";
    private static final String URL_KHAM_PHA    = CLOUD + "v1783355119/glassity/site/kham_pha_flowers.jpg";
    private static final String URL_BLOG_GUIDE  = CLOUD + "v1783354481/glassity/site/guide_diagram.png";
    private static final String URL_BLOG_TREND  = CLOUD + "v1783355123/glassity/site/blog_login_signup.jpg";

    private static final String URL_SHAPE_TRON       = CLOUD + "v1783354487/glassity/site/shape_tron.png";
    private static final String URL_SHAPE_TRAI_XOAN  = CLOUD + "v1783354492/glassity/site/shape_trai_xoan.png";
    private static final String URL_SHAPE_TRAI_TIM   = CLOUD + "v1783354498/glassity/site/shape_trai_tim.png";
    private static final String URL_SHAPE_KIM_CUONG  = CLOUD + "v1783354502/glassity/site/shape_kim_cuong.png";
    private static final String URL_SHAPE_VUONG      = CLOUD + "v1783354507/glassity/site/shape_vuong.png";

    private FragmentHomeBinding binding;
    private final ProductRepository productRepository = new ProductRepository();

    private FeaturedProductAdapter featuredAdapter;
    private ProductAdapter productAdapter;
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
        setupAdapters();
        setupHeroCarousel();
        setupStaticImages();
        setupClickListeners();
        loadProducts();
    }

    @Override
    public void onResume() {
        super.onResume();
        startAutoScroll();
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

    // ── Adapters ─────────────────────────────────────────────────────────────

    private void setupAdapters() {
        featuredAdapter = new FeaturedProductAdapter(product ->
                ProductDetailActivity.start(requireContext(), product.getProductId()));
        binding.rvFeaturedProducts.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvFeaturedProducts.setAdapter(featuredAdapter);

        productAdapter = new ProductAdapter(product ->
                ProductDetailActivity.start(requireContext(), product.getProductId()));
        binding.rvProducts.setAdapter(productAdapter);
    }

    // ── Hero carousel ─────────────────────────────────────────────────────────

    private void setupHeroCarousel() {
        heroBannerAdapter = new HeroBannerAdapter(HERO_URLS);
        binding.vpHero.setAdapter(heroBannerAdapter);

        setupDots(HERO_URLS.size());

        binding.vpHero.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
            }
        });
    }

    private void setupDots(int count) {
        binding.llHeroDots.removeAllViews();
        for (int i = 0; i < count; i++) {
            ImageView dot = new ImageView(requireContext());
            int size = dpToPx(8);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMargins(dpToPx(3), 0, dpToPx(3), 0);
            dot.setLayoutParams(lp);
            dot.setBackgroundResource(i == 0
                    ? R.drawable.dot_active : R.drawable.dot_inactive);
            binding.llHeroDots.addView(dot);
        }
    }

    private void updateDots(int selected) {
        for (int i = 0; i < binding.llHeroDots.getChildCount(); i++) {
            binding.llHeroDots.getChildAt(i).setBackgroundResource(
                    i == selected ? R.drawable.dot_active : R.drawable.dot_inactive);
        }
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
        com.bumptech.glide.Glide.with(this).load(URL_BLOG_GUIDE).centerCrop().into(binding.imgBlog1);
        com.bumptech.glide.Glide.with(this).load(URL_BLOG_TREND).centerCrop().into(binding.imgBlog2);

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

        binding.btnSearch.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SearchActivity.class)));

        binding.btnCart.setOnClickListener(v ->
                androidx.navigation.fragment.NavHostFragment.findNavController(this)
                        .navigate(R.id.cartFragment));

        binding.btnViewAllFeatured.setOnClickListener(v ->
                ProductListActivity.startAll(requireContext()));

        binding.btnViewAllProducts.setOnClickListener(v ->
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

        // Banner "Khám phá Glassity" → mở tất cả sản phẩm
        binding.bannerKhamPha.setOnClickListener(v ->
                ProductListActivity.startAll(requireContext()));

        // Blog → placeholder Toast (chưa có BlogActivity)
        binding.blogCard1.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Blog sắp ra mắt", Toast.LENGTH_SHORT).show());
        binding.blogCard2.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Blog sắp ra mắt", Toast.LENGTH_SHORT).show());
    }

    // ── Load data ─────────────────────────────────────────────────────────────

    private void loadProducts() {
        productRepository.getBestSellerProducts(10, new ProductRepository.ProductListCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                if (binding == null) return;
                featuredAdapter.submitList(products);
                productAdapter.submitList(products);
                binding.tvEmptyProducts.setVisibility(
                        products.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(String error) {
                if (binding == null) return;
                binding.tvEmptyProducts.setVisibility(View.VISIBLE);
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

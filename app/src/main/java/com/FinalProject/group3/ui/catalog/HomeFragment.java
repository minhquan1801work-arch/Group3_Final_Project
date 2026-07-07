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

    // Điền Cloudinary URL vào đây khi có ảnh
    private static final List<String> HERO_URLS = Arrays.asList(
            "",   // hero slide 1
            "",   // hero slide 2
            ""    // hero slide 3
    );

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
        setupClickListeners();
        loadProducts();
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

    // ── Adapters ─────────────────────────────────────────────────────────────

    private void setupAdapters() {
        featuredAdapter = new FeaturedProductAdapter(product ->
                ProductDetailActivity.start(requireContext(), product.getProductId()));
        binding.rvFeaturedProducts.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvFeaturedProducts.setAdapter(featuredAdapter);

        productAdapter = new ProductAdapter(product ->
                ProductDetailActivity.start(requireContext(), product.getProductId()));
        com.FinalProject.group3.utils.CartQuickActions.wire(
                productAdapter, requireActivity(), binding.btnCart, binding.tvCartBadge);
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

        // Blog cards → LA.Blog1 (chọn kính theo dáng mặt) / LA.Blog2 (retro)
        binding.blogCard1.setOnClickListener(v -> BlogActivity.start(requireContext(), 1));
        binding.blogCard2.setOnClickListener(v -> BlogActivity.start(requireContext(), 2));
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

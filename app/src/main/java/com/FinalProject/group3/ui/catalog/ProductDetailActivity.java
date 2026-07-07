package com.FinalProject.group3.ui.catalog;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalProject.group3.R;
import com.FinalProject.group3.adapter.ProductAdapter;
import com.FinalProject.group3.databinding.ActivityProductDetailBinding;
import com.FinalProject.group3.databinding.ItemImageThumbBinding;
import com.FinalProject.group3.databinding.ItemReviewBinding;
import com.FinalProject.group3.model.CartDetail;
import com.FinalProject.group3.model.Product;
import com.FinalProject.group3.repository.CartRepository;
import com.FinalProject.group3.repository.ProductRepository;
import com.FinalProject.group3.ui.order.CheckoutActivity;
import com.FinalProject.group3.utils.FirebaseHelper;
import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * DL_Product Detail_Layout2 (Figma) — màn Chi tiết sản phẩm. [Task B1]
 *
 * Luồng BPMN: Xem chi tiết → chọn màu + số lượng →
 *   - "Thêm vào giỏ hàng" → CartRepository.addToCart() → ở lại trang (đi tiếp qua tab Giỏ)
 *   - "MUA NGAY"          → addToCartReturningId() → CheckoutActivity.start(item vừa thêm)
 *     (item vẫn nằm trong giỏ; Checkout đặt hàng xong sẽ tự xóa item đã mua — đúng BPMN)
 * "Thêm giỏ" yêu cầu đăng nhập; "Mua ngay" khách vẫn được dùng qua CheckoutActivity.startDirect().
 */
public class ProductDetailActivity extends AppCompatActivity {

    private static final String EXTRA_PRODUCT_ID = "product_id";
    private static final NumberFormat VND_FORMAT = NumberFormat.getInstance(new Locale("vi", "VN"));

    // Map hex màu (Firestore) → tên tiếng Việt hiển thị "Màu sắc: Đen"
    private static final Map<String, String> COLOR_NAMES = new HashMap<>();
    static {
        COLOR_NAMES.put("#1A1614", "Đen");
        COLOR_NAMES.put("#FFFFFF", "Trắng");
        COLOR_NAMES.put("#C0C0C0", "Bạc");
        COLOR_NAMES.put("#4A4A4A", "Xám");
        COLOR_NAMES.put("#C8A96E", "Vàng đồng");
        COLOR_NAMES.put("#C88B3A", "Hổ phách");
        COLOR_NAMES.put("#8B6914", "Nâu đậm");
        COLOR_NAMES.put("#AC9C8D", "Nâu be");
        COLOR_NAMES.put("#72383D", "Đỏ rượu");
        COLOR_NAMES.put("#4A90D9", "Xanh dương");
    }

    // Review demo khớp Figma — TODO [B-next]: đọc từ collection "reviews" khi có
    private static final String[][] DEMO_REVIEWS = {
            {"@jessica.2501", "19/5/2026", "Giao hàng nhanh, kính chắc chắn, sẽ ủng hộ tiếp khi có tiền. Nhìu <3"},
            {"@huyenguyen", "1/2026", "Kính đẹp y hình, đóng gói kỹ. Đeo nhẹ mặt, rất ưng!"},
    };

    private ActivityProductDetailBinding binding;
    private final ProductRepository productRepo = new ProductRepository();
    private final CartRepository cartRepo = new CartRepository();

    private Product product;
    private int quantity = 1;
    private int selectedColorIndex = 0;
    private ProductAdapter relatedAdapter;

    public static void start(Context context, String productId) {
        Intent intent = new Intent(context, ProductDetailActivity.class);
        intent.putExtra(EXTRA_PRODUCT_ID, productId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        com.FinalProject.group3.utils.InsetsUtil.applySystemBarsPadding(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        // Icon giỏ header → mở tab Giỏ hàng (khách cần đăng nhập trước)
        binding.btnCartHeader.setOnClickListener(v -> {
            if (FirebaseHelper.getCurrentUserId() == null) {
                com.FinalProject.group3.utils.LoginRequiredDialog.show(
                        this, "Đăng nhập để xem giỏ hàng của bạn");
                return;
            }
            Intent i = new Intent(this, com.FinalProject.group3.MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            i.putExtra(com.FinalProject.group3.MainActivity.EXTRA_OPEN_CART, true);
            startActivity(i);
        });

        setupQtyStepper();
        setupTabs();
        bindDemoReviews();

        binding.btnAddToCart.setOnClickListener(v -> addToCart(false));
        binding.btnBuyNow.setOnClickListener(v -> addToCart(true));
        binding.btnAllReviews.setOnClickListener(v ->
                Toast.makeText(this, "Tất cả đánh giá — sẽ làm ở bước tiếp theo", Toast.LENGTH_SHORT).show());

        String productId = getIntent().getStringExtra(EXTRA_PRODUCT_ID);
        if (productId == null) { finish(); return; }
        loadProduct(productId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding != null)
            com.FinalProject.group3.utils.CartQuickActions.refreshBadge(binding.tvCartBadge);
    }

    // ── Load & bind sản phẩm ──────────────────────────────────────────────────
    private void loadProduct(String productId) {
        productRepo.getProductById(productId, new ProductRepository.ProductCallback() {
            @Override
            public void onSuccess(Product p) {
                if (binding == null || p == null) return;
                product = p;
                bindProduct();
                loadRelated();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ProductDetailActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void bindProduct() {
        binding.tvName.setText(product.getName());
        binding.tvPrice.setText(VND_FORMAT.format(product.getPrice()) + " đ");
        binding.tvStock.setText(product.getStock() > 0
                ? "Còn " + product.getStock() + " sản phẩm" : "Hết hàng");

        // Mô tả: Firestore, fallback đoạn mặc định nếu trống
        String desc = product.getDescription();
        if (desc == null || desc.trim().isEmpty()) {
            desc = "Kính mắt Glassity thiết kế hiện đại, tròng chống tia UV400, "
                    + "gọng nhẹ bền bỉ, phù hợp mọi khuôn mặt và phong cách.";
        }
        binding.tvDescription.setText(desc
                + "\n\n– Chất liệu: nhựa cao cấp\n– Màu sắc: nhiều màu\n– Tình trạng: Mới 100%"
                + "\n– Lưu ý: Sản phẩm chỉ bao gồm mắt kính và không bao gồm phụ kiện khác");

        setupImages();
        setupColors();
        binding.tvQty.setText(String.valueOf(quantity));
    }

    // ── Ảnh: ViewPager2 + thumbnail ───────────────────────────────────────────
    private void setupImages() {
        List<String> images = (product.getImages() != null && !product.getImages().isEmpty())
                ? product.getImages() : new ArrayList<>();

        ImagePagerAdapter pagerAdapter = new ImagePagerAdapter(images);
        binding.vpImages.setAdapter(pagerAdapter);

        ThumbAdapter thumbAdapter = new ThumbAdapter(images,
                pos -> binding.vpImages.setCurrentItem(pos, true));
        binding.rvThumbs.setAdapter(thumbAdapter);
        binding.rvThumbs.setVisibility(images.size() > 1 ? View.VISIBLE : View.GONE);

        binding.vpImages.registerOnPageChangeCallback(
                new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                    @Override public void onPageSelected(int position) {
                        thumbAdapter.setSelected(position);
                    }
                });
    }

    // ── Chọn màu: dot động từ product.colors ─────────────────────────────────
    private void setupColors() {
        binding.llColors.removeAllViews();
        List<String> colors = product.getColors();
        if (colors == null || colors.isEmpty()) {
            binding.tvVariant.setText("Kiểu dáng: Kính mát");
            return;
        }

        int size = (int) (28 * getResources().getDisplayMetrics().density);
        int margin = (int) (10 * getResources().getDisplayMetrics().density);

        for (int i = 0; i < colors.size(); i++) {
            final int index = i;
            View dot = new View(this);
            android.widget.LinearLayout.LayoutParams lp =
                    new android.widget.LinearLayout.LayoutParams(size, size);
            lp.setMargins(0, 0, margin, 0);
            dot.setLayoutParams(lp);
            dot.setOnClickListener(v -> {
                selectedColorIndex = index;
                refreshColorDots();
            });
            binding.llColors.addView(dot);
        }
        refreshColorDots();
    }

    private void refreshColorDots() {
        List<String> colors = product.getColors();
        for (int i = 0; i < binding.llColors.getChildCount(); i++) {
            View dot = binding.llColors.getChildAt(i);
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(parseColorSafe(colors.get(i)));
            // Dot đang chọn: viền cam (giống thumbnail Figma)
            bg.setStroke(i == selectedColorIndex ? 4 : 1,
                    i == selectedColorIndex
                            ? getColor(R.color.color_price)
                            : getColor(R.color.color_field_border));
            dot.setBackground(bg);
        }
        binding.tvVariant.setText("Kiểu dáng: Kính mát | Màu sắc: "
                + colorName(colors.get(selectedColorIndex)));
    }

    private int parseColorSafe(String hex) {
        try {
            return Color.parseColor(hex.startsWith("#") ? hex : "#" + hex);
        } catch (IllegalArgumentException e) {
            return getColor(R.color.brand_dark);
        }
    }

    private String colorName(String hex) {
        String name = COLOR_NAMES.get(hex == null ? "" : hex.toUpperCase(Locale.US));
        return name != null ? name : hex;
    }

    // ── Stepper số lượng (min 1, max stock) ───────────────────────────────────
    private void setupQtyStepper() {
        binding.btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                binding.tvQty.setText(String.valueOf(quantity));
            }
        });
        binding.btnPlus.setOnClickListener(v -> {
            int max = (product != null && product.getStock() > 0) ? product.getStock() : 99;
            if (quantity < max) {
                quantity++;
                binding.tvQty.setText(String.valueOf(quantity));
            } else {
                Toast.makeText(this, "Chỉ còn " + max + " sản phẩm trong kho", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Tabs: cuộn tới section tương ứng ──────────────────────────────────────
    private void setupTabs() {
        binding.tabReview.setOnClickListener(v -> {
            setActiveTab(binding.tabReview);
            binding.scrollContent.smoothScrollTo(0, binding.sectionReview.getTop());
        });
        binding.tabDetail.setOnClickListener(v -> {
            setActiveTab(binding.tabDetail);
            binding.scrollContent.smoothScrollTo(0, binding.sectionDetail.getTop());
        });
        binding.tabForYou.setOnClickListener(v -> {
            setActiveTab(binding.tabForYou);
            binding.scrollContent.smoothScrollTo(0, binding.sectionRelated.getTop());
        });
    }

    private void setActiveTab(View active) {
        View[] tabs = {binding.tabReview, binding.tabDetail, binding.tabForYou};
        for (View t : tabs) {
            ((android.widget.TextView) t).setTextColor(
                    t == active ? getColor(R.color.color_price) : getColor(R.color.color_text_secondary));
            ((android.widget.TextView) t).setTypeface(null,
                    t == active ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }
    }

    // ── Reviews demo (Figma DL_Review) ────────────────────────────────────────
    private void bindDemoReviews() {
        binding.tvRating.setText("4.9");
        binding.tvRatingCount.setText("Đánh giá sản phẩm (" + DEMO_REVIEWS.length + ")");

        LayoutInflater inflater = LayoutInflater.from(this);
        for (String[] r : DEMO_REVIEWS) {
            ItemReviewBinding item = ItemReviewBinding.inflate(inflater, binding.llReviews, false);
            item.tvReviewer.setText(r[0]);
            item.tvReviewDate.setText(r[1]);
            item.tvReviewText.setText(r[2]);
            int starSize = (int) (14 * getResources().getDisplayMetrics().density);
            for (int i = 0; i < 5; i++) {
                ImageView star = new ImageView(this);
                star.setLayoutParams(new ViewGroup.LayoutParams(starSize, starSize));
                star.setImageResource(R.drawable.ic_star);
                item.llStars.addView(star);
            }
            binding.llReviews.addView(item.getRoot());
        }
    }

    // ── Sản phẩm liên quan: cùng category, trừ chính nó ───────────────────────
    private void loadRelated() {
        relatedAdapter = new ProductAdapter(p -> ProductDetailActivity.start(this, p.getProductId()));
        com.FinalProject.group3.utils.CartQuickActions.wire(
                relatedAdapter, this, binding.btnCartHeader, binding.tvCartBadge);
        binding.rvRelated.setAdapter(relatedAdapter);

        productRepo.getProductsByCategory(product.getCategoryId(),
                new ProductRepository.ProductListCallback() {
                    @Override
                    public void onSuccess(List<Product> products) {
                        if (binding == null) return;
                        List<Product> related = new ArrayList<>();
                        for (Product p : products) {
                            if (!p.getProductId().equals(product.getProductId())) related.add(p);
                        }
                        relatedAdapter.submitList(related);
                        binding.sectionRelated.setVisibility(related.isEmpty() ? View.GONE : View.VISIBLE);
                    }

                    @Override public void onFailure(String error) { /* im lặng — section phụ */ }
                });
    }

    // ── Thêm vào giỏ / Mua ngay ───────────────────────────────────────────────
    private void addToCart(boolean buyNow) {
        if (product == null) return;

        if (currentVariantStock() <= 0) {
            Toast.makeText(this, "Sản phẩm đã hết hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Guest: Thêm giỏ → dialog đăng nhập; Mua ngay → Checkout trực tiếp không qua giỏ
        if (FirebaseHelper.getCurrentUserId() == null) {
            if (buyNow) {
                CheckoutActivity.startDirect(this, product.getProductId(),
                        currentVariantColor(), quantity);
            } else {
                com.FinalProject.group3.utils.LoginRequiredDialog.show(
                        this, "Đăng nhập để thêm sản phẩm vào giỏ hàng");
            }
            return;
        }

        String color = currentVariantColor();
        CartDetail item = new CartDetail(product.getProductId(), quantity, color);

        if (buyNow) {
            // MUA NGAY: thêm giỏ → sang thẳng Checkout với đúng item này
            binding.btnBuyNow.setEnabled(false);
            cartRepo.addToCartReturningId(item, new CartRepository.IdCallback() {
                @Override
                public void onSuccess(String cartDetailId) {
                    binding.btnBuyNow.setEnabled(true);
                    ArrayList<String> ids = new ArrayList<>();
                    ids.add(cartDetailId);
                    CheckoutActivity.start(ProductDetailActivity.this, ids);
                }

                @Override
                public void onFailure(String error) {
                    binding.btnBuyNow.setEnabled(true);
                    Toast.makeText(ProductDetailActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Fly animation: lấy ImageView hiện tại từ ViewPager2
            ImageView fromImage = getCurrentPagerImageView();
            if (fromImage != null) {
                com.FinalProject.group3.utils.CartQuickActions.flyToCart(
                        this, fromImage, binding.btnCartHeader, () -> {
                            com.FinalProject.group3.utils.CartQuickActions.animateCartIcon(binding.btnCartHeader);
                            com.FinalProject.group3.utils.CartQuickActions.refreshBadge(binding.tvCartBadge);
                        });
            }
            cartRepo.addToCartReturningId(item, new CartRepository.IdCallback() {
                @Override
                public void onSuccess(String cartDetailId) {
                    getSharedPreferences("cart_prefs", MODE_PRIVATE)
                            .edit().putString("last_added_id", cartDetailId).apply();
                    // Nếu không fly được, bounce + badge tại đây
                    if (fromImage == null) {
                        com.FinalProject.group3.utils.CartQuickActions.animateCartIcon(binding.btnCartHeader);
                        com.FinalProject.group3.utils.CartQuickActions.refreshBadge(binding.tvCartBadge);
                    }
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(ProductDetailActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /** Lấy ImageView của trang hiện tại trong ViewPager2 để dùng làm nguồn fly animation. */
    private ImageView getCurrentPagerImageView() {
        try {
            androidx.recyclerview.widget.RecyclerView rv =
                    (androidx.recyclerview.widget.RecyclerView) binding.vpImages.getChildAt(0);
            if (rv == null) return null;
            View page = rv.getLayoutManager().findViewByPosition(binding.vpImages.getCurrentItem());
            return (page instanceof ImageView) ? (ImageView) page : null;
        } catch (Exception e) {
            return null;
        }
    }

    // ═══ Adapter ảnh lớn (ViewPager2) ═══
    private static class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.VH> {
        private final List<String> urls;
        ImagePagerAdapter(List<String> urls) { this.urls = urls; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView iv = new ImageView(parent.getContext());
            iv.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new VH(iv);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Glide.with(holder.itemView)
                    .load(urls.isEmpty() ? null : urls.get(position))
                    .placeholder(R.drawable.bg_product_placeholder)
                    .error(R.drawable.bg_product_placeholder)
                    .into((ImageView) holder.itemView);
        }

        @Override public int getItemCount() { return Math.max(urls.size(), 1); }

        class VH extends RecyclerView.ViewHolder {
            VH(@NonNull View v) { super(v); }
        }
    }

    // ═══ Adapter thumbnail ═══
    private static class ThumbAdapter extends RecyclerView.Adapter<ThumbAdapter.VH> {
        interface OnThumbClick { void onClick(int position); }

        private final List<String> urls;
        private final OnThumbClick listener;
        private int selected = 0;

        ThumbAdapter(List<String> urls, OnThumbClick listener) {
            this.urls = urls;
            this.listener = listener;
        }

        void setSelected(int pos) {
            int old = selected;
            selected = pos;
            notifyItemChanged(old);
            notifyItemChanged(selected);
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(ItemImageThumbBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Glide.with(holder.itemView)
                    .load(urls.get(position))
                    .placeholder(R.drawable.bg_product_placeholder)
                    .error(R.drawable.bg_product_placeholder)
                    .centerCrop()
                    .into(holder.b.ivThumb);
            holder.b.viewSelected.setVisibility(position == selected ? View.VISIBLE : View.GONE);
            holder.itemView.setOnClickListener(v -> listener.onClick(holder.getAdapterPosition()));
        }

        @Override public int getItemCount() { return urls.size(); }

        class VH extends RecyclerView.ViewHolder {
            final ItemImageThumbBinding b;
            VH(ItemImageThumbBinding b) { super(b.getRoot()); this.b = b; }
        }
    }
}

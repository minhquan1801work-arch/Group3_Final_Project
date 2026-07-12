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
import com.FinalProject.group3.repository.FavoriteRepository;
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

    private ActivityProductDetailBinding binding;
    private final ProductRepository productRepo = new ProductRepository();
    private final CartRepository cartRepo = new CartRepository();
    private final FavoriteRepository favoriteRepo = new FavoriteRepository();

    private Product product;
    private int quantity = 1;
    private int selectedVariantIndex = 0;
    // variantStartIndex[i] = vị trí ảnh đầu tiên của variant i trong flat list
    private int[] variantStartIndex;
    private ProductAdapter relatedAdapter;
    private boolean isFavorited = false;

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
            // Không CLEAR_TOP: giữ ProductDetail dưới stack để back từ giỏ quay lại đây
            Intent i = new Intent(this, com.FinalProject.group3.MainActivity.class);
            i.putExtra(com.FinalProject.group3.MainActivity.EXTRA_OPEN_CART, true);
            startActivity(i);
        });

        // Logo header → về thẳng Trang chủ
        binding.imgHeaderLogo.setOnClickListener(v -> {
            Intent i = new Intent(this, com.FinalProject.group3.MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            i.putExtra(com.FinalProject.group3.MainActivity.EXTRA_OPEN_HOME, true);
            startActivity(i);
        });

        setupQtyStepper();
        setupTabs();

        Glide.with(this)
                .load(com.FinalProject.group3.utils.CloudinaryUtil.optimize(
                        "https://res.cloudinary.com/aa1g9udv/image/upload/v1783354481/glassity/site/guide_diagram.png", 600))
                .into(binding.imgSizeDiagram);

        binding.btnAddToCart.setOnClickListener(v -> addToCart(false));
        binding.btnBuyNow.setOnClickListener(v -> addToCart(true));
        binding.btnAllReviews.setOnClickListener(v ->
                AllReviewsActivity.start(this, getIntent().getStringExtra(EXTRA_PRODUCT_ID)));

        binding.ivFavorite.setOnClickListener(v -> onFavoriteClick());

        // Thử kính ảo (AR try-on)
        binding.btnTryOn.setOnClickListener(v -> TryOnActivity.start(this));

        String productId = getIntent().getStringExtra(EXTRA_PRODUCT_ID);
        if (productId == null) { finish(); return; }
        loadProduct(productId);
        loadReviews(productId);
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
                checkFavoriteState(p.getProductId());
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
        updateStockDisplay();

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

    // ── Ảnh + màu: setup đồng thời để sync 2 chiều ────────────────────────────
    private void setupImages() {
        List<com.FinalProject.group3.model.ProductVariant> variants = product.getVariants();
        boolean hasVariants = variants != null && !variants.isEmpty();

        // Flatten tất cả ảnh, ghi nhớ startIndex của từng variant
        List<String> flatImages = new ArrayList<>();
        if (hasVariants) {
            variantStartIndex = new int[variants.size()];
            for (int i = 0; i < variants.size(); i++) {
                variantStartIndex[i] = flatImages.size();
                List<String> vImgs = variants.get(i).getImages();
                if (vImgs != null) flatImages.addAll(vImgs);
            }
        } else {
            variantStartIndex = new int[]{0};
            List<String> imgs = product.getImages();
            if (imgs != null) flatImages.addAll(imgs);
        }

        ImagePagerAdapter pagerAdapter = new ImagePagerAdapter(flatImages);
        binding.vpImages.setAdapter(pagerAdapter);

        ThumbAdapter thumbAdapter = new ThumbAdapter(flatImages,
                pos -> binding.vpImages.setCurrentItem(pos, true));
        binding.rvThumbs.setAdapter(thumbAdapter);
        binding.rvThumbs.setVisibility(flatImages.size() > 1 ? View.VISIBLE : View.GONE);

        binding.vpImages.registerOnPageChangeCallback(
                new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                    @Override public void onPageSelected(int position) {
                        thumbAdapter.setSelected(position);
                        // Swipe ảnh → tìm variant tương ứng → highlight dot
                        if (hasVariants) {
                            int newVariant = variantForImagePosition(position);
                            if (newVariant != selectedVariantIndex) {
                                selectedVariantIndex = newVariant;
                                refreshColorDots();
                                updateStockDisplay();
                            }
                        }
                    }
                });
    }

    private int variantForImagePosition(int imagePos) {
        int result = 0;
        for (int i = 0; i < variantStartIndex.length; i++) {
            if (imagePos >= variantStartIndex[i]) result = i;
        }
        return result;
    }

    // ── Chọn màu ─────────────────────────────────────────────────────────────
    private void setupColors() {
        binding.llColors.removeAllViews();

        List<com.FinalProject.group3.model.ProductVariant> variants = product.getVariants();
        boolean hasVariants = variants != null && !variants.isEmpty();

        // Fallback về colors cũ nếu chưa migrate sang variants
        List<String> colors = hasVariants ? null : product.getColors();

        if (!hasVariants && (colors == null || colors.isEmpty())) {
            binding.tvVariant.setText("Kiểu dáng: Kính mát");
            return;
        }

        int count = hasVariants ? variants.size() : colors.size();
        int size  = (int) (28 * getResources().getDisplayMetrics().density);
        int margin = (int) (10 * getResources().getDisplayMetrics().density);

        for (int i = 0; i < count; i++) {
            final int index = i;
            View dot = new View(this);
            android.widget.LinearLayout.LayoutParams lp =
                    new android.widget.LinearLayout.LayoutParams(size, size);
            lp.setMargins(0, 0, margin, 0);
            dot.setLayoutParams(lp);
            dot.setOnClickListener(v -> {
                selectedVariantIndex = index;
                refreshColorDots();
                updateStockDisplay();
                // Nhảy tới ảnh đầu tiên của variant vừa chọn
                if (variantStartIndex != null && index < variantStartIndex.length) {
                    binding.vpImages.setCurrentItem(variantStartIndex[index], true);
                }
            });
            binding.llColors.addView(dot);
        }
        refreshColorDots();
    }

    private void refreshColorDots() {
        List<com.FinalProject.group3.model.ProductVariant> variants = product.getVariants();
        boolean hasVariants = variants != null && !variants.isEmpty();
        List<String> colors = hasVariants ? null : product.getColors();

        int count = binding.llColors.getChildCount();
        for (int i = 0; i < count; i++) {
            View dot = binding.llColors.getChildAt(i);
            String hex = hasVariants ? variants.get(i).getColor() : colors.get(i);
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.OVAL);
            bg.setColor(parseColorSafe(hex));
            bg.setStroke(i == selectedVariantIndex ? 4 : 1,
                    i == selectedVariantIndex
                            ? getColor(R.color.color_price)
                            : getColor(R.color.color_field_border));
            dot.setBackground(bg);
        }

        String displayName;
        if (hasVariants) {
            com.FinalProject.group3.model.ProductVariant sel = variants.get(selectedVariantIndex);
            displayName = (sel.getColorName() != null && !sel.getColorName().isEmpty())
                    ? sel.getColorName() : colorName(sel.getColor());
        } else {
            String hex = (colors != null && !colors.isEmpty()) ? colors.get(selectedVariantIndex) : "";
            displayName = colorName(hex);
        }
        binding.tvVariant.setText("Kiểu dáng: Kính mát | Màu sắc: " + displayName);
    }

    private void updateStockDisplay() {
        List<com.FinalProject.group3.model.ProductVariant> variants = product.getVariants();
        int stock;
        if (variants != null && !variants.isEmpty()) {
            stock = variants.get(selectedVariantIndex).getStock();
        } else {
            stock = product.getStock();
        }
        binding.tvStock.setText(stock > 0 ? "Còn " + stock + " sản phẩm" : "Hết hàng");
    }

    private int currentVariantStock() {
        if (product == null) return 99;
        List<com.FinalProject.group3.model.ProductVariant> v = product.getVariants();
        if (v != null && !v.isEmpty()) return v.get(selectedVariantIndex).getStock();
        return product.getStock() > 0 ? product.getStock() : 99;
    }

    private String currentVariantColor() {
        if (product == null) return null;
        List<com.FinalProject.group3.model.ProductVariant> v = product.getVariants();
        if (v != null && !v.isEmpty()) return v.get(selectedVariantIndex).getColor();
        List<String> colors = product.getColors();
        return (colors != null && !colors.isEmpty()) ? colors.get(selectedVariantIndex) : null;
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
            int max = currentVariantStock();
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

    // ── Reviews thật từ collection "reviews" (Figma DL_Review) ────────────────
    private void loadReviews(String productId) {
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_REVIEWS)
                .whereEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(snap -> {
                    if (binding == null) return;
                    List<com.google.firebase.firestore.DocumentSnapshot> docs =
                            new ArrayList<>(snap.getDocuments());
                    // sort client-side theo createdAt desc (tránh cần composite index)
                    java.util.Collections.sort(docs, (a, b) -> {
                        com.google.firebase.Timestamp ta = a.getTimestamp("createdAt");
                        com.google.firebase.Timestamp tb = b.getTimestamp("createdAt");
                        if (ta == null || tb == null) return 0;
                        return tb.compareTo(ta);
                    });
                    bindReviews(docs);
                })
                .addOnFailureListener(e -> {
                    if (binding != null) bindReviews(new ArrayList<>());
                });
    }

    private void bindReviews(List<com.google.firebase.firestore.DocumentSnapshot> docs) {
        binding.llReviews.removeAllViews();
        binding.tvRatingCount.setText("Đánh giá sản phẩm (" + docs.size() + ")");

        if (docs.isEmpty()) {
            binding.tvRating.setText("—");
            android.widget.TextView empty = new android.widget.TextView(this);
            empty.setText("Chưa có đánh giá cho sản phẩm này");
            empty.setTextColor(getColor(R.color.color_text_secondary));
            empty.setTextSize(12);
            int pad = (int) (16 * getResources().getDisplayMetrics().density);
            empty.setPadding(pad, pad / 2, pad, pad);
            binding.llReviews.addView(empty);
            return;
        }

        double sum = 0;
        LayoutInflater inflater = LayoutInflater.from(this);
        for (com.google.firebase.firestore.DocumentSnapshot d : docs) {
            sum += com.FinalProject.group3.utils.ReviewViewBinder.ratingOf(d);
            ItemReviewBinding item = ItemReviewBinding.inflate(inflater, binding.llReviews, false);
            com.FinalProject.group3.utils.ReviewViewBinder.bind(this, item, d);
            binding.llReviews.addView(item.getRoot());
        }

        binding.tvRating.setText(String.format(new Locale("vi", "VN"), "%.1f", sum / docs.size()));
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

    // ── Yêu thích ─────────────────────────────────────────────────────────────

    private void checkFavoriteState(String productId) {
        if (FirebaseHelper.getCurrentUserId() == null) return;
        com.FinalProject.group3.utils.FirebaseHelper.getDb()
                .collection(com.FinalProject.group3.utils.FirebaseHelper.COL_FAVORITES)
                .whereEqualTo("customerId", FirebaseHelper.getCurrentUserId())
                .whereEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (binding == null) return;
                    isFavorited = !snapshot.isEmpty();
                    binding.ivFavorite.setImageResource(
                            isFavorited ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
                });
    }

    private void onFavoriteClick() {
        if (product == null) return;
        if (FirebaseHelper.getCurrentUserId() == null) {
            com.FinalProject.group3.utils.LoginRequiredDialog.show(
                    this, "Đăng nhập để lưu sản phẩm yêu thích");
            return;
        }
        favoriteRepo.toggleFavorite(product.getProductId(), new FavoriteRepository.ToggleCallback() {
            @Override
            public void onSuccess(boolean nowFavorite) {
                if (binding == null) return;
                isFavorited = nowFavorite;
                binding.ivFavorite.setImageResource(
                        nowFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
                Toast.makeText(ProductDetailActivity.this,
                        nowFavorite ? "Đã thêm vào yêu thích" : "Đã bỏ yêu thích",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ProductDetailActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
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
            String url = urls.isEmpty() ? null : urls.get(position);
            Glide.with(holder.itemView)
                    .load(com.FinalProject.group3.utils.CloudinaryUtil.optimize(url, 800))
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
                    .load(com.FinalProject.group3.utils.CloudinaryUtil.optimize(urls.get(position), 200))
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

package com.FinalProject.group3.ui.account;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalProject.group3.databinding.ActivityFavoriteBinding;
import com.FinalProject.group3.databinding.ItemFavoriteBinding;
import com.FinalProject.group3.model.Favorite;
import com.FinalProject.group3.model.Product;
import com.FinalProject.group3.model.ProductVariant;
import com.FinalProject.group3.repository.FavoriteRepository;
import com.FinalProject.group3.repository.ProductRepository;
import com.FinalProject.group3.ui.catalog.ProductDetailActivity;
import com.FinalProject.group3.utils.CartQuickActions;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * LA.Favor (Figma) — trang Sản phẩm yêu thích. [Person A]
 *
 * Mỗi dòng: ảnh, tên, sao, chấm màu, tim đỏ (bấm để bỏ yêu thích),
 * nút "Thêm vào giỏ hàng" + "Mua ngay" (dùng chung CartQuickActions).
 */
public class FavoriteActivity extends AppCompatActivity {

    private ActivityFavoriteBinding binding;
    private final FavoriteRepository favoriteRepo = new FavoriteRepository();
    private final ProductRepository productRepo = new ProductRepository();
    private final FavoriteAdapter adapter = new FavoriteAdapter();

    public static Intent intent(Context context) {
        return new Intent(context, FavoriteActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavoriteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        com.FinalProject.group3.utils.InsetsUtil.applySystemBarsPadding(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        binding.rvFavorites.setAdapter(adapter);

        // Logo header → về thẳng Trang chủ
        binding.imgLogo.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.FinalProject.group3.MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(com.FinalProject.group3.MainActivity.EXTRA_OPEN_HOME, true);
            startActivity(intent);
        });

        // Icon giỏ hàng ở header → mở tab Giỏ hàng
        binding.btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.FinalProject.group3.MainActivity.class);
            intent.putExtra(com.FinalProject.group3.MainActivity.EXTRA_OPEN_CART, true);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
        CartQuickActions.refreshBadge(binding.tvCartBadge);
    }

    private void loadFavorites() {
        binding.progressBar.setVisibility(View.VISIBLE);
        favoriteRepo.getFavorites(new FavoriteRepository.FavoriteListCallback() {
            @Override
            public void onSuccess(List<Favorite> favorites) {
                if (binding == null) return;
                if (favorites.isEmpty()) {
                    binding.progressBar.setVisibility(View.GONE);
                    showList(favorites);
                    return;
                }
                // Load product cho từng mục, xong hết mới hiển thị
                final int[] loaded = {0};
                for (Favorite f : favorites) {
                    productRepo.getProductById(f.getProductId(), new ProductRepository.ProductCallback() {
                        @Override public void onSuccess(Product product) {
                            f.setProduct(product);
                            if (++loaded[0] == favorites.size()) showList(favorites);
                        }
                        @Override public void onFailure(String error) {
                            if (++loaded[0] == favorites.size()) showList(favorites);
                        }
                    });
                }
            }

            @Override
            public void onFailure(String error) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(FavoriteActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showList(List<Favorite> favorites) {
        binding.progressBar.setVisibility(View.GONE);
        // Bỏ mục mà sản phẩm đã bị xóa khỏi Firestore
        List<Favorite> valid = new ArrayList<>();
        for (Favorite f : favorites) if (f.getProduct() != null) valid.add(f);

        binding.tvCount.setText(valid.size() + " Sản phẩm yêu thích");
        boolean empty = valid.isEmpty();
        binding.rvFavorites.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.llEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        adapter.setItems(valid);
    }

    // ═══ Adapter ═══
    private class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.VH> {

        private final List<Favorite> items = new ArrayList<>();

        void setItems(List<Favorite> list) {
            items.clear();
            items.addAll(list);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(ItemFavoriteBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Favorite f = items.get(position);
            Product p = f.getProduct();
            ItemFavoriteBinding b = holder.binding;

            b.tvName.setText(p.getName());
            b.tvRating.setText("4.5 ★"); // demo — chưa có rating trong Firestore

            // Ảnh: variant đầu tiên, fallback images cũ
            String imgUrl = null;
            if (p.getVariants() != null && !p.getVariants().isEmpty()) {
                ProductVariant v0 = p.getVariants().get(0);
                if (v0.getImages() != null && !v0.getImages().isEmpty())
                    imgUrl = v0.getImages().get(0);
            }
            if (imgUrl == null && p.getImages() != null && !p.getImages().isEmpty())
                imgUrl = p.getImages().get(0);
            Glide.with(b.ivProduct).load(com.FinalProject.group3.utils.CloudinaryUtil.optimize(imgUrl, 250))
                    .placeholder(com.FinalProject.group3.R.drawable.bg_product_placeholder)
                    .into(b.ivProduct);

            // Chấm màu (tối đa 4)
            b.llDots.removeAllViews();
            if (p.getVariants() != null) {
                int size = (int) (12 * getResources().getDisplayMetrics().density);
                int margin = (int) (4 * getResources().getDisplayMetrics().density);
                int count = Math.min(p.getVariants().size(), 4);
                for (int i = 0; i < count; i++) {
                    String hex = p.getVariants().get(i).getColor();
                    if (hex == null) continue;
                    View dot = new View(FavoriteActivity.this);
                    android.widget.LinearLayout.LayoutParams lp =
                            new android.widget.LinearLayout.LayoutParams(size, size);
                    lp.setMarginEnd(margin);
                    dot.setLayoutParams(lp);
                    GradientDrawable bg = new GradientDrawable();
                    bg.setShape(GradientDrawable.OVAL);
                    try { bg.setColor(Color.parseColor(hex)); }
                    catch (IllegalArgumentException e) { bg.setColor(Color.DKGRAY); }
                    dot.setBackground(bg);
                    b.llDots.addView(dot);
                }
            }

            // Cả dòng → mở chi tiết
            holder.itemView.setOnClickListener(v ->
                    ProductDetailActivity.start(FavoriteActivity.this, p.getProductId()));

            // Tim đỏ → bỏ yêu thích
            b.btnHeart.setOnClickListener(v ->
                    favoriteRepo.removeFavorite(f.getFavoriteId(), new FavoriteRepository.SimpleCallback() {
                        @Override public void onSuccess() {
                            Toast.makeText(FavoriteActivity.this,
                                    "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
                            loadFavorites();
                        }
                        @Override public void onFailure(String error) {
                            Toast.makeText(FavoriteActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    }));

            // 2 nút — tái dùng CartQuickActions (đã xử lý guest + last_added_id)
            b.btnAddToCart.setOnClickListener(v -> {
                // Ảnh sản phẩm bay lên icon giỏ + bounce + cập nhật badge
                CartQuickActions.flyToCart(FavoriteActivity.this, b.ivProduct,
                        binding.btnCart, () -> {
                            CartQuickActions.animateCartIcon(binding.btnCart);
                            CartQuickActions.refreshBadge(binding.tvCartBadge);
                        });
                CartQuickActions.addToCart(FavoriteActivity.this, p, () ->
                        Toast.makeText(FavoriteActivity.this,
                                "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show());
            });
            b.btnBuyNow.setOnClickListener(v ->
                    CartQuickActions.buyNow(FavoriteActivity.this, p));
        }

        @Override
        public int getItemCount() { return items.size(); }

        class VH extends RecyclerView.ViewHolder {
            final ItemFavoriteBinding binding;
            VH(ItemFavoriteBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}

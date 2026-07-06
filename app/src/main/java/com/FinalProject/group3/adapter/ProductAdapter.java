package com.FinalProject.group3.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalProject.group3.databinding.ItemProductBinding;
import com.FinalProject.group3.model.Product;
import com.FinalProject.group3.model.ProductVariant;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter luoi 2 cot san pham — dung cho ProductListActivity, HomeFragment, v.v.
 *
 * Hien thi: anh (placeholder den khi chua co URL), heart icon yeu thich,
 * ten, gia mau wine, color swatches toi da 3 cham, nut "Them vao gio" + "Mua ngay".
 *
 * Mau san pham luu dang hex string trong Firestore (vi du: ["#1A1614", "#AC9C8D"]).
 * Neu luu dang ten ("Den", "Nau") thi dot dung mau mac dinh brand_dark.
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public interface OnAddToCartListener {
        void onAddToCart(Product product);
    }

    public interface OnBuyNowListener {
        void onBuyNow(Product product);
    }

    private final List<Product> products = new ArrayList<>();
    private final OnProductClickListener clickListener;
    private OnAddToCartListener addToCartListener;
    private OnBuyNowListener buyNowListener;
    private static final NumberFormat VND_FORMAT = NumberFormat.getInstance(new Locale("vi", "VN"));

    public ProductAdapter(OnProductClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnAddToCartListener(OnAddToCartListener l) { this.addToCartListener = l; }
    public void setOnBuyNowListener(OnBuyNowListener l)       { this.buyNowListener = l; }

    public void submitList(List<Product> newList) {
        products.clear();
        products.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.binding.tvName.setText(product.getName());
        holder.binding.tvPrice.setText(VND_FORMAT.format(product.getPrice()) + "d");

        // Anh: uu tien variants[0].images[0], fallback ve images[0] cu
        String imageUrl = null;
        List<ProductVariant> variants = product.getVariants();
        if (variants != null && !variants.isEmpty()) {
            ProductVariant v0 = variants.get(0);
            if (v0.getImages() != null && !v0.getImages().isEmpty()) imageUrl = v0.getImages().get(0);
        }
        if (imageUrl == null && product.getImages() != null && !product.getImages().isEmpty()) {
            imageUrl = product.getImages().get(0);
        }
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(com.FinalProject.group3.R.drawable.bg_product_placeholder)
                .error(com.FinalProject.group3.R.drawable.bg_product_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()
                .into(holder.binding.ivProduct);

        // Color swatches — lay mau tu variants, fallback colors cu
        List<String> colors;
        if (variants != null && !variants.isEmpty()) {
            colors = new ArrayList<>();
            for (ProductVariant v : variants) {
                String c = v.getColor();
                if (c != null && !c.isEmpty()) colors.add(c);
            }
        } else {
            colors = product.getColors();
        }
        View[] dots = {holder.binding.dot1, holder.binding.dot2, holder.binding.dot3};
        for (int i = 0; i < dots.length; i++) {
            if (colors != null && i < colors.size()) {
                dots[i].setVisibility(View.VISIBLE);
                setDotColor(dots[i], colors.get(i));
            } else {
                dots[i].setVisibility(View.GONE);
            }
        }

        // Click toan card -> mo ProductDetail
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onProductClick(product);
        });

        // Nut "Them vao gio"
        holder.binding.btnAddToCart.setOnClickListener(v -> {
            if (addToCartListener != null) addToCartListener.onAddToCart(product);
            else Toast.makeText(v.getContext(), "Da them: " + product.getName(), Toast.LENGTH_SHORT).show();
        });

        // Nut "Mua ngay"
        holder.binding.btnBuyNow.setOnClickListener(v -> {
            if (buyNowListener != null) buyNowListener.onBuyNow(product);
            else Toast.makeText(v.getContext(), "Mua ngay: " + product.getName(), Toast.LENGTH_SHORT).show();
        });

        // Heart / favorite (UI toggle — logic Firestore lam o buoc tiep theo)
        holder.binding.ivFavorite.setOnClickListener(v ->
                Toast.makeText(v.getContext(), "Yeu thich: " + product.getName(), Toast.LENGTH_SHORT).show());
    }

    /** Dat mau cho dot: thu parse hex, neu khong duoc thi dung brand_dark */
    private void setDotColor(View dot, String colorStr) {
        try {
            int color = Color.parseColor(colorStr.startsWith("#") ? colorStr : "#" + colorStr);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(color);
            dot.setBackground(drawable);
        } catch (IllegalArgumentException e) {
            // Ten mau kieu "Den", "Nau" -> dung brand_dark
            dot.setBackgroundResource(com.FinalProject.group3.R.drawable.bg_circle);
        }
    }

    @Override
    public int getItemCount() { return products.size(); }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        final ItemProductBinding binding;
        ProductViewHolder(ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

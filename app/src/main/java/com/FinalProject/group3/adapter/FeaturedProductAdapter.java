package com.FinalProject.group3.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalProject.group3.databinding.ItemProductFeaturedBinding;
import com.FinalProject.group3.model.Product;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho hàng sản phẩm nằm ngang (horizontal) ở section
 * "SẢN PHẨM BÁN CHẠY" trong HomeFragment.
 * Dùng item_product_featured.xml (card 140dp cố định).
 */
public class FeaturedProductAdapter extends RecyclerView.Adapter<FeaturedProductAdapter.ViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    private final List<Product> products = new ArrayList<>();
    private final OnProductClickListener listener;
    private static final NumberFormat VND = NumberFormat.getInstance(new Locale("vi", "VN"));

    public FeaturedProductAdapter(OnProductClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Product> list) {
        products.clear();
        products.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductFeaturedBinding b = ItemProductFeaturedBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product p = products.get(position);
        holder.b.tvName.setText(p.getName());
        holder.b.tvPrice.setText(VND.format(p.getPrice()) + "đ");

        String url = (p.getImages() != null && !p.getImages().isEmpty()) ? p.getImages().get(0) : null;
        Glide.with(holder.itemView.getContext())
                .load(url)
                .placeholder(com.FinalProject.group3.R.drawable.bg_product_placeholder)
                .error(com.FinalProject.group3.R.drawable.bg_product_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()
                .into(holder.b.ivProduct);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProductClick(p);
        });
    }

    @Override
    public int getItemCount() { return products.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemProductFeaturedBinding b;
        ViewHolder(ItemProductFeaturedBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}

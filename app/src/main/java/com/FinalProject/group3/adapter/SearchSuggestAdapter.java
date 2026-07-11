package com.FinalProject.group3.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalProject.group3.databinding.ItemSearchSuggestBinding;
import com.FinalProject.group3.model.Product;
import com.FinalProject.group3.model.ProductVariant;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Card "Gợi ý tìm kiếm" trong SearchActivity — ảnh + tên đậm 1 dòng (theo Figma).
 */
public class SearchSuggestAdapter extends RecyclerView.Adapter<SearchSuggestAdapter.ViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    private final List<Product> products = new ArrayList<>();
    private final OnProductClickListener listener;

    public SearchSuggestAdapter(OnProductClickListener listener) {
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
        ItemSearchSuggestBinding b = ItemSearchSuggestBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product p = products.get(position);
        holder.b.tvName.setText(p.getName());

        String url = null;
        List<ProductVariant> variants = p.getVariants();
        if (variants != null && !variants.isEmpty()) {
            ProductVariant v0 = variants.get(0);
            if (v0.getImages() != null && !v0.getImages().isEmpty()) url = v0.getImages().get(0);
        }
        if (url == null && p.getImages() != null && !p.getImages().isEmpty()) url = p.getImages().get(0);

        Glide.with(holder.itemView.getContext())
                .load(com.FinalProject.group3.utils.CloudinaryUtil.optimize(url, 400))
                .placeholder(com.FinalProject.group3.R.drawable.bg_product_placeholder)
                .error(com.FinalProject.group3.R.drawable.bg_product_placeholder)
                .centerCrop()
                .into(holder.b.ivProduct);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProductClick(p);
        });
    }

    @Override
    public int getItemCount() { return products.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemSearchSuggestBinding b;
        ViewHolder(ItemSearchSuggestBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}

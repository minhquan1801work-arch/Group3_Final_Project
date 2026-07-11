package com.FinalProject.group3.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalProject.group3.databinding.ItemCollectionProductBinding;
import com.FinalProject.group3.model.Product;
import com.FinalProject.group3.model.ProductVariant;
import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Card sản phẩm nhỏ trong trang chi tiết Bộ sưu tập ("Hàng mới về") — theo Figma:
 * ảnh vuông nhỏ, tên in đậm, dòng phụ (giá).
 */
public class CollectionProductAdapter extends RecyclerView.Adapter<CollectionProductAdapter.ViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    private final List<Product> products = new ArrayList<>();
    private final OnProductClickListener listener;
    private static final NumberFormat VND = NumberFormat.getInstance(new Locale("vi", "VN"));

    public CollectionProductAdapter(OnProductClickListener listener) {
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
        ItemCollectionProductBinding b = ItemCollectionProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product p = products.get(position);
        holder.b.tvName.setText(p.getName());
        holder.b.tvSub.setText(VND.format(p.getPrice()) + "đ");

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
        final ItemCollectionProductBinding b;
        ViewHolder(ItemCollectionProductBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}

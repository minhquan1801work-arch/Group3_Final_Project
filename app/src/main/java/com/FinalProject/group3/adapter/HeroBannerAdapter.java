package com.FinalProject.group3.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalProject.group3.R;
import com.FinalProject.group3.databinding.ItemHeroBannerBinding;
import com.FinalProject.group3.utils.CloudinaryUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

/**
 * Hero carousel trang chủ — mỗi slide: ảnh + label "XEM NGAY" gạch chân (Figma).
 * Bấm slide → callback vị trí, HomeFragment map sang product ID tương ứng.
 */
public class HeroBannerAdapter extends RecyclerView.Adapter<HeroBannerAdapter.VH> {

    public interface OnBannerClickListener {
        void onClick(int position);
    }

    private final List<String> imageUrls;
    private OnBannerClickListener listener;

    public HeroBannerAdapter(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public void setOnBannerClickListener(OnBannerClickListener l) {
        this.listener = l;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHeroBannerBinding b = ItemHeroBannerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        b.getRoot().setLayoutParams(new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.MATCH_PARENT));
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        String url = imageUrls.get(position);
        if (url != null && !url.isEmpty()) {
            Glide.with(holder.b.ivBanner.getContext())
                    .load(url)
                    .thumbnail(Glide.with(holder.b.ivBanner.getContext())
                            .load(CloudinaryUtil.blurPlaceholder(url))
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                    .placeholder(R.color.brand_dark)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .centerCrop()
                    .into(holder.b.ivBanner);
        } else {
            holder.b.ivBanner.setBackgroundColor(
                    holder.b.ivBanner.getContext().getColor(R.color.brand_dark));
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(holder.getBindingAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemHeroBannerBinding b;
        VH(ItemHeroBannerBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}

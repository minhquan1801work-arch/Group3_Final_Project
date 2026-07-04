package com.FinalProject.group3.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.FinalProject.group3.R;

import java.util.List;

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
        ImageView iv = new ImageView(parent.getContext());
        iv.setLayoutParams(new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.MATCH_PARENT));
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new VH(iv);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        String url = imageUrls.get(position);
        if (url != null && !url.isEmpty()) {
            Glide.with(holder.iv.getContext())
                    .load(url)
                    .placeholder(R.color.brand_dark)
                    .centerCrop()
                    .into(holder.iv);
        } else {
            holder.iv.setBackgroundColor(
                    holder.iv.getContext().getColor(R.color.brand_dark));
        }
        holder.iv.setOnClickListener(v -> {
            if (listener != null) listener.onClick(holder.getBindingAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView iv;
        VH(ImageView iv) {
            super(iv);
            this.iv = iv;
        }
    }
}

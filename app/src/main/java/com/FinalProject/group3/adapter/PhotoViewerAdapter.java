package com.FinalProject.group3.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalProject.group3.databinding.ItemPhotoViewerBinding;
import com.FinalProject.group3.utils.CloudinaryUtil;
import com.bumptech.glide.Glide;

import java.util.List;

/** 1 ảnh review full-size/trang, dùng trong ViewPager2 của PhotoViewerActivity. */
public class PhotoViewerAdapter extends RecyclerView.Adapter<PhotoViewerAdapter.VH> {

    private final List<String> urls;

    public PhotoViewerAdapter(List<String> urls) {
        this.urls = urls;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemPhotoViewerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Glide.with(holder.itemView)
                .load(CloudinaryUtil.optimize(urls.get(position), 1080))
                .into(holder.b.photoView);
    }

    @Override
    public int getItemCount() {
        return urls.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemPhotoViewerBinding b;
        VH(ItemPhotoViewerBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}

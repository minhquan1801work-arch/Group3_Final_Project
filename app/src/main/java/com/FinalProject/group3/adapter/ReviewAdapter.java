package com.FinalProject.group3.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalProject.group3.databinding.ItemReviewBinding;
import com.FinalProject.group3.utils.ReviewViewBinder;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/** Danh sách review đầy đủ (LA.Review) — dùng item_review.xml, gán qua ReviewViewBinder. */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.VH> {

    private final List<DocumentSnapshot> reviews = new ArrayList<>();

    public void submitList(List<DocumentSnapshot> docs) {
        reviews.clear();
        reviews.addAll(docs);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemReviewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ReviewViewBinder.bind(holder.itemView.getContext(), holder.b, reviews.get(position));
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemReviewBinding b;
        VH(ItemReviewBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}

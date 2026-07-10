package com.FinalProject.group3.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalProject.group3.databinding.ItemNotificationBinding;
import com.FinalProject.group3.model.Notification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Adapter list thông báo — chấm đỏ hiện khi status = UNREAD, bấm → mark READ. */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    private OnNotificationClickListener clickListener;

    public void setOnNotificationClickListener(OnNotificationClickListener l) {
        this.clickListener = l;
    }

    private final List<Notification> items = new ArrayList<>();
    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());

    public void setItems(List<Notification> notifications) {
        items.clear();
        items.addAll(notifications);
        notifyDataSetChanged();
    }

    public Notification getItemAt(int position) {
        return (position >= 0 && position < items.size()) ? items.get(position) : null;
    }

    public void removeAt(int position) {
        if (position < 0 || position >= items.size()) return;
        items.remove(position);
        notifyItemRemoved(position);
    }

    public boolean isEmpty() { return items.isEmpty(); }

    public List<Notification> getUnreadItems() {
        List<Notification> unread = new ArrayList<>();
        for (Notification n : items) {
            if ("UNREAD".equals(n.getStatus())) unread.add(n);
        }
        return unread;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification n = items.get(position);
        holder.binding.tvMessage.setText(n.getMessage());
        holder.binding.tvTime.setText(n.getCreatedAt() != null
                ? timeFormat.format(n.getCreatedAt()) : "");
        boolean unread = "UNREAD".equals(n.getStatus());
        holder.binding.dotUnread.setVisibility(unread ? View.VISIBLE : View.INVISIBLE);
        // Chưa đọc → nền xám rất nhạt, đọc rồi → nền trắng
        holder.itemView.setBackgroundColor(unread ? 0xFFF3F3F3 : 0xFFFFFFFF);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onNotificationClick(n);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemNotificationBinding binding;

        ViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

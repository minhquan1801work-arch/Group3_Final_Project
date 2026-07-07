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
        holder.binding.dotUnread.setVisibility(
                "UNREAD".equals(n.getStatus()) ? View.VISIBLE : View.INVISIBLE);

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

package com.FinalProject.group3.ui.account;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalProject.group3.R;
import com.FinalProject.group3.adapter.NotificationAdapter;
import com.FinalProject.group3.databinding.FragmentNotificationBinding;
import com.FinalProject.group3.model.Notification;
import com.FinalProject.group3.ui.order.OrderHistoryActivity;
import com.FinalProject.group3.utils.FirebaseHelper;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Tab Thông báo.
 *
 * Điều hướng khi bấm:
 *   ORDER     → OrderHistoryActivity (xem lịch sử đơn hàng)
 *   PROMOTION → VoucherActivity      (dùng mã giảm giá)
 *   SYSTEM    → không điều hướng     (chỉ mark đọc)
 */
public class NotificationFragment extends Fragment {

    private FragmentNotificationBinding binding;
    private final NotificationAdapter adapter = new NotificationAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentNotificationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvNotifications.setAdapter(adapter);
        new ItemTouchHelper(new SwipeToDeleteCallback())
                .attachToRecyclerView(binding.rvNotifications);

        // Bấm vào → mark READ + điều hướng theo type
        adapter.setOnNotificationClickListener(n -> {
            markRead(n);
            navigateTo(n);
        });

        // Đọc tất cả
        binding.btnMarkAllRead.setOnClickListener(v -> markAllRead());

        String uid = FirebaseHelper.getCurrentUserId();
        if (uid == null) {
            showEmpty(true);
            binding.btnMarkAllRead.setVisibility(View.GONE);
            binding.getRoot().post(() -> {
                if (!isAdded() || getActivity() == null) return;
                com.FinalProject.group3.utils.LoginRequiredDialog.show(
                        requireActivity(),
                        "Đăng nhập để xem thông báo từ Glassity",
                        () -> {
                            if (isAdded()) {
                                androidx.navigation.fragment.NavHostFragment
                                        .findNavController(NotificationFragment.this)
                                        .navigate(R.id.homeFragment);
                            }
                        });
            });
            return;
        }

        loadNotifications(uid);
    }

    private void loadNotifications(String uid) {
        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_NOTIFICATIONS)
                .whereEqualTo("customerId", uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (binding == null) return;
                    List<Notification> all = snapshot.toObjects(Notification.class);
                    // Lọc bỏ các thông báo cũ ghi sai field (message null/rỗng)
                    List<Notification> valid = new ArrayList<>();
                    for (Notification n : all) {
                        if (n.getMessage() != null && !n.getMessage().isEmpty()) valid.add(n);
                    }
                    valid.sort(Comparator.comparing(Notification::getCreatedAt,
                            Comparator.nullsLast(Comparator.reverseOrder())));
                    adapter.setItems(valid);
                    showEmpty(valid.isEmpty());
                })
                .addOnFailureListener(e -> {
                    if (binding != null) showEmpty(true);
                });
    }

    // ── Mark read ──────────────────────────────────────────────────────────────

    private void markRead(Notification n) {
        if ("READ".equals(n.getStatus())) return;
        n.setStatus("READ");
        adapter.notifyDataSetChanged();
        if (n.getNotificationId() != null) {
            FirebaseHelper.getDb()
                    .collection(FirebaseHelper.COL_NOTIFICATIONS)
                    .document(n.getNotificationId())
                    .update("status", "READ");
        }
    }

    private void markAllRead() {
        List<Notification> unread = adapter.getUnreadItems();
        if (unread.isEmpty()) return;
        // Batch update Firestore
        WriteBatch batch = FirebaseHelper.getDb().batch();
        for (Notification n : unread) {
            if (n.getNotificationId() == null) continue;
            batch.update(
                    FirebaseHelper.getDb()
                            .collection(FirebaseHelper.COL_NOTIFICATIONS)
                            .document(n.getNotificationId()),
                    "status", "READ");
            n.setStatus("READ");
        }
        batch.commit();
        adapter.notifyDataSetChanged();
    }

    // ── Điều hướng theo type ───────────────────────────────────────────────────

    private void navigateTo(Notification n) {
        if (!isAdded()) return;
        String type = n.getType();
        if (type == null) return;
        switch (type) {
            case "ORDER":
                if (n.getOrderId() != null && !n.getOrderId().isEmpty()) {
                    startActivity(com.FinalProject.group3.ui.order.OrderDetailActivity
                            .intent(requireContext(), n.getOrderId()));
                } else {
                    startActivity(OrderHistoryActivity.intentWithTab(requireContext(), 2));
                }
                break;
            case "PROMOTION":
                startActivity(VoucherActivity.intent(requireContext()));
                break;
            // SYSTEM → không điều hướng, chỉ đọc
        }
    }

    // ── Swipe-to-delete ────────────────────────────────────────────────────────

    private class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

        private final Paint paint = new Paint();

        SwipeToDeleteCallback() {
            super(0, ItemTouchHelper.LEFT);
            paint.setColor(Color.parseColor("#D32F2F"));
            paint.setAntiAlias(true);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                              @NonNull RecyclerView.ViewHolder target) { return false; }

        @Override
        public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
            return 0.3f;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int pos = viewHolder.getAdapterPosition();
            Notification n = adapter.getItemAt(pos);
            if (n == null) return;
            adapter.removeAt(pos);
            if (n.getNotificationId() != null) {
                FirebaseHelper.getDb()
                        .collection(FirebaseHelper.COL_NOTIFICATIONS)
                        .document(n.getNotificationId())
                        .delete();
            }
            if (adapter.isEmpty()) showEmpty(true);
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView rv,
                                @NonNull RecyclerView.ViewHolder vh,
                                float dX, float dY, int actionState, boolean isActive) {
            View item = vh.itemView;
            if (dX < 0) {
                RectF bg = new RectF(item.getRight() + dX, item.getTop(),
                        item.getRight(), item.getBottom());
                c.drawRect(bg, paint);

                Paint textPaint = new Paint();
                textPaint.setColor(Color.WHITE);
                textPaint.setTextSize(36f);
                textPaint.setAntiAlias(true);
                textPaint.setTextAlign(Paint.Align.CENTER);
                float cx = item.getRight() + dX / 2f;
                float cy = (item.getTop() + item.getBottom()) / 2f
                        - (textPaint.descent() + textPaint.ascent()) / 2f;
                c.drawText("Xóa", cx, cy, textPaint);
            }
            super.onChildDraw(c, rv, vh, dX, dY, actionState, isActive);
        }
    }

    private void showEmpty(boolean empty) {
        binding.tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.rvNotifications.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.btnMarkAllRead.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

package com.FinalProject.group3.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.FinalProject.group3.R;
import com.FinalProject.group3.adapter.NotificationAdapter;
import com.FinalProject.group3.databinding.FragmentNotificationBinding;
import com.FinalProject.group3.model.Notification;
import com.FinalProject.group3.utils.FirebaseHelper;

import java.util.Comparator;
import java.util.List;

/** Tab Thông báo — đọc collection notifications của user đang đăng nhập. */
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

        // Bấm thông báo UNREAD → mark READ trên Firestore + ẩn chấm đỏ ngay
        adapter.setOnNotificationClickListener(n -> {
            if (!"UNREAD".equals(n.getStatus())) return;
            n.setStatus("READ");
            adapter.notifyDataSetChanged();
            FirebaseHelper.getDb()
                    .collection(FirebaseHelper.COL_NOTIFICATIONS)
                    .document(n.getNotificationId())
                    .update("status", "READ");
        });

        String uid = FirebaseHelper.getCurrentUserId();
        if (uid == null) {
            showEmpty(true);
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

        // Không dùng orderBy trên query (tránh phải tạo composite index) —
        // sắp xếp mới nhất lên đầu ở client
        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_NOTIFICATIONS)
                .whereEqualTo("customerId", uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (binding == null) return;
                    List<Notification> list = snapshot.toObjects(Notification.class);
                    list.sort(Comparator.comparing(Notification::getCreatedAt,
                            Comparator.nullsLast(Comparator.reverseOrder())));
                    adapter.setItems(list);
                    showEmpty(list.isEmpty());
                })
                .addOnFailureListener(e -> {
                    if (binding != null) showEmpty(true);
                });
    }

    private void showEmpty(boolean empty) {
        binding.tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.rvNotifications.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

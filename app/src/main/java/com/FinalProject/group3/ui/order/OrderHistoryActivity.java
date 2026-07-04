package com.FinalProject.group3.ui.order;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalProject.group3.R;
import com.FinalProject.group3.databinding.ActivityOrderHistoryBinding;
import com.FinalProject.group3.databinding.ItemOrderBinding;
import com.FinalProject.group3.model.Order;
import com.FinalProject.group3.repository.OrderRepository;
import com.FinalProject.group3.utils.InsetsUtil;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class OrderHistoryActivity extends AppCompatActivity {

    public static Intent intent(Context context) {
        return new Intent(context, OrderHistoryActivity.class);
    }

    private ActivityOrderHistoryBinding binding;
    private OrderAdapter adapter;
    private final List<Order> allOrders = new ArrayList<>();
    private int currentTab = 0;

    private static final String[] TABS = {"Tất cả", "Chờ giao", "Đã giao", "Đã hủy"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applySystemBarsPadding(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        setupTabs();
        setupRecyclerView();
        loadOrders();
    }

    private void setupTabs() {
        for (int i = 0; i < TABS.length; i++) {
            final int index = i;
            TextView tab = (TextView) getLayoutInflater()
                    .inflate(R.layout.item_tab_chip, binding.tabLayout, false);
            tab.setText(TABS[i]);
            tab.setOnClickListener(v -> selectTab(index));
            binding.tabLayout.addView(tab);
        }
        selectTab(0);
    }

    private void selectTab(int index) {
        currentTab = index;
        for (int i = 0; i < binding.tabLayout.getChildCount(); i++) {
            View child = binding.tabLayout.getChildAt(i);
            child.setSelected(i == index);
        }
        filterOrders();
    }

    private void setupRecyclerView() {
        adapter = new OrderAdapter(new ArrayList<>());
        binding.rvOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.rvOrders.setAdapter(adapter);
    }

    private void loadOrders() {
        binding.progressBar.setVisibility(View.VISIBLE);
        new OrderRepository().getMyOrders(new OrderRepository.OrderListCallback() {
            @Override
            public void onSuccess(List<Order> orders) {
                binding.progressBar.setVisibility(View.GONE);
                allOrders.clear();
                allOrders.addAll(orders);
                filterOrders();
            }

            @Override
            public void onFailure(String error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(OrderHistoryActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterOrders() {
        List<Order> filtered = new ArrayList<>();
        for (Order o : allOrders) {
            if (matchesTab(o, currentTab)) filtered.add(o);
        }
        adapter.setData(filtered);
        binding.layoutEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        binding.rvOrders.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private boolean matchesTab(Order o, int tab) {
        if (tab == 0) return true;
        String status = o.getOrderStatus();
        if (tab == 1) return Arrays.asList("PENDING", "PROCESSING", "SHIPPED").contains(status);
        if (tab == 2) return "DELIVERED".equals(status);
        return "CANCELLED".equals(status);
    }

    // ── Adapter ──────────────────────────────────────────────────────────────

    class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.VH> {

        private List<Order> data;

        OrderAdapter(List<Order> data) { this.data = data; }

        void setData(List<Order> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemOrderBinding b = ItemOrderBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new VH(b);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            holder.bind(data.get(position));
        }

        @Override
        public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            final ItemOrderBinding b;

            VH(ItemOrderBinding b) {
                super(b.getRoot());
                this.b = b;
            }

            void bind(Order o) {
                b.tvOrderCode.setText(o.getOrderCode() != null ? o.getOrderCode() : o.getOrderId());
                if (o.getCreatedAt() != null) {
                    b.tvDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.US)
                            .format(o.getCreatedAt()));
                }
                b.tvStatus.setText(statusLabel(o.getOrderStatus()));
                b.tvStatus.setBackgroundResource(statusBackground(o.getOrderStatus()));

                NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
                b.tvTotal.setText(fmt.format(o.getTotalAmount()) + "đ");

                // Nút hành động
                boolean isDelivered = "DELIVERED".equals(o.getOrderStatus());
                boolean isCancellable = Arrays.asList("PENDING", "PROCESSING")
                        .contains(o.getOrderStatus());
                b.btnReview.setVisibility(isDelivered ? View.VISIBLE : View.GONE);
                b.btnReorder.setVisibility(isDelivered ? View.VISIBLE : View.GONE);

                b.btnDetail.setOnClickListener(v ->
                        startActivity(OrderDetailActivity.intent(
                                OrderHistoryActivity.this, o.getOrderId())));
                b.btnReview.setOnClickListener(v ->
                        Toast.makeText(OrderHistoryActivity.this,
                                "Vui lòng chọn sản phẩm để đánh giá",
                                Toast.LENGTH_SHORT).show());
                b.btnReorder.setOnClickListener(v ->
                        Toast.makeText(OrderHistoryActivity.this,
                                "Tính năng đặt lại đơn sắp ra mắt",
                                Toast.LENGTH_SHORT).show());
            }

            private String statusLabel(String s) {
                if (s == null) return "";
                switch (s) {
                    case "PENDING": return "Chờ xác nhận";
                    case "PROCESSING": return "Đang xử lý";
                    case "SHIPPED": return "Đang giao";
                    case "DELIVERED": return "Đã giao";
                    case "CANCELLED": return "Đã hủy";
                    default: return s;
                }
            }

            private int statusBackground(String s) {
                if ("DELIVERED".equals(s)) return R.drawable.bg_status_delivered;
                if ("CANCELLED".equals(s)) return R.drawable.bg_status_cancelled;
                return R.drawable.bg_status_pending;
            }
        }
    }
}

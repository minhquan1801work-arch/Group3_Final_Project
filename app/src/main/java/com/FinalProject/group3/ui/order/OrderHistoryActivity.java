package com.FinalProject.group3.ui.order;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import com.FinalProject.group3.model.OrderDetail;
import com.FinalProject.group3.model.Product;
import com.FinalProject.group3.repository.OrderRepository;
import com.FinalProject.group3.repository.ProductRepository;
import com.FinalProject.group3.utils.FirebaseHelper;
import com.FinalProject.group3.utils.InsetsUtil;
import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class OrderHistoryActivity extends AppCompatActivity {

    // Có thể truyền vào tab mặc định khi mở từ PaymentResult
    private static final String EXTRA_TAB = "tab";

    public static Intent intent(Context context) {
        return new Intent(context, OrderHistoryActivity.class);
    }

    public static Intent intentWithTab(Context context, int tab) {
        return new Intent(context, OrderHistoryActivity.class).putExtra(EXTRA_TAB, tab);
    }

    private ActivityOrderHistoryBinding binding;
    private OrderAdapter adapter;
    private final List<Order> allOrders = new ArrayList<>();
    private int currentTab = 0;

    private static final String[] TABS = {"Tất cả", "Chờ giao hàng", "Đã giao", "Đã hủy"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applySystemBarsPadding(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        int startTab = getIntent().getIntExtra(EXTRA_TAB, 0);
        setupRecyclerView();   // phải trước setupTabs vì selectTab() gọi filterOrders() → adapter
        setupTabs(startTab);
        loadOrders();
    }

    private void setupTabs(int startTab) {
        for (int i = 0; i < TABS.length; i++) {
            final int index = i;
            TextView tab = (TextView) LayoutInflater.from(this)
                    .inflate(R.layout.item_tab_chip, binding.tabContainer, false);
            tab.setText(TABS[i]);
            tab.setOnClickListener(v -> selectTab(index));
            binding.tabContainer.addView(tab);
        }
        selectTab(startTab);
    }

    private void selectTab(int index) {
        currentTab = index;
        for (int i = 0; i < binding.tabContainer.getChildCount(); i++) {
            binding.tabContainer.getChildAt(i).setSelected(i == index);
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
        String s = o.getOrderStatus();
        if (tab == 1) return Arrays.asList("PENDING", "PROCESSING", "SHIPPED").contains(s);
        if (tab == 2) return "DELIVERED".equals(s);
        return "CANCELLED".equals(s);
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
                String status = o.getOrderStatus();
                b.tvStatus.setText(statusLabel(status));
                b.tvStatus.setTextColor(statusColor(status));

                NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
                b.tvTotal.setText(fmt.format(o.getTotalAmount()) + "đ");

                // Ẩn hết button trước, rồi show theo trạng thái
                b.btnDetail.setVisibility(View.VISIBLE);
                b.btnReview.setVisibility(View.GONE);
                b.btnReorder.setVisibility(View.GONE);

                if ("DELIVERED".equals(status)) {
                    b.btnDetail.setVisibility(View.GONE);
                    b.btnReorder.setVisibility(View.VISIBLE);
                    b.btnReview.setVisibility(View.VISIBLE);
                    if (o.isReviewed()) {
                        b.btnReview.setText("Đã đánh giá");
                        b.btnReview.setEnabled(false);
                        b.btnReview.setAlpha(0.5f);
                    } else {
                        b.btnReview.setText("Đánh giá");
                        b.btnReview.setEnabled(true);
                        b.btnReview.setAlpha(1f);
                    }
                } else if ("CANCELLED".equals(status)) {
                    b.btnReorder.setVisibility(View.VISIBLE);
                    b.btnDetail.setText("Chi tiết");
                } else {
                    b.btnDetail.setText("Xem chi tiết");
                }

                b.btnDetail.setOnClickListener(v ->
                        startActivity(OrderDetailActivity.intent(
                                OrderHistoryActivity.this, o.getOrderId())));
                b.btnReorder.setOnClickListener(v ->
                        Toast.makeText(OrderHistoryActivity.this,
                                "Tính năng Mua lại sắp ra mắt", Toast.LENGTH_SHORT).show());
                b.btnReview.setOnClickListener(v ->
                        startActivity(ReviewActivity.intent(
                                OrderHistoryActivity.this, o.getOrderId())));

                // Load sản phẩm trong đơn (lazy, per item)
                loadFirstProduct(o.getOrderId(), b);
            }

            private void loadFirstProduct(String orderId, ItemOrderBinding b) {
                b.tvProductCount.setText("...");
                b.tvProductName.setText("");
                b.tvProductColor.setText("");
                b.tvQty.setText("");
                b.tvMoreItems.setVisibility(View.GONE);

                FirebaseHelper.getDb()
                        .collection(FirebaseHelper.COL_ORDERS)
                        .document(orderId)
                        .collection(FirebaseHelper.COL_ORDER_DETAILS)
                        .get()
                        .addOnSuccessListener(snapshot -> {
                            List<OrderDetail> details = snapshot.toObjects(OrderDetail.class);
                            int count = details.size();
                            b.tvProductCount.setText(count + " sản phẩm");

                            if (details.isEmpty()) return;

                            OrderDetail first = details.get(0);
                            b.tvProductColor.setText(first.getColor() != null ? first.getColor() : "");
                            b.tvQty.setText("x" + first.getQuantity());

                            if (count > 1) {
                                b.tvMoreItems.setVisibility(View.VISIBLE);
                                b.tvMoreItems.setText("Xem thêm " + (count - 1) + " sản phẩm");
                            }

                            // Load tên + ảnh sản phẩm đầu tiên
                            new ProductRepository().getProductById(first.getProductId(),
                                    new ProductRepository.ProductCallback() {
                                        @Override
                                        public void onSuccess(Product product) {
                                            b.tvProductName.setText(product.getName());
                                            List<String> imgs = product.getImages();
                                            if (imgs != null && !imgs.isEmpty()) {
                                                Glide.with(b.ivProduct.getContext())
                                                        .load(imgs.get(0))
                                                        .centerCrop()
                                                        .into(b.ivProduct);
                                            }
                                        }

                                        @Override
                                        public void onFailure(String error) {
                                            b.tvProductName.setText("Sản phẩm");
                                        }
                                    });
                        });
            }

            private String statusLabel(String s) {
                if (s == null) return "";
                switch (s) {
                    case "PENDING":    return "Chờ xác nhận";
                    case "PROCESSING": return "Đang xử lý";
                    case "SHIPPED":    return "Đang giao";
                    case "DELIVERED":  return "Hoàn thành";
                    case "CANCELLED":  return "Đã hủy";
                    default:           return s;
                }
            }

            private int statusColor(String s) {
                if ("DELIVERED".equals(s))  return Color.parseColor("#2E7D32"); // xanh
                if ("CANCELLED".equals(s))  return Color.parseColor("#B3261E"); // đỏ
                return Color.parseColor("#E65100"); // cam — PENDING/PROCESSING/SHIPPED
            }
        }
    }
}

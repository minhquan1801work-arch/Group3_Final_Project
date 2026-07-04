package com.FinalProject.group3.ui.account;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
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
import com.FinalProject.group3.databinding.ActivityPointHistoryBinding;
import com.FinalProject.group3.databinding.ItemPointHistoryBinding;
import com.FinalProject.group3.model.Order;
import com.FinalProject.group3.repository.OrderRepository;
import com.FinalProject.group3.utils.InsetsUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PointHistoryActivity extends AppCompatActivity {

    public static Intent intent(Context context) {
        return new Intent(context, PointHistoryActivity.class);
    }

    private ActivityPointHistoryBinding binding;
    private PointAdapter adapter;
    private final List<Order> deliveredOrders = new ArrayList<>();
    private int currentMonth = -1; // -1 = tất cả

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPointHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applySystemBarsPadding(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        adapter = new PointAdapter(new ArrayList<>());
        binding.rvPoints.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPoints.setAdapter(adapter);

        loadOrders();
    }

    private void loadOrders() {
        binding.progressBar.setVisibility(View.VISIBLE);
        new OrderRepository().getMyOrders(new OrderRepository.OrderListCallback() {
            @Override
            public void onSuccess(List<Order> orders) {
                binding.progressBar.setVisibility(View.GONE);
                deliveredOrders.clear();
                for (Order o : orders) {
                    if ("DELIVERED".equals(o.getOrderStatus())) deliveredOrders.add(o);
                }
                buildTabs();
                filterAndShow(-1);
            }

            @Override
            public void onFailure(String error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(PointHistoryActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buildTabs() {
        binding.tabContainer.removeAllViews();

        // Tab "Tất cả"
        addTab("Tất cả", -1);

        // Các tháng xuất hiện trong dữ liệu
        List<String> months = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy", Locale.US);
        for (Order o : deliveredOrders) {
            if (o.getCreatedAt() != null) {
                String m = sdf.format(o.getCreatedAt());
                if (!months.contains(m)) months.add(m);
            }
        }
        for (int i = 0; i < months.size(); i++) {
            String label = "Tháng " + months.get(i).split("/")[0];
            final int month = Integer.parseInt(months.get(i).split("/")[0]);
            addTab(label, month);
        }
    }

    private void addTab(String label, int month) {
        TextView tab = (TextView) LayoutInflater.from(this)
                .inflate(R.layout.item_tab_chip, binding.tabContainer, false);
        tab.setText(label);
        tab.setOnClickListener(v -> filterAndShow(month));
        binding.tabContainer.addView(tab);
    }

    private void filterAndShow(int month) {
        currentMonth = month;
        List<Order> filtered = new ArrayList<>();
        for (Order o : deliveredOrders) {
            if (month == -1) {
                filtered.add(o);
            } else if (o.getCreatedAt() != null) {
                Calendar c = Calendar.getInstance();
                c.setTime(o.getCreatedAt());
                if (c.get(Calendar.MONTH) + 1 == month) filtered.add(o);
            }
        }

        // Tổng điểm
        int total = 0;
        for (Order o : filtered) total += (int) (o.getTotalAmount() / 1000);
        binding.tvTotalPoints.setText("Tổng điểm hiện có: " + total + " điểm");

        adapter.setData(filtered);
        binding.tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        binding.rvPoints.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }

    // ── Adapter ──────────────────────────────────────────────────────────────

    static class PointAdapter extends RecyclerView.Adapter<PointAdapter.VH> {

        private List<Order> data;

        PointAdapter(List<Order> data) { this.data = data; }

        void setData(List<Order> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemPointHistoryBinding b = ItemPointHistoryBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new VH(b);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            holder.bind(data.get(position));
        }

        @Override
        public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            final ItemPointHistoryBinding b;

            VH(ItemPointHistoryBinding b) {
                super(b.getRoot());
                this.b = b;
            }

            void bind(Order o) {
                b.tvOrderCode.setText(o.getOrderCode() != null ? o.getOrderCode() : o.getOrderId());
                if (o.getCreatedAt() != null) {
                    b.tvDate.setText(new SimpleDateFormat("HH:mm · dd/MM/yyyy", Locale.US)
                            .format(o.getCreatedAt()));
                }
                int pts = (int) (o.getTotalAmount() / 1000);
                b.tvPoints.setText("+" + pts);
            }
        }
    }
}

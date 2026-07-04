package com.FinalProject.group3.ui.order;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalProject.group3.databinding.ActivityCheckoutVoucherBinding;
import com.FinalProject.group3.databinding.ItemCheckoutVoucherBinding;
import com.FinalProject.group3.model.Voucher;
import com.FinalProject.group3.utils.FirebaseHelper;
import com.FinalProject.group3.utils.InsetsUtil;
import com.google.android.material.tabs.TabLayout;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * LA.Voucher3 — Chọn mã giảm giá cho đơn hàng đang thanh toán.
 * Mở từ CheckoutActivity qua ActivityResultLauncher, trả về mã đã chọn.
 */
public class CheckoutVoucherActivity extends AppCompatActivity {

    public static final String EXTRA_CURRENT_CODE = "current_code";
    public static final String RESULT_CODE = "code";

    private static final NumberFormat VND = NumberFormat.getInstance(new Locale("vi", "VN"));

    private static List<Voucher> brandVouchers() {
        long day = 24L * 60 * 60 * 1000;
        List<Voucher> list = new ArrayList<>();
        list.add(new Voucher("FREESHIP", "Miễn phí vận chuyển toàn quốc",
                "SHIPPING", 0, new Date(System.currentTimeMillis() + 3 * day)));
        list.add(new Voucher("GIAM10", "Giảm 10% tối đa 100Kđ",
                "DISCOUNT", 300000, new Date(System.currentTimeMillis() + 3 * day)));
        list.add(new Voucher("GIAM50K", "Giảm 50Kđ",
                "DISCOUNT", 500000, new Date(System.currentTimeMillis() + 7 * day)));
        return list;
    }

    public static Intent intent(Context context, String currentCode) {
        return new Intent(context, CheckoutVoucherActivity.class)
                .putExtra(EXTRA_CURRENT_CODE, currentCode);
    }

    private ActivityCheckoutVoucherBinding binding;
    private final List<Voucher> allVouchers = new ArrayList<>();
    private VoucherSelectAdapter adapter;
    private String selectedCode;
    private int currentTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutVoucherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applySystemBarsPadding(binding.getRoot());

        selectedCode = getIntent().getStringExtra(EXTRA_CURRENT_CODE);

        binding.btnBack.setOnClickListener(v -> finish());

        adapter = new VoucherSelectAdapter();
        binding.rvVouchers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvVouchers.setAdapter(adapter);

        // "Không dùng mã" → clear voucher và trả về
        binding.rowNoVoucher.setOnClickListener(v -> returnCode(null));
        binding.rbNoVoucher.setChecked(selectedCode == null);

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                showTab();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        binding.btnApply.setOnClickListener(v -> {
            String code = binding.etVoucherCode.getText().toString().trim().toUpperCase(Locale.US);
            if (code.isEmpty()) {
                Toast.makeText(this, "Nhập mã giảm giá trước", Toast.LENGTH_SHORT).show();
                return;
            }
            returnCode(code);
        });

        loadVouchers();
    }

    /** Load từ Firestore (vouchers đã claim) + brand vouchers luôn có sẵn. */
    private void loadVouchers() {
        String uid = FirebaseHelper.getCurrentUserId();
        if (uid == null) {
            // Khách chưa đăng nhập → chỉ hiện brand
            mergeAndShow(new ArrayList<>());
            return;
        }
        binding.progressBar.setVisibility(View.VISIBLE);
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_CUSTOMERS)
                .document(uid).collection("vouchers").get()
                .addOnSuccessListener(snapshot -> {
                    binding.progressBar.setVisibility(View.GONE);
                    List<Voucher> claimed = snapshot.toObjects(Voucher.class);
                    mergeAndShow(claimed);
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    mergeAndShow(new ArrayList<>());
                });
    }

    /** Gộp claimed + brand (không trùng code). */
    private void mergeAndShow(List<Voucher> claimed) {
        allVouchers.clear();
        allVouchers.addAll(claimed);
        for (Voucher brand : brandVouchers()) {
            boolean owned = false;
            for (Voucher c : claimed)
                if (c.getCode().equals(brand.getCode())) { owned = true; break; }
            if (!owned) allVouchers.add(brand);
        }
        updateTabCounts();
        showTab();
    }

    private void updateTabCounts() {
        int disc = 0, ship = 0;
        for (Voucher v : allVouchers)
            if ("SHIPPING".equals(v.getType())) ship++; else disc++;
        setTabText(0, "Tất cả (" + allVouchers.size() + ")");
        setTabText(1, "Giảm giá (" + disc + ")");
        setTabText(2, "Vận chuyển (" + ship + ")");
    }

    private void showTab() {
        List<Voucher> show;
        if (currentTab == 1) {
            show = new ArrayList<>();
            for (Voucher v : allVouchers) if (!"SHIPPING".equals(v.getType())) show.add(v);
        } else if (currentTab == 2) {
            show = new ArrayList<>();
            for (Voucher v : allVouchers) if ("SHIPPING".equals(v.getType())) show.add(v);
        } else {
            show = new ArrayList<>(allVouchers);
        }
        adapter.setItems(show, selectedCode);
        binding.rvVouchers.setVisibility(show.isEmpty() ? View.GONE : View.VISIBLE);
        binding.tvEmpty.setVisibility(show.isEmpty() ? View.VISIBLE : View.GONE);
        binding.rbNoVoucher.setChecked(selectedCode == null);
    }

    private void setTabText(int index, String text) {
        TabLayout.Tab tab = binding.tabLayout.getTabAt(index);
        if (tab != null) tab.setText(text);
    }

    private void returnCode(String code) {
        if (code == null) {
            setResult(Activity.RESULT_OK, new Intent().putExtra(RESULT_CODE, ""));
        } else {
            setResult(Activity.RESULT_OK, new Intent().putExtra(RESULT_CODE, code));
        }
        finish();
    }

    // ── Inner Adapter ─────────────────────────────────────────────────────────
    private class VoucherSelectAdapter extends RecyclerView.Adapter<VoucherSelectAdapter.VH> {

        private final List<Voucher> items = new ArrayList<>();
        private String checkedCode;

        void setItems(List<Voucher> vouchers, String checked) {
            items.clear();
            items.addAll(vouchers);
            checkedCode = checked;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(ItemCheckoutVoucherBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Voucher v = items.get(position);
            holder.binding.tvTitle.setText(v.getTitle());
            holder.binding.tvMinOrder.setText(v.getMinOrder() > 0
                    ? "Đơn tối thiểu " + VND.format(v.getMinOrder()) + "đ"
                    : "Không cần đơn tối thiểu");
            holder.binding.ivIcon.setImageResource("SHIPPING".equals(v.getType())
                    ? com.FinalProject.group3.R.drawable.ic_voucher_ship
                    : com.FinalProject.group3.R.drawable.ic_voucher_discount);
            holder.binding.rbVoucher.setChecked(v.getCode().equals(checkedCode));

            String days = "Còn " + v.daysLeft() + " ngày";
            android.text.SpannableString span =
                    new android.text.SpannableString(days + " | Điều kiện áp dụng");
            span.setSpan(new android.text.style.ForegroundColorSpan(0xFF8A8079),
                    0, days.length(), 0);
            span.setSpan(new android.text.style.ForegroundColorSpan(0xFF1976D2),
                    days.length() + 3, span.length(), 0);
            holder.binding.tvFooter.setText(span);

            holder.itemView.setOnClickListener(v2 -> {
                checkedCode = v.getCode();
                notifyDataSetChanged();
                binding.rbNoVoucher.setChecked(false);
                returnCode(v.getCode());
            });
        }

        @Override
        public int getItemCount() { return items.size(); }

        class VH extends RecyclerView.ViewHolder {
            final ItemCheckoutVoucherBinding binding;
            VH(ItemCheckoutVoucherBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}

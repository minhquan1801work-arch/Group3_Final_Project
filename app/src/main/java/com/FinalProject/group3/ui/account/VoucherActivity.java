package com.FinalProject.group3.ui.account;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.FinalProject.group3.databinding.ActivityVoucherBinding;
import com.FinalProject.group3.databinding.DialogClaimVoucherBinding;
import com.FinalProject.group3.adapter.VoucherAdapter;
import com.FinalProject.group3.model.Voucher;
import com.FinalProject.group3.utils.FirebaseHelper;
import com.FinalProject.group3.utils.InsetsUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * LA.Voucher — Kho mã giảm giá của user (customers/{uid}/vouchers).
 *  - Tabs Tất cả / Giảm giá / Vận chuyển (đếm số lượng như Figma)
 *  - Kho trống → 🎁 "Kho chưa có Mã giảm giá nào"
 *  - "Nhận mã từ Glassity" → BottomSheet các mã thương hiệu đang phát hành,
 *    bấm Nhận là lưu vào kho (mã trùng không hiện lại)
 *  - "Dùng ngay" → hướng dẫn nhập mã ở màn Thanh toán (mã khớp checkout)
 */
public class VoucherActivity extends AppCompatActivity {

    /** Mã thương hiệu đang phát hành — code khớp với CheckoutActivity. */
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

    private ActivityVoucherBinding binding;
    private VoucherAdapter adapter;
    private final List<Voucher> claimed = new ArrayList<>();
    private int currentTab = 0; // 0 tất cả, 1 giảm giá, 2 vận chuyển

    public static Intent intent(Context context) {
        return new Intent(context, VoucherActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVoucherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applySystemBarsPadding(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        // "Dùng ngay" → mở giỏ hàng để chọn sản phẩm mua, nhắc kèm mã cần nhập
        adapter = new VoucherAdapter("Dùng ngay", v -> {
            Toast.makeText(this, "Nhập mã " + v.getCode()
                    + " ở màn Thanh toán để được giảm", Toast.LENGTH_LONG).show();
            android.content.Intent i = new android.content.Intent(
                    this, com.FinalProject.group3.MainActivity.class);
            i.putExtra(com.FinalProject.group3.MainActivity.EXTRA_OPEN_CART, true);
            startActivity(i);
        });
        binding.rvVouchers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvVouchers.setAdapter(adapter);

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                showTab();
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        binding.btnClaim.setOnClickListener(v -> showClaimSheet());

        loadClaimed();
    }

    private void loadClaimed() {
        String uid = FirebaseHelper.getCurrentUserId();
        if (uid == null) {
            claimed.clear();
            showTab();
            return;
        }
        binding.progressBar.setVisibility(View.VISIBLE);
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_CUSTOMERS)
                .document(uid).collection("vouchers").get()
                .addOnSuccessListener(snapshot -> {
                    binding.progressBar.setVisibility(View.GONE);
                    claimed.clear();
                    claimed.addAll(snapshot.toObjects(Voucher.class));
                    showTab();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /** Lọc theo tab + cập nhật số đếm trên 3 tab (LA.Voucher2). */
    private void showTab() {
        List<Voucher> discount = new ArrayList<>(), shipping = new ArrayList<>();
        for (Voucher v : claimed)
            ("SHIPPING".equals(v.getType()) ? shipping : discount).add(v);

        setTabText(0, "Tất cả (" + claimed.size() + ")");
        setTabText(1, "Giảm giá (" + discount.size() + ")");
        setTabText(2, "Vận chuyển (" + shipping.size() + ")");

        List<Voucher> show = currentTab == 1 ? discount
                : currentTab == 2 ? shipping : claimed;
        adapter.setItems(show);
        binding.rvVouchers.setVisibility(show.isEmpty() ? View.GONE : View.VISIBLE);
        binding.layoutEmpty.setVisibility(show.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void setTabText(int index, String text) {
        TabLayout.Tab tab = binding.tabLayout.getTabAt(index);
        if (tab != null) tab.setText(text);
    }

    /** BottomSheet nhận mã: chỉ hiện mã CHƯA có trong kho. */
    private void showClaimSheet() {
        String uid = FirebaseHelper.getCurrentUserId();
        if (uid == null) {
            Toast.makeText(this, "Đăng nhập để nhận mã giảm giá", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Voucher> available = new ArrayList<>();
        for (Voucher brand : brandVouchers()) {
            boolean owned = false;
            for (Voucher mine : claimed)
                if (mine.getCode().equals(brand.getCode())) { owned = true; break; }
            if (!owned) available.add(brand);
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        DialogClaimVoucherBinding sheet = DialogClaimVoucherBinding.inflate(getLayoutInflater());
        dialog.setContentView(sheet.getRoot());

        sheet.tvClaimEmpty.setVisibility(available.isEmpty() ? View.VISIBLE : View.GONE);
        VoucherAdapter claimAdapter = new VoucherAdapter("Nhận", v -> {
            // Lưu vào kho — KHÔNG ghi voucherId (@DocumentId)
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("code", v.getCode());
            data.put("title", v.getTitle());
            data.put("type", v.getType());
            data.put("minOrder", v.getMinOrder());
            data.put("expireAt", v.getExpireAt());
            FirebaseHelper.getDb().collection(FirebaseHelper.COL_CUSTOMERS)
                    .document(uid).collection("vouchers").add(data)
                    .addOnSuccessListener(r -> {
                        Toast.makeText(this, "Đã nhận mã " + v.getCode(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadClaimed();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
        });
        sheet.rvClaimVouchers.setLayoutManager(new LinearLayoutManager(this));
        sheet.rvClaimVouchers.setAdapter(claimAdapter);
        claimAdapter.setItems(available);

        dialog.show();
    }
}

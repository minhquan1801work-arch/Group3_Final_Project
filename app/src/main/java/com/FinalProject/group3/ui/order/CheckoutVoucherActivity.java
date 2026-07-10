package com.FinalProject.group3.ui.order;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalProject.group3.R;
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
 * Chọn mã giảm giá + mã vận chuyển cho đơn hàng.
 * Hai loại độc lập: 1 DISCOUNT + 1 SHIPPING cùng lúc.
 * Icon SHIPPING = xanh lá, DISCOUNT = wine/brick.
 * Tab "Tất cả": giảm giá trước (có section header), vận chuyển sau.
 * Mã không đủ điều kiện: mờ xám, không cho bấm.
 *
 * TEST CODES (nhập thẳng vào ô mã):
 *   SHIPPING: FREESHIP (miễn phí, k cần đơn tối thiểu), SHIP50 (giảm 50% phí, đơn ≥200K)
 *   DISCOUNT: NEWUSER (−30K, đơn ≥100K), GIAM10 (−10% tối đa 100K, đơn ≥300K),
 *             MEMBER15 (−15% tối đa 150K, đơn ≥400K), GIAM50K (−50K, đơn ≥500K),
 *             SALE20 (−20% tối đa 200K, đơn ≥500K), GIAM100K (−100K, đơn ≥800K)
 */
public class CheckoutVoucherActivity extends AppCompatActivity {

    public static final String EXTRA_DISCOUNT_CODE = "discount_code";
    public static final String EXTRA_SHIP_CODE = "ship_code";
    public static final String EXTRA_SUBTOTAL = "subtotal";
    public static final String RESULT_DISCOUNT_CODE = "result_discount_code";
    public static final String RESULT_SHIP_CODE = "result_ship_code";

    // Icon background colors
    private static final int COLOR_SHIP     = 0xFF388E3C; // xanh lá
    private static final int COLOR_DISCOUNT = 0xFFB05A4A; // wine/brick

    private static final NumberFormat VND = NumberFormat.getInstance(new Locale("vi", "VN"));

    // ─── Danh sách mã mặc định của brand ─────────────────────────────────────
    private static List<Voucher> brandVouchers() {
        long day = 24L * 60 * 60 * 1000;
        List<Voucher> list = new ArrayList<>();
        // SHIPPING
        list.add(new Voucher("FREESHIP", "Miễn phí vận chuyển toàn quốc",
                "SHIPPING", 0, new Date(System.currentTimeMillis() + 7 * day)));
        list.add(new Voucher("SHIP50", "Giảm 50% phí vận chuyển",
                "SHIPPING", 200000, new Date(System.currentTimeMillis() + 5 * day)));
        // DISCOUNT
        list.add(new Voucher("NEWUSER", "Giảm 30.000đ cho đơn đầu tiên",
                "DISCOUNT", 100000, new Date(System.currentTimeMillis() + 30 * day)));
        list.add(new Voucher("GIAM10", "Giảm 10% tối đa 100.000đ",
                "DISCOUNT", 300000, new Date(System.currentTimeMillis() + 3 * day)));
        list.add(new Voucher("MEMBER15", "Giảm 15% tối đa 150.000đ",
                "DISCOUNT", 400000, new Date(System.currentTimeMillis() + 14 * day)));
        list.add(new Voucher("GIAM50K", "Giảm 50.000đ",
                "DISCOUNT", 500000, new Date(System.currentTimeMillis() + 7 * day)));
        list.add(new Voucher("SALE20", "Giảm 20% tối đa 200.000đ",
                "DISCOUNT", 500000, new Date(System.currentTimeMillis() + 2 * day)));
        list.add(new Voucher("GIAM100K", "Giảm 100.000đ",
                "DISCOUNT", 800000, new Date(System.currentTimeMillis() + 10 * day)));
        return list;
    }

    public static Intent intent(Context context, String discountCode, String shipCode) {
        return intent(context, discountCode, shipCode, 0);
    }

    public static Intent intent(Context context, String discountCode, String shipCode, double subtotal) {
        return new Intent(context, CheckoutVoucherActivity.class)
                .putExtra(EXTRA_DISCOUNT_CODE, discountCode)
                .putExtra(EXTRA_SHIP_CODE, shipCode)
                .putExtra(EXTRA_SUBTOTAL, subtotal);
    }

    private ActivityCheckoutVoucherBinding binding;
    private final List<Voucher> allVouchers = new ArrayList<>();
    private VoucherSelectAdapter adapter;
    private String checkedDiscountCode;
    private String checkedShipCode;
    private double orderSubtotal = 0;
    private int currentTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutVoucherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applySystemBarsPadding(binding.getRoot());

        checkedDiscountCode = getIntent().getStringExtra(EXTRA_DISCOUNT_CODE);
        checkedShipCode     = getIntent().getStringExtra(EXTRA_SHIP_CODE);
        orderSubtotal       = getIntent().getDoubleExtra(EXTRA_SUBTOTAL, 0);

        binding.btnBack.setOnClickListener(v -> finish());

        adapter = new VoucherSelectAdapter();
        binding.rvVouchers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvVouchers.setAdapter(adapter);

        binding.btnConfirmVoucher.setOnClickListener(v -> returnCodes());

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
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
            boolean isShip = false;
            for (Voucher vch : allVouchers)
                if (vch.getCode().equals(code) && "SHIPPING".equals(vch.getType())) {
                    isShip = true; break;
                }
            if (isShip) checkedShipCode = code;
            else checkedDiscountCode = code;
            returnCodes();
        });

        loadVouchers();
    }

    private void loadVouchers() {
        String uid = FirebaseHelper.getCurrentUserId();
        if (uid == null) { mergeAndShow(new ArrayList<>()); return; }
        binding.progressBar.setVisibility(View.VISIBLE);
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_CUSTOMERS)
                .document(uid).collection("vouchers").get()
                .addOnSuccessListener(snapshot -> {
                    binding.progressBar.setVisibility(View.GONE);
                    mergeAndShow(snapshot.toObjects(Voucher.class));
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    mergeAndShow(new ArrayList<>());
                });
    }

    private void mergeAndShow(List<Voucher> claimed) {
        allVouchers.clear();
        allVouchers.addAll(claimed);
        for (Voucher brand : brandVouchers()) {
            boolean owned = false;
            for (Voucher c : claimed)
                if (c.getCode().equals(brand.getCode())) { owned = true; break; }
            if (!owned) allVouchers.add(brand);
        }

        // Auto-select discount với giá trị cao nhất theo đơn hiện tại
        if (checkedDiscountCode == null) {
            Voucher best = null;
            double bestVal = -1;
            for (Voucher v : allVouchers) {
                if ("SHIPPING".equals(v.getType())) continue;
                double val = computeDiscountValue(v);
                if (val > bestVal) { bestVal = val; best = v; }
            }
            if (best != null) checkedDiscountCode = best.getCode();
        }
        // Auto-select shipping đủ điều kiện đầu tiên
        if (checkedShipCode == null) {
            for (Voucher v : allVouchers) {
                if ("SHIPPING".equals(v.getType()) && isEligible(v)) {
                    checkedShipCode = v.getCode(); break;
                }
            }
        }

        updateTabCounts();
        showTab();
    }

    private boolean isEligible(Voucher v) {
        return orderSubtotal >= v.getMinOrder();
    }

    private double computeDiscountValue(Voucher v) {
        if (!isEligible(v)) return 0;
        switch (v.getCode()) {
            case "NEWUSER":   return 30000;
            case "GIAM10":    return Math.min(orderSubtotal * 0.10, 100000);
            case "MEMBER15":  return Math.min(orderSubtotal * 0.15, 150000);
            case "GIAM50K":   return 50000;
            case "SALE20":    return Math.min(orderSubtotal * 0.20, 200000);
            case "GIAM100K":  return 100000;
            default:          return 0;
        }
    }

    private List<Voucher> getByType(String type) {
        List<Voucher> r = new ArrayList<>();
        for (Voucher v : allVouchers) if (type.equals(v.getType())) r.add(v);
        return r;
    }

    private List<ListItem> currentShown() {
        List<ListItem> items = new ArrayList<>();
        if (currentTab == 0) { // Tất cả — discount trước với section header, shipping sau
            List<Voucher> discounts = getByType("DISCOUNT");
            List<Voucher> ships     = getByType("SHIPPING");
            if (!discounts.isEmpty()) {
                items.add(new ListItem("Mã giảm giá"));
                for (Voucher v : discounts) items.add(new ListItem(v, isEligible(v)));
            }
            if (!ships.isEmpty()) {
                items.add(new ListItem("Mã vận chuyển"));
                for (Voucher v : ships) items.add(new ListItem(v, isEligible(v)));
            }
        } else if (currentTab == 1) { // Giảm giá
            for (Voucher v : allVouchers)
                if (!"SHIPPING".equals(v.getType())) items.add(new ListItem(v, isEligible(v)));
        } else { // Vận chuyển
            for (Voucher v : allVouchers)
                if ("SHIPPING".equals(v.getType())) items.add(new ListItem(v, isEligible(v)));
        }
        return items;
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
        List<ListItem> show = currentShown();
        boolean hasVoucher = false;
        for (ListItem i : show) if (i.type == ListItem.TYPE_VOUCHER) { hasVoucher = true; break; }
        adapter.setItems(show);
        binding.rvVouchers.setVisibility(show.isEmpty() ? View.GONE : View.VISIBLE);
        binding.tvEmpty.setVisibility(!hasVoucher ? View.VISIBLE : View.GONE);
    }

    private void setTabText(int index, String text) {
        TabLayout.Tab tab = binding.tabLayout.getTabAt(index);
        if (tab != null) tab.setText(text);
    }

    private void returnCodes() {
        Intent result = new Intent();
        result.putExtra(RESULT_DISCOUNT_CODE, checkedDiscountCode != null ? checkedDiscountCode : "");
        result.putExtra(RESULT_SHIP_CODE,     checkedShipCode     != null ? checkedShipCode     : "");
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    // ── Wrapper item: HEADER hoặc VOUCHER ────────────────────────────────────
    private static class ListItem {
        static final int TYPE_HEADER  = 0;
        static final int TYPE_VOUCHER = 1;

        final int     type;
        final String  headerText;
        final Voucher voucher;
        final boolean eligible;

        ListItem(String headerText) {
            this.type       = TYPE_HEADER;
            this.headerText = headerText;
            this.voucher    = null;
            this.eligible   = false;
        }

        ListItem(Voucher voucher, boolean eligible) {
            this.type       = TYPE_VOUCHER;
            this.headerText = null;
            this.voucher    = voucher;
            this.eligible   = eligible;
        }
    }

    // ── Adapter ───────────────────────────────────────────────────────────────
    private class VoucherSelectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final List<ListItem> items = new ArrayList<>();

        void setItems(List<ListItem> newItems) {
            items.clear();
            items.addAll(newItems);
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) { return items.get(position).type; }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == ListItem.TYPE_HEADER) {
                android.widget.TextView tv = new android.widget.TextView(parent.getContext());
                tv.setPadding(48, 32, 48, 8);
                tv.setTextSize(11);
                tv.setTextColor(0xFF9E9E9E);
                tv.setTypeface(null, android.graphics.Typeface.BOLD);
                tv.setAllCaps(true);
                tv.setLayoutParams(new RecyclerView.LayoutParams(
                        RecyclerView.LayoutParams.MATCH_PARENT,
                        RecyclerView.LayoutParams.WRAP_CONTENT));
                return new RecyclerView.ViewHolder(tv) {};
            }
            return new VH(ItemCheckoutVoucherBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ListItem item = items.get(position);
            if (item.type == ListItem.TYPE_HEADER) {
                ((android.widget.TextView) holder.itemView).setText(item.headerText);
                return;
            }

            VH vh = (VH) holder;
            Voucher v    = item.voucher;
            boolean isShip = "SHIPPING".equals(v.getType());
            boolean eligible = item.eligible;

            vh.binding.tvTitle.setText(v.getTitle());
            vh.binding.tvMinOrder.setText(v.getMinOrder() > 0
                    ? "Đơn tối thiểu " + VND.format(v.getMinOrder()) + "đ"
                    : "Không cần đơn tối thiểu");

            // Icon + màu nền: xanh lá (ship) / wine-brick (discount)
            vh.binding.ivIcon.setImageResource(isShip
                    ? R.drawable.ic_voucher_ship : R.drawable.ic_voucher_discount);
            vh.binding.ivIcon.setImageTintList(ColorStateList.valueOf(0xFFFFFFFF));
            vh.binding.flIconBg.setBackgroundColor(isShip ? COLOR_SHIP : COLOR_DISCOUNT);

            // Mờ nếu không đủ điều kiện
            vh.itemView.setAlpha(eligible ? 1f : 0.4f);

            boolean checked = isShip
                    ? v.getCode().equals(checkedShipCode)
                    : v.getCode().equals(checkedDiscountCode);
            vh.binding.rbVoucher.setChecked(checked);

            String days = "Còn " + v.daysLeft() + " ngày";
            android.text.SpannableString span =
                    new android.text.SpannableString(days + " | Điều kiện áp dụng");
            span.setSpan(new android.text.style.ForegroundColorSpan(0xFF8A8079),
                    0, days.length(), 0);
            span.setSpan(new android.text.style.ForegroundColorSpan(0xFF1976D2),
                    days.length() + 3, span.length(), 0);
            vh.binding.tvFooter.setText(span);

            if (eligible) {
                vh.itemView.setOnClickListener(v2 -> {
                    if (isShip)
                        checkedShipCode = v.getCode().equals(checkedShipCode) ? null : v.getCode();
                    else
                        checkedDiscountCode = v.getCode().equals(checkedDiscountCode) ? null : v.getCode();
                    notifyDataSetChanged();
                });
            } else {
                vh.itemView.setOnClickListener(v2 ->
                        Toast.makeText(CheckoutVoucherActivity.this,
                                "Đơn hàng chưa đủ điều kiện dùng mã này", Toast.LENGTH_SHORT).show());
            }
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

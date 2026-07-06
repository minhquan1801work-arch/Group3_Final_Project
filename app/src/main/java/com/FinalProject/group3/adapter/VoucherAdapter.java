package com.FinalProject.group3.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalProject.group3.R;
import com.FinalProject.group3.databinding.ItemVoucherBinding;
import com.FinalProject.group3.model.Voucher;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter voucher (LA.Voucher) — dùng chung cho 2 chỗ:
 *  - Kho của tôi: nút "Dùng ngay"
 *  - BottomSheet nhận mã thương hiệu: nút "Nhận"
 */
public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VH> {

    public interface OnVoucherAction {
        void onAction(Voucher voucher);
    }

    private static final NumberFormat VND = NumberFormat.getInstance(new Locale("vi", "VN"));

    private final List<Voucher> items = new ArrayList<>();
    private final String actionText;
    private final OnVoucherAction listener;

    public VoucherAdapter(String actionText, OnVoucherAction listener) {
        this.actionText = actionText;
        this.listener = listener;
    }

    public void setItems(List<Voucher> vouchers) {
        items.clear();
        items.addAll(vouchers);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemVoucherBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Voucher v = items.get(position);
        holder.binding.tvTitle.setText(v.getTitle());
        holder.binding.tvMinOrder.setText(v.getMinOrder() > 0
                ? "Đơn tối thiểu " + VND.format(v.getMinOrder()) + "đ"
                : "Không cần đơn tối thiểu");
        if ("SHIPPING".equals(v.getType())) {
            holder.binding.ivIcon.setImageResource(R.drawable.ic_voucher_ship);
            holder.binding.flIconBg.setBackgroundColor(0xFF1E6B4A); // xanh lá — miễn ship
        } else {
            holder.binding.ivIcon.setImageResource(R.drawable.ic_voucher_discount);
            holder.binding.flIconBg.setBackgroundColor(0xFF72383D); // wine — giảm giá
        }
        holder.binding.btnAction.setText(actionText);
        holder.binding.btnAction.setOnClickListener(view -> listener.onAction(v));

        // "Còn x ngày | Điều kiện áp dụng" — nửa sau màu xanh như Figma
        String days = "Còn " + v.daysLeft() + " ngày";
        android.text.SpannableString span =
                new android.text.SpannableString(days + " | Điều kiện áp dụng");
        span.setSpan(new android.text.style.ForegroundColorSpan(0xFF8A8079),
                0, days.length(), 0);
        span.setSpan(new android.text.style.ForegroundColorSpan(0xFF1976D2),
                days.length() + 3, span.length(), 0);
        holder.binding.tvFooter.setText(span);
        holder.binding.tvFooter.setOnClickListener(view ->
                android.widget.Toast.makeText(view.getContext(),
                        "Mã " + v.getCode() + ": " + v.getTitle()
                                + (v.getMinOrder() > 0
                                    ? ", áp dụng cho đơn từ " + VND.format(v.getMinOrder()) + "đ"
                                    : ""),
                        android.widget.Toast.LENGTH_LONG).show());
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemVoucherBinding binding;

        VH(ItemVoucherBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

package com.FinalProject.group3.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalProject.group3.databinding.ItemCartBinding;
import com.FinalProject.group3.model.CartDetail;
import com.FinalProject.group3.model.Product;
import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * DL_Cart — Adapter cho danh sách sản phẩm trong Giỏ hàng.
 *
 * Mỗi item có: checkbox chọn mua, ảnh, tên, chấm màu, giá,
 * stepper tăng/giảm số lượng, nút xóa.
 * Fragment lắng nghe qua CartItemListener để ghi Firestore + tính lại tổng tiền.
 */
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface CartItemListener {
        void onQuantityChanged(CartDetail item, int newQuantity);
        void onDeleteClick(CartDetail item);
        void onSelectionChanged(); // tick/bỏ tick checkbox → tính lại tổng
        void onVariantClick(CartDetail item, android.view.View anchor); // chọn lại màu
    }

    private static final NumberFormat VND_FORMAT = NumberFormat.getInstance(new Locale("vi", "VN"));

    private final List<CartDetail> items = new ArrayList<>();
    private final Set<String> selectedIds = new HashSet<>(); // cartDetailId đang được tick
    private final CartItemListener listener;

    public CartAdapter(CartItemListener listener) {
        this.listener = listener;
    }

    public void submitList(List<CartDetail> newItems) {
        submitListWithAutoSelect(newItems, null);
    }

    /**
     * Nạp danh sách và tự chọn item vừa thêm.
     * @param autoSelectId cartDetailId muốn pre-select; null → chọn item cuối cùng (mới nhất).
     */
    public void submitListWithAutoSelect(List<CartDetail> newItems, String autoSelectId) {
        items.clear();
        items.addAll(newItems);
        selectedIds.clear();

        if (autoSelectId != null) {
            // Chọn đúng item vừa thêm vào giỏ
            for (CartDetail d : newItems)
                if (d.getCartDetailId().equals(autoSelectId)) { selectedIds.add(autoSelectId); break; }
        }
        if (selectedIds.isEmpty() && !newItems.isEmpty()) {
            // Vào giỏ trực tiếp → chọn item cuối trong danh sách (mới nhất)
            selectedIds.add(newItems.get(newItems.size() - 1).getCartDetailId());
        }
        notifyDataSetChanged();
    }

    // ── API cho Fragment ──────────────────────────────────────────────────────
    public List<CartDetail> getSelectedItems() {
        List<CartDetail> result = new ArrayList<>();
        for (CartDetail d : items)
            if (selectedIds.contains(d.getCartDetailId())) result.add(d);
        return result;
    }

    public boolean isAllSelected() {
        return !items.isEmpty() && selectedIds.size() == items.size();
    }

    public void setAllSelected(boolean selected) {
        selectedIds.clear();
        if (selected) for (CartDetail d : items) selectedIds.add(d.getCartDetailId());
        notifyDataSetChanged();
        listener.onSelectionChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartDetail item = items.get(position);
        Product product = item.getProduct();
        ItemCartBinding b = holder.binding;

        // Thông tin sản phẩm (product được Fragment gắn vào sau khi load Firestore)
        if (product != null) {
            b.tvName.setText(product.getName());
            b.tvPrice.setText(VND_FORMAT.format(product.getPrice() * item.getQuantity()) + "VND");
            // Ảnh: ưu tiên ảnh của variant khớp màu đã chọn, fallback variant đầu / images cũ
            String imgUrl = null;
            if (product.getVariants() != null && !product.getVariants().isEmpty()) {
                com.FinalProject.group3.model.ProductVariant match = null;
                for (com.FinalProject.group3.model.ProductVariant pv : product.getVariants()) {
                    if (pv.getColor() != null && pv.getColor().equalsIgnoreCase(item.getColor())) {
                        match = pv;
                        break;
                    }
                }
                if (match == null) match = product.getVariants().get(0);
                if (match.getImages() != null && !match.getImages().isEmpty()) {
                    imgUrl = match.getImages().get(0);
                }
            }
            if (imgUrl == null && product.getImages() != null && !product.getImages().isEmpty()) {
                imgUrl = product.getImages().get(0);
            }
            if (imgUrl != null) {
                Glide.with(b.ivProduct).load(imgUrl)
                        .placeholder(com.FinalProject.group3.R.drawable.bg_product_placeholder)
                        .into(b.ivProduct);
            }
        }

        // Chip phân loại (Figma: "Màu: đen ▾") — ưu tiên colorName từ variant
        String variantName = null;
        if (product != null && product.getVariants() != null) {
            for (com.FinalProject.group3.model.ProductVariant pv : product.getVariants()) {
                if (pv.getColor() != null && pv.getColor().equalsIgnoreCase(item.getColor())
                        && pv.getColorName() != null && !pv.getColorName().isEmpty()) {
                    variantName = pv.getColorName();
                    break;
                }
            }
        }
        b.tvVariant.setText("Màu: " + (variantName != null ? variantName : colorName(item.getColor())) + " ▾");
        b.tvVariant.setOnClickListener(v -> listener.onVariantClick(item, v));

        b.tvQuantity.setText(String.valueOf(item.getQuantity()));

        // Checkbox chọn mua
        b.cbSelect.setOnCheckedChangeListener(null);
        b.cbSelect.setChecked(selectedIds.contains(item.getCartDetailId()));
        b.cbSelect.setOnCheckedChangeListener((v, checked) -> {
            if (checked) selectedIds.add(item.getCartDetailId());
            else selectedIds.remove(item.getCartDetailId());
            listener.onSelectionChanged();
        });

        // Stepper: giảm về 0 = hỏi xóa (chi tiết giống app TMĐT thật)
        b.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() <= 1) listener.onDeleteClick(item);
            else listener.onQuantityChanged(item, item.getQuantity() - 1);
        });
        b.btnPlus.setOnClickListener(v -> {
            int max = (product != null && product.getStock() > 0) ? product.getStock() : 99;
            if (item.getQuantity() < max) listener.onQuantityChanged(item, item.getQuantity() + 1);
        });
    }

    // Đổi mã hex trong Firestore thành tên màu tiếng Việt (Figma hiển thị tên)
    private static String colorName(String hex) {
        if (hex == null) return "đen";
        switch (hex.toUpperCase(Locale.US)) {
            case "#1A1614": case "#111111": case "#000000": return "đen";
            case "#FFFFFF": return "trắng";
            case "#C0C0C0": return "bạc";
            case "#C8A96E": return "vàng đồng";
            case "#C88B3A": return "hổ phách";
            case "#8B6914": case "#72383D": return "nâu đô";
            case "#AC9C8D": return "be";
            case "#4A90D9": return "xanh";
            case "#4A4A4A": return "xám";
            default: return "khác";
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        final ItemCartBinding binding;
        CartViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

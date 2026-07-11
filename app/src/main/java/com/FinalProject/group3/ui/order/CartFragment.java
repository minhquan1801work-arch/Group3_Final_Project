package com.FinalProject.group3.ui.order;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalProject.group3.R;
import com.FinalProject.group3.adapter.CartAdapter;
import com.FinalProject.group3.databinding.FragmentCartBinding;
import com.FinalProject.group3.model.CartDetail;
import com.FinalProject.group3.model.Product;
import com.FinalProject.group3.repository.CartRepository;
import com.FinalProject.group3.repository.ProductRepository;
import com.FinalProject.group3.ui.account.LoginActivity;
import com.FinalProject.group3.utils.FirebaseHelper;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * DL_Cart — Giỏ hàng (Figma frame DL_Cart).
 *
 * Luồng BPMN: Xem giỏ hàng → chọn sản phẩm muốn mua (checkbox) →
 * chỉnh số lượng / xóa → nhấn MUA HÀNG → CheckoutActivity.
 *
 * 3 trạng thái UI:
 *  1. Chưa đăng nhập  → icon + "Đăng nhập để xem Giỏ hàng" + nút ĐĂNG NHẬP
 *  2. Giỏ trống       → icon + thông báo + nút TIẾP TỤC MUA SẮM (Figma DL_Cart trống)
 *  3. Có sản phẩm     → list + chọn tất cả + tổng tiền + nút MUA HÀNG
 */
public class CartFragment extends Fragment implements CartAdapter.CartItemListener {

    private static final NumberFormat VND_FORMAT = NumberFormat.getInstance(new Locale("vi", "VN"));
    private static final String CART_PREFS = "cart_prefs";
    private static final String KEY_LAST_ADDED = "last_added_id";

    private FragmentCartBinding binding;
    private CartAdapter adapter;
    private final CartRepository cartRepo = new CartRepository();
    private final ProductRepository productRepo = new ProductRepository();

    private String selectedDiscountCode = null;
    private String selectedShipCode = null;

    // Chế độ Sửa (chọn nhiều để xóa) — nút "Sửa"/"Xong" ở góc phải header
    private boolean editMode = false;

    // Launcher để mở màn chọn voucher ngay từ giỏ hàng
    private ActivityResultLauncher<Intent> voucherLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        voucherLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK
                            && result.getData() != null) {
                        String disc = result.getData()
                                .getStringExtra(CheckoutVoucherActivity.RESULT_DISCOUNT_CODE);
                        String ship = result.getData()
                                .getStringExtra(CheckoutVoucherActivity.RESULT_SHIP_CODE);
                        selectedDiscountCode = (disc == null || disc.isEmpty()) ? null : disc;
                        selectedShipCode    = (ship == null || ship.isEmpty()) ? null : ship;
                        updateVoucherRow();
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);

        adapter = new CartAdapter(this);
        binding.rvCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCart.setAdapter(adapter);

        // Back về đúng nơi trước đó: pop nav stack; nếu giỏ là màn gốc
        // (mở từ ProductDetail/Favorite qua EXTRA_OPEN_CART) → đóng MainActivity
        binding.btnBack.setOnClickListener(v -> {
            boolean popped = androidx.navigation.fragment.NavHostFragment
                    .findNavController(this).popBackStack();
            if (!popped) requireActivity().finish();
        });

        binding.cbSelectAll.setOnClickListener(v ->
                adapter.setAllSelected(binding.cbSelectAll.isChecked()));

        binding.btnBuy.setOnClickListener(v -> goToCheckout());

        // Chế độ Sửa: chọn 1/nhiều sản phẩm rồi xóa một lượt
        binding.tvEdit.setOnClickListener(v -> setEditMode(!editMode));
        binding.btnDeleteSelected.setOnClickListener(v -> confirmDeleteSelected());

        binding.rowVoucher.setOnClickListener(v ->
                voucherLauncher.launch(CheckoutVoucherActivity.intent(
                        requireContext(), selectedDiscountCode, selectedShipCode)));

        // Kéo item sang trái → xác nhận xóa (giống màn Lựa chọn địa chỉ)
        new ItemTouchHelper(new SwipeToDeleteCallback()).attachToRecyclerView(binding.rvCart);

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCart();
    }

    // ── Load giỏ hàng từ Firestore ────────────────────────────────────────────
    private void loadCart() {
        if (binding == null) return;

        if (FirebaseHelper.getCurrentUserId() == null) {
            showGuestDialog();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        cartRepo.getCartItems(new CartRepository.CartDetailCallback() {
            @Override
            public void onSuccess(List<CartDetail> items) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                if (items.isEmpty()) {
                    showEmpty(getString(R.string.cart_empty_msg),
                            getString(R.string.cart_continue_shopping), false);
                } else {
                    loadProducts(items);
                }
            }

            @Override
            public void onFailure(String error) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProducts(List<CartDetail> items) {
        final int[] loaded = {0};
        for (CartDetail item : items) {
            productRepo.getProductById(item.getProductId(), new ProductRepository.ProductCallback() {
                @Override
                public void onSuccess(Product product) {
                    item.setProduct(product);
                    if (++loaded[0] == items.size()) showValidItems(items);
                }

                @Override
                public void onFailure(String error) {
                    if (++loaded[0] == items.size()) showValidItems(items);
                }
            });
        }
    }

    /**
     * Lọc bỏ item "mồ côi" — cart detail trỏ tới sản phẩm đã bị xóa khỏi Firestore
     * (data seed cũ). Tự xóa luôn document đó để lần sau không hiện lại ô đen.
     */
    private void showValidItems(List<CartDetail> items) {
        if (binding == null) return;
        List<CartDetail> valid = new ArrayList<>();
        for (CartDetail item : items) {
            if (item.getProduct() != null) {
                valid.add(item);
            } else {
                cartRepo.removeFromCart(item.getCartDetailId(), new CartRepository.SimpleCallback() {
                    @Override public void onSuccess() { }
                    @Override public void onFailure(String error) { }
                });
            }
        }
        if (valid.isEmpty()) {
            showEmpty(getString(R.string.cart_empty_msg),
                    getString(R.string.cart_continue_shopping), false);
        } else {
            showCart(valid);
        }
    }

    // ── Dialog khi khách chưa đăng nhập ──────────────────────────────────────
    private void showGuestDialog() {
        if (binding == null || !isAdded()) return;
        // Ẩn hết nội dung giỏ, để nền trắng sau dialog
        setCartVisible(false);
        binding.tvTitle.setText(R.string.cart_title);
        binding.llEmpty.setVisibility(View.GONE);
        binding.btnContinueShopping.setVisibility(View.GONE);

        binding.getRoot().post(() -> {
            if (!isAdded() || getActivity() == null) return;
            com.FinalProject.group3.utils.LoginRequiredDialog.show(
                    requireActivity(),
                    "Đăng nhập để xem và quản lý giỏ hàng của bạn",
                    () -> {
                        // Bấm "Để sau" → quay về Home
                        if (isAdded()) {
                            androidx.navigation.fragment.NavHostFragment
                                    .findNavController(CartFragment.this)
                                    .navigate(R.id.homeFragment);
                        }
                    });
        });
    }

    // ── Hiển thị ──────────────────────────────────────────────────────────────
    private void showCart(List<CartDetail> items) {
        if (binding == null) return;
        setCartVisible(true);
        binding.tvTitle.setText(getString(R.string.cart_title) + "(" + items.size() + ")");

        // Auto-select: nếu vừa thêm vào giỏ → chọn item đó; ngược lại chọn item mới nhất
        String lastAddedId = requireContext()
                .getSharedPreferences(CART_PREFS, Context.MODE_PRIVATE)
                .getString(KEY_LAST_ADDED, null);
        requireContext().getSharedPreferences(CART_PREFS, Context.MODE_PRIVATE)
                .edit().remove(KEY_LAST_ADDED).apply();

        adapter.submitListWithAutoSelect(items, lastAddedId);
        updateTotal();
    }

    private void showEmpty(String message, String buttonText, boolean needLogin) {
        setCartVisible(false);
        binding.tvTitle.setText(R.string.cart_title);
        binding.tvEmptyMsg.setText(message);
        binding.btnContinueShopping.setText(buttonText);
        binding.btnContinueShopping.setOnClickListener(v -> {
            if (needLogin) {
                startActivity(new Intent(requireContext(), LoginActivity.class));
            } else {
                androidx.navigation.fragment.NavHostFragment.findNavController(this)
                        .navigate(R.id.homeFragment);
            }
        });
    }

    private void setCartVisible(boolean hasItems) {
        int cart = hasItems ? View.VISIBLE : View.GONE;
        int empty = hasItems ? View.GONE : View.VISIBLE;
        binding.rvCart.setVisibility(cart);
        binding.rowVoucher.setVisibility(cart);
        binding.llBottomBar.setVisibility(cart);
        binding.llEmpty.setVisibility(empty);
        binding.btnContinueShopping.setVisibility(empty);
        binding.tvEdit.setVisibility(cart);
        if (!hasItems && editMode) setEditMode(false); // giỏ trống → thoát chế độ Sửa
    }

    private void updateTotal() {
        List<CartDetail> selected = adapter.getSelectedItems();
        double total = 0;
        for (CartDetail d : selected)
            if (d.getProduct() != null) total += d.getProduct().getPrice() * d.getQuantity();
        binding.tvTotal.setText(VND_FORMAT.format(total) + "VND");
        binding.btnBuy.setText(getString(R.string.cart_buy_btn) + " (" + selected.size() + ")");
        binding.btnBuy.setEnabled(!selected.isEmpty());
        binding.btnDeleteSelected.setText("XÓA (" + selected.size() + ")");
        binding.btnDeleteSelected.setEnabled(!selected.isEmpty());
    }

    // ── Chế độ Sửa: chọn nhiều sản phẩm để xóa một lượt ──────────────────────
    private void setEditMode(boolean enabled) {
        editMode = enabled;
        binding.tvEdit.setText(enabled ? "Xong" : "Sửa");
        binding.btnBuy.setVisibility(enabled ? View.GONE : View.VISIBLE);
        binding.btnDeleteSelected.setVisibility(enabled ? View.VISIBLE : View.GONE);
        updateTotal();
    }

    private void confirmDeleteSelected() {
        List<CartDetail> selected = adapter.getSelectedItems();
        if (selected.isEmpty()) {
            Toast.makeText(requireContext(), "Chọn ít nhất 1 sản phẩm để xóa", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean isAll = selected.size() == adapter.getItemCount();
        String msg = isAll
                ? "Bạn có chắc chắn muốn xóa TẤT CẢ sản phẩm trong giỏ?"
                : "Bạn có chắc chắn muốn xóa " + selected.size() + " sản phẩm đã chọn khỏi giỏ?";
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa sản phẩm")
                .setMessage(msg)
                .setPositiveButton("Xóa", (d, w) -> deleteSelected(selected))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteSelected(List<CartDetail> selected) {
        binding.progressBar.setVisibility(View.VISIBLE);
        final int[] done = {0};
        for (CartDetail item : selected) {
            cartRepo.removeFromCart(item.getCartDetailId(), new CartRepository.SimpleCallback() {
                @Override public void onSuccess() { onOneDeleted(); }
                @Override public void onFailure(String error) { onOneDeleted(); }
                private void onOneDeleted() {
                    if (++done[0] == selected.size() && binding != null) {
                        setEditMode(false);
                        loadCart();
                    }
                }
            });
        }
    }

    private void updateVoucherRow() {
        if (binding == null) return;
        StringBuilder sb = new StringBuilder();
        if (selectedDiscountCode != null) sb.append(selectedDiscountCode);
        if (selectedShipCode != null) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(selectedShipCode);
        }
        if (sb.length() > 0) {
            binding.tvVoucherValue.setText(sb.toString());
            binding.tvVoucherValue.setVisibility(View.VISIBLE);
        } else {
            binding.tvVoucherValue.setVisibility(View.GONE);
        }
    }

    // ── CartAdapter.CartItemListener ─────────────────────────────────────────
    @Override
    public void onQuantityChanged(CartDetail item, int newQuantity) {
        item.setQuantity(newQuantity);
        adapter.notifyDataSetChanged();
        updateTotal();
        cartRepo.updateQuantity(item.getCartDetailId(), newQuantity, new CartRepository.SimpleCallback() {
            @Override public void onSuccess() { }
            @Override public void onFailure(String error) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                loadCart();
            }
        });
    }

    @Override
    public void onDeleteClick(CartDetail item) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.cart_delete_title)
                .setMessage(R.string.cart_delete_msg)
                .setPositiveButton("Xóa", (d, w) ->
                        cartRepo.removeFromCart(item.getCartDetailId(), new CartRepository.SimpleCallback() {
                            @Override public void onSuccess() { loadCart(); }
                            @Override public void onFailure(String error) {
                                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                            }
                        }))
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onSelectionChanged() {
        binding.cbSelectAll.setChecked(adapter.isAllSelected());
        updateTotal();
    }

    // ── BottomSheet chỉnh variant + số lượng (Figma DL_Choose product) ────────
    @Override
    public void onVariantClick(CartDetail item) {
        Product product = item.getProduct();
        if (product == null) return;

        List<com.FinalProject.group3.model.ProductVariant> variants = product.getVariants();
        boolean hasVariants = variants != null && !variants.isEmpty();
        List<String> oldColors = product.getColors();
        if (!hasVariants && (oldColors == null || oldColors.isEmpty())) return;

        com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext());
        View sheet = getLayoutInflater().inflate(R.layout.bottom_sheet_cart_variant, null);
        dialog.setContentView(sheet);

        android.widget.ImageView ivProduct = sheet.findViewById(R.id.ivSheetProduct);
        android.widget.TextView tvPrice  = sheet.findViewById(R.id.tvSheetPrice);
        android.widget.TextView tvStock  = sheet.findViewById(R.id.tvSheetStock);
        android.widget.LinearLayout llColors = sheet.findViewById(R.id.llSheetColors);
        android.widget.TextView tvQty    = sheet.findViewById(R.id.tvSheetQty);

        int count = hasVariants ? variants.size() : oldColors.size();

        // Vị trí variant đang chọn = variant khớp màu hiện tại của item
        int selectedNow = 0;
        for (int i = 0; i < count; i++) {
            String hex = hasVariants ? variants.get(i).getColor() : oldColors.get(i);
            if (hex != null && hex.equalsIgnoreCase(item.getColor())) { selectedNow = i; break; }
        }
        final int[] selected = { selectedNow };
        final int[] qty = { item.getQuantity() };

        tvPrice.setText(VND_FORMAT.format(product.getPrice()) + "VND");
        tvQty.setText(String.valueOf(qty[0]));

        // Cập nhật ảnh + kho theo variant đang chọn
        Runnable refreshInfo = () -> {
            String imgUrl = null;
            int stock = product.getStock();
            if (hasVariants) {
                com.FinalProject.group3.model.ProductVariant v = variants.get(selected[0]);
                stock = v.getStock();
                if (v.getImages() != null && !v.getImages().isEmpty()) imgUrl = v.getImages().get(0);
            }
            if (imgUrl == null && product.getImages() != null && !product.getImages().isEmpty())
                imgUrl = product.getImages().get(0);
            if (imgUrl != null)
                com.bumptech.glide.Glide.with(ivProduct).load(com.FinalProject.group3.utils.CloudinaryUtil.optimize(imgUrl, 250))
                        .placeholder(R.drawable.bg_product_placeholder).into(ivProduct);
            tvStock.setText("Kho: " + stock);
            // Kẹp số lượng theo kho của variant mới
            int max = stock > 0 ? stock : 99;
            if (qty[0] > max) { qty[0] = max; tvQty.setText(String.valueOf(qty[0])); }
        };

        // Dựng dãy chip màu
        int padH = (int) (12 * getResources().getDisplayMetrics().density);
        int padV = (int) (6  * getResources().getDisplayMetrics().density);
        int marginEnd = (int) (8 * getResources().getDisplayMetrics().density);
        for (int i = 0; i < count; i++) {
            final int index = i;
            android.widget.TextView chip = new android.widget.TextView(requireContext());
            String label;
            if (hasVariants) {
                com.FinalProject.group3.model.ProductVariant v = variants.get(i);
                label = (v.getColorName() != null && !v.getColorName().isEmpty())
                        ? v.getColorName() : colorNameOf(v.getColor());
            } else {
                label = colorNameOf(oldColors.get(i));
            }
            chip.setText(label);
            chip.setTextSize(13);
            chip.setPadding(padH, padV, padH, padV);
            android.widget.LinearLayout.LayoutParams lp =
                    new android.widget.LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(marginEnd);
            chip.setLayoutParams(lp);
            chip.setOnClickListener(v -> {
                selected[0] = index;
                for (int c = 0; c < llColors.getChildCount(); c++)
                    styleChip((android.widget.TextView) llColors.getChildAt(c), c == index);
                refreshInfo.run();
            });
            styleChip(chip, i == selectedNow);
            llColors.addView(chip);
        }
        refreshInfo.run();

        // Stepper trong sheet
        sheet.findViewById(R.id.btnSheetMinus).setOnClickListener(v -> {
            if (qty[0] > 1) tvQty.setText(String.valueOf(--qty[0]));
        });
        sheet.findViewById(R.id.btnSheetPlus).setOnClickListener(v -> {
            int stock = hasVariants ? variants.get(selected[0]).getStock() : product.getStock();
            int max = stock > 0 ? stock : 99;
            if (qty[0] < max) tvQty.setText(String.valueOf(++qty[0]));
            else Toast.makeText(requireContext(), "Chỉ còn " + max + " sản phẩm trong kho",
                    Toast.LENGTH_SHORT).show();
        });

        // XÁC NHẬN → ghi Firestore cả màu + số lượng, refresh UI
        sheet.findViewById(R.id.btnSheetConfirm).setOnClickListener(v -> {
            String newColor = hasVariants
                    ? variants.get(selected[0]).getColor() : oldColors.get(selected[0]);
            item.setColor(newColor);
            item.setQuantity(qty[0]);
            adapter.notifyDataSetChanged();
            updateTotal();
            dialog.dismiss();
            cartRepo.updateItem(item.getCartDetailId(), newColor, qty[0],
                    new CartRepository.SimpleCallback() {
                        @Override public void onSuccess() { }
                        @Override public void onFailure(String error) {
                            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                            loadCart();
                        }
                    });
        });

        dialog.show();
    }

    private void styleChip(android.widget.TextView chip, boolean isSelected) {
        chip.setBackgroundResource(isSelected
                ? R.drawable.bg_chip_variant_selected : R.drawable.bg_chip_variant);
        chip.setTextColor(getResources().getColor(isSelected
                ? R.color.color_text_primary : R.color.color_text_secondary, null));
        chip.setTypeface(null, isSelected
                ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
    }

    private static String colorNameOf(String hex) {
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
            default: return hex;
        }
    }

    // ── MUA HÀNG → Checkout ───────────────────────────────────────────────────
    private void goToCheckout() {
        List<CartDetail> selected = adapter.getSelectedItems();
        if (selected.isEmpty()) {
            Toast.makeText(requireContext(), "Chọn ít nhất 1 sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<String> ids = new ArrayList<>();
        for (CartDetail d : selected) ids.add(d.getCartDetailId());
        CheckoutActivity.start(requireContext(), ids, selectedDiscountCode, selectedShipCode);
    }

    // ── Swipe-to-delete: kéo item sang trái → dialog xác nhận (tái dùng onDeleteClick) ──
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
            CartDetail item = adapter.getItemAt(pos);
            // Khôi phục item ngay — chỉ xóa thật sau khi user xác nhận trong dialog
            adapter.notifyItemChanged(pos);
            if (item != null) onDeleteClick(item);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

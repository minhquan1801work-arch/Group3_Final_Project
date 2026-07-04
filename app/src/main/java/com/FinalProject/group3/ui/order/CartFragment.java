package com.FinalProject.group3.ui.order;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

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

    private FragmentCartBinding binding;
    private CartAdapter adapter;
    private final CartRepository cartRepo = new CartRepository();
    private final ProductRepository productRepo = new ProductRepository();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);

        adapter = new CartAdapter(this);
        binding.rvCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCart.setAdapter(adapter);

        binding.cbSelectAll.setOnClickListener(v ->
                adapter.setAllSelected(binding.cbSelectAll.isChecked()));

        binding.btnBuy.setOnClickListener(v -> goToCheckout());

        // Figma: dòng "Mã giảm giá >" — voucher được chọn/nhập ở bước Thanh toán
        binding.rowVoucher.setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.cart_voucher_hint, Toast.LENGTH_SHORT).show());

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load lại mỗi lần quay về tab Giỏ hàng (vd: sau khi thêm từ ProductDetail)
        loadCart();
    }

    // ── Load giỏ hàng từ Firestore ────────────────────────────────────────────
    private void loadCart() {
        if (binding == null) return;

        // Trạng thái 1: khách chưa đăng nhập
        if (FirebaseHelper.getCurrentUserId() == null) {
            showEmpty(getString(R.string.cart_login_msg), getString(R.string.cart_login_btn), true);
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

    // Giỏ chỉ lưu productId → phải load thêm thông tin Product cho từng item
    private void loadProducts(List<CartDetail> items) {
        final int[] loaded = {0};
        for (CartDetail item : items) {
            productRepo.getProductById(item.getProductId(), new ProductRepository.ProductCallback() {
                @Override
                public void onSuccess(Product product) {
                    item.setProduct(product);
                    if (++loaded[0] == items.size()) showCart(items);
                }

                @Override
                public void onFailure(String error) {
                    // Sản phẩm bị xóa khỏi shop → vẫn hiện item, chỉ thiếu ảnh/tên
                    if (++loaded[0] == items.size()) showCart(items);
                }
            });
        }
    }

    // ── Hiển thị ──────────────────────────────────────────────────────────────
    private void showCart(List<CartDetail> items) {
        if (binding == null) return;
        setCartVisible(true);
        binding.tvTitle.setText(getString(R.string.cart_title) + "(" + items.size() + ")");
        adapter.submitList(items);
        binding.cbSelectAll.setChecked(true);
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
                // Chuyển về tab Trang chủ để tiếp tục mua sắm
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
    }

    private void updateTotal() {
        List<CartDetail> selected = adapter.getSelectedItems();
        double total = 0;
        for (CartDetail d : selected)
            if (d.getProduct() != null) total += d.getProduct().getPrice() * d.getQuantity();
        binding.tvTotal.setText(VND_FORMAT.format(total) + "VND");
        // Figma: "MUA HÀNG (3)" — số item đang được tick
        binding.btnBuy.setText(getString(R.string.cart_buy_btn) + " (" + selected.size() + ")");
        binding.btnBuy.setEnabled(!selected.isEmpty());
    }

    // ── CartAdapter.CartItemListener ─────────────────────────────────────────
    @Override
    public void onQuantityChanged(CartDetail item, int newQuantity) {
        item.setQuantity(newQuantity);
        adapter.notifyDataSetChanged();
        updateTotal();
        // Optimistic update: UI đổi ngay, Firestore ghi ngầm phía sau
        cartRepo.updateQuantity(item.getCartDetailId(), newQuantity, new CartRepository.SimpleCallback() {
            @Override public void onSuccess() { }
            @Override public void onFailure(String error) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                loadCart(); // ghi lỗi → load lại cho khớp server
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

    // ── MUA HÀNG → Checkout ───────────────────────────────────────────────────
    private void goToCheckout() {
        List<CartDetail> selected = adapter.getSelectedItems();
        if (selected.isEmpty()) {
            Toast.makeText(requireContext(), "Chọn ít nhất 1 sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<String> ids = new ArrayList<>();
        for (CartDetail d : selected) ids.add(d.getCartDetailId());
        CheckoutActivity.start(requireContext(), ids);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

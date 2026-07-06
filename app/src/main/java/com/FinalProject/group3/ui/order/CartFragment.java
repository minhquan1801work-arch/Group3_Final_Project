package com.FinalProject.group3.ui.order;

import android.content.Context;
import android.content.Intent;
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
    private static final String CART_PREFS = "cart_prefs";
    private static final String KEY_LAST_ADDED = "last_added_id";

    private FragmentCartBinding binding;
    private CartAdapter adapter;
    private final CartRepository cartRepo = new CartRepository();
    private final ProductRepository productRepo = new ProductRepository();

    private String selectedDiscountCode = null;
    private String selectedShipCode = null;

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

        binding.cbSelectAll.setOnClickListener(v ->
                adapter.setAllSelected(binding.cbSelectAll.isChecked()));

        binding.btnBuy.setOnClickListener(v -> goToCheckout());

        binding.rowVoucher.setOnClickListener(v ->
                voucherLauncher.launch(CheckoutVoucherActivity.intent(
                        requireContext(), selectedDiscountCode, selectedShipCode)));

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
    }

    private void updateTotal() {
        List<CartDetail> selected = adapter.getSelectedItems();
        double total = 0;
        for (CartDetail d : selected)
            if (d.getProduct() != null) total += d.getProduct().getPrice() * d.getQuantity();
        binding.tvTotal.setText(VND_FORMAT.format(total) + "VND");
        binding.btnBuy.setText(getString(R.string.cart_buy_btn) + " (" + selected.size() + ")");
        binding.btnBuy.setEnabled(!selected.isEmpty());
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

    @Override
    public void onVariantClick(CartDetail item, View anchor) {
        Product product = item.getProduct();
        if (product == null || product.getColors() == null || product.getColors().isEmpty()) return;

        java.util.List<String> colors = product.getColors();
        String[] colorNames = new String[colors.size()];
        for (int i = 0; i < colors.size(); i++)
            colorNames[i] = colorNamePublic(colors.get(i));

        android.widget.ArrayAdapter<String> arrAdapter = new android.widget.ArrayAdapter<>(
                requireContext(), R.layout.item_color_popup, colorNames);

        android.widget.ListPopupWindow popup = new android.widget.ListPopupWindow(requireContext());
        popup.setAnchorView(anchor);
        popup.setAdapter(arrAdapter);
        popup.setWidth(anchor.getWidth() > 0 ? anchor.getWidth() * 3 : 200);
        popup.setModal(true);
        popup.setBackgroundDrawable(
                new android.graphics.drawable.ColorDrawable(0xFFFFFFFF));
        popup.setOnItemClickListener((parent, view, which, id) -> {
            String newColor = colors.get(which);
            item.setColor(newColor);
            adapter.notifyDataSetChanged();
            popup.dismiss();
            cartRepo.updateColor(item.getCartDetailId(), newColor,
                    new CartRepository.SimpleCallback() {
                        @Override public void onSuccess() { }
                        @Override public void onFailure(String error) {
                            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                            loadCart();
                        }
                    });
        });
        popup.show();
    }

    private static String colorNamePublic(String hex) {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

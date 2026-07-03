package com.FinalProject.group3.ui.order;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalProject.group3.R;
import com.FinalProject.group3.databinding.ActivityCheckoutBinding;
import com.FinalProject.group3.databinding.DialogVoucherBinding;
import com.FinalProject.group3.databinding.ItemCheckoutProductBinding;
import com.FinalProject.group3.model.CartDetail;
import com.FinalProject.group3.model.Customer;
import com.FinalProject.group3.model.Order;
import com.FinalProject.group3.model.OrderDetail;
import com.FinalProject.group3.model.Product;
import com.FinalProject.group3.repository.CartRepository;
import com.FinalProject.group3.repository.OrderRepository;
import com.FinalProject.group3.repository.ProductRepository;
import com.FinalProject.group3.utils.FirebaseHelper;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * DL_Checkout — màn Thanh toán (Figma frame DL_Checkout / LA.Pay).
 *
 * Luồng BPMN: Nhận items đã chọn từ Giỏ hàng → xác nhận địa chỉ →
 * chọn phương thức vận chuyển → (tùy chọn) áp mã giảm giá →
 * chọn phương thức thanh toán → ĐẶT HÀNG:
 *   1. OrderRepository.createOrder() (order + orderDetails)
 *   2. Ghi payments (trạng thái PENDING)
 *   3. Xóa các item đã mua khỏi Giỏ hàng
 *   4. Mở PaymentResultActivity (COD → thành công / Bank → chờ thanh toán)
 */
public class CheckoutActivity extends AppCompatActivity {

    private static final String EXTRA_CART_DETAIL_IDS = "cart_detail_ids";
    private static final NumberFormat VND_FORMAT = NumberFormat.getInstance(new Locale("vi", "VN"));

    // Phí vận chuyển (Figma LA.Payment: tiêu chuẩn 35k / nhanh 50k)
    private static final double SHIP_STANDARD = 35000;
    private static final double SHIP_FAST = 50000;

    // Voucher demo (LA.Voucher): mã → luật giảm
    private static final String VOUCHER_FREESHIP = "FREESHIP";
    private static final String VOUCHER_GIAM10 = "GIAM10";   // -10%, tối đa 100k, đơn ≥ 300k
    private static final String VOUCHER_GIAM50K = "GIAM50K"; // -50k, đơn ≥ 500k

    private ActivityCheckoutBinding binding;
    private final CartRepository cartRepo = new CartRepository();
    private final ProductRepository productRepo = new ProductRepository();
    private final OrderRepository orderRepo = new OrderRepository();

    private final List<CartDetail> items = new ArrayList<>();
    private Customer customer;
    private String appliedVoucher = null;

    public static void start(Context context, ArrayList<String> cartDetailIds) {
        Intent intent = new Intent(context, CheckoutActivity.class);
        intent.putStringArrayListExtra(EXTRA_CART_DETAIL_IDS, cartDetailIds);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        // 2 radio vận chuyển nằm ngoài RadioGroup (để canh giá bên phải theo Figma)
        // → tự quản lý exclusivity
        binding.rbShipStandard.setOnClickListener(v -> {
            binding.rbShipFast.setChecked(false);
            updateSummary();
        });
        binding.rbShipFast.setOnClickListener(v -> {
            binding.rbShipStandard.setChecked(false);
            updateSummary();
        });

        // 2 box phương thức thanh toán (Figma: box viền riêng) — exclusivity thủ công
        binding.rbCod.setOnClickListener(v -> binding.rbBank.setChecked(false));
        binding.rbBank.setOnClickListener(v -> binding.rbCod.setChecked(false));

        binding.rowVoucher.setOnClickListener(v -> showVoucherSheet());
        binding.btnApplyVoucherInline.setOnClickListener(v ->
                applyVoucherCode(binding.etVoucher.getText().toString().trim().toUpperCase(Locale.US)));
        binding.rowAddress.setOnClickListener(v -> showEditAddressDialog());
        binding.btnEditAddress.setOnClickListener(v -> showEditAddressDialog());
        binding.btnPlaceOrder.setOnClickListener(v -> placeOrder());

        binding.rvProducts.setLayoutManager(new LinearLayoutManager(this));

        loadCustomer();
        loadItems(getIntent().getStringArrayListExtra(EXTRA_CART_DETAIL_IDS));
    }

    // ── 1. Địa chỉ nhận hàng (customers/{uid}) ────────────────────────────────
    private void loadCustomer() {
        String uid = FirebaseHelper.getCurrentUserId();
        if (uid == null) { finish(); return; }
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_CUSTOMERS).document(uid).get()
                .addOnSuccessListener(doc -> {
                    try {
                        customer = doc.toObject(Customer.class);
                    } catch (RuntimeException e) {
                        // Data bẩn (field trùng @DocumentId) → map thủ công, không crash
                        customer = new Customer();
                        customer.setName(doc.getString("name"));
                        customer.setEmail(doc.getString("email"));
                        customer.setPhone(doc.getString("phone"));
                        customer.setAddress(doc.getString("address"));
                    }
                    bindAddress();
                });
    }

    private void bindAddress() {
        if (customer == null) return;
        String phone = customer.getPhone() == null ? "" : customer.getPhone();
        binding.tvReceiver.setText(customer.getName() + " | " + phone);
        binding.tvAddress.setText(customer.getAddress() == null || customer.getAddress().isEmpty()
                ? getString(R.string.checkout_address_empty) : customer.getAddress());
    }

    // Dialog sửa nhanh người nhận / SĐT / địa chỉ (DL_Add address rút gọn)
    private void showEditAddressDialog() {
        if (customer == null) return;
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad / 2, pad, 0);

        EditText etName = new EditText(this);
        etName.setHint("Tên người nhận");
        etName.setText(customer.getName());
        EditText etPhone = new EditText(this);
        etPhone.setHint("Số điện thoại");
        etPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        etPhone.setText(customer.getPhone());
        EditText etAddress = new EditText(this);
        etAddress.setHint("Địa chỉ nhận hàng");
        etAddress.setText(customer.getAddress());

        layout.addView(etName);
        layout.addView(etPhone);
        layout.addView(etAddress);

        new AlertDialog.Builder(this)
                .setTitle(R.string.checkout_address_title)
                .setView(layout)
                .setPositiveButton("Lưu", (d, w) -> {
                    customer.setName(etName.getText().toString().trim());
                    customer.setPhone(etPhone.getText().toString().trim());
                    customer.setAddress(etAddress.getText().toString().trim());
                    bindAddress();
                    // Lưu lại vào Firestore để lần sau không phải nhập lại
                    Map<String, Object> update = new HashMap<>();
                    update.put("name", customer.getName());
                    update.put("phone", customer.getPhone());
                    update.put("address", customer.getAddress());
                    FirebaseHelper.getDb().collection(FirebaseHelper.COL_CUSTOMERS)
                            .document(FirebaseHelper.getCurrentUserId()).update(update);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // ── 2. Load các item đã chọn từ Giỏ hàng ─────────────────────────────────
    private void loadItems(List<String> selectedIds) {
        if (selectedIds == null || selectedIds.isEmpty()) { finish(); return; }
        binding.progressBar.setVisibility(View.VISIBLE);

        cartRepo.getCartItems(new CartRepository.CartDetailCallback() {
            @Override
            public void onSuccess(List<CartDetail> all) {
                items.clear();
                for (CartDetail d : all)
                    if (selectedIds.contains(d.getCartDetailId())) items.add(d);
                loadProducts();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(CheckoutActivity.this, error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadProducts() {
        final int[] loaded = {0};
        for (CartDetail item : items) {
            productRepo.getProductById(item.getProductId(), new ProductRepository.ProductCallback() {
                @Override
                public void onSuccess(Product product) {
                    item.setProduct(product);
                    if (++loaded[0] == items.size()) bindItems();
                }

                @Override
                public void onFailure(String error) {
                    if (++loaded[0] == items.size()) bindItems();
                }
            });
        }
    }

    private void bindItems() {
        binding.progressBar.setVisibility(View.GONE);
        binding.rvProducts.setAdapter(new CheckoutProductAdapter(items));
        updateSummary();
    }

    // ── 3+4+6. Tính tiền: hàng + ship − voucher ──────────────────────────────
    private double subtotal() {
        double s = 0;
        for (CartDetail d : items)
            if (d.getProduct() != null) s += d.getProduct().getPrice() * d.getQuantity();
        return s;
    }

    private int itemCount() {
        int n = 0;
        for (CartDetail d : items) n += d.getQuantity();
        return n;
    }

    private double shippingFee() {
        return binding.rbShipFast.isChecked() ? SHIP_FAST : SHIP_STANDARD;
    }

    // FREESHIP giảm vào phí ship (Figma: dòng "Giảm giá phí vận chuyển")
    private double shipDiscount() {
        return VOUCHER_FREESHIP.equals(appliedVoucher) ? shippingFee() : 0;
    }

    // GIAM10 / GIAM50K giảm vào tiền hàng (Figma: "Tổng cộng Voucher giảm giá")
    private double voucherDiscount() {
        double sub = subtotal();
        if (VOUCHER_GIAM10.equals(appliedVoucher) && sub >= 300000)
            return Math.min(sub * 0.10, 100000);
        if (VOUCHER_GIAM50K.equals(appliedVoucher) && sub >= 500000) return 50000;
        return 0;
    }

    // Nhập mã tay (Figma: ô "Nhập mã giảm giá...")
    private void applyVoucherCode(String code) {
        if (code.isEmpty()) {
            appliedVoucher = null;
            updateSummary();
            return;
        }
        double sub = subtotal();
        switch (code) {
            case VOUCHER_FREESHIP:
                appliedVoucher = code;
                break;
            case VOUCHER_GIAM10:
                if (sub < 300000) { Toast.makeText(this, "GIAM10 chỉ áp dụng cho đơn từ 300.000đ", Toast.LENGTH_SHORT).show(); return; }
                appliedVoucher = code;
                break;
            case VOUCHER_GIAM50K:
                if (sub < 500000) { Toast.makeText(this, "GIAM50K chỉ áp dụng cho đơn từ 500.000đ", Toast.LENGTH_SHORT).show(); return; }
                appliedVoucher = code;
                break;
            default:
                Toast.makeText(this, "Mã giảm giá không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
        }
        Toast.makeText(this, "Đã áp dụng mã " + code, Toast.LENGTH_SHORT).show();
        updateSummary();
    }

    private void updateSummary() {
        double sub = subtotal(), ship = shippingFee();
        double shipDisc = shipDiscount(), vDisc = voucherDiscount();
        double total = Math.max(0, sub + ship - shipDisc - vDisc);

        binding.tvSubtotalLabel.setText(getString(R.string.checkout_summary_subtotal)
                + " (" + itemCount() + " sản phẩm)");
        binding.tvSubtotal.setText(VND_FORMAT.format(sub) + "đ");
        binding.tvShippingFee.setText(VND_FORMAT.format(ship) + "đ");

        binding.rowShipDiscount.setVisibility(shipDisc > 0 ? View.VISIBLE : View.GONE);
        binding.tvShipDiscount.setText("-" + VND_FORMAT.format(shipDisc) + "đ");

        binding.rowVoucherDiscount.setVisibility(vDisc > 0 ? View.VISIBLE : View.GONE);
        binding.tvDiscount.setText("-" + VND_FORMAT.format(vDisc) + "đ");

        binding.tvGrandTotal.setText(VND_FORMAT.format(total) + "đ");
        binding.tvBottomTotal.setText(VND_FORMAT.format(total) + "đ");
        binding.tvVoucherValue.setText(appliedVoucher == null ? "" : appliedVoucher);
    }

    // ── 4. BottomSheet chọn voucher (LA.Voucher) ─────────────────────────────
    private void showVoucherSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        DialogVoucherBinding vb = DialogVoucherBinding.inflate(getLayoutInflater());
        dialog.setContentView(vb.getRoot());

        // Hiện lại lựa chọn cũ
        if (VOUCHER_FREESHIP.equals(appliedVoucher)) vb.rbFreeship.setChecked(true);
        else if (VOUCHER_GIAM10.equals(appliedVoucher)) vb.rbGiam10.setChecked(true);
        else if (VOUCHER_GIAM50K.equals(appliedVoucher)) vb.rbGiam50k.setChecked(true);

        vb.btnApplyVoucher.setOnClickListener(v -> {
            double sub = subtotal();
            if (vb.rbGiam10.isChecked() && sub < 300000) {
                Toast.makeText(this, "GIAM10 chỉ áp dụng cho đơn từ 300.000đ", Toast.LENGTH_SHORT).show();
                return;
            }
            if (vb.rbGiam50k.isChecked() && sub < 500000) {
                Toast.makeText(this, "GIAM50K chỉ áp dụng cho đơn từ 500.000đ", Toast.LENGTH_SHORT).show();
                return;
            }
            if (vb.rbFreeship.isChecked()) appliedVoucher = VOUCHER_FREESHIP;
            else if (vb.rbGiam10.isChecked()) appliedVoucher = VOUCHER_GIAM10;
            else if (vb.rbGiam50k.isChecked()) appliedVoucher = VOUCHER_GIAM50K;
            else appliedVoucher = null;
            updateSummary();
            dialog.dismiss();
        });
        dialog.show();
    }

    // ── ĐẶT HÀNG ─────────────────────────────────────────────────────────────
    private void placeOrder() {
        if (customer == null || customer.getAddress() == null || customer.getAddress().isEmpty()) {
            Toast.makeText(this, R.string.checkout_err_no_address, Toast.LENGTH_SHORT).show();
            return;
        }
        if (items.isEmpty()) return;

        binding.btnPlaceOrder.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        String uid = FirebaseHelper.getCurrentUserId();
        String method = binding.rbBank.isChecked() ? "BANK_TRANSFER" : "COD";
        double total = Math.max(0, subtotal() + shippingFee() - shipDiscount() - voucherDiscount());

        // Mã đơn: GLS-yyMMdd-XXXX (dễ đọc khi CSKH đối chiếu)
        String orderCode = "GLS-" + new SimpleDateFormat("yyMMdd", Locale.US).format(new Date())
                + "-" + String.format(Locale.US, "%04d", new Random().nextInt(10000));

        // Địa chỉ giao = tên + SĐT + địa chỉ (đủ thông tin cho shipper)
        String shippingAddress = customer.getName() + " | " + customer.getPhone()
                + " | " + customer.getAddress();

        Order order = new Order(uid, orderCode, total, method, shippingAddress);

        List<OrderDetail> details = new ArrayList<>();
        for (CartDetail d : items) {
            double price = d.getProduct() != null ? d.getProduct().getPrice() : 0;
            details.add(new OrderDetail(d.getProductId(), d.getQuantity(), price, d.getColor()));
        }

        orderRepo.createOrder(order, details, new OrderRepository.SimpleCallback() {
            @Override
            public void onSuccess(String orderId) {
                createPayment(orderId, uid, method, total);
                removeOrderedItemsFromCart();
                PaymentResultActivity.start(CheckoutActivity.this, orderCode, total, method);
                finish();
            }

            @Override
            public void onFailure(String error) {
                binding.btnPlaceOrder.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(CheckoutActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Ghi bản ghi payments (PaymentRepository chưa có trong project → ghi trực tiếp)
    private void createPayment(String orderId, String uid, String method, double amount) {
        Map<String, Object> payment = new HashMap<>();
        payment.put("orderId", orderId);
        payment.put("customerId", uid);
        payment.put("method", method);
        payment.put("status", "PENDING"); // COD & Bank đều chờ xác nhận
        payment.put("transactionId", null);
        payment.put("amount", amount);
        payment.put("createdAt", new Date());
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_PAYMENTS).add(payment);
    }

    // Chỉ xóa các item ĐÃ MUA — item chưa tick vẫn nằm lại trong giỏ
    private void removeOrderedItemsFromCart() {
        for (CartDetail d : items) {
            cartRepo.removeFromCart(d.getCartDetailId(), new CartRepository.SimpleCallback() {
                @Override public void onSuccess() { }
                @Override public void onFailure(String error) { }
            });
        }
    }

    // ── Adapter danh sách sản phẩm rút gọn ───────────────────────────────────
    private static class CheckoutProductAdapter
            extends RecyclerView.Adapter<CheckoutProductAdapter.VH> {

        private final List<CartDetail> items;

        CheckoutProductAdapter(List<CartDetail> items) { this.items = items; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(ItemCheckoutProductBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            CartDetail d = items.get(position);
            Product p = d.getProduct();
            if (p != null) {
                holder.binding.tvName.setText(p.getName());
                holder.binding.tvPrice.setText(VND_FORMAT.format(p.getPrice() * d.getQuantity()) + "đ");
                if (p.getImages() != null && !p.getImages().isEmpty()) {
                    Glide.with(holder.binding.ivProduct).load(p.getImages().get(0))
                            .placeholder(R.drawable.bg_product_placeholder)
                            .into(holder.binding.ivProduct);
                }
            }
            holder.binding.tvQty.setText("x" + d.getQuantity());
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            final ItemCheckoutProductBinding binding;
            VH(ItemCheckoutProductBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}

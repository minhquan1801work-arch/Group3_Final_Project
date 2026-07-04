package com.FinalProject.group3.ui.order;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalProject.group3.R;
import com.FinalProject.group3.databinding.ActivityCheckoutBinding;
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
 * DL_Checkout — màn Thanh toán (Figma frame LA.Payment).
 *
 * Luồng BPMN: Nhận items đã chọn từ Giỏ hàng → xác nhận địa chỉ →
 * chọn phương thức vận chuyển → (tùy chọn) áp mã giảm giá →
 * (tùy chọn) dùng điểm thành viên → chọn phương thức thanh toán → ĐẶT HÀNG.
 */
public class CheckoutActivity extends AppCompatActivity {

    private static final String EXTRA_CART_DETAIL_IDS = "cart_detail_ids";
    private static final NumberFormat VND_FORMAT = NumberFormat.getInstance(new Locale("vi", "VN"));

    private static final double SHIP_STANDARD = 35000;
    private static final double SHIP_FAST = 50000;

    private static final String VOUCHER_FREESHIP = "FREESHIP";
    private static final String VOUCHER_GIAM10 = "GIAM10";
    private static final String VOUCHER_GIAM50K = "GIAM50K";

    private ActivityCheckoutBinding binding;
    private final CartRepository cartRepo = new CartRepository();
    private final ProductRepository productRepo = new ProductRepository();
    private final OrderRepository orderRepo = new OrderRepository();

    private final List<CartDetail> items = new ArrayList<>();
    private Customer customer;
    private String appliedVoucher = null;

    // Điểm thành viên
    private int pointsBalance = 0;
    private boolean usePoints = false;

    // Địa chỉ giao hàng
    private String shipName, shipPhone, shipFullAddress;
    private final com.FinalProject.group3.repository.AddressRepository addressRepo =
            new com.FinalProject.group3.repository.AddressRepository();

    // Mở Sổ địa chỉ → nhận địa chỉ được chọn
    private final androidx.activity.result.ActivityResultLauncher<Intent> addressLauncher =
            registerForActivityResult(
                    new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            shipName = result.getData().getStringExtra(AddressListActivity.RESULT_NAME);
                            shipPhone = result.getData().getStringExtra(AddressListActivity.RESULT_PHONE);
                            shipFullAddress = result.getData().getStringExtra(AddressListActivity.RESULT_FULL_ADDRESS);
                            bindAddress();
                        } else {
                            loadDefaultAddress();
                        }
                    });

    // Mở màn chọn mã giảm giá → nhận code được chọn
    private final androidx.activity.result.ActivityResultLauncher<Intent> voucherLauncher =
            registerForActivityResult(
                    new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            String code = result.getData().getStringExtra(CheckoutVoucherActivity.RESULT_CODE);
                            appliedVoucher = (code == null || code.isEmpty()) ? null : code;
                            updateSummary();
                        }
                    });

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
        com.FinalProject.group3.utils.InsetsUtil.applySystemBarsPadding(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        // Vận chuyển — 2 radio, exclusivity thủ công
        binding.rbShipStandard.setOnClickListener(v -> {
            binding.rbShipFast.setChecked(false);
            updateSummary();
        });
        binding.rbShipFast.setOnClickListener(v -> {
            binding.rbShipStandard.setChecked(false);
            updateSummary();
        });

        // 3 phương thức thanh toán — exclusivity thủ công
        binding.rbCod.setOnClickListener(v -> {
            binding.rbBank.setChecked(false);
            binding.rbWallet.setChecked(false);
            binding.layoutWalletLogos.setVisibility(View.GONE);
        });
        binding.rbBank.setOnClickListener(v -> {
            binding.rbCod.setChecked(false);
            binding.rbWallet.setChecked(false);
            binding.layoutWalletLogos.setVisibility(View.GONE);
        });
        binding.rbWallet.setOnClickListener(v -> {
            binding.rbCod.setChecked(false);
            binding.rbBank.setChecked(false);
            binding.layoutWalletLogos.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Tính năng ví điện tử đang phát triển", Toast.LENGTH_SHORT).show();
        });

        // Điểm thành viên toggle
        binding.swUsePoints.setOnCheckedChangeListener((btn, checked) -> {
            usePoints = checked;
            binding.rowPointsInfo.setVisibility(checked ? View.VISIBLE : View.GONE);
            updateSummary();
        });

        // Mã giảm giá → mở CheckoutVoucherActivity
        binding.rowVoucher.setOnClickListener(v ->
                voucherLauncher.launch(CheckoutVoucherActivity.intent(this, appliedVoucher)));
        // Ô nhập mã inline (vẫn giữ như Figma)
        binding.btnApplyVoucherInline.setOnClickListener(v ->
                applyVoucherCode(binding.etVoucher.getText().toString().trim().toUpperCase(Locale.US)));

        binding.rowAddress.setOnClickListener(v ->
                addressLauncher.launch(AddressListActivity.intent(this)));
        binding.btnEditAddress.setOnClickListener(v ->
                addressLauncher.launch(AddressListActivity.intent(this)));
        binding.btnPlaceOrder.setOnClickListener(v -> placeOrder());

        binding.rvProducts.setLayoutManager(new LinearLayoutManager(this));

        setupTermsNote();
        loadCustomer();
        loadItems(getIntent().getStringArrayListExtra(EXTRA_CART_DETAIL_IDS));
    }

    /** Footnote: "Điều khoản Glassity" → Toast (Figma link clickable). */
    private void setupTermsNote() {
        String full = getString(R.string.checkout_terms_note);
        String link = "Điều khoản Glassity";
        int start = full.indexOf(link);
        if (start < 0) return;
        SpannableString span = new SpannableString(full);
        span.setSpan(new ForegroundColorSpan(0xFF1565C0), start, start + link.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Toast.makeText(CheckoutActivity.this,
                        "Điều khoản Glassity — sẽ cập nhật sau", Toast.LENGTH_SHORT).show();
            }
        }, start, start + link.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.tvTermsNote.setText(span);
        binding.tvTermsNote.setMovementMethod(LinkMovementMethod.getInstance());
        binding.tvTermsNote.setHighlightColor(android.graphics.Color.TRANSPARENT);
    }

    // ── 1. Địa chỉ nhận hàng ─────────────────────────────────────────────────
    private void loadCustomer() {
        String uid = FirebaseHelper.getCurrentUserId();
        if (uid == null) { finish(); return; }
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_CUSTOMERS).document(uid).get()
                .addOnSuccessListener(doc -> {
                    try {
                        customer = doc.toObject(Customer.class);
                    } catch (RuntimeException e) {
                        customer = new Customer();
                        customer.setName(doc.getString("name"));
                        customer.setEmail(doc.getString("email"));
                        customer.setPhone(doc.getString("phone"));
                        customer.setAddress(doc.getString("address"));
                    }
                    // Đọc điểm thành viên (field "points" trong Firestore)
                    Long pts = doc.getLong("points");
                    pointsBalance = (pts != null) ? pts.intValue() : 0;
                    bindPointsToggle();
                    loadDefaultAddress();
                });
    }

    private void bindPointsToggle() {
        binding.tvPointsBalance.setText(getString(R.string.checkout_points_balance, pointsBalance));
        // Toggle ẩn/hiện dựa vào có điểm không
        binding.swUsePoints.setEnabled(pointsBalance > 0);
    }

    private void loadDefaultAddress() {
        addressRepo.getAddresses(new com.FinalProject.group3.repository.AddressRepository.ListCallback() {
            @Override
            public void onSuccess(List<com.FinalProject.group3.model.Address> list) {
                if (!list.isEmpty()) {
                    com.FinalProject.group3.model.Address def = list.get(0);
                    shipName = def.getName();
                    shipPhone = def.getPhone();
                    shipFullAddress = def.fullAddress();
                } else if (customer != null && customer.getAddress() != null
                        && !customer.getAddress().isEmpty()) {
                    shipName = customer.getName();
                    shipPhone = customer.getPhone();
                    shipFullAddress = customer.getAddress();
                }
                bindAddress();
            }

            @Override
            public void onFailure(String error) { bindAddress(); }
        });
    }

    private void bindAddress() {
        if (shipFullAddress != null && !shipFullAddress.isEmpty()) {
            binding.tvReceiver.setText(shipName + " | " + (shipPhone == null ? "" : shipPhone));
            binding.tvAddress.setText(shipFullAddress);
        } else {
            binding.tvReceiver.setText(customer != null && customer.getName() != null
                    ? customer.getName() : "");
            binding.tvAddress.setText(R.string.checkout_address_empty);
        }
    }

    // ── 2. Load items từ giỏ hàng ────────────────────────────────────────────
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

    // ── 3. Tính tiền ──────────────────────────────────────────────────────────
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

    private double shipDiscount() {
        return VOUCHER_FREESHIP.equals(appliedVoucher) ? shippingFee() : 0;
    }

    private double voucherDiscount() {
        double sub = subtotal();
        if (VOUCHER_GIAM10.equals(appliedVoucher) && sub >= 300000)
            return Math.min(sub * 0.10, 100000);
        if (VOUCHER_GIAM50K.equals(appliedVoucher) && sub >= 500000) return 50000;
        return 0;
    }

    private double pointsDiscount() {
        if (!usePoints || pointsBalance <= 0) return 0;
        // 1 điểm = 1đ; không giảm vượt quá tổng tiền trước khi trừ điểm
        double beforePoints = subtotal() + shippingFee() - shipDiscount() - voucherDiscount();
        return Math.min(pointsBalance, Math.max(0, beforePoints));
    }

    private int rewardPointsEarned() {
        // 1000đ tiền hàng = 1 điểm thưởng
        return (int) (subtotal() / 1000);
    }

    private void applyVoucherCode(String code) {
        if (code.isEmpty()) { appliedVoucher = null; updateSummary(); return; }
        double sub = subtotal();
        switch (code) {
            case VOUCHER_FREESHIP:
                appliedVoucher = code; break;
            case VOUCHER_GIAM10:
                if (sub < 300000) {
                    Toast.makeText(this, "GIAM10 chỉ áp dụng cho đơn từ 300.000đ", Toast.LENGTH_SHORT).show();
                    return;
                }
                appliedVoucher = code; break;
            case VOUCHER_GIAM50K:
                if (sub < 500000) {
                    Toast.makeText(this, "GIAM50K chỉ áp dụng cho đơn từ 500.000đ", Toast.LENGTH_SHORT).show();
                    return;
                }
                appliedVoucher = code; break;
            default:
                Toast.makeText(this, "Mã giảm giá không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
        }
        Toast.makeText(this, "Đã áp dụng mã " + code, Toast.LENGTH_SHORT).show();
        updateSummary();
    }

    private void updateSummary() {
        double sub = subtotal(), ship = shippingFee();
        double shipDisc = shipDiscount(), vDisc = voucherDiscount(), ptsDisc = pointsDiscount();
        double total = Math.max(0, sub + ship - shipDisc - vDisc - ptsDisc);
        int earned = rewardPointsEarned();

        binding.tvSubtotalLabel.setText(getString(R.string.checkout_summary_subtotal)
                + " (" + itemCount() + " sản phẩm)");
        binding.tvSubtotal.setText(VND_FORMAT.format(sub) + "đ");
        binding.tvShippingFee.setText(VND_FORMAT.format(ship) + "đ");

        // Điểm thưởng (info, không trừ tiền)
        if (earned > 0) {
            binding.rowRewardPoints.setVisibility(View.VISIBLE);
            binding.tvRewardPoints.setText("+" + earned);
        } else {
            binding.rowRewardPoints.setVisibility(View.GONE);
        }

        binding.rowShipDiscount.setVisibility(shipDisc > 0 ? View.VISIBLE : View.GONE);
        binding.tvShipDiscount.setText("-" + VND_FORMAT.format(shipDisc) + "đ");

        binding.rowVoucherDiscount.setVisibility(vDisc > 0 ? View.VISIBLE : View.GONE);
        binding.tvDiscount.setText("-" + VND_FORMAT.format(vDisc) + "đ");

        binding.rowPointsDiscount.setVisibility(ptsDisc > 0 ? View.VISIBLE : View.GONE);
        binding.tvPointsDiscountSummary.setText("-" + VND_FORMAT.format(ptsDisc) + "đ");

        // Cập nhật dòng info trong toggle điểm
        binding.tvPointsDiscountAmount.setText("-" + VND_FORMAT.format(Math.min(pointsBalance, sub + ship)) + "đ");

        binding.tvGrandTotal.setText(VND_FORMAT.format(total) + "đ");
        binding.tvBottomTotal.setText(VND_FORMAT.format(total) + "đ");
        binding.tvVoucherValue.setText(appliedVoucher == null ? "" : appliedVoucher);
    }

    // ── ĐẶT HÀNG ─────────────────────────────────────────────────────────────
    private void placeOrder() {
        if (binding.rbWallet.isChecked()) {
            Toast.makeText(this, "Tính năng ví điện tử đang phát triển, vui lòng chọn COD hoặc Chuyển khoản", Toast.LENGTH_LONG).show();
            return;
        }
        if (shipFullAddress == null || shipFullAddress.isEmpty()) {
            Toast.makeText(this, R.string.checkout_err_no_address, Toast.LENGTH_SHORT).show();
            addressLauncher.launch(AddressListActivity.intent(this));
            return;
        }
        if (items.isEmpty()) return;

        binding.btnPlaceOrder.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        String uid = FirebaseHelper.getCurrentUserId();
        String method = binding.rbBank.isChecked() ? "BANK_TRANSFER" : "COD";
        double total = Math.max(0, subtotal() + shippingFee() - shipDiscount() - voucherDiscount() - pointsDiscount());

        String orderCode = "GLS-" + new SimpleDateFormat("yyMMdd", Locale.US).format(new Date())
                + "-" + String.format(Locale.US, "%04d", new Random().nextInt(10000));
        String shippingAddress = shipName + " | " + shipPhone + " | " + shipFullAddress;

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

    private void createPayment(String orderId, String uid, String method, double amount) {
        Map<String, Object> payment = new HashMap<>();
        payment.put("orderId", orderId);
        payment.put("customerId", uid);
        payment.put("method", method);
        payment.put("status", "PENDING");
        payment.put("transactionId", null);
        payment.put("amount", amount);
        payment.put("usedPoints", usePoints ? (long) pointsDiscount() : 0L);
        payment.put("createdAt", new Date());
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_PAYMENTS).add(payment);
    }

    private void removeOrderedItemsFromCart() {
        for (CartDetail d : items)
            cartRepo.removeFromCart(d.getCartDetailId(), new CartRepository.SimpleCallback() {
                @Override public void onSuccess() {}
                @Override public void onFailure(String error) {}
            });
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
                if (p.getImages() != null && !p.getImages().isEmpty())
                    Glide.with(holder.binding.ivProduct).load(p.getImages().get(0))
                            .placeholder(R.drawable.bg_product_placeholder)
                            .into(holder.binding.ivProduct);
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

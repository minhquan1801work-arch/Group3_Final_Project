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

    private static final String VOUCHER_FREESHIP  = "FREESHIP";
    private static final String VOUCHER_SHIP50    = "SHIP50";
    private static final String VOUCHER_NEWUSER   = "NEWUSER";
    private static final String VOUCHER_GIAM10    = "GIAM10";
    private static final String VOUCHER_MEMBER15  = "MEMBER15";
    private static final String VOUCHER_GIAM50K   = "GIAM50K";
    private static final String VOUCHER_SALE20    = "SALE20";
    private static final String VOUCHER_GIAM100K  = "GIAM100K";

    private ActivityCheckoutBinding binding;
    private final CartRepository cartRepo = new CartRepository();
    private final ProductRepository productRepo = new ProductRepository();
    private final OrderRepository orderRepo = new OrderRepository();

    private final List<CartDetail> items = new ArrayList<>();
    private Customer customer;
    private String appliedDiscountVoucher = null;
    private String appliedShipVoucher = null;

    // Điểm thành viên
    private int pointsBalance = 0;
    private boolean usePoints = false;

    // Địa chỉ giao hàng
    private String shipName, shipPhone, shipFullAddress, shipAddressId;
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
                            shipAddressId = result.getData().getStringExtra(AddressListActivity.RESULT_ADDRESS_ID);
                            bindAddress();
                        } else {
                            loadDefaultAddress();
                        }
                    });

    // Mở màn chọn voucher — nhận cả discount + ship code cùng lúc
    private final androidx.activity.result.ActivityResultLauncher<Intent> voucherLauncher =
            registerForActivityResult(
                    new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            String disc = result.getData().getStringExtra(CheckoutVoucherActivity.RESULT_DISCOUNT_CODE);
                            String ship = result.getData().getStringExtra(CheckoutVoucherActivity.RESULT_SHIP_CODE);
                            appliedDiscountVoucher = (disc == null || disc.isEmpty()) ? null : disc;
                            appliedShipVoucher = (ship == null || ship.isEmpty()) ? null : ship;
                            updateSummary();
                        }
                    });

    private static final String EXTRA_INIT_DISCOUNT_CODE = "init_discount_code";
    private static final String EXTRA_INIT_SHIP_CODE = "init_ship_code";

    // Chế độ "mua trực tiếp" cho KHÁCH chưa đăng nhập — không đi qua giỏ hàng
    private static final String EXTRA_DIRECT_PRODUCT_ID = "direct_product_id";
    private static final String EXTRA_DIRECT_COLOR = "direct_color";
    private static final String EXTRA_DIRECT_QTY = "direct_qty";

    private boolean isGuest = false;

    // Dữ liệu cascading dropdown cho guest form
    private final java.util.List<com.FinalProject.group3.utils.AddressApiHelper.AdminUnit> guestDistricts = new java.util.ArrayList<>();
    private final java.util.List<com.FinalProject.group3.utils.AddressApiHelper.AdminUnit> guestWards     = new java.util.ArrayList<>();

    /** Mua ngay không cần tài khoản: truyền thẳng sản phẩm, không qua cart. */
    public static void startDirect(Context context, String productId, String color, int quantity) {
        context.startActivity(new Intent(context, CheckoutActivity.class)
                .putExtra(EXTRA_DIRECT_PRODUCT_ID, productId)
                .putExtra(EXTRA_DIRECT_COLOR, color)
                .putExtra(EXTRA_DIRECT_QTY, quantity));
    }

    public static void start(Context context, ArrayList<String> cartDetailIds) {
        context.startActivity(new Intent(context, CheckoutActivity.class)
                .putStringArrayListExtra(EXTRA_CART_DETAIL_IDS, cartDetailIds));
    }

    public static void start(Context context, ArrayList<String> cartDetailIds,
                             String discountCode, String shipCode) {
        Intent intent = new Intent(context, CheckoutActivity.class)
                .putStringArrayListExtra(EXTRA_CART_DETAIL_IDS, cartDetailIds);
        if (discountCode != null && !discountCode.isEmpty())
            intent.putExtra(EXTRA_INIT_DISCOUNT_CODE, discountCode);
        if (shipCode != null && !shipCode.isEmpty())
            intent.putExtra(EXTRA_INIT_SHIP_CODE, shipCode);
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

        // Điểm thành viên toggle — snap back nếu không có điểm
        binding.swUsePoints.setOnCheckedChangeListener((btn, checked) -> {
            if (checked && pointsBalance <= 0) {
                btn.setChecked(false);
                binding.tvPointsError.setVisibility(View.VISIBLE);
                return;
            }
            binding.tvPointsError.setVisibility(View.GONE);
            usePoints = checked;
            binding.rowPointsInfo.setVisibility(checked ? View.VISIBLE : View.GONE);
            updateSummary();
        });

        // Mã giảm giá / vận chuyển → 1 picker, chọn được cả 2 loại độc lập
        binding.rowVoucher.setOnClickListener(v ->
                voucherLauncher.launch(
                        CheckoutVoucherActivity.intent(this, appliedDiscountVoucher, appliedShipVoucher, subtotal())));
        // Ô nhập mã inline
        binding.btnApplyVoucherInline.setOnClickListener(v ->
                applyVoucherCode(binding.etVoucher.getText().toString().trim().toUpperCase(Locale.US)));

        binding.rowAddress.setOnClickListener(v ->
                addressLauncher.launch(AddressListActivity.intentWithSelected(this, shipAddressId)));
        binding.btnEditAddress.setOnClickListener(v ->
                addressLauncher.launch(AddressListActivity.intentWithSelected(this, shipAddressId)));
        binding.btnPlaceOrder.setOnClickListener(v -> placeOrder());

        binding.rvProducts.setLayoutManager(new LinearLayoutManager(this));

        // Pre-fill vouchers nếu được truyền từ CartFragment
        String initDiscount = getIntent().getStringExtra(EXTRA_INIT_DISCOUNT_CODE);
        String initShip = getIntent().getStringExtra(EXTRA_INIT_SHIP_CODE);
        if (initDiscount != null) appliedDiscountVoucher = initDiscount;
        if (initShip != null) appliedShipVoucher = initShip;

        setupTermsNote();

        isGuest = FirebaseHelper.getCurrentUserId() == null;
        if (isGuest) setupGuestUi();
        else loadCustomer();

        String directProductId = getIntent().getStringExtra(EXTRA_DIRECT_PRODUCT_ID);
        if (directProductId != null) {
            loadDirectItem(directProductId,
                    getIntent().getStringExtra(EXTRA_DIRECT_COLOR),
                    getIntent().getIntExtra(EXTRA_DIRECT_QTY, 1));
        } else {
            loadItems(getIntent().getStringArrayListExtra(EXTRA_CART_DETAIL_IDS));
        }
    }

    // ── Chế độ KHÁCH: form nhập tay, ẩn điểm + sổ địa chỉ; voucher yêu cầu đăng nhập ──
    private void setupGuestUi() {
        binding.rowAddress.setVisibility(View.GONE);
        binding.layoutGuestAddress.setVisibility(View.VISIBLE);
        binding.layoutPointsSection.setVisibility(View.GONE);

        // Disable district/ward trước khi có data
        setGuestDropdownEnabled(binding.actGuestDistrict, false);
        setGuestDropdownEnabled(binding.actGuestWard,     false);

        // Tỉnh/TP: fetch từ API, khi chọn → load quận/huyện
        loadGuestProvinces();

        // Tách 2 block riêng: (1) chọn mã thành viên — cần đăng nhập, (2) nhập mã hiện có
        binding.tvVoucherRowTitle.setText("Lựa chọn mã giảm giá dành cho thành viên");
        binding.tvVoucherValue.setVisibility(android.view.View.GONE);
        binding.gapVoucherGuest.setVisibility(android.view.View.VISIBLE);
        binding.tvGuestVoucherLabel.setVisibility(android.view.View.VISIBLE);
        binding.rowVoucher.setOnClickListener(v ->
                com.FinalProject.group3.utils.LoginRequiredDialog.show(
                        this, "Lựa chọn mã giảm giá dành cho thành viên — vui lòng đăng nhập"));

        // Khách vẫn được nhập mã thủ công và được hưởng giảm giá nếu hợp lệ
        binding.etVoucher.setFocusableInTouchMode(true);
        binding.btnApplyVoucherInline.setOnClickListener(v ->
                applyVoucherCode(binding.etVoucher.getText().toString().trim()
                        .toUpperCase(java.util.Locale.US)));
    }

    private void loadGuestProvinces() {
        com.FinalProject.group3.utils.AddressApiHelper.fetchProvinces(units -> {
            android.widget.ArrayAdapter<com.FinalProject.group3.utils.AddressApiHelper.AdminUnit> adapter =
                    new android.widget.ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_1, units);
            binding.actGuestProvince.setAdapter(adapter);
            binding.actGuestProvince.setThreshold(0);
            binding.actGuestProvince.setKeyListener(null);
            binding.actGuestProvince.setOnClickListener(v -> binding.actGuestProvince.showDropDown());
            binding.actGuestProvince.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) binding.actGuestProvince.showDropDown();
            });
            binding.actGuestProvince.setOnItemClickListener((parent, view, pos, id) -> {
                com.FinalProject.group3.utils.AddressApiHelper.AdminUnit selected =
                        (com.FinalProject.group3.utils.AddressApiHelper.AdminUnit) parent.getItemAtPosition(pos);
                binding.actGuestDistrict.setText("", false);
                binding.actGuestWard.setText("", false);
                guestDistricts.clear(); guestWards.clear();
                setGuestDropdownEnabled(binding.actGuestWard, false);
                loadGuestDistricts(selected.code);
            });
        });
    }

    private void loadGuestDistricts(int provinceCode) {
        setGuestDropdownEnabled(binding.actGuestDistrict, false);
        com.FinalProject.group3.utils.AddressApiHelper.fetchDistricts(provinceCode, units -> {
            guestDistricts.clear();
            guestDistricts.addAll(units);
            android.widget.ArrayAdapter<com.FinalProject.group3.utils.AddressApiHelper.AdminUnit> adapter =
                    new android.widget.ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_1, guestDistricts);
            binding.actGuestDistrict.setAdapter(adapter);
            binding.actGuestDistrict.setThreshold(0);
            binding.actGuestDistrict.setKeyListener(null);
            binding.actGuestDistrict.setOnClickListener(v -> {
                if (!guestDistricts.isEmpty()) binding.actGuestDistrict.showDropDown();
            });
            binding.actGuestDistrict.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && !guestDistricts.isEmpty()) binding.actGuestDistrict.showDropDown();
            });
            binding.actGuestDistrict.setOnItemClickListener((parent, view, pos, id) -> {
                com.FinalProject.group3.utils.AddressApiHelper.AdminUnit selected =
                        (com.FinalProject.group3.utils.AddressApiHelper.AdminUnit) parent.getItemAtPosition(pos);
                binding.actGuestWard.setText("", false);
                guestWards.clear();
                setGuestDropdownEnabled(binding.actGuestWard, false);
                loadGuestWards(selected.code);
            });
            setGuestDropdownEnabled(binding.actGuestDistrict, true);
        });
    }

    private void loadGuestWards(int districtCode) {
        setGuestDropdownEnabled(binding.actGuestWard, false);
        com.FinalProject.group3.utils.AddressApiHelper.fetchWards(districtCode, units -> {
            guestWards.clear();
            guestWards.addAll(units);
            android.widget.ArrayAdapter<com.FinalProject.group3.utils.AddressApiHelper.AdminUnit> adapter =
                    new android.widget.ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_1, guestWards);
            binding.actGuestWard.setAdapter(adapter);
            binding.actGuestWard.setThreshold(0);
            binding.actGuestWard.setKeyListener(null);
            binding.actGuestWard.setOnClickListener(v -> {
                if (!guestWards.isEmpty()) binding.actGuestWard.showDropDown();
            });
            binding.actGuestWard.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && !guestWards.isEmpty()) binding.actGuestWard.showDropDown();
            });
            setGuestDropdownEnabled(binding.actGuestWard, true);
        });
    }

    private void setGuestDropdownEnabled(android.widget.AutoCompleteTextView view, boolean enabled) {
        view.setEnabled(enabled);
        // Dim cả TIL cha để viền + mũi tên cũng xám khi chưa chọn cấp trên
        android.view.View til = (android.view.View) view.getParent().getParent();
        til.setAlpha(enabled ? 1f : 0.4f);
    }

    // ── Mua trực tiếp: dựng CartDetail từ Intent, không đọc giỏ ───────────────
    private void loadDirectItem(String productId, String color, int quantity) {
        binding.progressBar.setVisibility(View.VISIBLE);
        CartDetail item = new CartDetail(productId, quantity, color);
        items.clear();
        items.add(item);
        loadProducts();
    }

    /** Footnote: "Điều khoản Glassity" → mở trang Điều khoản sử dụng. */
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
                startActivity(com.FinalProject.group3.ui.account.PolicyActivity.intent(
                        CheckoutActivity.this, com.FinalProject.group3.ui.account.PolicyActivity.TYPE_TERMS));
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
        binding.tvPointsLabel.setText(pointsBalance > 0
                ? "Sử dụng " + pointsBalance + " điểm thành viên"
                : "Sử dụng Điểm thành viên");
        binding.tvPointsBalance.setText(getString(R.string.checkout_points_balance, pointsBalance));
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
                    shipAddressId = def.getAddressId();
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
        binding.tvAddressError.setVisibility(View.GONE);
        if (shipFullAddress != null && !shipFullAddress.isEmpty()) {
            binding.tvReceiver.setText(shipName != null ? shipName : "");
            binding.tvReceiver.setVisibility(View.VISIBLE);
            binding.tvAddress.setText((shipPhone != null ? shipPhone + "\n" : "") + shipFullAddress);
            binding.tvAddress.setTextColor(getColor(R.color.color_text_secondary));
        } else {
            binding.tvReceiver.setVisibility(View.GONE);
            binding.tvAddress.setText("Nhấn để thêm địa chỉ nhận hàng");
            binding.tvAddress.setTextColor(getColor(R.color.color_hint));
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
        double sub = subtotal();
        if (VOUCHER_FREESHIP.equals(appliedShipVoucher)) return shippingFee();
        if (VOUCHER_SHIP50.equals(appliedShipVoucher) && sub >= 200000) return shippingFee() * 0.5;
        return 0;
    }

    private double voucherDiscount() {
        if (appliedDiscountVoucher == null) return 0;
        double sub = subtotal();
        switch (appliedDiscountVoucher) {
            case VOUCHER_NEWUSER:  return sub >= 100000 ? 30000 : 0;
            case VOUCHER_GIAM10:   return sub >= 300000 ? Math.min(sub * 0.10, 100000) : 0;
            case VOUCHER_MEMBER15: return sub >= 400000 ? Math.min(sub * 0.15, 150000) : 0;
            case VOUCHER_GIAM50K:  return sub >= 500000 ? 50000 : 0;
            case VOUCHER_SALE20:   return sub >= 500000 ? Math.min(sub * 0.20, 200000) : 0;
            case VOUCHER_GIAM100K: return sub >= 800000 ? 100000 : 0;
            default: return 0;
        }
    }

    private double pointsDiscount() {
        if (!usePoints || pointsBalance <= 0) return 0;
        double beforePoints = subtotal() + shippingFee() - shipDiscount() - voucherDiscount();
        return Math.min(pointsBalance, Math.max(0, beforePoints));
    }

    private int rewardPointsEarned() {
        if (isGuest) return 0; // khách không có tài khoản tích điểm
        return (int) (subtotal() / 1000);
    }

    private void applyVoucherCode(String code) {
        if (code.isEmpty()) {
            appliedDiscountVoucher = null;
            appliedShipVoucher = null;
            updateSummary();
            return;
        }
        double sub = subtotal();
        switch (code) {
            case VOUCHER_FREESHIP:
                appliedShipVoucher = code; break;
            case VOUCHER_SHIP50:
                if (sub < 200000) { toast("SHIP50 áp dụng đơn từ 200.000đ"); return; }
                appliedShipVoucher = code; break;
            case VOUCHER_NEWUSER:
                if (sub < 100000) { toast("NEWUSER áp dụng đơn từ 100.000đ"); return; }
                appliedDiscountVoucher = code; break;
            case VOUCHER_GIAM10:
                if (sub < 300000) { toast("GIAM10 áp dụng đơn từ 300.000đ"); return; }
                appliedDiscountVoucher = code; break;
            case VOUCHER_MEMBER15:
                if (sub < 400000) { toast("MEMBER15 áp dụng đơn từ 400.000đ"); return; }
                appliedDiscountVoucher = code; break;
            case VOUCHER_GIAM50K:
                if (sub < 500000) { toast("GIAM50K áp dụng đơn từ 500.000đ"); return; }
                appliedDiscountVoucher = code; break;
            case VOUCHER_SALE20:
                if (sub < 500000) { toast("SALE20 áp dụng đơn từ 500.000đ"); return; }
                appliedDiscountVoucher = code; break;
            case VOUCHER_GIAM100K:
                if (sub < 800000) { toast("GIAM100K áp dụng đơn từ 800.000đ"); return; }
                appliedDiscountVoucher = code; break;
            default:
                Toast.makeText(this, "Mã không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
        }
        Toast.makeText(this, "Đã áp dụng mã " + code, Toast.LENGTH_SHORT).show();
        updateSummary();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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

        // Row mã giảm giá (1 dòng hiện cả discount + ship)
        StringBuilder voucherText = new StringBuilder();
        boolean hasDiscount = false, hasShip = false;
        if (appliedDiscountVoucher != null) {
            if (vDisc > 0) { voucherText.append("-").append(VND_FORMAT.format(vDisc)).append("đ"); hasDiscount = true; }
            else { voucherText.append(appliedDiscountVoucher).append(" (chưa đủ ĐK)"); }
        }
        if (appliedShipVoucher != null) {
            if (voucherText.length() > 0) voucherText.append(" · ");
            if (shipDisc > 0) { voucherText.append("Miễn phí vận chuyển"); hasShip = true; }
            else voucherText.append(appliedShipVoucher).append(" (chưa đủ ĐK)");
        }
        if (voucherText.length() == 0) {
            binding.tvVoucherValue.setText("Chọn mã");
            binding.tvVoucherValue.setTextColor(getColor(R.color.color_hint));
        } else {
            binding.tvVoucherValue.setText(voucherText.toString());
            binding.tvVoucherValue.setTextColor((hasDiscount || hasShip)
                    ? getColor(R.color.color_price) : getColor(R.color.color_hint));
        }
    }

    // ── ĐẶT HÀNG ─────────────────────────────────────────────────────────────
    private void placeOrder() {
        if (binding.rbWallet.isChecked()) {
            Toast.makeText(this, "Tính năng ví điện tử đang phát triển, vui lòng chọn COD hoặc Chuyển khoản", Toast.LENGTH_LONG).show();
            return;
        }

        String guestEmail = null;
        if (isGuest) {
            // Khách: validate form nhập tay (tên, SĐT, email, Tỉnh/Thành, Phường/Xã, địa chỉ chi tiết)
            String name     = binding.etGuestName.getText().toString().trim();
            String phone    = binding.etGuestPhone.getText().toString().trim();
            String email    = binding.etGuestEmail.getText().toString().trim();
            String province = binding.actGuestProvince.getText().toString().trim();
            String district = binding.actGuestDistrict.getText().toString().trim();
            String ward     = binding.actGuestWard.getText().toString().trim();
            String detail   = binding.etGuestDetail.getText().toString().trim();
            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || !email.contains("@")
                    || province.isEmpty() || district.isEmpty() || ward.isEmpty() || detail.isEmpty()) {
                binding.tvGuestAddressError.setText(R.string.err_guest_form_incomplete);
                binding.tvGuestAddressError.setVisibility(View.VISIBLE);
                binding.nestedScroll.post(() -> binding.nestedScroll.smoothScrollTo(0, 0));
                return;
            }
            if (!com.FinalProject.group3.utils.ValidationUtils.isValidPhone(phone)) {
                binding.tvGuestAddressError.setText(R.string.err_phone_invalid);
                binding.tvGuestAddressError.setVisibility(View.VISIBLE);
                binding.nestedScroll.post(() -> binding.nestedScroll.smoothScrollTo(0, 0));
                return;
            }
            binding.tvGuestAddressError.setVisibility(View.GONE);
            shipName = name;
            shipPhone = phone;
            // Định dạng khớp Address.fullAddress(): "detail, ward, district, province"
            shipFullAddress = detail + ", " + ward + ", " + district + ", " + province;
            guestEmail = email;
        } else if (shipFullAddress == null || shipFullAddress.isEmpty()) {
            binding.tvAddressError.setVisibility(View.VISIBLE);
            binding.nestedScroll.post(() -> binding.nestedScroll.smoothScrollTo(0, 0));
            return;
        }
        if (items.isEmpty()) return;

        binding.btnPlaceOrder.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        String uid = isGuest ? "GUEST" : FirebaseHelper.getCurrentUserId();
        final String finalGuestEmail = guestEmail;
        final String finalGuestPhone = isGuest ? shipPhone : null;
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

        int earnedPoints = rewardPointsEarned();
        int usedPoints2 = usePoints ? (int) pointsDiscount() : 0;

        order.setShippingFee(shippingFee());
        order.setShipDiscount(shipDiscount());
        order.setVoucherDiscount(voucherDiscount());
        order.setUsedPoints(usedPoints2);
        order.setEarnedPoints(earnedPoints);

        orderRepo.createOrder(order, details, new OrderRepository.SimpleCallback() {
            @Override
            public void onSuccess(String orderId) {
                if (isGuest) {
                    // Lưu email/SĐT khách vào đơn — để claim về tài khoản nếu họ đăng ký sau
                    Map<String, Object> guestInfo = new HashMap<>();
                    guestInfo.put("guestEmail", finalGuestEmail);
                    guestInfo.put("guestPhone", finalGuestPhone);
                    FirebaseHelper.getDb().collection(FirebaseHelper.COL_ORDERS)
                            .document(orderId).update(guestInfo);
                } else {
                    removeOrderedItemsFromCart();
                    updateCustomerPoints(uid, earnedPoints, usedPoints2);
                    // Tóm tắt sản phẩm cho thông báo: "Tên kính đầu tiên (+N sản phẩm)"
                    String productSummary = null;
                    if (!items.isEmpty() && items.get(0).getProduct() != null) {
                        productSummary = items.get(0).getProduct().getName();
                        if (items.size() > 1) {
                            productSummary += " (+" + (items.size() - 1) + " san pham)";
                        }
                    }
                    com.FinalProject.group3.utils.NotificationHelper
                            .sendOrderPlaced(uid, orderId, orderCode, method, productSummary);
                }
                createPayment(orderId, uid, method, total);
                PaymentResultActivity.start(CheckoutActivity.this, orderCode, orderId, total, method);
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

    private void updateCustomerPoints(String uid, int earned, int used) {
        long delta = earned - used;
        if (delta == 0) return;
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_CUSTOMERS)
                .document(uid)
                .update("points", com.google.firebase.firestore.FieldValue.increment(delta));
    }

    // ── Adapter danh sách sản phẩm rút gọn ───────────────────────────────────
    private class CheckoutProductAdapter
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
                // Ảnh: ưu tiên ảnh của variant khớp màu đang chọn
                String imgUrl = null;
                if (p.getVariants() != null && !p.getVariants().isEmpty()) {
                    for (com.FinalProject.group3.model.ProductVariant v : p.getVariants()) {
                        if (v.getColor() != null && v.getColor().equalsIgnoreCase(d.getColor())
                                && v.getImages() != null && !v.getImages().isEmpty()) {
                            imgUrl = v.getImages().get(0);
                            break;
                        }
                    }
                }
                if (imgUrl == null && p.getImages() != null && !p.getImages().isEmpty())
                    imgUrl = p.getImages().get(0);
                if (imgUrl != null)
                    Glide.with(holder.binding.ivProduct).load(com.FinalProject.group3.utils.CloudinaryUtil.optimize(imgUrl, 250))
                            .placeholder(R.drawable.bg_product_placeholder)
                            .into(holder.binding.ivProduct);
            }
            holder.binding.tvQty.setText(String.valueOf(d.getQuantity()));

            // Color chip — hiện tên màu tiếng Việt từ variant thay vì mã hex
            holder.binding.tvColor.setText(displayColorName(p, d.getColor()));

            // Stepper
            holder.binding.btnMinus.setOnClickListener(v -> {
                if (d.getQuantity() <= 1) return;
                d.setQuantity(d.getQuantity() - 1);
                notifyItemChanged(holder.getAdapterPosition());
                updateSummary();
            });
            holder.binding.btnPlus.setOnClickListener(v -> {
                int maxStock = resolveStock(p, d.getColor());
                if (d.getQuantity() >= maxStock) {
                    Toast.makeText(CheckoutActivity.this, "Đã đạt số lượng tối đa trong kho", Toast.LENGTH_SHORT).show();
                    return;
                }
                d.setQuantity(d.getQuantity() + 1);
                notifyItemChanged(holder.getAdapterPosition());
                updateSummary();
            });

            holder.binding.layoutColorPicker.setOnClickListener(v -> showColorPicker(d, p, holder));
            holder.binding.tvQty.setOnClickListener(v -> showQtyEditor(d, p, holder));
        }

        /** Kho của đúng variant đang chọn (theo màu); fallback field stock cũ nếu không có variants. */
        private int resolveStock(Product p, String color) {
            if (p == null) return 99;
            List<com.FinalProject.group3.model.ProductVariant> variants = p.getVariants();
            if (variants != null && !variants.isEmpty()) {
                for (com.FinalProject.group3.model.ProductVariant v : variants) {
                    if (v.getColor() != null && v.getColor().equalsIgnoreCase(color)) return v.getStock();
                }
            }
            return p.getStock() > 0 ? p.getStock() : 99;
        }

        private void showColorPicker(CartDetail d, Product p, VH holder) {
            if (p == null) return;

            // Đóng bàn phím nếu có (guest form còn field đang focus) rồi đợi đúng 1 khung
            // hình cho layout ổn định mới mở popup — ListPopupWindow tính vị trí/kích thước
            // theo layout hiện tại, mở ngay lúc bàn phím đang thu vào sẽ bị lệch vùng nhận touch.
            // Luôn post() (không dựa vào giá trị trả về của hideSoftInputFromWindow, vốn không
            // đồng nhất giữa các máy/phiên bản Android).
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(holder.itemView.getWindowToken(), 0);

            holder.itemView.post(() -> {
                // Ưu tiên variants (data mới): value = hex, label = colorName tiếng Việt
                final List<String> colorValues = new ArrayList<>();
                final List<String> colorLabels = new ArrayList<>();
                if (p.getVariants() != null && !p.getVariants().isEmpty()) {
                    for (com.FinalProject.group3.model.ProductVariant v : p.getVariants()) {
                        if (v.getColor() == null || v.getColor().isEmpty()) continue;
                        colorValues.add(v.getColor());
                        colorLabels.add((v.getColorName() != null && !v.getColorName().isEmpty())
                                ? v.getColorName() : v.getColor());
                    }
                } else if (p.getColors() != null) {
                    for (String c : p.getColors()) { colorValues.add(c); colorLabels.add(c); }
                }
                if (colorValues.isEmpty()) return;

                android.widget.ArrayAdapter<String> arrAdapter = new android.widget.ArrayAdapter<>(
                        CheckoutActivity.this,
                        R.layout.item_color_popup, colorLabels);
                android.widget.ListPopupWindow popup = new android.widget.ListPopupWindow(CheckoutActivity.this);
                popup.setAnchorView(holder.binding.layoutColorPicker);
                popup.setAdapter(arrAdapter);
                popup.setWidth(Math.max(holder.binding.layoutColorPicker.getWidth(), 360));
                popup.setModal(true);
                popup.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0xFFFFFFFF));
                popup.setOnItemClickListener((parent, view, which, id) -> {
                    d.setColor(colorValues.get(which));
                    notifyItemChanged(holder.getAdapterPosition());
                    popup.dismiss();
                });
                popup.show();
            });
        }

        /** Tên màu hiển thị: tra colorName trong variants theo hex đang chọn */
        private String displayColorName(Product p, String colorHex) {
            if (p != null && p.getVariants() != null) {
                for (com.FinalProject.group3.model.ProductVariant v : p.getVariants()) {
                    if (v.getColor() != null && v.getColor().equalsIgnoreCase(colorHex)
                            && v.getColorName() != null && !v.getColorName().isEmpty())
                        return v.getColorName();
                }
            }
            return (colorHex != null && !colorHex.isEmpty()) ? colorHex : "Mặc định";
        }

        private void showQtyEditor(CartDetail d, Product p, VH holder) {
            android.widget.EditText et = new android.widget.EditText(CheckoutActivity.this);
            et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            et.setText(String.valueOf(d.getQuantity()));
            et.setSelectAllOnFocus(true);
            et.setPadding(48, 24, 48, 24);
            new androidx.appcompat.app.AlertDialog.Builder(CheckoutActivity.this)
                    .setTitle("Số lượng")
                    .setView(et)
                    .setPositiveButton("OK", (dlg, which) -> {
                        try {
                            int qty = Integer.parseInt(et.getText().toString().trim());
                            int maxStock = resolveStock(p, d.getColor());
                            if (qty < 1) qty = 1;
                            if (qty > maxStock) {
                                Toast.makeText(CheckoutActivity.this,
                                        "Tối đa " + maxStock + " sản phẩm trong kho",
                                        Toast.LENGTH_SHORT).show();
                                qty = maxStock;
                            }
                            d.setQuantity(qty);
                            notifyItemChanged(holder.getAdapterPosition());
                            updateSummary();
                        } catch (NumberFormatException ignored) {}
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        }

        @Override
        public int getItemCount() { return items.size(); }

        class VH extends RecyclerView.ViewHolder {
            final ItemCheckoutProductBinding binding;
            VH(ItemCheckoutProductBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}

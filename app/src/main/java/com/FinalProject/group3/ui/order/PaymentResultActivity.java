package com.FinalProject.group3.ui.order;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.FinalProject.group3.MainActivity;
import com.FinalProject.group3.R;
import com.FinalProject.group3.adapter.ProductAdapter;
import com.FinalProject.group3.databinding.ActivityPaymentResultBinding;
import com.FinalProject.group3.model.CartDetail;
import com.FinalProject.group3.model.Product;
import com.FinalProject.group3.repository.CartRepository;
import com.FinalProject.group3.repository.ProductRepository;
import com.FinalProject.group3.utils.FirebaseHelper;

import java.util.ArrayList;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * LA_Payment — màn kết quả sau khi ĐẶT HÀNG (Figma frame LA_Pay...).
 *
 * 2 biến thể theo phương thức thanh toán:
 *  - COD           → "ĐẶT HÀNG THÀNH CÔNG" (icon check xanh)
 *  - BANK_TRANSFER → "ĐANG CHỜ THANH TOÁN" (icon đồng hồ) + thông tin chuyển khoản
 *
 * Bên dưới có grid "Có thể bạn cũng thích" (giữ chân khách tiếp tục mua sắm).
 */
public class PaymentResultActivity extends AppCompatActivity {

    private static final String EXTRA_ORDER_CODE = "order_code";
    private static final String EXTRA_ORDER_ID   = "order_id";
    private static final String EXTRA_AMOUNT = "amount";
    private static final String EXTRA_METHOD = "method";
    private static final NumberFormat VND_FORMAT = NumberFormat.getInstance(new Locale("vi", "VN"));

    private ActivityPaymentResultBinding binding;

    public static void start(Context context, String orderCode, String orderId,
                              double amount, String method) {
        Intent intent = new Intent(context, PaymentResultActivity.class);
        intent.putExtra(EXTRA_ORDER_CODE, orderCode);
        intent.putExtra(EXTRA_ORDER_ID, orderId);
        intent.putExtra(EXTRA_AMOUNT, amount);
        intent.putExtra(EXTRA_METHOD, method);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        com.FinalProject.group3.utils.InsetsUtil.applySystemBarsPadding(binding.getRoot());

        String orderCode = getIntent().getStringExtra(EXTRA_ORDER_CODE);
        String orderId   = getIntent().getStringExtra(EXTRA_ORDER_ID);
        double amount = getIntent().getDoubleExtra(EXTRA_AMOUNT, 0);
        String method = getIntent().getStringExtra(EXTRA_METHOD);

        binding.tvOrderCode.setText("Mã đơn: " + orderCode);

        if ("BANK_TRANSFER".equals(method)) {
            // Biến thể 1: chờ chuyển khoản
            binding.ivStatus.setImageResource(R.drawable.ic_clock);
            binding.tvStatusTitle.setText(R.string.payment_pending_title);
            binding.tvStatusDesc.setText(R.string.payment_pending_desc);
            binding.llBankInfo.setVisibility(View.VISIBLE);
            setupVietQR(orderCode, amount);
        } else {
            // Biến thể 2: COD đặt hàng thành công
            binding.ivStatus.setImageResource(R.drawable.ic_check_circle);
            binding.tvStatusTitle.setText(R.string.payment_success_title);
            binding.tvStatusDesc.setText(R.string.payment_success_desc);
        }

        binding.btnGoHome.setOnClickListener(v -> {
            // Quay về MainActivity, xóa hết stack Checkout phía trên
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        final String finalOrderId = orderId;
        binding.btnViewOrder.setOnClickListener(v -> {
            if (finalOrderId != null && !finalOrderId.isEmpty())
                startActivity(OrderDetailActivity.intent(this, finalOrderId));
            else
                startActivity(OrderHistoryActivity.intent(this));
        });

        loadSuggestions();
    }

    // "Có thể bạn cũng thích" — lấy sản phẩm bán chạy
    private void loadSuggestions() {
        binding.rvSuggest.setLayoutManager(new GridLayoutManager(this, 2));
        ProductAdapter adapter = new ProductAdapter(product ->
                com.FinalProject.group3.ui.catalog.ProductDetailActivity
                        .start(this, product.getProductId()));

        // "Thêm vào giỏ" — dùng CartQuickActions để thống nhất luồng (không Toast)
        adapter.setOnAddToCartListener((product, itemThumbnailView) ->
                com.FinalProject.group3.utils.CartQuickActions.addToCart(this, product));

        // "Mua ngay" → thêm cart với qty=1 + màu đầu tiên → thẳng Checkout
        adapter.setOnBuyNowListener(product -> {
            if (FirebaseHelper.getCurrentUserId() == null) {
                Toast.makeText(this, "Vui lòng đăng nhập để mua hàng", Toast.LENGTH_SHORT).show();
                return;
            }
            String color = (product.getColors() != null && !product.getColors().isEmpty())
                    ? product.getColors().get(0) : "";
            CartDetail item = new CartDetail(product.getProductId(), 1, color);
            new CartRepository().addToCartReturningId(item, new CartRepository.IdCallback() {
                @Override public void onSuccess(String cartDetailId) {
                    ArrayList<String> ids = new ArrayList<>();
                    ids.add(cartDetailId);
                    CheckoutActivity.start(PaymentResultActivity.this, ids);
                }
                @Override public void onFailure(String error) {
                    Toast.makeText(PaymentResultActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.rvSuggest.setAdapter(adapter);

        new ProductRepository().getBestSellerProducts(6, new ProductRepository.ProductListCallback() {
            @Override public void onSuccess(List<Product> products) { adapter.submitList(products); }
            @Override public void onFailure(String error) { }
        });
    }

    private void setupVietQR(String orderCode, double amount) {
        // Nội dung chuyển khoản: "GLASSITY <mã đơn>" — dễ đối soát
        String transferContent = "GLASSITY " + (orderCode != null ? orderCode : "");
        String amountText = VND_FORMAT.format((long) amount) + "đ";

        // Điền text
        binding.tvBankAmount.setText(amountText);
        binding.tvBankContent.setText(transferContent);

        // VietQR URL — MB Bank, tài khoản Nguyen Minh Quan, template compact2
        // compact2: hiển thị logo ngân hàng + QR + tên TK trong một ảnh duy nhất
        String qrUrl = "https://img.vietqr.io/image/MB-0977780173-compact2.jpg"
                + "?amount=" + (long) amount
                + "&addInfo=" + Uri.encode(transferContent)
                + "&accountName=Nguyen%20Minh%20Quan";

        com.bumptech.glide.Glide.with(this)
                .load(qrUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(binding.ivVietQR);

        // Copy nội dung CK
        binding.btnCopyContent.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("nội dung CK", transferContent));
            Toast.makeText(this, "Đã copy nội dung CK", Toast.LENGTH_SHORT).show();
        });

        // Copy STK
        binding.btnCopyAccount.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("STK", "0977780173"));
            Toast.makeText(this, "Đã copy số tài khoản", Toast.LENGTH_SHORT).show();
        });
    }
}

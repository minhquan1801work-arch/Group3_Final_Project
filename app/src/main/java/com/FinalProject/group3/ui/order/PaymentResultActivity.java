package com.FinalProject.group3.ui.order;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.FinalProject.group3.MainActivity;
import com.FinalProject.group3.R;
import com.FinalProject.group3.adapter.ProductAdapter;
import com.FinalProject.group3.databinding.ActivityPaymentResultBinding;
import com.FinalProject.group3.model.Product;
import com.FinalProject.group3.repository.ProductRepository;

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
    private static final String EXTRA_AMOUNT = "amount";
    private static final String EXTRA_METHOD = "method";
    private static final NumberFormat VND_FORMAT = NumberFormat.getInstance(new Locale("vi", "VN"));

    private ActivityPaymentResultBinding binding;

    public static void start(Context context, String orderCode, double amount, String method) {
        Intent intent = new Intent(context, PaymentResultActivity.class);
        intent.putExtra(EXTRA_ORDER_CODE, orderCode);
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
        double amount = getIntent().getDoubleExtra(EXTRA_AMOUNT, 0);
        String method = getIntent().getStringExtra(EXTRA_METHOD);

        binding.tvOrderCode.setText("Mã đơn: " + orderCode);

        if ("BANK_TRANSFER".equals(method)) {
            // Biến thể 1: chờ chuyển khoản
            binding.ivStatus.setImageResource(R.drawable.ic_clock);
            binding.tvStatusTitle.setText(R.string.payment_pending_title);
            binding.tvStatusDesc.setText(R.string.payment_pending_desc);
            binding.llBankInfo.setVisibility(android.view.View.VISIBLE);
            binding.tvBankDetail.setText(
                    "Ngân hàng: Vietcombank\n" +
                    "Số tài khoản: 0123456789\n" +
                    "Chủ tài khoản: GLASSITY STORE\n" +
                    "Số tiền: " + VND_FORMAT.format(amount) + "đ\n" +
                    "Nội dung CK: " + orderCode);
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

        binding.btnViewOrder.setOnClickListener(v ->
                // TODO [A4]: mở OrderHistoryActivity khi hoàn thành
                Toast.makeText(this, "Lịch sử đơn hàng — đang phát triển (task A4)",
                        Toast.LENGTH_SHORT).show());

        loadSuggestions();
    }

    // "Có thể bạn cũng thích" — lấy sản phẩm bán chạy
    private void loadSuggestions() {
        binding.rvSuggest.setLayoutManager(new GridLayoutManager(this, 2));
        ProductAdapter adapter = new ProductAdapter(product ->
                com.FinalProject.group3.ui.catalog.ProductDetailActivity
                        .start(this, product.getProductId()));
        binding.rvSuggest.setAdapter(adapter);

        new ProductRepository().getBestSellerProducts(6, new ProductRepository.ProductListCallback() {
            @Override public void onSuccess(List<Product> products) { adapter.submitList(products); }
            @Override public void onFailure(String error) { }
        });
    }
}

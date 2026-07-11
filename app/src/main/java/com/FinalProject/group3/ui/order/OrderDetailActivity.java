package com.FinalProject.group3.ui.order;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalProject.group3.databinding.ActivityOrderDetailBinding;
import com.FinalProject.group3.databinding.ItemOrderDetailProductBinding;
import com.FinalProject.group3.model.Order;
import com.FinalProject.group3.model.OrderDetail;
import com.FinalProject.group3.model.Product;
import com.FinalProject.group3.repository.OrderRepository;
import com.FinalProject.group3.repository.ProductRepository;
import com.FinalProject.group3.utils.FirebaseHelper;
import com.FinalProject.group3.utils.InsetsUtil;
import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderDetailActivity extends AppCompatActivity {

    private static final String EXTRA_ORDER_ID = "orderId";

    public static Intent intent(Context context, String orderId) {
        return new Intent(context, OrderDetailActivity.class)
                .putExtra(EXTRA_ORDER_ID, orderId);
    }

    private ActivityOrderDetailBinding binding;
    private final OrderRepository orderRepo = new OrderRepository();
    private final ProductRepository productRepo = new ProductRepository();
    private ProductsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applySystemBarsPadding(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        adapter = new ProductsAdapter(new ArrayList<>());
        binding.rvProducts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvProducts.setAdapter(adapter);

        String orderId = getIntent().getStringExtra(EXTRA_ORDER_ID);
        if (orderId != null) loadOrder(orderId);
    }

    private void loadOrder(String orderId) {
        binding.progressBar.setVisibility(View.VISIBLE);
        orderRepo.getOrderById(orderId, new OrderRepository.OrderCallback() {
            @Override
            public void onSuccess(Order order) {
                bindOrder(order);
                loadDetails(orderId);
            }

            @Override
            public void onFailure(String error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(OrderDetailActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindOrder(Order o) {
        binding.tvOrderCode.setText(o.getOrderCode() != null ? o.getOrderCode() : o.getOrderId());
        binding.tvShippingStatus.setText(statusLabel(o.getOrderStatus()));
        binding.tvPaymentMethod.setText(paymentLabel(o.getPaymentMethod()));
        binding.tvShippingAddress.setText(o.getShippingAddress());

        if (o.getCreatedAt() != null) {
            binding.tvPaymentDate.setText(new SimpleDateFormat("HH:mm · dd/MM/yyyy", Locale.US)
                    .format(o.getCreatedAt()));
        }

        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        binding.tvTotal.setText(fmt.format(o.getTotalAmount()) + "đ");

        // Phí vận chuyển (nếu có lưu, dùng giá trị đó; nếu không thì ẩn)
        if (o.getShippingFee() > 0) {
            binding.tvShippingFee.setText(fmt.format(o.getShippingFee()) + "đ");
        } else {
            binding.tvShippingFee.setText("—");
        }

        // Giảm phí ship
        if (o.getShipDiscount() > 0) {
            binding.rowShipDiscount.setVisibility(android.view.View.VISIBLE);
            binding.tvShipDiscount.setText("-" + fmt.format(o.getShipDiscount()) + "đ");
        }

        // Voucher giảm giá
        if (o.getVoucherDiscount() > 0) {
            binding.rowVoucherDiscount.setVisibility(android.view.View.VISIBLE);
            binding.tvVoucherDiscount.setText("-" + fmt.format(o.getVoucherDiscount()) + "đ");
        }

        // Điểm đã dùng
        if (o.getUsedPoints() > 0) {
            binding.rowPointsUsed.setVisibility(android.view.View.VISIBLE);
            binding.tvPointsUsed.setText("-" + fmt.format(o.getUsedPoints()) + "đ");
        }

        // Điểm tích lũy
        if (o.getEarnedPoints() > 0) {
            binding.rowEarnedPoints.setVisibility(android.view.View.VISIBLE);
            binding.tvEarnedPoints.setText("+" + o.getEarnedPoints() + " điểm");
        }
    }

    private void loadDetails(String orderId) {
        com.FinalProject.group3.utils.FirebaseHelper.getDb()
                .collection(com.FinalProject.group3.utils.FirebaseHelper.COL_ORDERS)
                .document(orderId)
                .collection(com.FinalProject.group3.utils.FirebaseHelper.COL_ORDER_DETAILS)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<OrderDetail> details = snapshot.toObjects(OrderDetail.class);
                    enrichProducts(details);
                })
                .addOnFailureListener(e -> binding.progressBar.setVisibility(View.GONE));
    }

    private void enrichProducts(List<OrderDetail> details) {
        if (details.isEmpty()) {
            binding.progressBar.setVisibility(View.GONE);
            return;
        }
        AtomicInteger remaining = new AtomicInteger(details.size());
        for (OrderDetail od : details) {
            productRepo.getProductById(od.getProductId(), new ProductRepository.ProductCallback() {
                @Override
                public void onSuccess(Product product) {
                    od.setProduct(product);
                    if (remaining.decrementAndGet() == 0) {
                        binding.progressBar.setVisibility(View.GONE);
                        adapter.setData(details);
                        bindSubtotal(details);
                    }
                }

                @Override
                public void onFailure(String error) {
                    if (remaining.decrementAndGet() == 0) {
                        binding.progressBar.setVisibility(View.GONE);
                        adapter.setData(details);
                        bindSubtotal(details);
                    }
                }
            });
        }
    }

    private void bindSubtotal(List<OrderDetail> details) {
        double sub = 0;
        for (OrderDetail od : details) sub += od.getPrice() * od.getQuantity();
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        binding.tvSubtotal.setText(fmt.format(sub) + "đ");
        binding.tvSubtotalLabel.setText("Tổng tiền hàng (" + details.size() + " sản phẩm)");
    }

    private String statusLabel(String s) {
        if (s == null) return "";
        switch (s) {
            case "PENDING": return "Chờ xác nhận";
            case "PROCESSING": return "Đang xử lý";
            case "SHIPPED": return "Đang giao hàng";
            case "DELIVERED": return "Đã giao thành công";
            case "COMPLETED": return "Hoàn thành";
            case "CANCELLED": return "Đã hủy";
            default: return s;
        }
    }

    private String paymentLabel(String s) {
        if ("BANK_TRANSFER".equals(s)) return "Chuyển khoản ngân hàng";
        return "Thanh toán khi nhận hàng (COD)";
    }

    // ── Adapter ──────────────────────────────────────────────────────────────

    static class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.VH> {

        private List<OrderDetail> data;

        ProductsAdapter(List<OrderDetail> data) { this.data = data; }

        void setData(List<OrderDetail> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemOrderDetailProductBinding b = ItemOrderDetailProductBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new VH(b);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            holder.bind(data.get(position));
        }

        @Override
        public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            final ItemOrderDetailProductBinding b;

            VH(ItemOrderDetailProductBinding b) {
                super(b.getRoot());
                this.b = b;
            }

            void bind(OrderDetail od) {
                NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
                b.tvPrice.setText(fmt.format(od.getPrice()) + "đ");
                b.tvQty.setText("x" + od.getQuantity());
                b.tvColor.setText(od.getColor() != null ? od.getColor() : "");

                if (od.getProduct() != null) {
                    b.tvName.setText(od.getProduct().getName());
                    List<String> imgs = od.getProduct().getImages();
                    if (imgs != null && !imgs.isEmpty()) {
                        Glide.with(b.ivProduct.getContext())
                                .load(com.FinalProject.group3.utils.CloudinaryUtil.optimize(imgs.get(0), 250))
                                .centerCrop()
                                .into(b.ivProduct);
                    }
                } else {
                    b.tvName.setText("Sản phẩm #" + od.getProductId());
                }
            }
        }
    }
}

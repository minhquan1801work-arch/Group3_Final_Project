package com.FinalProject.group3.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.FinalProject.group3.R;
import com.FinalProject.group3.databinding.FragmentProfileBinding;
import com.FinalProject.group3.utils.FirebaseHelper;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * LA.Profile — trang THÀNH VIÊN: thẻ barcode + điểm, grid tiện ích,
 * list hỗ trợ/chính sách. Các mục chưa làm → Toast (TODO nối dần:
 * Đơn hàng → A4 OrderHistory, Thông tin tài khoản → màn sửa profile...).
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Số thẻ demo sinh từ uid — mỗi user 1 số ổn định
        FirebaseUser user = FirebaseHelper.getAuth().getCurrentUser();
        if (user != null) {
            int hash = Math.abs(user.getUid().hashCode()) % 1000000000;
            String digits = String.format(java.util.Locale.US, "%09d", hash);
            binding.tvMemberCode.setText(digits.substring(0, 4) + " " + digits.substring(4) + " ⧉");

            // Load điểm thật từ Firestore
            com.FinalProject.group3.utils.FirebaseHelper.getDb()
                    .collection(com.FinalProject.group3.utils.FirebaseHelper.COL_CUSTOMERS)
                    .document(user.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists() && binding != null) {
                            Long pts = doc.getLong("points");
                            int p = (pts != null) ? pts.intValue() : 0;
                            binding.tvPoints.setText(p + " điểm");
                        }
                    });
        }

        // Grid tiện ích
        binding.menuOrders.setOnClickListener(v ->
                startActivity(com.FinalProject.group3.ui.order.OrderHistoryActivity
                        .intent(requireContext())));
        binding.menuPoints.setOnClickListener(v ->
                startActivity(PointHistoryActivity.intent(requireContext())));
        binding.menuVouchers.setOnClickListener(v ->
                startActivity(VoucherActivity.intent(requireContext())));
        binding.menuAccount.setOnClickListener(v ->
                startActivity(AccountInfoActivity.intent(requireContext())));
        binding.menuSettings.setOnClickListener(v ->
                startActivity(SettingsActivity.intent(requireContext())));

        // Header
        binding.btnSearch.setOnClickListener(v -> toast("Tìm kiếm — sắp ra mắt"));
        binding.btnBag.setOnClickListener(v ->
                androidx.navigation.fragment.NavHostFragment.findNavController(this)
                        .navigate(R.id.cartFragment));

        // List hỗ trợ / chính sách
        binding.rowSupport.setOnClickListener(v ->
                startActivity(ContactActivity.intent(requireContext())));
        binding.rowFaq.setOnClickListener(v -> toast("Câu hỏi thường gặp — sắp ra mắt"));
        binding.rowPrivacy.setOnClickListener(v -> toast("Chính sách bảo mật — sắp ra mắt"));
        binding.rowWarranty.setOnClickListener(v -> toast("Chính sách bảo hành — sắp ra mắt"));
        binding.rowShippingPolicy.setOnClickListener(v ->
                toast("Chính sách Giao hàng và Kiểm tra sản phẩm — sắp ra mắt"));

        binding.btnLogout.setOnClickListener(v -> {
            FirebaseHelper.signOut();
            Intent intent = new Intent(requireContext(), WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        binding.btnSeedOrders.setOnClickListener(v -> seedTestOrders());
    }

    private void seedTestOrders() {
        String uid = FirebaseHelper.getCurrentUserId();
        if (uid == null) { toast("Chưa đăng nhập"); return; }

        FirebaseHelper.getDb().collection(FirebaseHelper.COL_PRODUCTS)
                .limit(3).get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) { toast("Không có sản phẩm để tạo đơn test"); return; }

                    String dateStr = new SimpleDateFormat("yyMMdd", Locale.US).format(new Date());
                    String[] statuses = {"PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "DELIVERED"};
                    String address = "Test User | 0901234567 | 123 Đường Test, Quận 1, TP.HCM";

                    for (int i = 0; i < 5; i++) {
                        int docIdx = i % snap.size();
                        String productId = snap.getDocuments().get(docIdx).getId();
                        Double price = snap.getDocuments().get(docIdx).getDouble("price");
                        double p = (price != null) ? price : 500000;
                        double shipFee = 35000;
                        double total = p + shipFee;
                        final String status = statuses[i];
                        String code = "GLS-" + dateStr + "-T" + (i + 1);

                        Map<String, Object> order = new HashMap<>();
                        order.put("customerId", uid);
                        order.put("orderCode", code);
                        order.put("totalAmount", total);
                        order.put("shippingFee", shipFee);
                        order.put("shipDiscount", 0.0);
                        order.put("voucherDiscount", 0.0);
                        order.put("usedPoints", 0);
                        order.put("earnedPoints", (int)(p / 1000));
                        order.put("paymentMethod", "COD");
                        order.put("paymentStatus", "PENDING");
                        order.put("orderStatus", status);
                        order.put("shippingAddress", address);
                        order.put("reviewed", false);
                        order.put("createdAt", new Date());

                        final String finalProductId = productId;
                        final double finalPrice = p;
                        FirebaseHelper.getDb().collection(FirebaseHelper.COL_ORDERS)
                                .add(order)
                                .addOnSuccessListener(ref -> {
                                    Map<String, Object> detail = new HashMap<>();
                                    detail.put("productId", finalProductId);
                                    detail.put("quantity", 1);
                                    detail.put("price", finalPrice);
                                    detail.put("color", "Đen");
                                    ref.collection(FirebaseHelper.COL_ORDER_DETAILS).add(detail);
                                });
                    }
                    toast("Đã tạo 5 đơn test! Vào 'Đơn đã mua' để xem.");
                })
                .addOnFailureListener(e -> toast("Lỗi: " + e.getMessage()));
    }

    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

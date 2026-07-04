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
        }

        // Grid tiện ích
        binding.menuOrders.setOnClickListener(v -> toast("Đơn hàng của bạn — sẽ làm ở A4"));
        binding.menuPoints.setOnClickListener(v -> toast("Lịch sử tích điểm — sắp ra mắt"));
        binding.menuVouchers.setOnClickListener(v ->
                startActivity(VoucherActivity.intent(requireContext())));
        binding.menuAccount.setOnClickListener(v -> toast("Thông tin tài khoản — sắp ra mắt"));
        binding.menuSettings.setOnClickListener(v -> toast("Cài đặt — sắp ra mắt"));

        // Header
        binding.btnSearch.setOnClickListener(v -> toast("Tìm kiếm — sắp ra mắt"));
        binding.btnBag.setOnClickListener(v ->
                androidx.navigation.fragment.NavHostFragment.findNavController(this)
                        .navigate(R.id.cartFragment));

        // List hỗ trợ / chính sách
        binding.rowSupport.setOnClickListener(v -> toast("Liên hệ hỗ trợ: support@glassity.com"));
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

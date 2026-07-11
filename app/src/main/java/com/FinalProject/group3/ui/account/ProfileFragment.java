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

        // Header dùng chung với Home — hamburger mở drawer, hoạt động như nhau dù đăng nhập hay khách
        binding.btnMenu.setOnClickListener(v -> {
            if (getActivity() instanceof com.FinalProject.group3.MainActivity) {
                ((com.FinalProject.group3.MainActivity) getActivity()).openDrawer();
            }
        });

        // Logo header → về tab Trang chủ
        binding.imgLogo.setOnClickListener(v ->
                androidx.navigation.fragment.NavHostFragment.findNavController(this)
                        .navigate(R.id.homeFragment));

        // ── Guest (chưa đăng nhập): ẩn thẻ barcode, các nút yêu cầu đăng nhập ──
        FirebaseUser user = FirebaseHelper.getAuth().getCurrentUser();
        if (user == null) {
            setupGuestMode();
            return;
        }

        // Số thẻ demo sinh từ uid — mỗi user 1 số ổn định
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
        binding.menuFavorites.setOnClickListener(v ->
                startActivity(FavoriteActivity.intent(requireContext())));

        // Header
        binding.btnSearch.setOnClickListener(v -> startActivity(new Intent(requireContext(), com.FinalProject.group3.ui.catalog.SearchActivity.class)));
        binding.btnCart.setOnClickListener(v ->
                androidx.navigation.fragment.NavHostFragment.findNavController(this)
                        .navigate(R.id.cartFragment));

        // List hỗ trợ / chính sách
        binding.rowSupport.setOnClickListener(v ->
                startActivity(ContactActivity.intent(requireContext())));
        setupPolicyRows();

        binding.btnLogout.setOnClickListener(v -> {
            com.FinalProject.group3.utils.SessionManager.logout(requireContext());
            Intent intent = new Intent(requireContext(), WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

    }

    // ── Guest mode (Figma LA.Personal2): không barcode, nút nào cần tài khoản
    //    thì hiện dialog yêu cầu đăng nhập — trừ Cài đặt + hỗ trợ/chính sách ──
    private void setupGuestMode() {
        // Vẫn hiển thị thẻ barcode nhưng phủ lớp trắng mờ + lời mời đăng nhập
        binding.guestOverlay.setVisibility(View.VISIBLE);
        binding.guestOverlay.setOnClickListener(v ->
                com.FinalProject.group3.utils.LoginRequiredDialog.show(
                        requireContext(), "Đăng nhập để sử dụng tích điểm và ưu đãi thành viên"));

        // Các mục cần tài khoản → dialog đăng nhập
        binding.menuOrders.setOnClickListener(v -> showLoginRequired());
        binding.menuPoints.setOnClickListener(v -> showLoginRequired());
        binding.menuVouchers.setOnClickListener(v -> showLoginRequired());
        binding.menuAccount.setOnClickListener(v -> showLoginRequired());

        // Cài đặt vẫn mở bình thường; Yêu thích cần tài khoản
        binding.menuSettings.setOnClickListener(v ->
                startActivity(SettingsActivity.intent(requireContext())));
        binding.menuFavorites.setOnClickListener(v -> showLoginRequired());

        // Header
        binding.btnSearch.setOnClickListener(v -> startActivity(new Intent(requireContext(), com.FinalProject.group3.ui.catalog.SearchActivity.class)));
        binding.btnCart.setOnClickListener(v -> showLoginRequired());

        // Hỗ trợ / chính sách là thông tin công khai — mở bình thường
        binding.rowSupport.setOnClickListener(v ->
                startActivity(ContactActivity.intent(requireContext())));
        setupPolicyRows();

        // Nút Đăng xuất → thành Đăng nhập / Đăng ký
        binding.btnLogout.setText("Đăng nhập / Đăng ký");
        binding.btnLogout.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), LoginActivity.class)));
    }

    /** 4 trang chính sách tĩnh (Figma DL_FAQ / DL_Warranty / DL_Policy) */
    private void setupPolicyRows() {
        binding.rowFaq.setOnClickListener(v -> startActivity(
                PolicyActivity.intent(requireContext(), PolicyActivity.TYPE_FAQ)));
        binding.rowPrivacy.setOnClickListener(v -> startActivity(
                PolicyActivity.intent(requireContext(), PolicyActivity.TYPE_PRIVACY)));
        binding.rowWarranty.setOnClickListener(v -> startActivity(
                PolicyActivity.intent(requireContext(), PolicyActivity.TYPE_WARRANTY)));
        binding.rowShippingPolicy.setOnClickListener(v -> startActivity(
                PolicyActivity.intent(requireContext(), PolicyActivity.TYPE_SHIPPING)));
    }

    /** Dialog "Bạn cần đăng nhập..." (Figma LA.Personal2) */
    private void showLoginRequired() {
        com.FinalProject.group3.utils.LoginRequiredDialog.show(
                requireContext(), "Đăng nhập để sử dụng tính năng dành riêng cho thành viên");
    }

    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null)
            com.FinalProject.group3.utils.CartQuickActions.refreshBadge(binding.tvCartBadge);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

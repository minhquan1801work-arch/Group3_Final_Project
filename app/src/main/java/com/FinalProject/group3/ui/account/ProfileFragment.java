package com.FinalProject.group3.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.FinalProject.group3.databinding.FragmentProfileBinding;
import com.FinalProject.group3.utils.FirebaseHelper;
import com.google.firebase.auth.FirebaseUser;

/** LA.Profile — thông tin cơ bản + đăng xuất. Task #21 sẽ bổ sung sửa thông tin, xem đơn hàng... */
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

        FirebaseUser user = FirebaseHelper.getAuth().getCurrentUser();
        if (user != null) {
            binding.tvName.setText(user.getDisplayName() != null ? user.getDisplayName() : "Khách");
            binding.tvEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        } else {
            binding.tvName.setText("Khách");
            binding.tvEmail.setText("Chưa đăng nhập");
        }

        binding.btnLogout.setOnClickListener(v -> {
            FirebaseHelper.signOut();
            Intent intent = new Intent(requireContext(), WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

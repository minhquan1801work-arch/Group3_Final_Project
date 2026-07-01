package com.FinalProject.group3.ui.order;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.FinalProject.group3.databinding.FragmentCartBinding;

/**
 * DL_Cart — placeholder tạm, sẽ hoàn thiện ở bước tiếp theo (task #18 trong TEAM_GUIDELINE).
 * Giữ Fragment rỗng để BottomNavigation không bị lỗi khi build luồng điều hướng.
 */
public class CartFragment extends Fragment {

    private FragmentCartBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

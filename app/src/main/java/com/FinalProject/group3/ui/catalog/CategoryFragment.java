package com.FinalProject.group3.ui.catalog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.FinalProject.group3.adapter.CategoryAdapter;
import com.FinalProject.group3.databinding.FragmentCategoryBinding;
import com.FinalProject.group3.model.Category;
import com.FinalProject.group3.repository.CategoryRepository;

import java.util.List;

/** LA.Categories — danh sách đầy đủ các category, bấm vào 1 category -> ProductListActivity. */
public class CategoryFragment extends Fragment {

    private FragmentCategoryBinding binding;
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private CategoryAdapter categoryAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        categoryAdapter = new CategoryAdapter(category ->
                ProductListActivity.start(requireContext(), category.getCategoryId(), null, category.getName()));
        binding.rvCategories.setAdapter(categoryAdapter);

        loadCategories();
    }

    private void loadCategories() {
        categoryRepository.getAllCategories(new CategoryRepository.CategoryListCallback() {
            @Override
            public void onSuccess(List<Category> categories) {
                if (binding == null) return;
                categoryAdapter.submitList(categories);
                binding.tvEmpty.setVisibility(categories.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(String error) {
                if (binding == null) return;
                binding.tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

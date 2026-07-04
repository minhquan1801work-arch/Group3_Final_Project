package com.FinalProject.group3.ui.catalog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.FinalProject.group3.R;
import com.FinalProject.group3.adapter.ProductAdapter;
import com.FinalProject.group3.databinding.ActivitySearchBinding;
import com.FinalProject.group3.model.Product;
import com.FinalProject.group3.repository.ProductRepository;
import com.FinalProject.group3.utils.InsetsUtil;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SearchActivity extends AppCompatActivity {

    private static final String PREFS_SEARCH = "search_history";
    private static final String KEY_HISTORY  = "history";
    private static final int    MAX_HISTORY  = 8;

    private ActivitySearchBinding binding;
    private ProductAdapter adapter;
    private ProductRepository repo;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applySystemBarsPadding(binding.getRoot());

        prefs = getSharedPreferences(PREFS_SEARCH, MODE_PRIVATE);
        repo  = new ProductRepository();

        setupToolbar();
        setupSearchBar();
        setupResultsGrid();
        renderHistory();
        showHistorySection();
    }

    private void setupToolbar() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupSearchBar() {
        binding.etSearch.requestFocus();
        showKeyboard();

        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(binding.etSearch.getText().toString().trim());
                return true;
            }
            return false;
        });

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int cnt, int after) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int cnt) {
                if (s.length() == 0) showHistorySection();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.btnClearSearch.setOnClickListener(v -> {
            binding.etSearch.setText("");
            showHistorySection();
        });
    }

    private void setupResultsGrid() {
        adapter = new ProductAdapter(
                product -> ProductDetailActivity.start(this, product.getProductId()));
        binding.rvResults.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvResults.setAdapter(adapter);
    }

    private void performSearch(String query) {
        if (query.isEmpty()) return;
        saveHistory(query);
        hideKeyboard();
        showResultsSection();

        repo.searchProducts(query, new ProductRepository.ProductListCallback() {
            @Override public void onSuccess(List<Product> products) {
                binding.tvResultCount.setText(products.size() + " kết quả cho \"" + query + "\"");
                adapter.submitList(products);
                binding.layoutEmpty.setVisibility(products.isEmpty()
                        ? android.view.View.VISIBLE : android.view.View.GONE);
            }
            @Override public void onFailure(String error) {
                adapter.submitList(new ArrayList<>());
            }
        });
    }

    private void showHistorySection() {
        binding.layoutHistory.setVisibility(android.view.View.VISIBLE);
        binding.layoutResults.setVisibility(android.view.View.GONE);
        binding.btnClearSearch.setVisibility(android.view.View.GONE);
    }

    private void showResultsSection() {
        binding.layoutHistory.setVisibility(android.view.View.GONE);
        binding.layoutResults.setVisibility(android.view.View.VISIBLE);
        binding.btnClearSearch.setVisibility(
                binding.etSearch.getText().length() > 0
                        ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    private void renderHistory() {
        binding.chipGroupHistory.removeAllViews();
        List<String> history = getHistory();

        binding.btnClearHistory.setOnClickListener(v -> {
            prefs.edit().remove(KEY_HISTORY).apply();
            binding.chipGroupHistory.removeAllViews();
        });

        for (String term : history) {
            Chip chip = new Chip(this, null,
                    com.google.android.material.R.style.Widget_MaterialComponents_Chip_Entry);
            chip.setText(term);
            chip.setCloseIconVisible(false);
            chip.setOnClickListener(v -> {
                binding.etSearch.setText(term);
                binding.etSearch.setSelection(term.length());
                performSearch(term);
            });
            binding.chipGroupHistory.addView(chip);
        }
    }

    private List<String> getHistory() {
        Set<String> raw = prefs.getStringSet(KEY_HISTORY, new LinkedHashSet<>());
        return new ArrayList<>(raw);
    }

    private void saveHistory(String query) {
        LinkedHashSet<String> history = new LinkedHashSet<>(getHistory());
        history.remove(query);
        history.add(query);
        while (history.size() > MAX_HISTORY) {
            history.remove(history.iterator().next());
        }
        prefs.edit().putStringSet(KEY_HISTORY, history).apply();
        renderHistory();
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(binding.etSearch.getWindowToken(), 0);
    }
}

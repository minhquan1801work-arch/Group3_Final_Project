package com.FinalProject.group3.ui.catalog;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.FinalProject.group3.adapter.ProductAdapter;
import com.FinalProject.group3.adapter.SearchSuggestAdapter;
import com.FinalProject.group3.databinding.ActivitySearchBinding;
import com.FinalProject.group3.model.Favorite;
import com.FinalProject.group3.model.OrderDetail;
import com.FinalProject.group3.model.Product;
import com.FinalProject.group3.model.ProductVariant;
import com.FinalProject.group3.repository.FavoriteRepository;
import com.FinalProject.group3.repository.ProductRepository;
import com.FinalProject.group3.utils.FirebaseHelper;
import com.FinalProject.group3.utils.InsetsUtil;
import com.google.android.material.chip.Chip;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * DL.Search — tìm kiếm sản phẩm. [Task B7 — hoàn thiện]
 *
 * Cách hoạt động:
 *  - Toàn bộ products load 1 lần vào RAM → tìm kiểu contains, không dấu,
 *    không phân biệt hoa thường, khớp cả tên + mô tả + tên màu variant.
 *  - Type-ahead: gõ ≥2 ký tự → debounce 250ms → hiện kết quả ngay.
 *    Bấm Enter/Search mới lưu vào lịch sử.
 *  - Lịch sử: chỉ user đã đăng nhập, lưu SharedPreferences theo uid.
 *  - Gợi ý tìm kiếm: cá nhân hóa từ đơn đã mua + yêu thích + lịch sử search
 *    (chấm điểm theo collection / faceShapes / category / khoảng giá).
 *    Guest hoặc chưa có dữ liệu → random.
 *  - Camera: UI giữ chỗ cho tính năng tìm bằng hình ảnh (bổ sung sau).
 */
public class SearchActivity extends AppCompatActivity {

    private static final String PREFS_SEARCH = "search_history";
    private static final int    MAX_HISTORY  = 8;
    private static final int    SUGGEST_COUNT = 6;
    private static final long   TYPE_DEBOUNCE_MS = 250;

    private ActivitySearchBinding binding;
    private ProductAdapter resultAdapter;
    private SearchSuggestAdapter suggestAdapter;
    private SharedPreferences prefs;

    private final List<Product> allProducts = new ArrayList<>();
    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearch;

    private String uid; // null = guest

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applySystemBarsPadding(binding.getRoot());

        prefs = getSharedPreferences(PREFS_SEARCH, MODE_PRIVATE);
        uid   = FirebaseHelper.getCurrentUserId();

        setupToolbar();
        setupSearchBar();
        setupResultsGrid();
        setupSuggestGrid();
        renderHistory();

        loadAllProducts();
        showBrowse();
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

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

        // Type-ahead: debounce khi đang gõ
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int cnt, int after) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int cnt) {}
            @Override public void afterTextChanged(Editable s) {
                String q = s.toString().trim();
                updateSearchBarIcons(q);
                if (pendingSearch != null) debounceHandler.removeCallbacks(pendingSearch);

                if (q.isEmpty()) {
                    showBrowse();
                    return;
                }
                if (q.length() < 2) return; // 1 ký tự: chưa search

                pendingSearch = () -> liveSearch(q);
                debounceHandler.postDelayed(pendingSearch, TYPE_DEBOUNCE_MS);
            }
        });

        binding.btnClearSearch.setOnClickListener(v -> {
            binding.etSearch.setText("");
            showBrowse();
        });

        // Tìm bằng hình ảnh — UI giữ chỗ, tính năng làm sau
        binding.btnCamera.setOnClickListener(v ->
                Toast.makeText(this, "Tìm bằng hình ảnh — sắp ra mắt!", Toast.LENGTH_SHORT).show());
    }

    private void updateSearchBarIcons(String query) {
        boolean typing = !query.isEmpty();
        binding.btnClearSearch.setVisibility(typing ? View.VISIBLE : View.GONE);
        binding.btnCamera.setVisibility(typing ? View.GONE : View.VISIBLE);
    }

    private void setupResultsGrid() {
        resultAdapter = new ProductAdapter(
                product -> ProductDetailActivity.start(this, product.getProductId()));
        com.FinalProject.group3.utils.CartQuickActions.wire(resultAdapter, this);
        binding.rvResults.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvResults.setAdapter(resultAdapter);
    }

    private void setupSuggestGrid() {
        suggestAdapter = new SearchSuggestAdapter(
                product -> ProductDetailActivity.start(this, product.getProductId()));
        binding.rvSuggest.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvSuggest.setAdapter(suggestAdapter);
    }

    // ── Data ──────────────────────────────────────────────────────────────────

    private void loadAllProducts() {
        new ProductRepository().getAllProducts(new ProductRepository.ProductListCallback() {
            @Override public void onSuccess(List<Product> products) {
                if (binding == null) return;
                allProducts.clear();
                allProducts.addAll(products);
                buildSuggestions();
            }
            @Override public void onFailure(String error) {
                if (binding == null) return;
                Toast.makeText(SearchActivity.this, "Lỗi tải sản phẩm: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Tìm kiếm in-memory ────────────────────────────────────────────────────

    /** Bỏ dấu tiếng Việt + lowercase để so khớp. */
    private static String normalize(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd').replace('Đ', 'D');
        return n.toLowerCase(Locale.US);
    }

    private List<Product> filterProducts(String query) {
        String q = normalize(query);
        List<Product> matched = new ArrayList<>();
        for (Product p : allProducts) {
            if (normalize(p.getName()).contains(q)
                    || normalize(p.getDescription()).contains(q)
                    || matchesVariantColor(p, q)) {
                matched.add(p);
            }
        }
        return matched;
    }

    private boolean matchesVariantColor(Product p, String q) {
        if (p.getVariants() == null) return false;
        for (ProductVariant v : p.getVariants()) {
            if (v.getColorName() != null && normalize(v.getColorName()).contains(q)) return true;
        }
        return false;
    }

    /** Kết quả hiện realtime khi đang gõ — KHÔNG lưu lịch sử. */
    private void liveSearch(String query) {
        List<Product> results = filterProducts(query);
        showResults();
        binding.tvResultCount.setText(results.size() + " kết quả cho \"" + query + "\"");
        resultAdapter.submitList(results);
        binding.layoutEmpty.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /** Search chính thức (Enter/chip lịch sử) — lưu lịch sử nếu đã đăng nhập. */
    private void performSearch(String query) {
        if (query.isEmpty()) return;
        if (uid != null) saveHistory(query);
        hideKeyboard();
        liveSearch(query);
    }

    // ── Chuyển section ────────────────────────────────────────────────────────

    private void showBrowse() {
        binding.layoutBrowse.setVisibility(View.VISIBLE);
        binding.layoutResults.setVisibility(View.GONE);
        updateSearchBarIcons(binding.etSearch.getText().toString().trim());
    }

    private void showResults() {
        binding.layoutBrowse.setVisibility(View.GONE);
        binding.layoutResults.setVisibility(View.VISIBLE);
    }

    // ── Lịch sử (chỉ user đã đăng nhập) ───────────────────────────────────────

    private String historyKey() { return "history_" + uid; }

    private List<String> getHistory() {
        if (uid == null) return new ArrayList<>();
        Set<String> raw = prefs.getStringSet(historyKey(), new LinkedHashSet<>());
        return new ArrayList<>(raw);
    }

    private void saveHistory(String query) {
        if (uid == null) return;
        LinkedHashSet<String> history = new LinkedHashSet<>(getHistory());
        history.remove(query);
        history.add(query);
        while (history.size() > MAX_HISTORY) {
            history.remove(history.iterator().next());
        }
        prefs.edit().putStringSet(historyKey(), history).apply();
        renderHistory();
    }

    private void renderHistory() {
        List<String> history = getHistory();

        // Guest hoặc chưa từng search → ẩn section
        if (uid == null || history.isEmpty()) {
            binding.layoutHistory.setVisibility(View.GONE);
            return;
        }
        binding.layoutHistory.setVisibility(View.VISIBLE);
        binding.chipGroupHistory.removeAllViews();

        binding.btnClearHistory.setOnClickListener(v -> {
            prefs.edit().remove(historyKey()).apply();
            renderHistory();
        });

        // Mới nhất hiện trước
        Collections.reverse(history);
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

    // ── Gợi ý tìm kiếm (cá nhân hóa) ──────────────────────────────────────────
    //
    // Hồ sơ sở thích = sản phẩm đã mua + đã yêu thích + khớp lịch sử search.
    // Chấm điểm mọi sản phẩm chưa mua: +3 trùng collection, +2 mỗi faceShape
    // trùng, +1 trùng category, +1 cùng khoảng giá (±30%). Guest/không có
    // dữ liệu → random.

    private final Set<String> profileProductIds = new HashSet<>();   // SP user đã tương tác
    private final Set<String> purchasedProductIds = new HashSet<>(); // SP đã mua (loại khỏi gợi ý)
    private int pendingSources = 0;

    private void buildSuggestions() {
        if (uid == null) {
            showRandomSuggestions();
            return;
        }

        profileProductIds.clear();
        purchasedProductIds.clear();

        // Từ lịch sử search (đồng bộ, chạy ngay)
        for (String term : getHistory()) {
            for (Product p : filterProducts(term)) {
                profileProductIds.add(p.getProductId());
            }
        }

        // 2 nguồn async: favorites + orders
        pendingSources = 2;

        new FavoriteRepository().getFavorites(new FavoriteRepository.FavoriteListCallback() {
            @Override public void onSuccess(List<Favorite> favorites) {
                for (Favorite f : favorites) profileProductIds.add(f.getProductId());
                onSourceDone();
            }
            @Override public void onFailure(String error) { onSourceDone(); }
        });

        FirebaseHelper.getDb().collection(FirebaseHelper.COL_ORDERS)
                .whereEqualTo("customerId", uid)
                .get()
                .addOnSuccessListener(orderSnap -> {
                    if (orderSnap.isEmpty()) { onSourceDone(); return; }
                    final int[] remaining = { orderSnap.size() };
                    for (com.google.firebase.firestore.DocumentSnapshot order : orderSnap.getDocuments()) {
                        order.getReference().collection(FirebaseHelper.COL_ORDER_DETAILS).get()
                                .addOnSuccessListener(detailSnap -> {
                                    for (OrderDetail d : detailSnap.toObjects(OrderDetail.class)) {
                                        if (d.getProductId() != null) {
                                            profileProductIds.add(d.getProductId());
                                            purchasedProductIds.add(d.getProductId());
                                        }
                                    }
                                    if (--remaining[0] == 0) onSourceDone();
                                })
                                .addOnFailureListener(e -> {
                                    if (--remaining[0] == 0) onSourceDone();
                                });
                    }
                })
                .addOnFailureListener(e -> onSourceDone());
    }

    private void onSourceDone() {
        if (--pendingSources > 0) return;
        if (binding == null) return;

        if (profileProductIds.isEmpty()) {
            showRandomSuggestions();
            return;
        }
        showPersonalizedSuggestions();
    }

    private void showRandomSuggestions() {
        List<Product> pool = new ArrayList<>(allProducts);
        Collections.shuffle(pool);
        suggestAdapter.submitList(pool.subList(0, Math.min(SUGGEST_COUNT, pool.size())));
    }

    private void showPersonalizedSuggestions() {
        // 1. Tổng hợp hồ sơ sở thích từ các SP đã tương tác
        Map<String, Integer> collectionFreq = new HashMap<>();
        Map<String, Integer> shapeFreq      = new HashMap<>();
        Map<String, Integer> categoryFreq   = new HashMap<>();
        double priceSum = 0; int priceCount = 0;

        for (Product p : allProducts) {
            if (!profileProductIds.contains(p.getProductId())) continue;
            if (p.getCollection() != null && !p.getCollection().isEmpty())
                collectionFreq.merge(p.getCollection(), 1, Integer::sum);
            if (p.getFaceShapes() != null)
                for (String s : p.getFaceShapes()) shapeFreq.merge(s, 1, Integer::sum);
            if (p.getCategoryId() != null)
                categoryFreq.merge(p.getCategoryId(), 1, Integer::sum);
            if (p.getPrice() > 0) { priceSum += p.getPrice(); priceCount++; }
        }
        double avgPrice = priceCount > 0 ? priceSum / priceCount : 0;

        // 2. Chấm điểm các SP chưa mua
        List<Product> candidates = new ArrayList<>();
        Map<String, Integer> scores = new HashMap<>();
        for (Product p : allProducts) {
            if (purchasedProductIds.contains(p.getProductId())) continue;
            int score = 0;
            if (p.getCollection() != null && collectionFreq.containsKey(p.getCollection())) score += 3;
            if (p.getFaceShapes() != null)
                for (String s : p.getFaceShapes()) if (shapeFreq.containsKey(s)) score += 2;
            if (p.getCategoryId() != null && categoryFreq.containsKey(p.getCategoryId())) score += 1;
            if (avgPrice > 0 && p.getPrice() > 0
                    && Math.abs(p.getPrice() - avgPrice) <= avgPrice * 0.3) score += 1;
            if (score > 0) {
                candidates.add(p);
                scores.put(p.getProductId(), score);
            }
        }

        if (candidates.isEmpty()) {
            showRandomSuggestions();
            return;
        }

        // 3. Sort điểm giảm dần, điểm bằng nhau thì random
        Collections.shuffle(candidates);
        Collections.sort(candidates, (a, b) ->
                scores.get(b.getProductId()) - scores.get(a.getProductId()));
        suggestAdapter.submitList(
                candidates.subList(0, Math.min(SUGGEST_COUNT, candidates.size())));
    }

    // ── Keyboard ──────────────────────────────────────────────────────────────

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(binding.etSearch.getWindowToken(), 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pendingSearch != null) debounceHandler.removeCallbacks(pendingSearch);
        binding = null;
    }
}

package com.FinalProject.group3.ui.catalog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.FinalProject.group3.R;
import com.FinalProject.group3.adapter.ProductAdapter;
import com.FinalProject.group3.databinding.ActivityProductListBinding;
import com.FinalProject.group3.model.Product;
import com.FinalProject.group3.repository.ProductRepository;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DL.Product — danh sach san pham theo category hoac tat ca. [Task B3]
 *
 * Category IDs quy dinh (cập nhật khi seed data Firestore thật):
 *   CAT_KINH_MAT  = "cat_kinh_mat"   — Kính mắt thông thường
 *   CAT_SHAPE     = "cat_shape"      — Shape kính (kính theo dạng mặt)
 *   CAT_PHU_KIEN  = "cat_phu_kien"   — Phụ kiện (hộp, dây, v.v.)
 *
 * Sort (client-side sau khi load):
 *   SORT_DEFAULT  = 0 — Mặc định (thứ tự Firestore trả về)
 *   SORT_ASC      = 1 — Giá từ thấp đến cao
 *   SORT_DESC     = 2 — Giá từ cao đến thấp
 */
public class ProductListActivity extends AppCompatActivity {

    // ── Category ID quy định — cập nhật khi có data thật ─────────────────────
    // Danh mục chính
    public static final String CAT_KINH_MAT   = "cat_kinh_mat";
    public static final String CAT_PHU_KIEN   = "cat_phu_kien";
    // 5 dạng mặt — mỗi shape có category riêng để filter sản phẩm phù hợp
    public static final String CAT_SHAPE_TRON      = "cat_shape_tron";
    public static final String CAT_SHAPE_TRAI_XOAN = "cat_shape_trai_xoan";
    public static final String CAT_SHAPE_TRAI_TIM  = "cat_shape_trai_tim";
    public static final String CAT_SHAPE_KIM_CUONG = "cat_shape_kim_cuong";
    public static final String CAT_SHAPE_VUONG     = "cat_shape_vuong";

    private static final int SORT_DEFAULT = 0;
    private static final int SORT_ASC     = 1;
    private static final int SORT_DESC    = 2;

    // Mode đặc biệt: load tất cả 5 shape cùng lúc
    static final String MODE_ALL_SHAPES = "MODE_ALL_SHAPES";

    private static final String EXTRA_CATEGORY_ID   = "extra_category_id";
    private static final String EXTRA_CATEGORY_NAME = "extra_category_name";

    public static void start(Context context, String categoryId, String categoryName) {
        Intent intent = new Intent(context, ProductListActivity.class);
        intent.putExtra(EXTRA_CATEGORY_ID, categoryId);
        intent.putExtra(EXTRA_CATEGORY_NAME, categoryName);
        context.startActivity(intent);
    }

    public static void startAll(Context context) {
        Intent intent = new Intent(context, ProductListActivity.class);
        intent.putExtra(EXTRA_CATEGORY_NAME, "Tất cả sản phẩm");
        context.startActivity(intent);
    }

    /** Mở danh sách tất cả kính phù hợp với mọi dạng mặt (chip "Shape kính") */
    public static void startAllShapes(Context context) {
        Intent intent = new Intent(context, ProductListActivity.class);
        intent.putExtra(EXTRA_CATEGORY_ID, MODE_ALL_SHAPES);
        intent.putExtra(EXTRA_CATEGORY_NAME, "Shape kính");
        context.startActivity(intent);
    }

    private ActivityProductListBinding binding;
    private final ProductRepository productRepository = new ProductRepository();
    private ProductAdapter productAdapter;

    private List<Product> allProducts = new ArrayList<>();
    private String currentCategoryId = null;
    private int currentSort = SORT_DEFAULT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        com.FinalProject.group3.utils.InsetsUtil.applySystemBarsPadding(binding.getRoot());

        currentCategoryId = getIntent().getStringExtra(EXTRA_CATEGORY_ID);
        String categoryName = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);
        binding.tvTitle.setText(categoryName != null ? categoryName : "Sản phẩm");
        binding.btnBack.setOnClickListener(v -> finish());

        productAdapter = new ProductAdapter(product ->
                ProductDetailActivity.start(this, product.getProductId()));
        binding.rvProducts.setAdapter(productAdapter);

        setupChips();
        setupFilterButton();

        if (currentCategoryId == null) loadAllProducts();
        else if (MODE_ALL_SHAPES.equals(currentCategoryId)) loadAllShapes();
        else loadProducts(currentCategoryId);
    }

    // ── Chips lọc theo category ───────────────────────────────────────────────
    private void setupChips() {
        binding.chipKinhMat.setOnClickListener(v -> switchCategory(CAT_KINH_MAT, binding.chipKinhMat));
        binding.chipShape.setOnClickListener(v -> {
            // Chip "Shape kính" → load tất cả 5 shape cùng lúc
            if (MODE_ALL_SHAPES.equals(currentCategoryId)) return;
            currentCategoryId = MODE_ALL_SHAPES;
            currentSort = SORT_DEFAULT;
            resetChips();
            setActiveChip(binding.chipShape);
            loadAllShapes();
        });
        binding.chipPhuKien.setOnClickListener(v -> switchCategory(CAT_PHU_KIEN, binding.chipPhuKien));

        // Highlight chip tương ứng nếu mở từ category cụ thể
        if (CAT_KINH_MAT.equals(currentCategoryId))        setActiveChip(binding.chipKinhMat);
        else if (MODE_ALL_SHAPES.equals(currentCategoryId)) setActiveChip(binding.chipShape);
        else if (CAT_PHU_KIEN.equals(currentCategoryId))   setActiveChip(binding.chipPhuKien);
    }

    private void switchCategory(String categoryId, TextView chip) {
        if (categoryId.equals(currentCategoryId) || (currentCategoryId == null && categoryId == null)) return;
        currentCategoryId = categoryId;
        currentSort = SORT_DEFAULT;
        resetChips();
        setActiveChip(chip);
        loadProducts(categoryId);
    }

    private void setActiveChip(TextView chip) {
        chip.setBackgroundResource(R.drawable.bg_btn_wine_filled);
        chip.setTextColor(0xFFFFFFFF);
    }

    private void resetChips() {
        for (TextView c : new TextView[]{binding.chipKinhMat, binding.chipShape, binding.chipPhuKien}) {
            c.setBackgroundResource(R.drawable.bg_btn_wine_outline);
            c.setTextColor(getColor(R.color.brand_wine));
        }
    }

    // ── Nút filter: BottomSheetDialog sắp xếp giá (khớp Figma DL.Product) ────
    private void setupFilterButton() {
        binding.btnFilter.setOnClickListener(v -> showSortSheet());
    }

    private void showSortSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_filter, null);
        sheet.setContentView(sheetView);

        RadioGroup rg = sheetView.findViewById(R.id.rgSort);
        RadioButton rbAsc  = sheetView.findViewById(R.id.rbSortAsc);
        RadioButton rbDesc = sheetView.findViewById(R.id.rbSortDesc);

        // Giữ lại lựa chọn hiện tại
        if (currentSort == SORT_ASC)  rbAsc.setChecked(true);
        if (currentSort == SORT_DESC) rbDesc.setChecked(true);

        rg.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbSortAsc)  currentSort = SORT_ASC;
            if (checkedId == R.id.rbSortDesc) currentSort = SORT_DESC;
            applySort();
            sheet.dismiss();
        });

        sheet.show();
    }

    // ── Sort client-side ──────────────────────────────────────────────────────
    private void applySort() {
        if (allProducts.isEmpty()) return;
        List<Product> sorted = new ArrayList<>(allProducts);
        if (currentSort == SORT_ASC)
            Collections.sort(sorted, (a, b) -> Double.compare(a.getPrice(), b.getPrice()));
        else if (currentSort == SORT_DESC)
            Collections.sort(sorted, (a, b) -> Double.compare(b.getPrice(), a.getPrice()));
        productAdapter.submitList(sorted);
    }

    // ── Load data ─────────────────────────────────────────────────────────────
    private void loadAllProducts() {
        binding.progressBar.setVisibility(View.VISIBLE);
        productRepository.getAllProducts(new ProductRepository.ProductListCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                allProducts = products;
                applySort();
                binding.tvEmpty.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(String error) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.tvEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(ProductListActivity.this, "Lỗi tải sản phẩm: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProducts(String categoryId) {
        binding.progressBar.setVisibility(View.VISIBLE);
        productRepository.getProductsByCategory(categoryId, new ProductRepository.ProductListCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                allProducts = products;
                applySort();
                binding.tvEmpty.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(String error) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.tvEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(ProductListActivity.this, "Lỗi tải sản phẩm: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Load tất cả sản phẩm thuộc 5 dạng mặt (whereIn Firestore) */
    private void loadAllShapes() {
        binding.progressBar.setVisibility(View.VISIBLE);
        List<String> shapeIds = new ArrayList<>();
        shapeIds.add(CAT_SHAPE_TRON);
        shapeIds.add(CAT_SHAPE_TRAI_XOAN);
        shapeIds.add(CAT_SHAPE_TRAI_TIM);
        shapeIds.add(CAT_SHAPE_KIM_CUONG);
        shapeIds.add(CAT_SHAPE_VUONG);

        productRepository.getProductsByCategories(shapeIds, new ProductRepository.ProductListCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                allProducts = products;
                applySort();
                binding.tvEmpty.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(String error) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.tvEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(ProductListActivity.this, "Lỗi tải sản phẩm: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

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
 * DL.Product — danh sach san pham theo category hoac dang mat. [Task B3]
 *
 * Phan loai san pham dung 2 truong doc lap:
 *   categoryId  — loai san pham: cat_kinh_mat | cat_kinh_can | cat_phu_kien
 *   faceShapes  — dang mat phu hop: tron | trai_xoan | trai_tim | kim_cuong | vuong
 *
 * Khi mo man hinh:
 *   start(ctx, "cat_kinh_mat", null, "Kinh mat")   → loc theo category
 *   start(ctx, null, "tron", "Kinh mat tron")      → loc theo dang mat
 *   startAll(ctx)                                  → tat ca san pham
 *   startAllShapes(ctx)                            → tat ca co dang mat
 */
public class ProductListActivity extends AppCompatActivity {

    // ── Category IDs (loai san pham) ─────────────────────────────────────────
    public static final String CAT_KINH_MAT  = "cat_kinh_mat";
    public static final String CAT_KINH_CAN  = "cat_kinh_can";
    public static final String CAT_PHU_KIEN  = "cat_phu_kien";

    // ── Face shape values (dung voi faceShapes array) ──────────────────────
    public static final String SHAPE_TRON       = "tron";
    public static final String SHAPE_TRAI_XOAN  = "trai_xoan";
    public static final String SHAPE_TRAI_TIM   = "trai_tim";
    public static final String SHAPE_KIM_CUONG  = "kim_cuong";
    public static final String SHAPE_VUONG      = "vuong";

    private static final int SORT_DEFAULT = 0;
    private static final int SORT_ASC     = 1;
    private static final int SORT_DESC    = 2;

    private static final String EXTRA_CATEGORY_ID   = "extra_category_id";
    private static final String EXTRA_FACE_SHAPE    = "extra_face_shape";
    private static final String EXTRA_ALL_SHAPES    = "extra_all_shapes";
    private static final String EXTRA_CATEGORY_NAME = "extra_category_name";

    // ── Static launchers ──────────────────────────────────────────────────────
    public static void start(Context context, String categoryId, String faceShape, String title) {
        Intent intent = new Intent(context, ProductListActivity.class);
        if (categoryId != null) intent.putExtra(EXTRA_CATEGORY_ID, categoryId);
        if (faceShape  != null) intent.putExtra(EXTRA_FACE_SHAPE, faceShape);
        intent.putExtra(EXTRA_CATEGORY_NAME, title);
        context.startActivity(intent);
    }

    public static void startAll(Context context) {
        Intent intent = new Intent(context, ProductListActivity.class);
        intent.putExtra(EXTRA_CATEGORY_NAME, "Tất cả sản phẩm");
        context.startActivity(intent);
    }

    public static void startAllShapes(Context context) {
        Intent intent = new Intent(context, ProductListActivity.class);
        intent.putExtra(EXTRA_ALL_SHAPES, true);
        intent.putExtra(EXTRA_CATEGORY_NAME, "Shape kính");
        context.startActivity(intent);
    }

    // ── Fields ────────────────────────────────────────────────────────────────
    private ActivityProductListBinding binding;
    private final ProductRepository productRepository = new ProductRepository();
    private ProductAdapter productAdapter;

    private List<Product> allProducts = new ArrayList<>();
    private String currentCategoryId = null;
    private String currentFaceShape  = null;
    private boolean isAllShapes      = false;
    private int currentSort          = SORT_DEFAULT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        com.FinalProject.group3.utils.InsetsUtil.applySystemBarsPadding(binding.getRoot());

        currentCategoryId = getIntent().getStringExtra(EXTRA_CATEGORY_ID);
        currentFaceShape  = getIntent().getStringExtra(EXTRA_FACE_SHAPE);
        isAllShapes       = getIntent().getBooleanExtra(EXTRA_ALL_SHAPES, false);
        String title      = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);
        binding.tvTitle.setText(title != null ? title : "Sản phẩm");
        binding.btnBack.setOnClickListener(v -> finish());

        productAdapter = new ProductAdapter(product ->
                ProductDetailActivity.start(this, product.getProductId()));
        binding.rvProducts.setAdapter(productAdapter);

        setupChips();
        setupFilterButton();
        loadInitial();
    }

    // ── Chips ─────────────────────────────────────────────────────────────────
    private void setupChips() {
        binding.chipKinhMat.setOnClickListener(v -> switchToCategory(CAT_KINH_MAT, binding.chipKinhMat));
        binding.chipShape.setOnClickListener(v -> switchToAllShapes());
        binding.chipPhuKien.setOnClickListener(v -> switchToCategory(CAT_PHU_KIEN, binding.chipPhuKien));

        // Highlight chip tuong ung khi mo tu ben ngoai
        if (isAllShapes || currentFaceShape != null) setActiveChip(binding.chipShape);
        else if (CAT_KINH_MAT.equals(currentCategoryId))  setActiveChip(binding.chipKinhMat);
        else if (CAT_PHU_KIEN.equals(currentCategoryId))  setActiveChip(binding.chipPhuKien);
    }

    private void switchToCategory(String categoryId, TextView chip) {
        if (categoryId.equals(currentCategoryId) && currentFaceShape == null && !isAllShapes) return;
        currentCategoryId = categoryId;
        currentFaceShape  = null;
        isAllShapes       = false;
        currentSort       = SORT_DEFAULT;
        resetChips();
        setActiveChip(chip);
        loadByCategory(categoryId);
    }

    private void switchToAllShapes() {
        if (isAllShapes) return;
        isAllShapes       = true;
        currentCategoryId = null;
        currentFaceShape  = null;
        currentSort       = SORT_DEFAULT;
        resetChips();
        setActiveChip(binding.chipShape);
        loadAllShapes();
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

    // ── Filter / Sort ─────────────────────────────────────────────────────────
    private void setupFilterButton() {
        binding.btnFilter.setOnClickListener(v -> showSortSheet());
    }

    private void showSortSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_filter, null);
        sheet.setContentView(sheetView);

        RadioGroup rg   = sheetView.findViewById(R.id.rgSort);
        RadioButton asc = sheetView.findViewById(R.id.rbSortAsc);
        RadioButton dsc = sheetView.findViewById(R.id.rbSortDesc);
        if (currentSort == SORT_ASC)  asc.setChecked(true);
        if (currentSort == SORT_DESC) dsc.setChecked(true);

        rg.setOnCheckedChangeListener((group, id) -> {
            currentSort = (id == R.id.rbSortAsc) ? SORT_ASC : SORT_DESC;
            applySort();
            sheet.dismiss();
        });
        sheet.show();
    }

    private void applySort() {
        if (allProducts.isEmpty()) return;
        List<Product> sorted = new ArrayList<>(allProducts);
        if (currentSort == SORT_ASC)
            Collections.sort(sorted, (a, b) -> Double.compare(a.getPrice(), b.getPrice()));
        else if (currentSort == SORT_DESC)
            Collections.sort(sorted, (a, b) -> Double.compare(b.getPrice(), a.getPrice()));
        productAdapter.submitList(sorted);
    }

    // ── Load ──────────────────────────────────────────────────────────────────
    private void loadInitial() {
        if (currentFaceShape != null)    loadByFaceShape(currentFaceShape);
        else if (isAllShapes)            loadAllShapes();
        else if (currentCategoryId != null) loadByCategory(currentCategoryId);
        else                             loadAll();
    }

    private void loadAll() {
        showLoading();
        productRepository.getAllProducts(wrap());
    }

    private void loadByCategory(String categoryId) {
        showLoading();
        productRepository.getProductsByCategory(categoryId, wrap());
    }

    private void loadByFaceShape(String shape) {
        showLoading();
        productRepository.getProductsByFaceShape(shape, wrap());
    }

    private void loadAllShapes() {
        showLoading();
        productRepository.getProductsByFaceShapeAll(wrap());
    }

    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    private ProductRepository.ProductListCallback wrap() {
        return new ProductRepository.ProductListCallback() {
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
                Toast.makeText(ProductListActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        };
    }
}

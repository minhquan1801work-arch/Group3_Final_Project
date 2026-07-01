package com.FinalProject.group3.ui.catalog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.FinalProject.group3.adapter.ProductAdapter;
import com.FinalProject.group3.databinding.ActivityProductListBinding;
import com.FinalProject.group3.model.Product;
import com.FinalProject.group3.repository.ProductRepository;

import java.util.List;

/**
 * DL.Product — danh sach san pham theo category hoac tat ca.
 *
 * Vong doi Activity:
 * - onCreate: inflate binding, lay Intent extras, setup adapter, goi load
 * - onDestroy: binding = null (ViewBinding tu don, Activity bi huy)
 * Guard `if (isFinishing() || isDestroyed()) return` trong moi callback Firestore.
 */
public class ProductListActivity extends AppCompatActivity {

    private static final String EXTRA_CATEGORY_ID   = "extra_category_id";
    private static final String EXTRA_CATEGORY_NAME = "extra_category_name";

    /** Mo danh sach san pham theo category cu the */
    public static void start(Context context, String categoryId, String categoryName) {
        Intent intent = new Intent(context, ProductListActivity.class);
        intent.putExtra(EXTRA_CATEGORY_ID, categoryId);
        intent.putExtra(EXTRA_CATEGORY_NAME, categoryName);
        context.startActivity(intent);
    }

    /** Mo trang "Tat ca san pham" — khong filter category */
    public static void startAll(Context context) {
        Intent intent = new Intent(context, ProductListActivity.class);
        intent.putExtra(EXTRA_CATEGORY_NAME, "Tat ca san pham");
        context.startActivity(intent);
    }

    private ActivityProductListBinding binding;
    private final ProductRepository productRepository = new ProductRepository();
    private ProductAdapter productAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String categoryId   = getIntent().getStringExtra(EXTRA_CATEGORY_ID);
        String categoryName = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);
        binding.tvTitle.setText(categoryName != null ? categoryName : "San pham");
        binding.btnBack.setOnClickListener(v -> finish());

        productAdapter = new ProductAdapter(product ->
                Toast.makeText(this, "Chi tiet san pham se lam o buoc tiep theo", Toast.LENGTH_SHORT).show());
        binding.rvProducts.setAdapter(productAdapter);

        // categoryId == null -> load tat ca san pham
        if (categoryId == null) {
            loadAllProducts();
        } else {
            loadProducts(categoryId);
        }
    }

    /** Load tat ca san pham (khong filter) */
    private void loadAllProducts() {
        binding.progressBar.setVisibility(View.VISIBLE);
        productRepository.getAllProducts(new ProductRepository.ProductListCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                productAdapter.submitList(products);
                binding.tvEmpty.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(String error) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.tvEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(ProductListActivity.this, "Loi tai san pham: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Load san pham theo category */
    private void loadProducts(String categoryId) {
        binding.progressBar.setVisibility(View.VISIBLE);
        productRepository.getProductsByCategory(categoryId, new ProductRepository.ProductListCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                productAdapter.submitList(products);
                binding.tvEmpty.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(String error) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.tvEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(ProductListActivity.this, "Loi tai san pham: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

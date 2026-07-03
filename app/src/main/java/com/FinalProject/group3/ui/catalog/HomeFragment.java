package com.FinalProject.group3.ui.catalog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.FinalProject.group3.adapter.FeaturedProductAdapter;
import com.FinalProject.group3.adapter.ProductAdapter;
import com.FinalProject.group3.databinding.FragmentHomeBinding;
import com.FinalProject.group3.model.Product;
import com.FinalProject.group3.repository.ProductRepository;

import java.util.List;

/**
 * LA.Homepage -- khop Figma.
 *
 * Vong doi Fragment (chu y vi song trong BottomNavigationView):
 * - onCreateView: inflate binding (tuong duong Activity.onCreate)
 * - onViewCreated: setup adapter + load data (tuong duong onStart/onResume)
 * - onDestroyView: binding = null de tranh leak (tuong duong onDestroy)
 * Fragment instance co the con song (onStop) sau khi view bi huy khi chuyen tab
 * -> callback Firestore PHAI guard `if (binding == null) return` truoc khi dung UI.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private final ProductRepository productRepository = new ProductRepository();

    private FeaturedProductAdapter featuredAdapter;
    private ProductAdapter productAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupAdapters();
        setupClickListeners();
        loadProducts();
    }

    private void setupAdapters() {
        // Hang ngang "SAN PHAM BAN CHAY" -> mo Chi tiet san pham [B1]
        featuredAdapter = new FeaturedProductAdapter(product ->
                ProductDetailActivity.start(requireContext(), product.getProductId()));
        binding.rvFeaturedProducts.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvFeaturedProducts.setAdapter(featuredAdapter);

        // Luoi 2 cot "SAN PHAM NOI BAT" -> mo Chi tiet san pham [B1]
        productAdapter = new ProductAdapter(product ->
                ProductDetailActivity.start(requireContext(), product.getProductId()));
        binding.rvProducts.setAdapter(productAdapter);
    }

    private void setupClickListeners() {
        // Hamburger menu
        binding.btnMenu.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Menu danh muc", Toast.LENGTH_SHORT).show());

        // Search -> TODO SearchActivity
        binding.btnSearch.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Tim kiem", Toast.LENGTH_SHORT).show());

        // Cart icon header -> mở màn Giỏ hàng (cart không còn ở footer pill,
        // vào bằng NavController vì cartFragment vẫn nằm trong nav graph)
        binding.btnCart.setOnClickListener(v ->
                androidx.navigation.fragment.NavHostFragment.findNavController(this)
                        .navigate(com.FinalProject.group3.R.id.cartFragment));

        // "Xem tat ca" -> mo ProductListActivity (tat ca san pham)
        binding.btnViewAllFeatured.setOnClickListener(v ->
                ProductListActivity.startAll(requireContext()));

        // Bo suu tap -> TODO CollectionActivity
        binding.btnViewAllCollections.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Bo suu tap -- se lam o buoc tiep theo", Toast.LENGTH_SHORT).show());

        binding.btnViewAllProducts.setOnClickListener(v ->
                ProductListActivity.startAll(requireContext()));
    }

    private void loadProducts() {
        productRepository.getBestSellerProducts(10, new ProductRepository.ProductListCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                if (binding == null) return; // tab bi switch truoc khi Firestore tra ve
                featuredAdapter.submitList(products);
                productAdapter.submitList(products);
                binding.tvEmptyProducts.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(String error) {
                if (binding == null) return;
                binding.tvEmptyProducts.setVisibility(View.VISIBLE);
                Toast.makeText(requireContext(), "Loi tai san pham: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // tranh memory leak View sau khi Fragment view bi huy khi chuyen tab
    }
}

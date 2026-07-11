package com.FinalProject.group3.ui.catalog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalProject.group3.adapter.CollectionProductAdapter;
import com.FinalProject.group3.databinding.ActivityCollectionBinding;
import com.FinalProject.group3.model.Product;
import com.FinalProject.group3.repository.ProductRepository;
import com.FinalProject.group3.utils.InsetsUtil;

import java.util.List;

/**
 * 2 chế độ (theo Figma):
 *  - Không có EXTRA  → trang "March Collection": nền tối, 3 BST xếp dọc (01/02/03),
 *                      bấm 1 BST → mở lại chính activity này với EXTRA.
 *  - Có EXTRA_COLLECTION → trang chi tiết BST: hero full-bleed + title overlay,
 *                      sheet trắng "HÀNG MỚI VỀ" card nhỏ ngang, nút "Xem tất cả" → grid.
 */
public class CollectionActivity extends AppCompatActivity {

    public static final String EXTRA_COLLECTION = "extra_collection";

    private static final String CLOUD = "https://res.cloudinary.com/aa1g9udv/image/upload/";
    private static final String URL_MONOCHROME = CLOUD + "v1783502208/d21ee09b2dcb18b17af1ec5262d245334b74241b_lwh1kx.png";
    private static final String URL_ESSENTIAL   = CLOUD + "v1783502208/7aec1cc6374895c92464c3118255d38449be11ee_yzemoi.png";
    private static final String URL_SUNLIGHT    = CLOUD + "v1783502207/36566f6bfcef59072645817ac9273fc3824ad0c3_msnssy.png";

    // Ảnh hero riêng cho trang chi tiết 1 BST — khác ảnh preview (Home + danh sách BST)
    private static final String URL_HERO_MONOCHROME = CLOUD + "v1783354469/glassity/site/hero_bg1.png";
    private static final String URL_HERO_ESSENTIAL   = CLOUD + "v1783354471/glassity/site/hero_bg2.png";
    private static final String URL_HERO_SUNLIGHT    = CLOUD + "v1783354475/glassity/site/hero_bg3.png";

    public static final String COL_MONOCHROME = "Monochrome Collection";
    public static final String COL_ESSENTIAL  = "Essential Acetate";
    public static final String COL_SUNLIGHT   = "Sunlight Studio";

    private ActivityCollectionBinding binding;
    private ProductRepository repo;
    private CollectionProductAdapter adapter;

    public static void start(Context ctx) {
        ctx.startActivity(new Intent(ctx, CollectionActivity.class));
    }

    public static void start(Context ctx, String collection) {
        Intent i = new Intent(ctx, CollectionActivity.class);
        i.putExtra(EXTRA_COLLECTION, collection);
        ctx.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCollectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applySystemBarsPadding(binding.getRoot());

        repo = new ProductRepository();

        String collection = getIntent().getStringExtra(EXTRA_COLLECTION);
        if (collection != null) {
            showSingle(collection);
        } else {
            showAll();
        }
    }

    // ── Chế độ A: danh sách 3 BST nền tối ────────────────────────────────────
    private void showAll() {
        binding.layoutAll.setVisibility(View.VISIBLE);
        binding.layoutSingle.setVisibility(View.GONE);

        com.bumptech.glide.Glide.with(this).load(URL_MONOCHROME).centerCrop().into(binding.imgAllMonochrome);
        com.bumptech.glide.Glide.with(this).load(URL_ESSENTIAL).centerCrop().into(binding.imgAllEssential);
        com.bumptech.glide.Glide.with(this).load(URL_SUNLIGHT).centerCrop().into(binding.imgAllSunlight);

        binding.btnBackAll.setOnClickListener(v -> finish());
        binding.sectionAllMonochrome.setOnClickListener(v -> start(this, COL_MONOCHROME));
        binding.sectionAllEssential.setOnClickListener(v -> start(this, COL_ESSENTIAL));
        binding.sectionAllSunlight.setOnClickListener(v -> start(this, COL_SUNLIGHT));
    }

    // ── Chế độ B: chi tiết 1 BST ─────────────────────────────────────────────
    private void showSingle(String collection) {
        binding.layoutAll.setVisibility(View.GONE);
        binding.layoutSingle.setVisibility(View.VISIBLE);

        String heroUrl;
        switch (collection) {
            case COL_ESSENTIAL: heroUrl = URL_HERO_ESSENTIAL; break;
            case COL_SUNLIGHT:  heroUrl = URL_HERO_SUNLIGHT;  break;
            case COL_MONOCHROME:
            default:            heroUrl = URL_HERO_MONOCHROME; break;
        }
        com.bumptech.glide.Glide.with(this).load(heroUrl).centerCrop().into(binding.imgHero);
        binding.tvHeroTitle.setText(collection.toUpperCase().replace(" ", "\n"));

        binding.btnBack.setOnClickListener(v -> finish());

        adapter = new CollectionProductAdapter(
                p -> ProductDetailActivity.start(this, p.getProductId()));
        binding.rvProducts.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvProducts.setAdapter(adapter);

        // Mũi tên < > cuộn list
        binding.btnNext.setOnClickListener(v -> scrollBy(1));
        binding.btnPrev.setOnClickListener(v -> scrollBy(-1));

        // "Xem tất cả" → trang danh sách sản phẩm, lọc theo BST này
        binding.btnViewAll.setOnClickListener(v ->
                ProductListActivity.startCollection(this, collection));

        repo.getProductsByCollection(collection, new ProductRepository.ProductListCallback() {
            @Override public void onSuccess(List<Product> products) { adapter.submitList(products); }
            @Override public void onFailure(String error) {
                Toast.makeText(CollectionActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void scrollBy(int direction) {
        RecyclerView rv = binding.rvProducts;
        LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
        if (lm == null || adapter.getItemCount() == 0) return;
        int target = direction > 0
                ? Math.min(lm.findLastVisibleItemPosition() + 1, adapter.getItemCount() - 1)
                : Math.max(lm.findFirstVisibleItemPosition() - 1, 0);
        rv.smoothScrollToPosition(target);
    }
}

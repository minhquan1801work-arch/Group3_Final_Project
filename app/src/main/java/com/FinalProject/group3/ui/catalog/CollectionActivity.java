package com.FinalProject.group3.ui.catalog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.FinalProject.group3.adapter.ProductAdapter;
import com.FinalProject.group3.databinding.ActivityCollectionBinding;
import com.FinalProject.group3.model.Product;
import com.FinalProject.group3.repository.ProductRepository;
import com.FinalProject.group3.utils.InsetsUtil;

import java.util.List;

public class CollectionActivity extends AppCompatActivity {

    public static final String EXTRA_COLLECTION = "extra_collection";

    private static final String CLOUD = "https://res.cloudinary.com/aa1g9udv/image/upload/";
    private static final String URL_MONOCHROME = CLOUD + "v1783354469/glassity/site/hero_bg1.png";
    private static final String URL_ESSENTIAL   = CLOUD + "v1783354477/glassity/site/flatlay_background.png";
    private static final String URL_SUNLIGHT    = CLOUD + "v1783354471/glassity/site/hero_bg2.png";

    private ActivityCollectionBinding binding;
    private ProductRepository repo;

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

        com.bumptech.glide.Glide.with(this).load(URL_MONOCHROME).centerCrop().into(binding.imgMonochrome);
        com.bumptech.glide.Glide.with(this).load(URL_ESSENTIAL).centerCrop().into(binding.imgEssential);
        com.bumptech.glide.Glide.with(this).load(URL_SUNLIGHT).centerCrop().into(binding.imgSunlight);

        binding.btnBack.setOnClickListener(v -> finish());

        String collection = getIntent().getStringExtra(EXTRA_COLLECTION);
        if (collection != null) {
            showSingleCollection(collection);
        } else {
            showAllCollections();
        }
    }

    private void showAllCollections() {
        binding.tvTitle.setText("Bộ Sưu Tập");
        loadSection("Monochrome Collection", binding.rvMonochrome);
        loadSection("Essential Acetate",     binding.rvEssential);
        loadSection("Sunlight Studio",       binding.rvSunlight);
    }

    private void showSingleCollection(String collection) {
        binding.tvTitle.setText(collection);
        binding.sectionMonochrome.setVisibility(android.view.View.GONE);
        binding.sectionEssential.setVisibility(android.view.View.GONE);
        binding.sectionSunlight.setVisibility(android.view.View.GONE);

        switch (collection) {
            case "Monochrome Collection":
                binding.sectionMonochrome.setVisibility(android.view.View.VISIBLE);
                loadSection(collection, binding.rvMonochrome);
                break;
            case "Essential Acetate":
                binding.sectionEssential.setVisibility(android.view.View.VISIBLE);
                loadSection(collection, binding.rvEssential);
                break;
            case "Sunlight Studio":
                binding.sectionSunlight.setVisibility(android.view.View.VISIBLE);
                loadSection(collection, binding.rvSunlight);
                break;
        }
    }

    private void loadSection(String collection,
            androidx.recyclerview.widget.RecyclerView rv) {
        ProductAdapter adapter = new ProductAdapter(
                p -> ProductDetailActivity.start(this, p.getProductId()));
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rv.setAdapter(adapter);

        repo.getProductsByCollection(collection, new ProductRepository.ProductListCallback() {
            @Override public void onSuccess(List<Product> products) { adapter.submitList(products); }
            @Override public void onFailure(String error) {
                Toast.makeText(CollectionActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

package com.FinalProject.group3.ui.catalog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.FinalProject.group3.adapter.ReviewAdapter;
import com.FinalProject.group3.databinding.ActivityAllReviewsBinding;
import com.FinalProject.group3.utils.FirebaseHelper;
import com.FinalProject.group3.utils.ReviewViewBinder;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** LA.Review (Figma) — toàn bộ đánh giá của 1 sản phẩm, mở từ "Tất cả" ở ProductDetailActivity. */
public class AllReviewsActivity extends AppCompatActivity {

    private static final String EXTRA_PRODUCT_ID = "product_id";

    private ActivityAllReviewsBinding binding;
    private final ReviewAdapter adapter = new ReviewAdapter();

    public static void start(Context context, String productId) {
        context.startActivity(new Intent(context, AllReviewsActivity.class)
                .putExtra(EXTRA_PRODUCT_ID, productId));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllReviewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        com.FinalProject.group3.utils.InsetsUtil.applySystemBarsPadding(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        binding.rvReviews.setAdapter(adapter);

        String productId = getIntent().getStringExtra(EXTRA_PRODUCT_ID);
        if (productId == null) { finish(); return; }
        loadReviews(productId);
    }

    private void loadReviews(String productId) {
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_REVIEWS)
                .whereEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(snap -> {
                    if (binding == null) return;
                    List<DocumentSnapshot> docs = new ArrayList<>(snap.getDocuments());
                    Collections.sort(docs, (a, b) -> {
                        Timestamp ta = a.getTimestamp("createdAt");
                        Timestamp tb = b.getTimestamp("createdAt");
                        if (ta == null || tb == null) return 0;
                        return tb.compareTo(ta);
                    });
                    bind(docs);
                })
                .addOnFailureListener(e -> {
                    if (binding != null) bind(new ArrayList<>());
                });
    }

    private void bind(List<DocumentSnapshot> docs) {
        adapter.submitList(docs);

        if (docs.isEmpty()) {
            binding.rvReviews.setVisibility(View.GONE);
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.tvRatingAvg.setText("—");
            return;
        }

        double sum = 0;
        for (DocumentSnapshot d : docs) sum += ReviewViewBinder.ratingOf(d);
        binding.tvRatingAvg.setText(String.format(new Locale("vi", "VN"), "%.1f", sum / docs.size()));
    }
}

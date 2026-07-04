package com.FinalProject.group3.ui.order;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.FinalProject.group3.R;
import com.FinalProject.group3.databinding.ActivityReviewBinding;
import com.FinalProject.group3.utils.InsetsUtil;
import com.bumptech.glide.Glide;

public class ReviewActivity extends AppCompatActivity {

    private static final String EXTRA_PRODUCT_ID = "productId";
    private static final String EXTRA_PRODUCT_NAME = "productName";
    private static final String EXTRA_PRODUCT_IMAGE = "productImage";
    private static final String EXTRA_ORDER_ID = "orderId";

    public static Intent intent(Context context, String productId, String productName,
                                 String productImage, String orderId) {
        return new Intent(context, ReviewActivity.class)
                .putExtra(EXTRA_PRODUCT_ID, productId)
                .putExtra(EXTRA_PRODUCT_NAME, productName)
                .putExtra(EXTRA_PRODUCT_IMAGE, productImage)
                .putExtra(EXTRA_ORDER_ID, orderId);
    }

    private ActivityReviewBinding binding;
    private int selectedStar = 5;
    private final ImageView[] stars = new ImageView[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applySystemBarsPadding(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        String name = getIntent().getStringExtra(EXTRA_PRODUCT_NAME);
        String image = getIntent().getStringExtra(EXTRA_PRODUCT_IMAGE);

        binding.tvProductName.setText(name != null ? name : "");
        if (image != null && !image.isEmpty()) {
            Glide.with(this).load(image).centerCrop().into(binding.ivProduct);
        }

        setupStars();

        binding.btnAddPhoto.setOnClickListener(v ->
                Toast.makeText(this, "Tính năng upload ảnh sắp ra mắt", Toast.LENGTH_SHORT).show());

        binding.btnSubmit.setOnClickListener(v -> submit());
    }

    private void setupStars() {
        stars[0] = binding.star1;
        stars[1] = binding.star2;
        stars[2] = binding.star3;
        stars[3] = binding.star4;
        stars[4] = binding.star5;

        for (int i = 0; i < stars.length; i++) {
            final int idx = i + 1;
            stars[i].setOnClickListener(v -> {
                selectedStar = idx;
                updateStarUI();
            });
        }
        updateStarUI();
    }

    private void updateStarUI() {
        int filled = R.drawable.ic_star;
        int empty = R.drawable.ic_star_outline;
        for (int i = 0; i < stars.length; i++) {
            stars[i].setImageResource(i < selectedStar ? filled : empty);
        }
        String[] labels = {"Tệ", "Không tốt", "Bình thường", "Tốt", "Tuyệt vời"};
        binding.tvStarLabel.setText(labels[selectedStar - 1]);
    }

    private void submit() {
        String review = binding.etReview.getText().toString().trim();
        if (review.isEmpty()) {
            binding.etReview.setError("Vui lòng viết đánh giá");
            return;
        }
        // TODO: lưu đánh giá lên Firestore products/{id}/reviews
        Toast.makeText(this, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_LONG).show();
        finish();
    }
}

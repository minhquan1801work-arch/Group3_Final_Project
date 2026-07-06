package com.FinalProject.group3.ui.order;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.FinalProject.group3.R;
import com.FinalProject.group3.databinding.ActivityReviewBinding;
import com.FinalProject.group3.model.OrderDetail;
import com.FinalProject.group3.model.Product;
import com.FinalProject.group3.repository.ProductRepository;
import com.FinalProject.group3.utils.FirebaseHelper;
import com.FinalProject.group3.utils.InsetsUtil;
import com.bumptech.glide.Glide;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewActivity extends AppCompatActivity {

    private static final String EXTRA_ORDER_ID = "orderId";
    private static final int MAX_PHOTOS = 5;

    public static Intent intent(Context context, String orderId) {
        return new Intent(context, ReviewActivity.class)
                .putExtra(EXTRA_ORDER_ID, orderId);
    }

    private ActivityReviewBinding binding;
    private int selectedStar = 0;
    private final ImageView[] stars = new ImageView[5];

    private String orderId;
    private String firstProductId;
    private final List<Uri> selectedImages = new ArrayList<>();
    private Uri cameraImageUri;

    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<String> cameraPermLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applySystemBarsPadding(binding.getRoot());

        orderId = getIntent().getStringExtra(EXTRA_ORDER_ID);

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(), success -> {
                    if (Boolean.TRUE.equals(success) && cameraImageUri != null) {
                        addPhoto(cameraImageUri);
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(), uri -> {
                    if (uri != null) addPhoto(uri);
                });

        cameraPermLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), granted -> {
                    if (Boolean.TRUE.equals(granted)) launchCamera();
                    else Toast.makeText(this, "Cần quyền camera để chụp ảnh", Toast.LENGTH_SHORT).show();
                });

        binding.btnBack.setOnClickListener(v -> finish());
        setupStars();
        binding.btnSubmit.setOnClickListener(v -> submit());

        rebuildPhotoRow();
        if (orderId != null) loadProductFromOrder(orderId);
    }

    // ── Hàng ảnh (tối đa 5) ───────────────────────────────────────────────────

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    private void rebuildPhotoRow() {
        binding.llPhotos.removeAllViews();
        int size = dp(80);
        int marginEnd = dp(8);

        for (int i = 0; i < selectedImages.size(); i++) {
            final int idx = i;

            FrameLayout frame = new FrameLayout(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMarginEnd(marginEnd);
            frame.setLayoutParams(lp);

            ImageView iv = new ImageView(this);
            iv.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(this).load(selectedImages.get(i)).centerCrop().into(iv);
            frame.addView(iv);

            ImageView btnX = new ImageView(this);
            FrameLayout.LayoutParams xLp = new FrameLayout.LayoutParams(dp(20), dp(20));
            xLp.gravity = Gravity.TOP | Gravity.END;
            xLp.setMargins(0, dp(3), dp(3), 0);
            btnX.setLayoutParams(xLp);
            btnX.setImageResource(R.drawable.ic_close);
            btnX.setBackgroundResource(R.drawable.bg_photo_remove_btn);
            btnX.setPadding(dp(4), dp(4), dp(4), dp(4));
            btnX.setOnClickListener(v -> removePhoto(idx));
            frame.addView(btnX);

            binding.llPhotos.addView(frame);
        }

        if (selectedImages.size() < MAX_PHOTOS) {
            LinearLayout placeholder = new LinearLayout(this);
            placeholder.setLayoutParams(new LinearLayout.LayoutParams(size, size));
            placeholder.setOrientation(LinearLayout.VERTICAL);
            placeholder.setGravity(Gravity.CENTER);
            placeholder.setBackgroundResource(R.drawable.bg_chip_variant);
            placeholder.setOnClickListener(v -> showPhotoChooser());

            ImageView camera = new ImageView(this);
            LinearLayout.LayoutParams camLp = new LinearLayout.LayoutParams(dp(28), dp(28));
            camera.setLayoutParams(camLp);
            camera.setImageResource(R.drawable.ic_camera);
            camera.setColorFilter(
                    getResources().getColor(R.color.color_hint, getTheme()),
                    android.graphics.PorterDuff.Mode.SRC_IN);
            placeholder.addView(camera);

            TextView label = new TextView(this);
            LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            labelLp.topMargin = dp(4);
            label.setLayoutParams(labelLp);
            label.setTextSize(10);
            label.setTextColor(getResources().getColor(R.color.color_hint, getTheme()));
            if (selectedImages.isEmpty()) {
                label.setText("Thêm ảnh sản phẩm");
            } else {
                label.setText("(" + selectedImages.size() + "/5)");
            }
            placeholder.addView(label);

            binding.llPhotos.addView(placeholder);
        }
    }

    private void addPhoto(Uri uri) {
        if (selectedImages.size() >= MAX_PHOTOS) return;
        selectedImages.add(uri);
        rebuildPhotoRow();
    }

    private void removePhoto(int index) {
        if (index < 0 || index >= selectedImages.size()) return;
        selectedImages.remove(index);
        rebuildPhotoRow();
    }

    // ── Chọn ảnh ──────────────────────────────────────────────────────────────

    private void showPhotoChooser() {
        String[] options = {"Chụp ảnh", "Chọn từ thư viện"};
        new AlertDialog.Builder(this)
                .setTitle("Thêm ảnh đánh giá")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) requestCameraAndShoot();
                    else galleryLauncher.launch("image/*");
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void requestCameraAndShoot() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            cameraPermLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        File cacheDir = new File(getCacheDir(), "camera_images");
        //noinspection ResultOfMethodCallIgnored
        cacheDir.mkdirs();
        File imageFile = new File(cacheDir, "review_" + System.currentTimeMillis() + ".jpg");
        cameraImageUri = FileProvider.getUriForFile(this,
                getPackageName() + ".fileprovider", imageFile);
        cameraLauncher.launch(cameraImageUri);
    }

    // ── Load sản phẩm từ đơn hàng ─────────────────────────────────────────────

    private void loadProductFromOrder(String orderId) {
        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_ORDERS)
                .document(orderId)
                .collection(FirebaseHelper.COL_ORDER_DETAILS)
                .get()
                .addOnSuccessListener(snap -> {
                    List<OrderDetail> details = snap.toObjects(OrderDetail.class);
                    if (details.isEmpty()) return;

                    OrderDetail first = details.get(0);
                    firstProductId = first.getProductId();
                    String colorText = first.getColor() != null && !first.getColor().isEmpty()
                            ? " · " + first.getColor() : "";
                    int extra = details.size() - 1;

                    new ProductRepository().getProductById(firstProductId,
                            new ProductRepository.ProductCallback() {
                                @Override
                                public void onSuccess(Product product) {
                                    String suffix = extra > 0 ? " và " + extra + " sản phẩm khác" : "";
                                    binding.tvProductName.setText(product.getName() + colorText + suffix);
                                    List<String> imgs = product.getImages();
                                    if (imgs != null && !imgs.isEmpty()) {
                                        Glide.with(ReviewActivity.this)
                                                .load(imgs.get(0)).centerCrop()
                                                .into(binding.ivProduct);
                                    }
                                }

                                @Override
                                public void onFailure(String error) {
                                    binding.tvProductName.setText("Sản phẩm trong đơn hàng");
                                }
                            });
                });
    }

    // ── Ngôi sao ──────────────────────────────────────────────────────────────

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
        if (selectedStar == 0) {
            binding.tvStarLabel.setText("");
        } else {
            String[] labels = {"Tệ", "Không tốt", "Bình thường", "Tốt", "Tuyệt vời"};
            binding.tvStarLabel.setText(labels[selectedStar - 1]);
        }
    }

    // ── Gửi đánh giá ──────────────────────────────────────────────────────────

    private void submit() {
        if (selectedStar == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = FirebaseHelper.getCurrentUserId();
        if (uid == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        String comment = binding.etReview.getText().toString().trim();
        binding.btnSubmit.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        if (!selectedImages.isEmpty()) {
            uploadAllThenSave(uid, comment);
        } else {
            saveReview(uid, comment, new ArrayList<>());
        }
    }

    private void uploadAllThenSave(String uid, String comment) {
        List<String> urls = new ArrayList<>();
        uploadNext(uid, comment, 0, urls);
    }

    private void uploadNext(String uid, String comment, int idx, List<String> urls) {
        if (idx >= selectedImages.size()) {
            saveReview(uid, comment, urls);
            return;
        }
        String path = "reviews/" + uid + "/" + System.currentTimeMillis() + "_" + idx + ".jpg";
        StorageReference ref = FirebaseHelper.getStorageRef().child(path);
        ref.putFile(selectedImages.get(idx))
                .addOnSuccessListener(snap ->
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            urls.add(uri.toString());
                            uploadNext(uid, comment, idx + 1, urls);
                        }))
                .addOnFailureListener(e -> {
                    binding.btnSubmit.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Upload ảnh thất bại, thử lại", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveReview(String uid, String comment, List<String> imageUrls) {
        Map<String, Object> review = new HashMap<>();
        review.put("productId", firstProductId);
        review.put("orderId", orderId);
        review.put("customerId", uid);
        review.put("rating", selectedStar);
        review.put("comment", comment);
        if (!imageUrls.isEmpty()) review.put("imageUrls", imageUrls);
        review.put("createdAt", new Date());

        FirebaseHelper.getDb().collection(FirebaseHelper.COL_REVIEWS).add(review)
                .addOnSuccessListener(ref -> {
                    if (orderId != null) {
                        FirebaseHelper.getDb().collection(FirebaseHelper.COL_ORDERS)
                                .document(orderId).update("reviewed", true);
                    }
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.btnSubmit.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Gửi thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

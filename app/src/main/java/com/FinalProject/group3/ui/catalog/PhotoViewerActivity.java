package com.FinalProject.group3.ui.catalog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.FinalProject.group3.adapter.PhotoViewerAdapter;
import com.FinalProject.group3.databinding.ActivityPhotoViewerBinding;

import java.util.ArrayList;

/** Xem ảnh review full-size, pinch-zoom (PhotoView), vuốt ngang nếu nhiều ảnh. */
public class PhotoViewerActivity extends AppCompatActivity {

    private static final String EXTRA_URLS = "photo_urls";
    private static final String EXTRA_INDEX = "start_index";

    public static void start(Context context, ArrayList<String> urls, int startIndex) {
        Intent intent = new Intent(context, PhotoViewerActivity.class);
        intent.putStringArrayListExtra(EXTRA_URLS, urls);
        intent.putExtra(EXTRA_INDEX, startIndex);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityPhotoViewerBinding binding = ActivityPhotoViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        com.FinalProject.group3.utils.InsetsUtil.applySystemBarsPadding(binding.getRoot());

        ArrayList<String> urls = getIntent().getStringArrayListExtra(EXTRA_URLS);
        int startIndex = getIntent().getIntExtra(EXTRA_INDEX, 0);
        if (urls == null || urls.isEmpty()) { finish(); return; }

        binding.vpPhotos.setAdapter(new PhotoViewerAdapter(urls));
        binding.vpPhotos.setCurrentItem(startIndex, false);

        updateCounter(binding, startIndex, urls.size());
        binding.vpPhotos.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateCounter(binding, position, urls.size());
            }
        });
        binding.tvCounter.setVisibility(urls.size() > 1 ? android.view.View.VISIBLE : android.view.View.GONE);

        binding.btnClose.setOnClickListener(v -> finish());
    }

    private void updateCounter(ActivityPhotoViewerBinding binding, int position, int total) {
        binding.tvCounter.setText((position + 1) + " / " + total);
    }
}

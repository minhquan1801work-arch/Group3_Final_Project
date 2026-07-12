package com.FinalProject.group3.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.FinalProject.group3.R;
import com.FinalProject.group3.databinding.ItemReviewBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/** Gán dữ liệu 1 review (Firestore doc) vào item_review.xml — dùng chung cho
 *  ProductDetailActivity (mini list) và AllReviewsActivity (danh sách đầy đủ). */
public final class ReviewViewBinder {

    private static final SimpleDateFormat DATE_FMT =
            new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));

    private ReviewViewBinder() {}

    public static int ratingOf(DocumentSnapshot d) {
        Long ratingL = d.getLong("rating");
        return ratingL == null ? 5 : ratingL.intValue();
    }

    public static void bind(Context context, ItemReviewBinding item, DocumentSnapshot d) {
        int rating = ratingOf(d);
        String name = d.getString("userName");
        item.tvReviewer.setText(name != null && !name.isEmpty() ? name : "Khách hàng Glassity");
        Timestamp ts = d.getTimestamp("createdAt");
        item.tvReviewDate.setText(ts != null ? DATE_FMT.format(ts.toDate()) : "");
        item.tvReviewText.setText(d.getString("comment"));

        float density = context.getResources().getDisplayMetrics().density;
        int starSize = (int) (14 * density);
        item.llStars.removeAllViews();
        for (int i = 0; i < 5; i++) {
            ImageView star = new ImageView(context);
            star.setLayoutParams(new ViewGroup.LayoutParams(starSize, starSize));
            star.setImageResource(i < rating ? R.drawable.ic_star : R.drawable.ic_star_outline);
            item.llStars.addView(star);
        }

        item.llReviewImages.removeAllViews();
        Object imgs = d.get("imageUrls");
        String videoUrl = d.getString("videoUrl");
        boolean hasImages = imgs instanceof List && !((List<?>) imgs).isEmpty();

        // Video: thumbnail (frame đầu, Cloudinary trả về .jpg) + icon play, bấm mở trình phát
        if (videoUrl != null && !videoUrl.isEmpty()) {
            item.hsReviewImages.setVisibility(View.VISIBLE);
            int thumbSize = (int) (64 * density);
            int thumbGap = (int) (6 * density);

            android.widget.FrameLayout frame = new android.widget.FrameLayout(context);
            LinearLayout.LayoutParams flp = new LinearLayout.LayoutParams(thumbSize, thumbSize);
            flp.setMarginEnd(thumbGap);
            frame.setLayoutParams(flp);

            ImageView iv = new ImageView(context);
            iv.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            String thumbUrl = videoUrl.replaceAll("\\.(mp4|webm|mov)$", ".jpg");
            Glide.with(context).load(thumbUrl).into(iv);
            frame.addView(iv);

            ImageView play = new ImageView(context);
            int playSize = (int) (24 * density);
            android.widget.FrameLayout.LayoutParams plp =
                    new android.widget.FrameLayout.LayoutParams(playSize, playSize);
            plp.gravity = android.view.Gravity.CENTER;
            play.setLayoutParams(plp);
            play.setImageResource(R.drawable.ic_play_circle);
            frame.addView(play);

            final String url = videoUrl;
            frame.setOnClickListener(v -> {
                android.content.Intent i = new android.content.Intent(
                        android.content.Intent.ACTION_VIEW);
                i.setDataAndType(android.net.Uri.parse(url), "video/*");
                context.startActivity(i);
            });
            item.llReviewImages.addView(frame);
        }

        if (hasImages) {
            item.hsReviewImages.setVisibility(View.VISIBLE);
            int thumbSize = (int) (64 * density);
            int thumbGap = (int) (6 * density);

            java.util.ArrayList<String> urls = new java.util.ArrayList<>();
            for (Object url : (List<?>) imgs) urls.add(String.valueOf(url));

            for (int i = 0; i < urls.size(); i++) {
                final int index = i;
                ImageView iv = new ImageView(context);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(thumbSize, thumbSize);
                lp.setMarginEnd(thumbGap);
                iv.setLayoutParams(lp);
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(context).load(CloudinaryUtil.optimize(urls.get(i), 200)).into(iv);
                iv.setOnClickListener(v ->
                        com.FinalProject.group3.ui.catalog.PhotoViewerActivity.start(context, urls, index));
                item.llReviewImages.addView(iv);
            }
        } else if (videoUrl == null || videoUrl.isEmpty()) {
            item.hsReviewImages.setVisibility(View.GONE);
        }
    }
}

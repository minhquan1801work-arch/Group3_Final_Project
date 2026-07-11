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
        if (imgs instanceof List && !((List<?>) imgs).isEmpty()) {
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
        } else {
            item.hsReviewImages.setVisibility(View.GONE);
        }
    }
}

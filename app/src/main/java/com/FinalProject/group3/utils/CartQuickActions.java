package com.FinalProject.group3.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.FinalProject.group3.adapter.ProductAdapter;
import com.FinalProject.group3.model.CartDetail;
import com.FinalProject.group3.model.Product;
import com.FinalProject.group3.model.ProductVariant;
import com.FinalProject.group3.repository.CartRepository;
import com.FinalProject.group3.ui.order.CheckoutActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Gắn hành vi "Thêm vào giỏ" + "Mua ngay" thật cho ProductAdapter
 * (card sản phẩm ở HomeFragment, ProductListActivity, SearchActivity, CollectionActivity).
 *
 * - Thêm vào giỏ: màu mặc định = variant đầu tiên, qty 1.
 *   Sau khi thêm: icon giỏ bounce + badge số lượng cập nhật.
 * - Mua ngay: khách vẫn đi thẳng Checkout; thành viên thêm giỏ rồi sang Checkout.
 * - Guest "Thêm giỏ" → LoginRequiredDialog.
 */
public final class CartQuickActions {

    public static final String CART_PREFS = "cart_prefs";
    public static final String KEY_LAST_ADDED = "last_added_id";

    private CartQuickActions() {}

    private static String defaultColor(Product product) {
        List<ProductVariant> variants = product.getVariants();
        if (variants != null && !variants.isEmpty() && variants.get(0).getColor() != null) {
            return variants.get(0).getColor();
        }
        List<String> colors = product.getColors();
        return (colors != null && !colors.isEmpty()) ? colors.get(0) : "";
    }

    private static boolean requireLogin(Context context) {
        if (FirebaseHelper.getCurrentUserId() != null) return false;
        LoginRequiredDialog.show(context, "Đăng nhập để thêm sản phẩm vào giỏ hàng của bạn");
        return true;
    }

    // ── wire() ────────────────────────────────────────────────────────────────

    /** Gắn adapter không có animation (backward-compat). */
    public static void wire(ProductAdapter adapter, Context context) {
        wire(adapter, context, null, null);
    }

    /**
     * Gắn adapter có animation + badge.
     *
     * @param cartIconView  ImageView/ImageButton icon giỏ — sẽ bounce sau khi thêm
     * @param cartBadgeView TextView badge đỏ số lượng (id tvCartBadge)
     */
    public static void wire(ProductAdapter adapter, Context context,
                             @Nullable View cartIconView, @Nullable TextView cartBadgeView) {
        adapter.setOnAddToCartListener((product, itemThumbnailView) -> {
            if (requireLogin(context)) return;

            // Fly animation: ảnh sản phẩm thu nhỏ bay lên cart icon
            if (cartIconView != null && context instanceof Activity) {
                flyToCart((Activity) context, itemThumbnailView, cartIconView, () -> {
                    animateCartIcon(cartIconView);
                    if (cartBadgeView != null) refreshBadge(cartBadgeView);
                });
            }

            // Thêm vào giỏ song song với animation
            CartDetail item = new CartDetail(product.getProductId(), 1, defaultColor(product));
            new CartRepository().addToCartReturningId(item, new CartRepository.IdCallback() {
                @Override
                public void onSuccess(String cartDetailId) {
                    context.getSharedPreferences(CART_PREFS, Context.MODE_PRIVATE)
                            .edit().putString(KEY_LAST_ADDED, cartDetailId).apply();
                    // Nếu không có fly animation thì bounce + badge tại đây
                    if (cartIconView == null || !(context instanceof Activity)) {
                        if (cartIconView != null) animateCartIcon(cartIconView);
                        if (cartBadgeView != null) refreshBadge(cartBadgeView);
                    }
                }
                @Override
                public void onFailure(String error) {
                    Toast.makeText(context, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });
        adapter.setOnBuyNowListener(product -> buyNow(context, product));
        // Hiện badge ngay khi mở màn hình
        if (cartBadgeView != null) refreshBadge(cartBadgeView);
    }

    // ── addToCart ─────────────────────────────────────────────────────────────

    public static void addToCart(Context context, Product product) {
        addToCart(context, product, null);
    }

    public static void addToCart(Context context, Product product, @Nullable Runnable onSuccess) {
        if (requireLogin(context)) return;

        CartDetail item = new CartDetail(product.getProductId(), 1, defaultColor(product));
        new CartRepository().addToCartReturningId(item, new CartRepository.IdCallback() {
            @Override
            public void onSuccess(String cartDetailId) {
                context.getSharedPreferences(CART_PREFS, Context.MODE_PRIVATE)
                        .edit().putString(KEY_LAST_ADDED, cartDetailId).apply();
                if (onSuccess != null) onSuccess.run();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(context, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── buyNow ────────────────────────────────────────────────────────────────

    public static void buyNow(Context context, Product product) {
        if (FirebaseHelper.getCurrentUserId() == null) {
            CheckoutActivity.startDirect(context, product.getProductId(), defaultColor(product), 1);
            return;
        }

        CartDetail item = new CartDetail(product.getProductId(), 1, defaultColor(product));
        new CartRepository().addToCartReturningId(item, new CartRepository.IdCallback() {
            @Override
            public void onSuccess(String cartDetailId) {
                ArrayList<String> ids = new ArrayList<>();
                ids.add(cartDetailId);
                CheckoutActivity.start(context, ids);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(context, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Animation + Badge ─────────────────────────────────────────────────────

    /**
     * Hiệu ứng: ảnh sản phẩm thu nhỏ thành quả cầu và bay lên vị trí icon giỏ hàng.
     * Chạy trên DecorView để vượt qua ranh giới Fragment/RecyclerView.
     *
     * Public để ProductDetailActivity gọi trực tiếp từ "Thêm vào giỏ" button.
     */
    public static void flyToCart(Activity activity, View fromView, View toView, Runnable onEnd) {
        if (!(fromView instanceof ImageView)) {
            if (onEnd != null) onEnd.run();
            return;
        }
        Drawable img = ((ImageView) fromView).getDrawable();
        if (img == null || img.getConstantState() == null) {
            if (onEnd != null) onEnd.run();
            return;
        }

        float density = activity.getResources().getDisplayMetrics().density;
        int sizePx = (int) (52 * density);

        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        int screenW = decorView.getWidth();
        int screenH = decorView.getHeight();

        // Tọa độ màn hình của ảnh sản phẩm (giữa)
        int[] fromLoc = new int[2];
        fromView.getLocationOnScreen(fromLoc);
        int startX = fromLoc[0] + fromView.getWidth() / 2 - sizePx / 2;
        int startY = fromLoc[1] + fromView.getHeight() / 2 - sizePx / 2;

        // Tọa độ đích: cart icon nếu đang visible trên màn hình,
        // nếu không (scroll off-screen) → fallback góc trên phải
        int[] toLoc = new int[2];
        toView.getLocationOnScreen(toLoc);
        int iconW = toView.getWidth() > 0 ? toView.getWidth() : (int)(40 * density);
        int iconH = toView.getHeight() > 0 ? toView.getHeight() : (int)(40 * density);
        int endX, endY;
        if (toLoc[1] < 0 || toLoc[1] > screenH || toLoc[0] < 0) {
            // Cart icon đã scroll off-screen → nhắm vào góc trên phải cố định
            endX = screenW - (int)(52 * density);
            endY = (int)(32 * density);
        } else {
            endX = toLoc[0] + iconW / 2 - sizePx / 2;
            endY = toLoc[1] + iconH / 2 - sizePx / 2;
        }

        // Tạo ImageView tròn bay trên DecorView
        ImageView flying = new ImageView(activity);
        flying.setImageDrawable(img.getConstantState().newDrawable(activity.getResources()).mutate());
        flying.setScaleType(ImageView.ScaleType.CENTER_CROP);
        flying.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, android.graphics.Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });
        flying.setClipToOutline(true);
        flying.setElevation(24 * density);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(sizePx, sizePx);
        lp.leftMargin = startX;
        lp.topMargin = startY;
        decorView.addView(flying, lp);

        float dX = endX - startX;
        float dY = endY - startY;

        ObjectAnimator tx    = ObjectAnimator.ofFloat(flying, "translationX", 0f, dX);
        ObjectAnimator ty    = ObjectAnimator.ofFloat(flying, "translationY", 0f, dY);
        ObjectAnimator sx    = ObjectAnimator.ofFloat(flying, "scaleX", 1f, 0.22f);
        ObjectAnimator sy    = ObjectAnimator.ofFloat(flying, "scaleY", 1f, 0.22f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(flying, "alpha", 1f, 0.65f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(tx, ty, sx, sy, alpha);
        set.setDuration(620);
        set.setInterpolator(new AccelerateInterpolator(1.2f));
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                decorView.removeView(flying);
                if (onEnd != null) onEnd.run();
            }
        });
        set.start();
    }

    /** Bounce scale animation cho icon giỏ hàng sau khi thêm item. */
    public static void animateCartIcon(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.3f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.3f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(280);
        set.setInterpolator(new OvershootInterpolator(2.5f));
        set.start();
    }

    /** Đọc số item trong giỏ từ Firestore và cập nhật badge. */
    public static void refreshBadge(TextView badgeView) {
        if (FirebaseHelper.getCurrentUserId() == null) {
            badgeView.setVisibility(View.GONE);
            return;
        }
        new CartRepository().getCartItems(new CartRepository.CartDetailCallback() {
            @Override
            public void onSuccess(List<CartDetail> items) {
                int count = items.size();
                if (count > 0) {
                    badgeView.setVisibility(View.VISIBLE);
                    badgeView.setText(count > 9 ? "9+" : String.valueOf(count));
                } else {
                    badgeView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(String error) { /* im lặng — badge phụ */ }
        });
    }
}

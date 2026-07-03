package com.FinalProject.group3.utils;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Android 15+ (targetSdk 35/36) ép app vẽ tràn màn hình (edge-to-edge),
 * nên layout sẽ bị status bar / cụm camera (notch, punch-hole) đè lên nếu
 * không tự né. Util này đệm padding cho view gốc theo đúng kích thước
 * system bar + camera cutout của TỪNG máy — máy nào cũng tự fit.
 *
 * Dùng trong Activity, ngay sau setContentView:
 *   InsetsUtil.applySystemBarsPadding(binding.getRoot());
 */
public final class InsetsUtil {

    private InsetsUtil() {}

    public static void applySystemBarsPadding(View root) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }
}

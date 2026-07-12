package com.FinalProject.group3.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import com.FinalProject.group3.R;

/**
 * Vẽ ảnh gọng kính demo (PNG nền trong suốt) đè lên preview camera trước,
 * scale + xoay theo landmark 2 mắt và góc nghiêng đầu (roll) trả về từ ML Kit
 * Face Detection. Không xử lý phối cảnh 3D — chỉ biến đổi 2D (scale/rotate/translate).
 */
public class FaceOverlayView extends View {

    /** Tỉ lệ chiều rộng gọng kính / khoảng cách 2 tâm mắt — ước lượng cho ảnh demo dùng chung. */
    private static final float GLASSES_TO_EYE_DISTANCE_RATIO = 2.3f;

    private final Bitmap glassesBitmap;
    private final Matrix matrix = new Matrix();
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    // Kích thước khung ảnh phân tích (đã quy đổi theo rotationDegrees) + cờ ảnh có bị soi gương (camera trước)
    private int imageWidth;
    private int imageHeight;
    private boolean imageFlipped;

    private float scaleFactor = 1f;
    private float postScaleWidthOffset;
    private float postScaleHeightOffset;

    private PointF leftEye;
    private PointF rightEye;
    private float rollDegrees;
    private boolean hasFace;

    public FaceOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        glassesBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.glasses_tryon_demo);
    }

    /** Gọi mỗi khi frame mới có kích thước/rotation khác (thường cố định trong 1 phiên). */
    public void setImageSourceInfo(int width, int height, boolean flipped) {
        this.imageWidth = width;
        this.imageHeight = height;
        this.imageFlipped = flipped;
    }

    /** Gọi khi phát hiện khuôn mặt — tọa độ theo hệ tọa độ ảnh phân tích (chưa quy đổi ra view). */
    public void updateFace(PointF leftEye, PointF rightEye, float rollDegrees) {
        this.leftEye = leftEye;
        this.rightEye = rightEye;
        this.rollDegrees = rollDegrees;
        this.hasFace = leftEye != null && rightEye != null;
        postInvalidate();
    }

    /** Gọi khi không còn phát hiện khuôn mặt trong frame — ẩn overlay. */
    public void clearFace() {
        hasFace = false;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!hasFace || imageWidth == 0 || imageHeight == 0 || glassesBitmap == null) return;
        if (getWidth() == 0 || getHeight() == 0) return;

        computeTransformation();

        float lx = translateX(leftEye.x);
        float ly = translateY(leftEye.y);
        float rx = translateX(rightEye.x);
        float ry = translateY(rightEye.y);

        float centerX = (lx + rx) / 2f;
        float centerY = (ly + ry) / 2f;
        float eyeDistancePx = (float) Math.hypot(rx - lx, ry - ly);
        if (eyeDistancePx <= 0) return;

        float glassesWidth = eyeDistancePx * GLASSES_TO_EYE_DISTANCE_RATIO;
        float scale = glassesWidth / glassesBitmap.getWidth();

        // Đã test thực tế trên máy: camera trước (soi gương) cần GIỮ NGUYÊN dấu rollDegrees,
        // ngược lại (không soi gương) mới cần đảo dấu — trái với suy đoán lý thuyết ban đầu.
        float rotation = imageFlipped ? rollDegrees : -rollDegrees;

        matrix.reset();
        matrix.postTranslate(-glassesBitmap.getWidth() / 2f, -glassesBitmap.getHeight() / 2f);
        matrix.postScale(scale, scale);
        matrix.postRotate(rotation);
        matrix.postTranslate(centerX, centerY);

        canvas.drawBitmap(glassesBitmap, matrix, bitmapPaint);
    }

    /** Tính scale + offset để quy đổi tọa độ ảnh phân tích → tọa độ view, kiểu center-crop (khớp PreviewView fillCenter). */
    private void computeTransformation() {
        float viewAspectRatio = (float) getWidth() / getHeight();
        float imageAspectRatio = (float) imageWidth / imageHeight;
        postScaleWidthOffset = 0;
        postScaleHeightOffset = 0;
        if (viewAspectRatio > imageAspectRatio) {
            scaleFactor = (float) getWidth() / imageWidth;
            postScaleHeightOffset = ((float) getWidth() / imageAspectRatio - getHeight()) / 2;
        } else {
            scaleFactor = (float) getHeight() / imageHeight;
            postScaleWidthOffset = ((float) getHeight() * imageAspectRatio - getWidth()) / 2;
        }
    }

    private float translateX(float x) {
        float scaled = x * scaleFactor - postScaleWidthOffset;
        return imageFlipped ? getWidth() - scaled : scaled;
    }

    private float translateY(float y) {
        return y * scaleFactor - postScaleHeightOffset;
    }
}

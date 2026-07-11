package com.FinalProject.group3.utils;

/**
 * Chèn tham số tối ưu Cloudinary (f_auto,q_auto[,w_x]) vào URL ảnh trước khi
 * đưa cho Glide — giảm hẳn dung lượng tải về (đổi định dạng WebP/AVIF tự động,
 * nén chất lượng tự động, resize đúng kích thước hiển thị) thay vì luôn tải
 * nguyên bản gốc như trước. Không có tác dụng với URI cục bộ (ảnh chụp/chọn
 * từ máy) — chỉ áp cho URL thật sự trỏ tới Cloudinary.
 */
public final class CloudinaryUtil {

    private CloudinaryUtil() {}

    /** f_auto,q_auto — dùng cho ảnh không biết trước kích thước hiển thị cụ thể. */
    public static String optimize(String url) {
        return optimize(url, 0);
    }

    /** f_auto,q_auto,w_{width} — dùng khi biết rõ ảnh sẽ hiển thị ở view cỡ nào. */
    public static String optimize(String url, int width) {
        if (url == null || !url.contains("/upload/")) return url;
        String transform = width > 0 ? "f_auto,q_auto,w_" + width : "f_auto,q_auto";
        return url.replace("/upload/", "/upload/" + transform + "/");
    }

    /**
     * Bản ảnh siêu nhẹ + mờ (vài KB) để hiện ngay lập tức trong lúc chờ tải bản đẹp
     * (kỹ thuật "blur-up" — dùng làm Glide .thumbnail()).
     */
    public static String blurPlaceholder(String url) {
        if (url == null || !url.contains("/upload/")) return url;
        return url.replace("/upload/", "/upload/w_40,e_blur:1000,q_1,f_auto/");
    }
}

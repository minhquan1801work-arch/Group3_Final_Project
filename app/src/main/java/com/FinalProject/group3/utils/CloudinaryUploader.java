package com.FinalProject.group3.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Upload ảnh/video review lên Cloudinary bằng unsigned upload preset
 * (Firebase Storage cần gói Blaze nên không dùng được — app vốn đã host
 * toàn bộ ảnh sản phẩm trên Cloudinary).
 *
 * Preset phải được tạo trong Cloudinary console:
 * Settings → Upload → Upload presets → Add upload preset → Signing Mode: Unsigned.
 */
public final class CloudinaryUploader {

    private static final String CLOUD_NAME = "aa1g9udv";
    private static final String UPLOAD_PRESET = "glassity_reviews";

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface Callback {
        void onSuccess(String secureUrl);
        void onFailure(String error);
    }

    private CloudinaryUploader() {}

    public static void uploadImage(Context context, Uri uri, Callback callback) {
        upload(context, uri, "image", callback);
    }

    public static void uploadVideo(Context context, Uri uri, Callback callback) {
        upload(context, uri, "video", callback);
    }

    private static void upload(Context context, Uri uri, String resourceType, Callback callback) {
        Context appContext = context.getApplicationContext();
        executor.execute(() -> {
            try {
                String boundary = "----glassity" + System.currentTimeMillis();
                URL url = new URL("https://api.cloudinary.com/v1_1/" + CLOUD_NAME
                        + "/" + resourceType + "/upload");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(120000);
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
                    out.writeBytes("--" + boundary + "\r\n");
                    out.writeBytes("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n");
                    out.writeBytes(UPLOAD_PRESET + "\r\n");

                    out.writeBytes("--" + boundary + "\r\n");
                    out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"upload\"\r\n");
                    out.writeBytes("Content-Type: application/octet-stream\r\n\r\n");
                    try (InputStream in = appContext.getContentResolver().openInputStream(uri)) {
                        if (in == null) throw new IllegalStateException("Không đọc được file");
                        byte[] buf = new byte[8192];
                        int len;
                        while ((len = in.read(buf)) != -1) out.write(buf, 0, len);
                    }
                    out.writeBytes("\r\n--" + boundary + "--\r\n");
                }

                int code = conn.getResponseCode();
                InputStream respStream = code >= 200 && code < 300
                        ? conn.getInputStream() : conn.getErrorStream();
                StringBuilder sb = new StringBuilder();
                try (InputStream in = respStream) {
                    byte[] buf = new byte[4096];
                    int len;
                    while ((len = in.read(buf)) != -1) sb.append(new String(buf, 0, len));
                }
                conn.disconnect();

                JSONObject json = new JSONObject(sb.toString());
                if (code >= 200 && code < 300 && json.has("secure_url")) {
                    String secureUrl = json.getString("secure_url");
                    mainHandler.post(() -> callback.onSuccess(secureUrl));
                } else {
                    String msg = json.has("error")
                            ? json.getJSONObject("error").optString("message", "Upload thất bại")
                            : "Upload thất bại (HTTP " + code + ")";
                    mainHandler.post(() -> callback.onFailure(msg));
                }
            } catch (Exception e) {
                String msg = e.getMessage() != null ? e.getMessage() : "Lỗi kết nối";
                mainHandler.post(() -> callback.onFailure(msg));
            }
        });
    }
}

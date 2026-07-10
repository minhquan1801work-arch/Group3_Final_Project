package com.FinalProject.group3.utils;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Helper gọi API provinces.open-api.vn để lấy dữ liệu hành chính Việt Nam (2025).
 * Tất cả callback đều trả về main thread, dùng được trực tiếp để update UI.
 *
 * Cache in-memory theo session: provinces load 1 lần khi app khởi động (preload),
 * districts/wards cache sau lần đầu fetch → mọi lần sau trả về ngay lập tức.
 *
 * Endpoints:
 *   - Tỉnh/TP:    GET /api/p/
 *   - Quận/Huyện: GET /api/p/{provinceCode}?depth=2  → field "districts"
 *   - Phường/Xã:  GET /api/d/{districtCode}?depth=2  → field "wards"
 */
public class AddressApiHelper {

    public static final String API_BASE = "https://provinces.open-api.vn/api/";

    // ── In-memory cache (tồn tại suốt vòng đời app) ──────────────────────────
    private static volatile List<AdminUnit> cachedProvinces = null;
    private static final java.util.Map<Integer, List<AdminUnit>> cachedDistricts =
            new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<Integer, List<AdminUnit>> cachedWards =
            new java.util.concurrent.ConcurrentHashMap<>();

    /** Đơn vị hành chính: name để hiện UI, code để gọi API cấp dưới. */
    public static class AdminUnit {
        public final String name;
        public final int code;

        public AdminUnit(String name, int code) {
            this.name = name;
            this.code = code;
        }

        @Override
        public String toString() { return name; }
    }

    public interface Callback {
        void onResult(List<AdminUnit> units);
    }

    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    /**
     * Preload tỉnh/thành ngay khi app khởi động (gọi từ MainActivity).
     * Không callback — chỉ nạp vào cache nền, không block UI.
     */
    public static void preload() {
        if (cachedProvinces != null) return;
        new Thread(() -> {
            try {
                List<AdminUnit> result = parseArray(get(API_BASE + "p/"), null);
                cachedProvinces = result;
            } catch (Exception ignored) {}
        }).start();
    }

    /** Lấy 34 tỉnh/thành phố. Trả về ngay nếu đã cache, gọi API nếu chưa. */
    public static void fetchProvinces(Callback cb) {
        if (cachedProvinces != null) {
            cb.onResult(cachedProvinces); // instant, no delay
            return;
        }
        new Thread(() -> {
            try {
                List<AdminUnit> result = parseArray(get(API_BASE + "p/"), null);
                cachedProvinces = result;
                MAIN.post(() -> cb.onResult(result));
            } catch (Exception e) {
                MAIN.post(() -> cb.onResult(new ArrayList<>()));
            }
        }).start();
    }

    /** Lấy quận/huyện theo mã tỉnh — có cache theo provinceCode. */
    public static void fetchDistricts(int provinceCode, Callback cb) {
        List<AdminUnit> cached = cachedDistricts.get(provinceCode);
        if (cached != null) {
            cb.onResult(cached); // instant
            return;
        }
        new Thread(() -> {
            try {
                String json = get(API_BASE + "p/" + provinceCode + "?depth=2");
                List<AdminUnit> result = parseArray(
                        new JSONObject(json).getJSONArray("districts").toString(), null);
                cachedDistricts.put(provinceCode, result);
                MAIN.post(() -> cb.onResult(result));
            } catch (Exception e) {
                MAIN.post(() -> cb.onResult(new ArrayList<>()));
            }
        }).start();
    }

    /** Lấy phường/xã theo mã quận/huyện — có cache theo districtCode. */
    public static void fetchWards(int districtCode, Callback cb) {
        List<AdminUnit> cached = cachedWards.get(districtCode);
        if (cached != null) {
            cb.onResult(cached); // instant
            return;
        }
        new Thread(() -> {
            try {
                String json = get(API_BASE + "d/" + districtCode + "?depth=2");
                List<AdminUnit> result = parseArray(
                        new JSONObject(json).getJSONArray("wards").toString(), null);
                cachedWards.put(districtCode, result);
                MAIN.post(() -> cb.onResult(result));
            } catch (Exception e) {
                MAIN.post(() -> cb.onResult(new ArrayList<>()));
            }
        }).start();
    }

    /** Parse JSONArray string → List<AdminUnit>. arrayJson null = dùng root. */
    private static List<AdminUnit> parseArray(String json, String ignored) throws Exception {
        JSONArray arr = new JSONArray(json);
        List<AdminUnit> result = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            result.add(new AdminUnit(o.getString("name"), o.getInt("code")));
        }
        return result;
    }

    private static String get(String urlStr) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);
        conn.setRequestProperty("Accept", "application/json");
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        return sb.toString();
    }
}

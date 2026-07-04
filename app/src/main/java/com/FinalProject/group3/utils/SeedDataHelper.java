package com.FinalProject.group3.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.FinalProject.group3.ui.catalog.ProductListActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Seed categories + products mẫu vào Firestore lần đầu chạy app.
 * Chỉ chạy 1 lần duy nhất (dùng SharedPreferences để kiểm tra).
 *
 * Gọi từ WelcomeActivity.onCreate():
 *   SeedDataHelper.seedIfNeeded(this);
 *
 * Để seed lại từ đầu (reset): xóa SharedPreferences key "seed_done"
 * hoặc xóa data app trong Settings → Apps.
 */
public class SeedDataHelper {

    private static final String TAG = "SeedDataHelper";
    private static final String PREF_NAME = "glassity_prefs";
    private static final String KEY_SEED_DONE = "seed_done_v2";

    public static void seedIfNeeded(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_SEED_DONE, false)) return;

        FirebaseFirestore db = FirebaseHelper.getDb();
        seedCategories(db);
        seedProducts(db);

        prefs.edit().putBoolean(KEY_SEED_DONE, true).apply();
        Log.i(TAG, "Seed data completed.");
    }

    // ── Categories ────────────────────────────────────────────────────────────
    private static void seedCategories(FirebaseFirestore db) {
        String col = FirebaseHelper.COL_CATEGORIES;

        addCategory(db, col, ProductListActivity.CAT_KINH_MAT,        "Kính mắt",         "Kính mắt thông thường các loại");
        addCategory(db, col, ProductListActivity.CAT_PHU_KIEN,        "Phụ kiện",         "Hộp đựng kính, dây, khăn lau");
        addCategory(db, col, ProductListActivity.CAT_SHAPE_TRON,      "Kính mặt tròn",    "Kính phù hợp khuôn mặt tròn");
        addCategory(db, col, ProductListActivity.CAT_SHAPE_TRAI_XOAN, "Kính mặt trái xoan","Kính phù hợp khuôn mặt trái xoan");
        addCategory(db, col, ProductListActivity.CAT_SHAPE_TRAI_TIM,  "Kính mặt trái tim","Kính phù hợp khuôn mặt trái tim");
        addCategory(db, col, ProductListActivity.CAT_SHAPE_KIM_CUONG, "Kính mặt kim cương","Kính phù hợp khuôn mặt kim cương");
        addCategory(db, col, ProductListActivity.CAT_SHAPE_VUONG,     "Kính mặt vuông",   "Kính phù hợp khuôn mặt vuông");
    }

    private static void addCategory(FirebaseFirestore db, String col, String id, String name, String desc) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("description", desc);
        db.collection(col).document(id).set(data)
                .addOnFailureListener(e -> Log.e(TAG, "Category " + id + " failed: " + e.getMessage()));
    }

    // ── Products ──────────────────────────────────────────────────────────────
    private static void seedProducts(FirebaseFirestore db) {
        String col = FirebaseHelper.COL_PRODUCTS;

        // Kính mắt thông thường
        addProduct(db, col, "Kính Mát Glassity Classic", 449000, 50,
                "Gọng nhựa cao cấp, tròng chống UV400, nhẹ bền.",
                Arrays.asList("#1A1614", "#C0C0C0", "#C8A96E"),
                Arrays.asList(
                        "https://res.cloudinary.com/demo/image/upload/v1/samples/glasses1.jpg",
                        "https://res.cloudinary.com/demo/image/upload/v1/samples/glasses1b.jpg"),
                ProductListActivity.CAT_KINH_MAT);

        addProduct(db, col, "Kính Mát Glassity Urban", 590000, 30,
                "Thiết kế hiện đại, phù hợp đi làm và dạo phố.",
                Arrays.asList("#4A4A4A", "#72383D", "#4A90D9"),
                Arrays.asList("https://res.cloudinary.com/demo/image/upload/v1/samples/glasses2.jpg"),
                ProductListActivity.CAT_KINH_MAT);

        addProduct(db, col, "Kính Mát Glassity Vintage", 750000, 20,
                "Phong cách vintage retro, gọng kim loại mảnh.",
                Arrays.asList("#8B6914", "#AC9C8D"),
                Arrays.asList("https://res.cloudinary.com/demo/image/upload/v1/samples/glasses3.jpg"),
                ProductListActivity.CAT_KINH_MAT);

        // Shape — mặt tròn
        addProduct(db, col, "Kính Mặt Tròn Glassity Oval", 520000, 40,
                "Gọng chữ nhật góc vuông, cân bằng khuôn mặt tròn.",
                Arrays.asList("#1A1614", "#C0C0C0"),
                Arrays.asList("https://res.cloudinary.com/demo/image/upload/v1/samples/glasses4.jpg"),
                ProductListActivity.CAT_SHAPE_TRON);

        addProduct(db, col, "Kính Mặt Tròn Glassity Square", 480000, 35,
                "Gọng vuông cổ điển, tạo góc cạnh cho mặt tròn.",
                Arrays.asList("#4A4A4A", "#C8A96E", "#72383D"),
                Arrays.asList("https://res.cloudinary.com/demo/image/upload/v1/samples/glasses5.jpg"),
                ProductListActivity.CAT_SHAPE_TRON);

        // Shape — mặt trái xoan
        addProduct(db, col, "Kính Mặt Trái Xoan Glassity Wave", 610000, 25,
                "Gọng bo tròn nhẹ, phù hợp với mặt trái xoan cân đối.",
                Arrays.asList("#1A1614", "#4A90D9"),
                Arrays.asList("https://res.cloudinary.com/demo/image/upload/v1/samples/glasses6.jpg"),
                ProductListActivity.CAT_SHAPE_TRAI_XOAN);

        addProduct(db, col, "Kính Mặt Trái Xoan Glassity Air", 540000, 45,
                "Gọng mảnh, nhẹ, phong cách thanh lịch.",
                Arrays.asList("#C0C0C0", "#AC9C8D"),
                Arrays.asList("https://res.cloudinary.com/demo/image/upload/v1/samples/glasses7.jpg"),
                ProductListActivity.CAT_SHAPE_TRAI_XOAN);

        // Shape — mặt trái tim
        addProduct(db, col, "Kính Mặt Trái Tim Glassity Soft", 490000, 30,
                "Gọng tròn nhỏ bên dưới, cân bằng trán rộng.",
                Arrays.asList("#72383D", "#C8A96E"),
                Arrays.asList("https://res.cloudinary.com/demo/image/upload/v1/samples/glasses8.jpg"),
                ProductListActivity.CAT_SHAPE_TRAI_TIM);

        addProduct(db, col, "Kính Mặt Trái Tim Glassity Mini", 430000, 50,
                "Thiết kế gọng nhỏ, không che phủ phần cằm nhọn.",
                Arrays.asList("#1A1614", "#FFFFFF"),
                Arrays.asList("https://res.cloudinary.com/demo/image/upload/v1/samples/glasses9.jpg"),
                ProductListActivity.CAT_SHAPE_TRAI_TIM);

        // Shape — mặt kim cương
        addProduct(db, col, "Kính Mặt Kim Cương Glassity Edge", 680000, 20,
                "Gọng oval mềm mại, làm dịu đường nét sắc bén.",
                Arrays.asList("#4A4A4A", "#8B6914"),
                Arrays.asList("https://res.cloudinary.com/demo/image/upload/v1/samples/glasses10.jpg"),
                ProductListActivity.CAT_SHAPE_KIM_CUONG);

        addProduct(db, col, "Kính Mặt Kim Cương Glassity Curve", 590000, 28,
                "Viền cong dưới, phù hợp gò má cao.",
                Arrays.asList("#C0C0C0", "#4A90D9"),
                Arrays.asList("https://res.cloudinary.com/demo/image/upload/v1/samples/glasses11.jpg"),
                ProductListActivity.CAT_SHAPE_KIM_CUONG);

        // Shape — mặt vuông
        addProduct(db, col, "Kính Mặt Vuông Glassity Round", 510000, 35,
                "Gọng bo tròn hoàn toàn, mềm hóa đường hàm vuông.",
                Arrays.asList("#1A1614", "#72383D"),
                Arrays.asList("https://res.cloudinary.com/demo/image/upload/v1/samples/glasses12.jpg"),
                ProductListActivity.CAT_SHAPE_VUONG);

        addProduct(db, col, "Kính Mặt Vuông Glassity Pilot", 720000, 15,
                "Gọng aviator kinh điển, kéo dài khuôn mặt vuông.",
                Arrays.asList("#C8A96E", "#8B6914"),
                Arrays.asList("https://res.cloudinary.com/demo/image/upload/v1/samples/glasses13.jpg"),
                ProductListActivity.CAT_SHAPE_VUONG);

        // Phụ kiện
        addProduct(db, col, "Hộp Đựng Kính Glassity Box", 120000, 100,
                "Hộp cứng bọc da PU, bảo vệ kính tối ưu.",
                Arrays.asList("#1A1614", "#C8A96E"),
                Arrays.asList("https://res.cloudinary.com/demo/image/upload/v1/samples/box1.jpg"),
                ProductListActivity.CAT_PHU_KIEN);

        addProduct(db, col, "Khăn Lau Kính Microfiber", 35000, 200,
                "Vải siêu mịn, không trầy tròng kính.",
                Arrays.asList("#FFFFFF", "#4A4A4A"),
                Arrays.asList("https://res.cloudinary.com/demo/image/upload/v1/samples/cloth1.jpg"),
                ProductListActivity.CAT_PHU_KIEN);
    }

    private static void addProduct(FirebaseFirestore db, String col,
                                   String name, double price, int stock,
                                   String description, List<String> colors,
                                   List<String> images, String categoryId) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("price", price);
        data.put("stock", stock);
        data.put("description", description);
        data.put("colors", colors);
        data.put("images", images);
        data.put("categoryId", categoryId);

        db.collection(col).add(data)
                .addOnFailureListener(e -> Log.e(TAG, "Product " + name + " failed: " + e.getMessage()));
    }
}

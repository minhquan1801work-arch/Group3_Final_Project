package com.FinalProject.group3.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.FinalProject.group3.ui.catalog.ProductListActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Seed categories + products mẫu vào Firestore lần đầu chạy app.
 * Gọi từ WelcomeActivity.onCreate(): SeedDataHelper.seedIfNeeded(this)
 * Để seed lại: xóa data app hoặc đổi KEY_SEED_DONE sang version mới.
 *
 * Data model:
 *   categories: { categoryId (doc ID), name, description }
 *               Chỉ chứa LOẠI SẢN PHẨM (không có dạng mặt)
 *   products:   { name, price, stock, description, colors, images,
 *                 categoryId → loại SP,
 *                 faceShapes → array dạng mặt phù hợp (rỗng = không phân loại) }
 */
public class SeedDataHelper {

    private static final String TAG           = "SeedDataHelper";
    private static final String PREF_NAME     = "glassity_prefs";
    private static final String KEY_SEED_DONE = "seed_done_v3";

    public static void seedIfNeeded(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_SEED_DONE, false)) return;

        FirebaseFirestore db = FirebaseHelper.getDb();
        seedCategories(db);
        seedProducts(db);

        prefs.edit().putBoolean(KEY_SEED_DONE, true).apply();
        Log.i(TAG, "Seed data v3 completed.");
    }

    // ── Categories (chỉ loại sản phẩm, không có dạng mặt) ───────────────────
    private static void seedCategories(FirebaseFirestore db) {
        String col = FirebaseHelper.COL_CATEGORIES;
        addCategory(db, col, ProductListActivity.CAT_KINH_MAT, "Kính mắt", "Kính râm, kính thời trang các loại");
        addCategory(db, col, ProductListActivity.CAT_KINH_CAN, "Kính cận", "Kính cận, kính đọc sách");
        addCategory(db, col, ProductListActivity.CAT_PHU_KIEN, "Phụ kiện", "Hộp đựng kính, dây, khăn lau");
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

        // ─ Kính mắt
        addProduct(db, col, "Glassity Classic", 449000, 50,
                "Gọng nhựa cao cấp, tròng chống UV400, nhẹ bền.",
                Arrays.asList("#1A1614", "#C0C0C0", "#C8A96E"),
                Arrays.asList("https://res.cloudinary.com/demo/image/upload/v1/samples/glasses1.jpg"),
                ProductListActivity.CAT_KINH_MAT,
                Arrays.asList(ProductListActivity.SHAPE_TRON, ProductListActivity.SHAPE_TRAI_XOAN));

        addProduct(db, col, "Glassity Urban", 590000, 30,
                "Thiết kế hiện đại, phù hợp đi làm và dạo phố.",
                Arrays.asList("#4A4A4A", "#72383D", "#4A90D9"),
                Arrays.asList("https://res.cloudinary.com/demo/image/upload/v1/samples/glasses2.jpg"),
                ProductListActivity.CAT_KINH_MAT,
                Arrays.asList(ProductListActivity.SHAPE_VUONG, ProductListActivity.SHAPE_KIM_CUONG));

        addProduct(db, col, "Glassity Vintage", 750000, 20,
                "Phong cách vintage retro, gọng kim loại mảnh.",
                Arrays.asList("#8B6914", "#AC9C8D"),
                Arrays.asList("https://res.cloudinary.com/demo/image/upload/v1/samples/glasses3.jpg"),
                ProductListActivity.CAT_KINH_MAT,
                Arrays.asList(ProductListActivity.SHAPE_TRAI_TIM, ProductListActivity.SHAPE_TRON));

        addProduct(db, col, "Glassity Oval Frame", 520000, 40,
                "Gọng chữ nhật góc vuông, cân bằng khuôn mặt tròn.",
                Arrays.asList("#1A1614", "#C0C0C0"),
                Arrays.asList("https://res.cloudinary.com/demo/image/upload/v1/samples/glasses4.jpg"),
                ProductListActivity.CAT_KINH_MAT,
                Arrays.asList(ProductListActivity.SHAPE_TRON));

        addProduct(db, col, "Glassity Wave", 610000, 25,
                "Gọng bo tròn nhẹ, phù hợp với mặt trái xoan.",
                Arrays.asList("#1A1614", "#4A90D9"),
                Arrays.asList("https://res.cloudinary.com/demo/image/upload/v1/samples/glasses5.jpg"),
                ProductListActivity.CAT_KINH_MAT,
                Arrays.asList(ProductListActivity.SHAPE_TRAI_XOAN, ProductListActivity.SHAPE_TRAI_TIM));

        addProduct(db, col, "Glassity Edge", 680000, 20,
                "Gọng oval mềm mại, làm dịu đường nét sắc bén.",
                Arrays.asList("#4A4A4A", "#8B6914"),
                Arrays.asList("https://res.cloudinary.com/demo/image/upload/v1/samples/glasses6.jpg"),
                ProductListActivity.CAT_KINH_MAT,
                Arrays.asList(ProductListActivity.SHAPE_KIM_CUONG, ProductListActivity.SHAPE_TRAI_XOAN));

        addProduct(db, col, "Glassity Pilot", 720000, 15,
                "Gọng aviator kinh điển, kéo dài khuôn mặt vuông.",
                Arrays.asList("#C8A96E", "#8B6914"),
                Arrays.asList("https://res.cloudinary.com/demo/image/upload/v1/samples/glasses7.jpg"),
                ProductListActivity.CAT_KINH_MAT,
                Arrays.asList(ProductListActivity.SHAPE_VUONG));

        addProduct(db, col, "Glassity Soft Mini", 490000, 30,
                "Gọng tròn nhỏ bên dưới, cân bằng trán rộng.",
                Arrays.asList("#72383D", "#C8A96E"),
                Arrays.asList("https://res.cloudinary.com/demo/image/upload/v1/samples/glasses8.jpg"),
                ProductListActivity.CAT_KINH_MAT,
                Arrays.asList(ProductListActivity.SHAPE_TRAI_TIM));

        // ─ Kính cận
        addProduct(db, col, "Glassity Vision Round", 380000, 60,
                "Gọng tròn nhẹ, phù hợp kính cận số thấp đến cao.",
                Arrays.asList("#1A1614", "#C0C0C0"),
                Arrays.asList("https://res.cloudinary.com/demo/image/upload/v1/samples/glasses9.jpg"),
                ProductListActivity.CAT_KINH_CAN,
                Arrays.asList(ProductListActivity.SHAPE_TRON, ProductListActivity.SHAPE_TRAI_XOAN));

        addProduct(db, col, "Glassity Vision Square", 420000, 45,
                "Gọng chữ nhật thanh mảnh, trẻ trung.",
                Arrays.asList("#4A4A4A", "#72383D"),
                Arrays.asList("https://res.cloudinary.com/demo/image/upload/v1/samples/glasses10.jpg"),
                ProductListActivity.CAT_KINH_CAN,
                Arrays.asList(ProductListActivity.SHAPE_VUONG, ProductListActivity.SHAPE_KIM_CUONG));

        addProduct(db, col, "Glassity Vision Air", 350000, 80,
                "Gọng siêu nhẹ titanium, phù hợp mọi khuôn mặt.",
                Arrays.asList("#C0C0C0", "#AC9C8D"),
                Arrays.asList("https://res.cloudinary.com/demo/image/upload/v1/samples/glasses11.jpg"),
                ProductListActivity.CAT_KINH_CAN,
                Arrays.asList(
                        ProductListActivity.SHAPE_TRON, ProductListActivity.SHAPE_TRAI_XOAN,
                        ProductListActivity.SHAPE_TRAI_TIM, ProductListActivity.SHAPE_KIM_CUONG,
                        ProductListActivity.SHAPE_VUONG));

        // ─ Phụ kiện (faceShapes rỗng)
        addProduct(db, col, "Hộp Đựng Kính Glassity Box", 120000, 100,
                "Hộp cứng bọc da PU, bảo vệ kính tối ưu.",
                Arrays.asList("#1A1614", "#C8A96E"),
                Arrays.asList("https://res.cloudinary.com/demo/image/upload/v1/samples/box1.jpg"),
                ProductListActivity.CAT_PHU_KIEN,
                Collections.emptyList());

        addProduct(db, col, "Khăn Lau Kính Microfiber", 35000, 200,
                "Vải siêu mịn, không trầy tròng kính.",
                Arrays.asList("#FFFFFF", "#4A4A4A"),
                Arrays.asList("https://res.cloudinary.com/demo/image/upload/v1/samples/cloth1.jpg"),
                ProductListActivity.CAT_PHU_KIEN,
                Collections.emptyList());

        addProduct(db, col, "Dây Đeo Kính Silicon", 55000, 150,
                "Dây silicon chống trượt, phù hợp thể thao.",
                Arrays.asList("#1A1614", "#4A90D9", "#C0C0C0"),
                Arrays.asList("https://res.cloudinary.com/demo/image/upload/v1/samples/strap1.jpg"),
                ProductListActivity.CAT_PHU_KIEN,
                Collections.emptyList());
    }

    private static void addProduct(FirebaseFirestore db, String col,
                                   String name, double price, int stock,
                                   String description, List<String> colors,
                                   List<String> images, String categoryId,
                                   List<String> faceShapes) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("price", price);
        data.put("stock", stock);
        data.put("description", description);
        data.put("colors", colors);
        data.put("images", images);
        data.put("categoryId", categoryId);
        data.put("faceShapes", faceShapes);
        db.collection(col).add(data)
                .addOnFailureListener(e -> Log.e(TAG, "Product " + name + " failed: " + e.getMessage()));
    }
}

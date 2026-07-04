package com.FinalProject.group3.repository;

import com.FinalProject.group3.model.Product;
import com.FinalProject.group3.utils.FirebaseHelper;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.List;

/**
 * CRUD cho Product và Category.
 */
public class ProductRepository {

    public interface ProductListCallback {
        void onSuccess(List<Product> products);
        void onFailure(String error);
    }

    public interface ProductCallback {
        void onSuccess(Product product);
        void onFailure(String error);
    }

    private final FirebaseFirestore db = FirebaseHelper.getDb();

    // ── Lấy tất cả sản phẩm ───────────────────────────────────────────────────
    public void getAllProducts(ProductListCallback callback) {
        db.collection(FirebaseHelper.COL_PRODUCTS)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Product> list = snapshot.toObjects(Product.class);
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Lấy sản phẩm theo danh mục ────────────────────────────────────────────
    public void getProductsByCategory(String categoryId, ProductListCallback callback) {
        db.collection(FirebaseHelper.COL_PRODUCTS)
                .whereEqualTo("categoryId", categoryId)
                .get()
                .addOnSuccessListener(snapshot -> callback.onSuccess(snapshot.toObjects(Product.class)))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Tìm kiếm theo tên ─────────────────────────────────────────────────────
    public void searchProducts(String keyword, ProductListCallback callback) {
        // Firestore không hỗ trợ full-text search → dùng prefix match
        String end = keyword + "";
        db.collection(FirebaseHelper.COL_PRODUCTS)
                .orderBy("name")
                .startAt(keyword)
                .endAt(end)
                .get()
                .addOnSuccessListener(snapshot -> callback.onSuccess(snapshot.toObjects(Product.class)))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Lấy chi tiết 1 sản phẩm ───────────────────────────────────────────────
    public void getProductById(String productId, ProductCallback callback) {
        db.collection(FirebaseHelper.COL_PRODUCTS)
                .document(productId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) callback.onSuccess(doc.toObject(Product.class));
                    else callback.onFailure("Không tìm thấy sản phẩm");
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Lấy sản phẩm theo dạng mặt cụ thể (array-contains) ──────────────────
    public void getProductsByFaceShape(String shape, ProductListCallback callback) {
        db.collection(FirebaseHelper.COL_PRODUCTS)
                .whereArrayContains("faceShapes", shape)
                .get()
                .addOnSuccessListener(snapshot -> callback.onSuccess(snapshot.toObjects(Product.class)))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Lấy tất cả sản phẩm có ít nhất 1 dạng mặt (chip "Shape kính") ────────
    public void getProductsByFaceShapeAll(ProductListCallback callback) {
        List<String> allShapes = java.util.Arrays.asList(
                "tron", "trai_xoan", "trai_tim", "kim_cuong", "vuong");
        db.collection(FirebaseHelper.COL_PRODUCTS)
                .whereArrayContainsAny("faceShapes", allShapes)
                .get()
                .addOnSuccessListener(snapshot -> callback.onSuccess(snapshot.toObjects(Product.class)))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Lấy sản phẩm theo collection (BST) ───────────────────────────────────
    public void getProductsByCollection(String collection, ProductListCallback callback) {
        db.collection(FirebaseHelper.COL_PRODUCTS)
                .whereEqualTo("collection", collection)
                .get()
                .addOnSuccessListener(snapshot -> callback.onSuccess(snapshot.toObjects(Product.class)))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Lấy sản phẩm bán chạy (ví dụ: giới hạn 10) ───────────────────────────
    public void getBestSellerProducts(int limit, ProductListCallback callback) {
        db.collection(FirebaseHelper.COL_PRODUCTS)
                .limit(limit)
                .get()
                .addOnSuccessListener(snapshot -> callback.onSuccess(snapshot.toObjects(Product.class)))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}

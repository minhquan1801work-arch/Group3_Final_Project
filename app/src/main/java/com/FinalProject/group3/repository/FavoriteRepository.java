package com.FinalProject.group3.repository;

import com.FinalProject.group3.model.Favorite;
import com.FinalProject.group3.utils.FirebaseHelper;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Quản lý Sản phẩm yêu thích (LA.Favor).
 *
 * Cấu trúc Firestore:
 *   favorites/{favoriteId} → { customerId, productId, createdAt }
 */
public class FavoriteRepository {

    public interface FavoriteListCallback {
        void onSuccess(List<Favorite> favorites);
        void onFailure(String error);
    }

    /** nowFavorite = true nếu sau khi toggle sản phẩm ĐANG được yêu thích */
    public interface ToggleCallback {
        void onSuccess(boolean nowFavorite);
        void onFailure(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onFailure(String error);
    }

    private final FirebaseFirestore db = FirebaseHelper.getDb();

    // ── Danh sách yêu thích của user hiện tại ─────────────────────────────────
    public void getFavorites(FavoriteListCallback callback) {
        String uid = FirebaseHelper.getCurrentUserId();
        if (uid == null) { callback.onFailure("Chưa đăng nhập"); return; }

        db.collection(FirebaseHelper.COL_FAVORITES)
                .whereEqualTo("customerId", uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Favorite> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Favorite f = new Favorite(
                                doc.getString("customerId"), doc.getString("productId"));
                        f.setFavoriteId(doc.getId());
                        list.add(f);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Toggle tim: chưa có → thêm, có rồi → xóa ─────────────────────────────
    public void toggleFavorite(String productId, ToggleCallback callback) {
        String uid = FirebaseHelper.getCurrentUserId();
        if (uid == null) { callback.onFailure("Chưa đăng nhập"); return; }

        db.collection(FirebaseHelper.COL_FAVORITES)
                .whereEqualTo("customerId", uid)
                .whereEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        // Ghi bằng Map — không ghi field @DocumentId (quy tắc dự án)
                        Map<String, Object> data = new HashMap<>();
                        data.put("customerId", uid);
                        data.put("productId", productId);
                        data.put("createdAt", new java.util.Date());
                        db.collection(FirebaseHelper.COL_FAVORITES).add(data)
                                .addOnSuccessListener(r -> callback.onSuccess(true))
                                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                    } else {
                        snapshot.getDocuments().get(0).getReference().delete()
                                .addOnSuccessListener(v -> callback.onSuccess(false))
                                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Xóa 1 mục yêu thích theo id (dùng trong màn LA.Favor) ─────────────────
    public void removeFavorite(String favoriteId, SimpleCallback callback) {
        db.collection(FirebaseHelper.COL_FAVORITES).document(favoriteId).delete()
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}

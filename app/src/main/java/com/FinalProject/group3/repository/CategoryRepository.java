package com.FinalProject.group3.repository;

import com.FinalProject.group3.model.Category;
import com.FinalProject.group3.utils.FirebaseHelper;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * CRUD cho Category. Collection "categories" chỉ đọc từ client (rules write:false),
 * dữ liệu được seed/quản lý qua Firebase Console.
 */
public class CategoryRepository {

    public interface CategoryListCallback {
        void onSuccess(List<Category> categories);
        void onFailure(String error);
    }

    private final FirebaseFirestore db = FirebaseHelper.getDb();

    public void getAllCategories(CategoryListCallback callback) {
        db.collection(FirebaseHelper.COL_CATEGORIES)
                .get()
                .addOnSuccessListener(snapshot -> callback.onSuccess(snapshot.toObjects(Category.class)))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}

package com.FinalProject.group3.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Singleton helper — lấy instance Firebase dùng chung toàn app.
 * Dùng: FirebaseHelper.getAuth(), FirebaseHelper.getDb(), FirebaseHelper.getStorage()
 */
public class FirebaseHelper {

    private static FirebaseAuth auth;
    private static FirebaseFirestore db;
    private static FirebaseStorage storage;

    // ── Collections Firestore ──────────────────────────────────────────────────
    public static final String COL_CUSTOMERS     = "customers";
    public static final String COL_PRODUCTS      = "products";
    public static final String COL_CATEGORIES    = "categories";
    public static final String COL_CARTS         = "carts";
    public static final String COL_CART_DETAILS  = "cartDetails";
    public static final String COL_ORDERS        = "orders";
    public static final String COL_ORDER_DETAILS = "orderDetails";
    public static final String COL_FAVORITES     = "favorites";
    public static final String COL_NOTIFICATIONS = "notifications";
    public static final String COL_PAYMENTS      = "payments";
    public static final String COL_REVIEWS       = "reviews";

    private FirebaseHelper() {}

    public static FirebaseAuth getAuth() {
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
            // Email Firebase gửi (quên mật khẩu, xác minh email...) dùng template
            // tiếng Việt có sẵn thay vì mặc định tiếng Anh
            auth.setLanguageCode("vi");
        }
        return auth;
    }

    public static FirebaseFirestore getDb() {
        if (db == null) db = FirebaseFirestore.getInstance();
        return db;
    }

    public static FirebaseStorage getStorage() {
        if (storage == null) storage = FirebaseStorage.getInstance();
        return storage;
    }

    public static StorageReference getStorageRef() {
        return getStorage().getReference();
    }

    // ── Shortcuts thường dùng ──────────────────────────────────────────────────

    /** UID của user đang đăng nhập, null nếu chưa đăng nhập */
    public static String getCurrentUserId() {
        FirebaseUser user = getAuth().getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public static boolean isLoggedIn() {
        return getAuth().getCurrentUser() != null;
    }

    public static void signOut() {
        getAuth().signOut();
    }
}

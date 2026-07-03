package com.FinalProject.group3.repository;

import com.FinalProject.group3.model.Customer;
import com.FinalProject.group3.utils.FirebaseHelper;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Xử lý đăng ký / đăng nhập / đăng xuất.
 *
 * Dùng callback interface thay vì return value vì Firebase là bất đồng bộ (async).
 *
 * Ví dụ dùng trong Activity:
 *   AuthRepository repo = new AuthRepository();
 *   repo.login(email, password, new AuthRepository.AuthCallback() {
 *       @Override public void onSuccess() { // chuyển màn hình }
 *       @Override public void onFailure(String error) { // hiển thị lỗi }
 *   });
 */
public class AuthRepository {

    public interface AuthCallback {
        void onSuccess();
        void onFailure(String error);
    }

    private final FirebaseFirestore db = FirebaseHelper.getDb();

    // ── Đăng nhập Email / Password ─────────────────────────────────────────────
    public void login(String email, String password, AuthCallback callback) {
        FirebaseHelper.getAuth()
                .signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Đăng ký ────────────────────────────────────────────────────────────────
    public void register(String name, String email, String password, AuthCallback callback) {
        FirebaseHelper.getAuth()
                .createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    // Lưu thêm thông tin vào Firestore
                    String uid = result.getUser().getUid();
                    Customer customer = new Customer(name, email, "", "", "email");
                    db.collection(FirebaseHelper.COL_CUSTOMERS)
                            .document(uid)
                            .set(customer)
                            .addOnSuccessListener(v -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Google Sign-In ─────────────────────────────────────────────────────────
    public void loginWithGoogle(GoogleSignInAccount account, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        FirebaseHelper.getAuth()
                .signInWithCredential(credential)
                .addOnSuccessListener(result -> {
                    // Nếu là user mới → tạo document Firestore
                    if (result.getAdditionalUserInfo() != null
                            && result.getAdditionalUserInfo().isNewUser()) {
                        String uid = result.getUser().getUid();
                        Customer customer = new Customer(
                                account.getDisplayName(),
                                account.getEmail(),
                                "", "", "google");
                        db.collection(FirebaseHelper.COL_CUSTOMERS)
                                .document(uid)
                                .set(customer);
                    }
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Quên mật khẩu ─────────────────────────────────────────────────────────
    public void forgotPassword(String email, AuthCallback callback) {
        FirebaseHelper.getAuth()
                .sendPasswordResetEmail(email)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}

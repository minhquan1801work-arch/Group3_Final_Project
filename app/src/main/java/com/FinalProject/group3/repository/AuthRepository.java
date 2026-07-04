package com.FinalProject.group3.repository;

import com.FinalProject.group3.model.Customer;
import com.FinalProject.group3.utils.FirebaseHelper;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

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

    /** Đổi Exception của Firebase thành thông báo tiếng Việt dễ hiểu. */
    private static String toVietnameseError(Exception e) {
        if (e instanceof FirebaseAuthInvalidUserException) {
            return "Tài khoản không tồn tại hoặc đã bị vô hiệu hóa";
        }
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            return "Email hoặc mật khẩu không đúng";
        }
        if (e instanceof FirebaseAuthUserCollisionException) {
            return "Email này đã được đăng ký, hãy đăng nhập";
        }
        if (e instanceof FirebaseAuthWeakPasswordException) {
            return "Mật khẩu quá yếu (tối thiểu 6 ký tự)";
        }
        if (e instanceof FirebaseNetworkException) {
            return "Không có kết nối mạng, kiểm tra Internet của thiết bị";
        }
        return "Có lỗi xảy ra: " + e.getMessage();
    }

    /** Giải thích mã lỗi Google Sign-In (ApiException.getStatusCode()). */
    public static String googleErrorMessage(int statusCode) {
        switch (statusCode) {
            case 7:  // NETWORK_ERROR
                return "Lỗi mạng — kiểm tra Internet của thiết bị/emulator";
            case 10: // DEVELOPER_ERROR
                return "Sai cấu hình SHA-1/google-services.json — báo Minh Quân add SHA-1 máy này vào Firebase";
            case 12500: // SIGN_IN_FAILED — thường do Play Services lỗi thời hoặc chưa có Google account
                return "Google Play Services lỗi/cũ — vào Settings emulator thêm Google account và update Play Services";
            default:
                return "Đăng nhập Google thất bại (mã " + statusCode + ")";
        }
    }

    // ── Đăng nhập Email / Password ─────────────────────────────────────────────
    public void login(String email, String password, AuthCallback callback) {
        FirebaseHelper.getAuth()
                .signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(toVietnameseError(e)));
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
                            .addOnFailureListener(e -> callback.onFailure(toVietnameseError(e)));
                })
                .addOnFailureListener(e -> callback.onFailure(toVietnameseError(e)));
    }

    // ── Google Sign-In ─────────────────────────────────────────────────────────
    public void loginWithGoogle(GoogleSignInAccount account, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        FirebaseHelper.getAuth()
                .signInWithCredential(credential)
                .addOnSuccessListener(result -> {
                    // Luôn upsert document customer (merge) — đảm bảo tài khoản Google
                    // nào đăng nhập cũng có dữ liệu trong Firestore, kể cả khi lần
                    // tạo đầu tiên trước đó bị lỗi mạng
                    String uid = result.getUser().getUid();
                    Customer customer = new Customer(
                            account.getDisplayName(),
                            account.getEmail(),
                            "", "", "google");
                    db.collection(FirebaseHelper.COL_CUSTOMERS)
                            .document(uid)
                            .set(customer, SetOptions.merge())
                            .addOnSuccessListener(v -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onFailure(toVietnameseError(e)));
                })
                .addOnFailureListener(e -> callback.onFailure(toVietnameseError(e)));
    }

    // ── Quên mật khẩu ─────────────────────────────────────────────────────────
    public void forgotPassword(String email, AuthCallback callback) {
        FirebaseHelper.getAuth()
                .sendPasswordResetEmail(email)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(toVietnameseError(e)));
    }
}

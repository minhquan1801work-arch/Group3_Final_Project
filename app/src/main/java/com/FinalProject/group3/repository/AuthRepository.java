package com.FinalProject.group3.repository;

import android.content.Context;

import com.FinalProject.group3.R;
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
        /** Lỗi liên quan đến field email (tài khoản không tồn tại, bị khóa…). */
        default void onEmailError(String error) { onFailure(error); }
        /** Lỗi liên quan đến field mật khẩu (sai mật khẩu). */
        default void onPasswordError(String error) { onFailure(error); }
    }

    private final FirebaseFirestore db = FirebaseHelper.getDb();

    /** Đổi Exception của Firebase thành thông báo dễ hiểu (đa ngôn ngữ theo locale hiện tại). */
    private static String toVietnameseError(Context context, Exception e) {
        if (e instanceof FirebaseAuthInvalidUserException) {
            return context.getString(R.string.err_account_disabled_or_missing);
        }
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            return context.getString(R.string.err_wrong_password);
        }
        if (e instanceof FirebaseAuthUserCollisionException) {
            return context.getString(R.string.err_email_already_registered);
        }
        if (e instanceof FirebaseAuthWeakPasswordException) {
            return context.getString(R.string.err_password_weak);
        }
        if (e instanceof FirebaseNetworkException) {
            return context.getString(R.string.err_no_network);
        }
        return context.getString(R.string.err_generic, e.getMessage());
    }

    /**
     * Dispatch lỗi đăng nhập đến đúng field:
     *  - Tài khoản không tồn tại / bị khóa → onEmailError
     *  - Sai mật khẩu → onPasswordError
     *  - Còn lại → onFailure
     */
    private static void dispatchLoginError(Context context, Exception e, AuthCallback callback) {
        if (e instanceof FirebaseAuthInvalidUserException) {
            String code = ((FirebaseAuthInvalidUserException) e).getErrorCode();
            if ("ERROR_USER_DISABLED".equals(code)) {
                callback.onEmailError(context.getString(R.string.err_account_disabled));
            } else {
                callback.onEmailError(context.getString(R.string.err_account_not_exist));
            }
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            callback.onPasswordError(context.getString(R.string.err_wrong_password));
        } else {
            callback.onFailure(toVietnameseError(context, e));
        }
    }

    /** Giải thích mã lỗi Google Sign-In (ApiException.getStatusCode()). */
    public static String googleErrorMessage(Context context, int statusCode) {
        switch (statusCode) {
            case 7:  // NETWORK_ERROR
                return context.getString(R.string.err_google_network);
            case 10: // DEVELOPER_ERROR
                return context.getString(R.string.err_google_sha1_config);
            case 12500: // SIGN_IN_FAILED — thường do Play Services lỗi thời hoặc chưa có Google account
                return context.getString(R.string.err_google_play_services);
            default:
                return context.getString(R.string.err_google_login_status, statusCode);
        }
    }

    // ── Đăng nhập Email / Password ─────────────────────────────────────────────
    public void login(Context context, String email, String password, AuthCallback callback) {
        FirebaseHelper.getAuth()
                .signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(e -> dispatchLoginError(context, e, callback));
    }

    // ── Đăng ký ────────────────────────────────────────────────────────────────
    public void register(Context context, String name, String email, String password, AuthCallback callback) {
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
                            .addOnFailureListener(e -> callback.onFailure(toVietnameseError(context, e)));
                })
                .addOnFailureListener(e -> callback.onFailure(toVietnameseError(context, e)));
    }

    // ── Google Sign-In ─────────────────────────────────────────────────────────
    public void loginWithGoogle(Context context, GoogleSignInAccount account, AuthCallback callback) {
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
                            .addOnFailureListener(e -> callback.onFailure(toVietnameseError(context, e)));
                })
                .addOnFailureListener(e -> callback.onFailure(toVietnameseError(context, e)));
    }

    // ── Quên mật khẩu ─────────────────────────────────────────────────────────
    public void forgotPassword(Context context, String email, AuthCallback callback) {
        FirebaseHelper.getAuth()
                .sendPasswordResetEmail(email)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(toVietnameseError(context, e)));
    }
}

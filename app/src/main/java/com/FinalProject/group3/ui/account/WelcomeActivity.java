package com.FinalProject.group3.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.FinalProject.group3.MainActivity;
import com.FinalProject.group3.R;
import com.FinalProject.group3.databinding.ActivityWelcomeBinding;
import com.FinalProject.group3.repository.AuthRepository;
import com.FinalProject.group3.utils.SeedDataHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;

/**
 * LA.Login/Signup — màn hình chào mừng, cho phép chọn Đăng nhập / Đăng ký /
 * Đăng nhập nhanh bằng Google / Tiếp tục với vai trò Khách.
 *
 * Đây là Activity khởi động (LAUNCHER) thay cho MainActivity trống mặc định
 * — nhớ cập nhật AndroidManifest.xml (xem TEAM_GUIDELINE.md).
 */
public class WelcomeActivity extends AppCompatActivity {

    private ActivityWelcomeBinding binding;
    private final AuthRepository authRepository = new AuthRepository();
    private GoogleSignInClient googleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent data = result.getData();
                if (data == null) return;
                try {
                    GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data)
                            .getResult(ApiException.class);
                    handleGoogleAccount(account);
                } catch (ApiException e) {
                    Toast.makeText(this, getString(R.string.err_google_signin_failed, e.getStatusCode()), Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Còn phiên (Firebase Auth tự lưu đăng nhập, hoặc đã chọn Khách trước đó)
        // → vào thẳng Main, không bắt đăng nhập lại mỗi lần mở app
        if (com.FinalProject.group3.utils.SessionManager.hasSession(this)) {
            goToMain();
            return;
        }

        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        com.FinalProject.group3.utils.InsetsUtil.applySystemBarsPadding(binding.getRoot());

        // SeedDataHelper.seedIfNeeded(this); // tắt — data thật đã seed qua seed_firestore.js

        // default_web_client_id được google-services plugin tự sinh ra từ google-services.json.
        // Chỉ hoạt động SAU KHI đã thêm SHA-1 + tải lại google-services.json (xem FIREBASE_SETUP_CHECKLIST.md).
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.btnLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        binding.btnSignup.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class)));

        binding.btnGoogle.setOnClickListener(v ->
                googleSignInLauncher.launch(googleSignInClient.getSignInIntent()));

        // Facebook: UI giữ chỗ theo Figma — cần Facebook App ID mới kích hoạt được
        binding.btnFacebook.setOnClickListener(v ->
                android.widget.Toast.makeText(this,
                        R.string.msg_facebook_login_soon,
                        android.widget.Toast.LENGTH_SHORT).show());

        binding.tvContinueGuest.setOnClickListener(v -> {
            // Nhớ lựa chọn Khách — lần sau mở app vào thẳng Main
            com.FinalProject.group3.utils.SessionManager.setGuestMode(this, true);
            goToMain();
        });
    }

    private void handleGoogleAccount(GoogleSignInAccount account) {
        authRepository.loginWithGoogle(this, account, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                goToMain();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(WelcomeActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

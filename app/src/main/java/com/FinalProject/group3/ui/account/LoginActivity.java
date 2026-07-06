package com.FinalProject.group3.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.FinalProject.group3.MainActivity;
import com.FinalProject.group3.R;
import com.FinalProject.group3.databinding.ActivityLoginBinding;
import com.FinalProject.group3.repository.AuthRepository;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;

/** LA.Login — đăng nhập bằng Email/Password hoặc Google. */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private final AuthRepository authRepository = new AuthRepository();
    private GoogleSignInClient googleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent data = result.getData();
                if (data == null) return;
                try {
                    GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data)
                            .getResult(ApiException.class);
                    setLoading(true);
                    authRepository.loginWithGoogle(account, new AuthRepository.AuthCallback() {
                        @Override
                        public void onSuccess() {
                            setLoading(false);
                            goToMain();
                        }

                        @Override
                        public void onFailure(String error) {
                            setLoading(false);
                            Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (ApiException e) {
                    if (e.getStatusCode() == 12501) return; // người dùng bấm hủy — không báo lỗi
                    Toast.makeText(this, AuthRepository.googleErrorMessage(e.getStatusCode()), Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        com.FinalProject.group3.utils.InsetsUtil.applySystemBarsPadding(binding.getRoot());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.btnBack.setOnClickListener(v -> finish());

        binding.tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));

        binding.btnGoogle.setOnClickListener(v ->
                googleSignInLauncher.launch(googleSignInClient.getSignInIntent()));

        binding.btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String email = binding.tilEmail.getEditText().getText().toString().trim();
        String password = binding.tilPassword.getEditText().getText().toString();

        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);

        boolean valid = true;
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError(getString(R.string.err_required));
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.err_email_invalid));
            valid = false;
        }
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError(getString(R.string.err_required));
            valid = false;
        }
        if (!valid) return;

        setLoading(true);
        authRepository.login(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                goToMain();
            }

            @Override
            public void onFailure(String error) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEmailError(String error) {
                setLoading(false);
                binding.tilEmail.setError(error);
                binding.tilEmail.requestFocus();
            }

            @Override
            public void onPasswordError(String error) {
                setLoading(false);
                binding.tilPassword.setError(error);
                binding.tilPassword.requestFocus();
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? android.view.View.VISIBLE : android.view.View.GONE);
        binding.btnLogin.setEnabled(!loading);
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

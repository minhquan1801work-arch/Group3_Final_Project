package com.FinalProject.group3.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.FinalProject.group3.MainActivity;
import com.FinalProject.group3.R;
import com.FinalProject.group3.databinding.ActivitySignupBinding;
import com.FinalProject.group3.repository.AuthRepository;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;

/** LA.Signup — tạo tài khoản mới bằng Email/Password hoặc Google. */
public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
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
                            Toast.makeText(SignupActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (ApiException e) {
                    Toast.makeText(this, "Đăng ký Google thất bại: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnGoogle.setOnClickListener(v ->
                googleSignInLauncher.launch(googleSignInClient.getSignInIntent()));

        binding.btnSignup.setOnClickListener(v -> attemptSignup());
    }

    private void attemptSignup() {
        String name = binding.tilName.getEditText().getText().toString().trim();
        String email = binding.tilEmail.getEditText().getText().toString().trim();
        String password = binding.tilPassword.getEditText().getText().toString();
        String confirmPassword = binding.tilConfirmPassword.getEditText().getText().toString();

        binding.tilName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);

        boolean valid = true;
        if (TextUtils.isEmpty(name)) {
            binding.tilName.setError(getString(R.string.err_required));
            valid = false;
        }
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError(getString(R.string.err_required));
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.err_email_invalid));
            valid = false;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            binding.tilPassword.setError(getString(R.string.err_password_short));
            valid = false;
        }
        if (!confirmPassword.equals(password)) {
            binding.tilConfirmPassword.setError(getString(R.string.err_password_mismatch));
            valid = false;
        }
        if (!binding.cbTerms.isChecked()) {
            Toast.makeText(this, R.string.err_terms_required, Toast.LENGTH_SHORT).show();
            valid = false;
        }
        if (!valid) return;

        setLoading(true);
        authRepository.register(name, email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                goToMain();
            }

            @Override
            public void onFailure(String error) {
                setLoading(false);
                Toast.makeText(SignupActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSignup.setEnabled(!loading);
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

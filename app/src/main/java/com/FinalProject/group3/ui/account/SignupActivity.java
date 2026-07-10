package com.FinalProject.group3.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

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
                    if (e.getStatusCode() == 12501) return; // người dùng bấm hủy — không báo lỗi
                    Toast.makeText(this, AuthRepository.googleErrorMessage(e.getStatusCode()), Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        com.FinalProject.group3.utils.InsetsUtil.applySystemBarsPadding(binding.getRoot());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnGoogle.setOnClickListener(v ->
                googleSignInLauncher.launch(googleSignInClient.getSignInIntent()));

        binding.btnSignup.setOnClickListener(v -> attemptSignup());

        setupTermsLink();

        // Phản hồi ngay khi gõ — liệt kê đúng phần còn thiếu (chữ thường/IN HOA/số/độ dài),
        // hết thiếu thì hiện dòng xanh báo hợp lệ
        binding.tilPassword.getEditText().addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                binding.tilPassword.setError(null);
                updatePasswordHint(s.toString());
            }
        });
    }

    /** Cập nhật dòng gợi ý bên dưới ô mật khẩu: liệt kê đúng phần còn thiếu, hoặc báo hợp lệ (xanh). */
    private void updatePasswordHint(String password) {
        if (password.isEmpty()) {
            binding.tvPasswordRule.setText(R.string.signup_password_rule);
            binding.tvPasswordRule.setTextColor(getColor(R.color.color_text_secondary));
            return;
        }

        List<String> missing = new ArrayList<>();
        if (password.length() < 8) missing.add("đủ 8 ký tự");
        if (!password.matches(".*[a-z].*")) missing.add("chữ thường");
        if (!password.matches(".*[A-Z].*")) missing.add("chữ IN HOA");
        if (!password.matches(".*\\d.*")) missing.add("số");

        if (missing.isEmpty()) {
            binding.tvPasswordRule.setText("✓ Mật khẩu của bạn đã hợp lệ");
            binding.tvPasswordRule.setTextColor(getColor(R.color.color_success));
        } else {
            binding.tvPasswordRule.setText("Mật khẩu cần có: " + TextUtils.join(", ", missing));
            binding.tvPasswordRule.setTextColor(getColor(R.color.color_error));
        }
    }

    /** "Điều khoản sử dụng" trong nhãn checkbox → xanh, gạch chân, bấm mở trang Điều khoản. */
    private void setupTermsLink() {
        String full = getString(R.string.signup_terms);
        String link = "Điều khoản sử dụng";
        int start = full.indexOf(link);
        if (start < 0) return;

        SpannableString span = new SpannableString(full);
        span.setSpan(new ForegroundColorSpan(0xFF1565C0), start, start + link.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(PolicyActivity.intent(SignupActivity.this, PolicyActivity.TYPE_TERMS));
            }
        }, start, start + link.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.cbTerms.setText(span);
        binding.cbTerms.setMovementMethod(LinkMovementMethod.getInstance());
        binding.cbTerms.setHighlightColor(android.graphics.Color.TRANSPARENT);
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
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError(getString(R.string.err_required));
            valid = false;
        } else if (!com.FinalProject.group3.utils.ValidationUtils.isValidPassword(password)) {
            binding.tilPassword.setError(getString(R.string.err_password_format));
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
                // Tài khoản mới → gửi thông báo chào mừng + voucher NEWUSER
                com.FinalProject.group3.utils.NotificationHelper.sendWelcome(
                        com.FinalProject.group3.utils.FirebaseHelper.getCurrentUserId());
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

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
                    authRepository.loginWithGoogle(this, account, new AuthRepository.AuthCallback() {
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
                    Toast.makeText(this, AuthRepository.googleErrorMessage(this, e.getStatusCode()), Toast.LENGTH_LONG).show();
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

        // Facebook: UI giữ chỗ theo Figma — cần Facebook App ID mới kích hoạt được
        binding.btnFacebook.setOnClickListener(v ->
                Toast.makeText(this, R.string.msg_facebook_signup_soon,
                        Toast.LENGTH_SHORT).show());

        // Đề xuất mật khẩu mạnh: sinh ngẫu nhiên, điền cả 2 ô và hiện rõ cho khách xem
        binding.tvSuggestPassword.setOnClickListener(v -> {
            String suggested = generateStrongPassword();
            binding.tilPassword.getEditText().setText(suggested);
            binding.tilConfirmPassword.getEditText().setText(suggested);
            // Bỏ che dấu để khách đọc/ghi nhớ được — icon con mắt vẫn ẩn lại được
            binding.tilPassword.getEditText().setTransformationMethod(null);
            binding.tilConfirmPassword.getEditText().setTransformationMethod(null);
            Toast.makeText(this, R.string.msg_strong_password_generated,
                    Toast.LENGTH_LONG).show();
        });

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
        if (password.length() < 8) missing.add(getString(R.string.pwd_missing_length));
        if (!password.matches(".*[a-z].*")) missing.add(getString(R.string.pwd_missing_lowercase));
        if (!password.matches(".*[A-Z].*")) missing.add(getString(R.string.pwd_missing_uppercase));
        if (!password.matches(".*\\d.*")) missing.add(getString(R.string.pwd_missing_digit));

        if (missing.isEmpty()) {
            binding.tvPasswordRule.setText(R.string.pwd_valid);
            binding.tvPasswordRule.setTextColor(getColor(R.color.color_success));
        } else {
            binding.tvPasswordRule.setText(getString(R.string.pwd_missing_prefix) + TextUtils.join(", ", missing));
            binding.tvPasswordRule.setTextColor(getColor(R.color.color_error));
        }
    }

    /** "Điều khoản sử dụng" trong nhãn checkbox → xanh, gạch chân, bấm mở trang Điều khoản. */
    private void setupTermsLink() {
        String full = getString(R.string.signup_terms);
        String link = getString(R.string.signup_terms_link);
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

    /**
     * Sinh mật khẩu mạnh 12 ký tự: 4 chữ thường + 3 IN HOA + 3 số + 2 ký tự đặc biệt,
     * xáo trộn ngẫu nhiên. Bỏ các ký tự dễ nhầm (l, I, O, 0, 1) cho khách dễ đọc.
     */
    private String generateStrongPassword() {
        String lower   = "abcdefghjkmnpqrstuvwxyz";
        String upper   = "ABCDEFGHJKMNPQRSTUVWXYZ";
        String digits  = "23456789";
        String special = "@#$%&*";
        java.security.SecureRandom random = new java.security.SecureRandom();

        List<Character> chars = new ArrayList<>();
        for (int i = 0; i < 4; i++) chars.add(lower.charAt(random.nextInt(lower.length())));
        for (int i = 0; i < 3; i++) chars.add(upper.charAt(random.nextInt(upper.length())));
        for (int i = 0; i < 3; i++) chars.add(digits.charAt(random.nextInt(digits.length())));
        for (int i = 0; i < 2; i++) chars.add(special.charAt(random.nextInt(special.length())));
        java.util.Collections.shuffle(chars, random);

        StringBuilder sb = new StringBuilder();
        for (char c : chars) sb.append(c);
        return sb.toString();
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
        authRepository.register(this, name, email, password, new AuthRepository.AuthCallback() {
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

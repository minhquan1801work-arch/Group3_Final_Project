package com.FinalProject.group3.ui.account;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.FinalProject.group3.R;
import com.FinalProject.group3.databinding.ActivityForgotPasswordBinding;
import com.FinalProject.group3.repository.AuthRepository;
import com.FinalProject.group3.utils.InsetsUtil;

/**
 * Quên mật khẩu — chỉ khôi phục qua email (tài khoản Glassity chỉ đăng ký
 * bằng email, không có luồng SĐT, nên bỏ hẳn lựa chọn khôi phục bằng SĐT).
 *
 *  1. Nhập email → Firebase gửi link đặt lại mật khẩu.
 *  2. Màn xác nhận "đã gửi email", có thể gửi lại sau 60s.
 *
 * Việc đặt mật khẩu mới diễn ra trên trang do Firebase host (mở từ link
 * trong email) — app không tự làm màn nhập mật khẩu mới.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private final AuthRepository authRepository = new AuthRepository();

    private String pendingEmail;
    private CountDownTimer emailTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applySystemBarsPadding(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> handleBack());
        binding.tvBackToLogin.setOnClickListener(v -> finish());
        binding.btnBackToLoginFinal.setOnClickListener(v -> finish());

        binding.btnContinue.setOnClickListener(v -> attemptSendResetEmail());
        binding.tvResendEmail.setOnClickListener(v -> {
            if (pendingEmail != null) sendResetEmail(pendingEmail, true);
        });
    }

    // ── Bước 1: validate email + gửi link đặt lại mật khẩu ───────────────────
    private void attemptSendResetEmail() {
        String email = binding.etInput.getText().toString().trim();
        binding.tilInput.setError(null);

        if (TextUtils.isEmpty(email)) {
            binding.tilInput.setError(getString(R.string.err_required));
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilInput.setError(getString(R.string.err_email_invalid));
            return;
        }
        pendingEmail = email;
        sendResetEmail(email, false);
    }

    private void sendResetEmail(String email, boolean isResend) {
        setLoading(true);
        authRepository.forgotPassword(this, email, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                binding.tvEmailSentDesc.setText(getString(R.string.forgot_sent_desc, maskEmail(email)));
                showGroup(binding.groupEmailSent);
                startEmailResendCooldown();
                if (isResend)
                    Toast.makeText(ForgotPasswordActivity.this, R.string.msg_resend_email_sent, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                setLoading(false);
                Toast.makeText(ForgotPasswordActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startEmailResendCooldown() {
        binding.tvResendEmail.setEnabled(false);
        if (emailTimer != null) emailTimer.cancel();
        emailTimer = new CountDownTimer(60_000, 1000) {
            @Override public void onTick(long ms) {
                binding.tvResendEmail.setText(getString(R.string.forgot_resend) + " (" + ms / 1000 + "s)");
            }
            @Override public void onFinish() {
                binding.tvResendEmail.setEnabled(true);
                binding.tvResendEmail.setText(R.string.forgot_resend);
            }
        }.start();
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────
    private void showGroup(View group) {
        binding.groupMethodSelect.setVisibility(View.GONE);
        binding.groupEmailSent.setVisibility(View.GONE);
        group.setVisibility(View.VISIBLE);
    }

    private void handleBack() {
        if (binding.groupEmailSent.getVisibility() == View.VISIBLE) {
            showGroup(binding.groupMethodSelect);
        } else {
            finish();
        }
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnContinue.setEnabled(!loading);
    }

    /** "quangminh01@gmail.com" → "qu*******01@gmail.com" — che bớt để tránh lộ email đầy đủ trên màn hình. */
    private static String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at < 3) return email;
        String local = email.substring(0, at);
        String domain = email.substring(at);
        String masked = local.substring(0, 2)
                + "*".repeat(Math.max(1, local.length() - 4))
                + local.substring(local.length() - 2);
        return masked + domain;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (emailTimer != null) emailTimer.cancel();
    }
}

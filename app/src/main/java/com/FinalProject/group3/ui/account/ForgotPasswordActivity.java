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

/**
 * Quên mật khẩu.
 *
 * LƯU Ý: Figma thiết kế 3 màn (nhập email → nhập mã OTP → đặt mật khẩu mới trong app),
 * nhưng Firebase Auth (bản miễn phí, không dùng thêm Cloud Functions) KHÔNG hỗ trợ xác
 * thực OTP ngay trong app — flow chuẩn của Firebase là gửi một email chứa link, người
 * dùng bấm vào link đó và đổi mật khẩu trên trang web do Firebase host.
 * → Activity này gộp còn 2 bước: (1) nhập email, (2) xác nhận đã gửi + cho phép gửi lại.
 * Nút "Gửi lại" ở bước 2 có cooldown 60s để tránh spam, thay cho ô nhập OTP + đếm ngược
 * trong thiết kế gốc.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private final AuthRepository authRepository = new AuthRepository();
    private String pendingEmail;
    private CountDownTimer resendTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        binding.tvBackToLogin.setOnClickListener(v -> finish());
        binding.btnBackToLoginFinal.setOnClickListener(v -> finish());

        binding.btnContinue.setOnClickListener(v -> attemptSendResetEmail());
        binding.tvResend.setOnClickListener(v -> {
            if (pendingEmail != null) sendResetEmail(pendingEmail, true);
        });
    }

    private void attemptSendResetEmail() {
        String email = binding.tilEmail.getEditText().getText().toString().trim();
        binding.tilEmail.setError(null);

        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError(getString(R.string.err_required));
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.err_email_invalid));
            return;
        }
        sendResetEmail(email, false);
    }

    private void sendResetEmail(String email, boolean isResend) {
        pendingEmail = email;
        setLoading(true);
        authRepository.forgotPassword(email, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                showSentState();
                if (isResend) {
                    Toast.makeText(ForgotPasswordActivity.this, "Đã gửi lại email", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                setLoading(false);
                Toast.makeText(ForgotPasswordActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSentState() {
        binding.groupRequestEmail.setVisibility(View.GONE);
        binding.groupSent.setVisibility(View.VISIBLE);
        startResendCooldown();
    }

    private void startResendCooldown() {
        binding.tvResend.setEnabled(false);
        if (resendTimer != null) resendTimer.cancel();
        resendTimer = new CountDownTimer(60_000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long sec = millisUntilFinished / 1000;
                binding.tvResend.setText(getString(R.string.forgot_resend) + " (" + sec + "s)");
            }

            @Override
            public void onFinish() {
                binding.tvResend.setEnabled(true);
                binding.tvResend.setText(R.string.forgot_resend);
            }
        }.start();
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnContinue.setEnabled(!loading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resendTimer != null) resendTimer.cancel();
    }
}

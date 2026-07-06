package com.FinalProject.group3.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.FinalProject.group3.MainActivity;
import com.FinalProject.group3.R;
import com.FinalProject.group3.databinding.ActivityForgotPasswordBinding;
import com.FinalProject.group3.repository.AuthRepository;
import com.FinalProject.group3.utils.FirebaseHelper;
import com.FinalProject.group3.utils.InsetsUtil;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Quên mật khẩu — 3 bước:
 *
 *  1. Chọn phương thức (Email / SĐT) + nhập
 *  2a. Email: gửi reset link (Firebase) → màn xác nhận "check email"
 *  2b. Phone: Firebase Phone Auth gửi SMS OTP → nhập OTP
 *  3.  Phone: nhập mật khẩu mới → updatePassword()
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private final AuthRepository authRepository = new AuthRepository();

    // Phone OTP state
    private String storedVerificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private String pendingPhone; // E.164 format (+84...)
    private CountDownTimer otpTimer;

    // Email resend state
    private String pendingEmail;
    private CountDownTimer emailTimer;
    private boolean isPhoneMethod = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applySystemBarsPadding(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> handleBack());
        binding.tvBackToLogin.setOnClickListener(v -> finish());
        binding.btnBackToLoginFinal.setOnClickListener(v -> finish());

        // Đổi hint input theo radio selection
        binding.rgMethod.setOnCheckedChangeListener((group, checkedId) -> {
            isPhoneMethod = (checkedId == R.id.rbPhone);
            binding.tilInput.setError(null);
            if (isPhoneMethod) {
                binding.tilInput.setHint("Số điện thoại (VD: 0901234567)");
                binding.etInput.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
            } else {
                binding.tilInput.setHint(getString(R.string.login_email_hint));
                binding.etInput.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            }
        });

        binding.btnContinue.setOnClickListener(v -> attemptStep1());
        binding.btnVerifyOtp.setOnClickListener(v -> attemptVerifyOtp());
        binding.tvResendOtp.setOnClickListener(v -> resendSmsOtp());
        binding.tvResendEmail.setOnClickListener(v -> {
            if (pendingEmail != null) sendResetEmail(pendingEmail, true);
        });
        binding.btnConfirmReset.setOnClickListener(v -> attemptResetPassword());
    }

    // ── Bước 1: validate + gửi ─────────────────────────────────────────────────
    private void attemptStep1() {
        String input = binding.etInput.getText().toString().trim();
        binding.tilInput.setError(null);

        if (TextUtils.isEmpty(input)) {
            binding.tilInput.setError(getString(R.string.err_required));
            return;
        }

        if (isPhoneMethod) {
            String phone = normalizePhone(input);
            if (phone == null) {
                binding.tilInput.setError("Số điện thoại không hợp lệ");
                return;
            }
            pendingPhone = phone;
            sendSmsOtp(phone, false);
        } else {
            if (!Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
                binding.tilInput.setError(getString(R.string.err_email_invalid));
                return;
            }
            pendingEmail = input;
            sendResetEmail(input, false);
        }
    }

    // ── Email path ──────────────────────────────────────────────────────────────
    private void sendResetEmail(String email, boolean isResend) {
        setLoading(true);
        authRepository.forgotPassword(email, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                showGroup(binding.groupEmailSent);
                startEmailResendCooldown();
                if (isResend)
                    Toast.makeText(ForgotPasswordActivity.this, "Đã gửi lại email", Toast.LENGTH_SHORT).show();
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

    // ── Phone OTP path ──────────────────────────────────────────────────────────
    private void sendSmsOtp(String phone, boolean isResend) {
        setLoading(true);
        PhoneAuthOptions.Builder builder = PhoneAuthOptions.newBuilder(FirebaseHelper.getAuth())
                .setPhoneNumber(phone)
                .setTimeout(180L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(phoneCallbacks);
        if (isResend && resendToken != null) {
            builder.setForceResendingToken(resendToken);
        }
        PhoneAuthProvider.verifyPhoneNumber(builder.build());
    }

    private void resendSmsOtp() {
        if (pendingPhone != null) sendSmsOtp(pendingPhone, true);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks phoneCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            // Auto-verified (emulator / instant verify) → skip OTP entry
            setLoading(false);
            signInWithPhone(credential);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            setLoading(false);
            String msg = e.getMessage();
            if (msg != null && msg.contains("TOO_SHORT")) {
                msg = "Số điện thoại quá ngắn hoặc không đúng định dạng";
            } else if (msg != null && msg.contains("INVALID_PHONE_NUMBER")) {
                msg = "Số điện thoại không hợp lệ";
            } else if (msg != null && msg.contains("QUOTA_EXCEEDED")) {
                msg = "Đã vượt quá giới hạn gửi SMS. Vui lòng thử lại sau.";
            } else {
                msg = "Gửi OTP thất bại: " + msg;
            }
            Toast.makeText(ForgotPasswordActivity.this, msg, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
            setLoading(false);
            storedVerificationId = verificationId;
            resendToken = token;
            // Cập nhật mô tả với số điện thoại bị mask
            binding.tvOtpDesc.setText(
                    "Glassity vừa gửi mã xác minh đến " + maskPhone(pendingPhone)
                    + " – hãy lưu lại trong 3 phút, vui lòng kiểm tra tin nhắn SMS nhé!");
            showGroup(binding.groupPhoneOtp);
            startOtpCountdown();
        }
    };

    private void attemptVerifyOtp() {
        String code = binding.etOtp.getText().toString().trim();
        binding.tilOtp.setError(null);
        if (TextUtils.isEmpty(code) || code.length() < 6) {
            binding.tilOtp.setError("Nhập đủ 6 chữ số");
            return;
        }
        if (storedVerificationId == null) {
            Toast.makeText(this, "Phiên xác minh đã hết hạn, vui lòng gửi lại mã", Toast.LENGTH_SHORT).show();
            return;
        }
        setLoading(true);
        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(storedVerificationId, code);
        signInWithPhone(credential);
    }

    private void signInWithPhone(PhoneAuthCredential credential) {
        FirebaseHelper.getAuth().signInWithCredential(credential)
                .addOnSuccessListener(result -> {
                    setLoading(false);
                    // Kiểm tra customer record tồn tại cho UID này
                    String uid = result.getUser().getUid();
                    FirebaseHelper.getDb().collection(FirebaseHelper.COL_CUSTOMERS)
                            .document(uid).get()
                            .addOnSuccessListener(doc -> {
                                if (doc.exists()) {
                                    // Phone auth UID khớp với customer → cho đặt lại mật khẩu
                                    showGroup(binding.groupReset);
                                } else {
                                    // Phone không link với Firebase Auth account → tìm qua Firestore field
                                    lookupCustomerByPhone(result.getUser().getPhoneNumber());
                                }
                            })
                            .addOnFailureListener(e -> showGroup(binding.groupReset));
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    binding.tilOtp.setError("Mã xác minh không đúng, vui lòng thử lại");
                });
    }

    /** Tìm customer theo số điện thoại trong Firestore (cho tài khoản đăng ký bằng email). */
    private void lookupCustomerByPhone(String phone) {
        // Normalize phone về dạng lưu trong Firestore (có thể là "0901..." hoặc "+84...")
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_CUSTOMERS)
                .whereEqualTo("phone", phone).limit(1).get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        // Customer đăng ký bằng email, phone match → cho đặt lại mật khẩu
                        showGroup(binding.groupReset);
                    } else {
                        // Thử tìm với local format (remove +84, add 0)
                        String localPhone = toLocalPhone(phone);
                        FirebaseHelper.getDb().collection(FirebaseHelper.COL_CUSTOMERS)
                                .whereEqualTo("phone", localPhone).limit(1).get()
                                .addOnSuccessListener(snap2 -> {
                                    if (!snap2.isEmpty()) {
                                        showGroup(binding.groupReset);
                                    } else {
                                        Toast.makeText(this,
                                                "Số điện thoại chưa đăng ký tài khoản",
                                                Toast.LENGTH_SHORT).show();
                                        showGroup(binding.groupMethodSelect);
                                    }
                                })
                                .addOnFailureListener(e -> showGroup(binding.groupReset));
                    }
                })
                .addOnFailureListener(e -> showGroup(binding.groupReset));
    }

    private void startOtpCountdown() {
        binding.tvResendOtp.setEnabled(false);
        if (otpTimer != null) otpTimer.cancel();
        otpTimer = new CountDownTimer(180_000, 1000) {
            @Override public void onTick(long ms) {
                long total = ms / 1000;
                binding.tvCountdown.setText(
                        String.format(Locale.US, "%d:%02d", total / 60, total % 60));
            }
            @Override public void onFinish() {
                binding.tvCountdown.setText("0:00");
                binding.tvResendOtp.setEnabled(true);
                storedVerificationId = null; // expire
            }
        }.start();
    }

    // ── Bước 3: đặt lại mật khẩu ───────────────────────────────────────────────
    private void attemptResetPassword() {
        String newPass = binding.etNewPassword.getText().toString();
        String confirm = binding.etConfirmPassword.getText().toString();
        binding.tilNewPassword.setError(null);
        binding.tilConfirmPassword.setError(null);

        if (newPass.length() < 8) {
            binding.tilNewPassword.setError("Mật khẩu tối thiểu 8 ký tự");
            return;
        }
        if (!newPass.equals(confirm)) {
            binding.tilConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            return;
        }

        com.google.firebase.auth.FirebaseUser user = FirebaseHelper.getAuth().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Phiên xác minh hết hạn. Vui lòng thực hiện lại.", Toast.LENGTH_SHORT).show();
            showGroup(binding.groupMethodSelect);
            return;
        }

        binding.progressBarReset.setVisibility(View.VISIBLE);
        binding.btnConfirmReset.setEnabled(false);

        user.updatePassword(newPass)
                .addOnSuccessListener(v -> {
                    binding.progressBarReset.setVisibility(View.GONE);
                    Toast.makeText(this, "Đặt lại mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                    // Về màn hình chính (đã đăng nhập bằng phone credential)
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.progressBarReset.setVisibility(View.GONE);
                    binding.btnConfirmReset.setEnabled(true);
                    Toast.makeText(this, "Có lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────
    private void showGroup(View group) {
        binding.groupMethodSelect.setVisibility(View.GONE);
        binding.groupPhoneOtp.setVisibility(View.GONE);
        binding.groupEmailSent.setVisibility(View.GONE);
        binding.groupReset.setVisibility(View.GONE);
        group.setVisibility(View.VISIBLE);
    }

    private void handleBack() {
        if (binding.groupPhoneOtp.getVisibility() == View.VISIBLE
                || binding.groupEmailSent.getVisibility() == View.VISIBLE) {
            showGroup(binding.groupMethodSelect);
        } else if (binding.groupReset.getVisibility() == View.VISIBLE) {
            showGroup(binding.groupPhoneOtp);
        } else {
            finish();
        }
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnContinue.setEnabled(!loading);
    }

    /** "0901234567" hoặc "84901234567" → "+84901234567". null nếu không hợp lệ. */
    private static String normalizePhone(String input) {
        String digits = input.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return null;
        if (digits.startsWith("84") && digits.length() == 11) return "+" + digits;
        if (digits.startsWith("0") && digits.length() == 10) return "+84" + digits.substring(1);
        if (digits.length() == 9) return "+84" + digits;
        return null;
    }

    /** "+84901234567" → "090*****67" */
    private static String maskPhone(String e164) {
        if (e164 == null) return "";
        String local = e164.startsWith("+84") ? "0" + e164.substring(3) : e164;
        if (local.length() >= 6)
            return local.substring(0, 3) + "*****" + local.substring(local.length() - 2);
        return local;
    }

    /** "+84901234567" → "0901234567" */
    private static String toLocalPhone(String e164) {
        if (e164 == null) return "";
        return e164.startsWith("+84") ? "0" + e164.substring(3) : e164;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (otpTimer != null) otpTimer.cancel();
        if (emailTimer != null) emailTimer.cancel();
    }
}

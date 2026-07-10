package com.FinalProject.group3.utils;

import java.util.regex.Pattern;

/**
 * Validate định dạng mật khẩu + số điện thoại — dùng chung cho Signup,
 * ForgotPassword (đặt lại mật khẩu), Checkout (khách nhập tay), AddAddress.
 */
public final class ValidationUtils {

    // Tối thiểu 8 ký tự, có ít nhất 1 chữ thường, 1 chữ IN HOA, 1 chữ số
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    // Số điện thoại VN: đúng 10 chữ số, bắt đầu bằng 0
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9}$");

    private ValidationUtils() {}

    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }
}

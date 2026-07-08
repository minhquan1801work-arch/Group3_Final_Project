package com.FinalProject.group3.utils;

import android.content.Context;

/**
 * Nhớ trạng thái phiên để WelcomeActivity không bắt chọn lại mỗi lần mở app.
 *
 * - User đăng nhập: Firebase Auth tự lưu phiên → chỉ cần check getCurrentUser().
 * - Khách (guest): Firebase không biết → lưu cờ "continue_as_guest" vào SharedPreferences.
 * - Đăng xuất: clear cờ guest để quay về màn Welcome đúng nghĩa.
 */
public final class SessionManager {

    private static final String PREFS = "session_prefs";
    private static final String KEY_GUEST = "continue_as_guest";

    private SessionManager() {}

    /** Gọi khi user bấm "Tiếp tục với vai trò Khách" */
    public static void setGuestMode(Context context, boolean isGuest) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_GUEST, isGuest).apply();
    }

    public static boolean isGuestMode(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_GUEST, false);
    }

    /** Còn phiên (đăng nhập hoặc khách) → mở lại app vào thẳng Main */
    public static boolean hasSession(Context context) {
        return FirebaseHelper.isLoggedIn() || isGuestMode(context);
    }

    /** Gọi khi đăng xuất: xóa phiên Firebase + cờ guest */
    public static void logout(Context context) {
        FirebaseHelper.signOut();
        setGuestMode(context, false);
    }
}

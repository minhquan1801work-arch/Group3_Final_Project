package com.FinalProject.group3.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.FinalProject.group3.model.Notification;

public final class NotificationHelper {

    private static final String PREFS = "notification_prefs";
    private static final String KEY_SEEDED_PREFIX = "campaign_seeded_";

    private NotificationHelper() {}

    public static void push(String uid, String message, String type) {
        pushOrder(uid, message, type, null);
    }

    /** Ghi thong bao kem orderId de nhan thang vao OrderDetailActivity. */
    public static void pushOrder(String uid, String message, String type, String orderId) {
        if (uid == null || uid.isEmpty() || "GUEST".equals(uid)) return;
        Notification n = new Notification(uid, message, type);
        if (orderId != null) n.setOrderId(orderId);
        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_NOTIFICATIONS)
                .add(n);
    }

    public static void sendWelcome(String uid) {
        push(uid, "Chao mung ban den voi Glassity! Kham pha ngay bo suu tap "
                + "kinh mat moi nhat danh rieng cho ban.", "SYSTEM");
        push(uid, "Qua chao mung: nhap ma NEWUSER khi thanh toan de duoc giam 10% "
                + "cho don hang dau tien cua ban.", "PROMOTION");
    }

    public static void sendOrderPlaced(String uid, String orderId, String orderCode,
                                       String paymentMethod, String productSummary) {
        String payNote = "BANK_TRANSFER".equals(paymentMethod)
                ? " Vui long hoan tat chuyen khoan de don duoc xu ly."
                : " Ban se thanh toan khi nhan hang (COD).";
        String name = (productSummary != null && !productSummary.isEmpty())
                ? " - " + productSummary : "";
        pushOrder(uid, "Dat hang thanh cong! Don " + orderCode + name
                + " dang cho xac nhan." + payNote, "ORDER", orderId);
    }

    public static void seedDemoCampaigns(Context context) {
        String uid = FirebaseHelper.getCurrentUserId();
        if (uid == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String key = KEY_SEEDED_PREFIX + uid;
        if (prefs.getBoolean(key, false)) return;
        prefs.edit().putBoolean(key, true).apply();

        push(uid, "BST Sunlight Studio da len ke! Kinh mat he 2026 giam den 20% "
                + "voi ma SALE20 - ap dung cho don tu 1 trieu.", "PROMOTION");
        push(uid, "FREESHIP toan quoc cho don tu 500K - nhap ma FREESHIP "
                + "tai buoc thanh toan ngay hom nay.", "PROMOTION");
        push(uid, "Uu dai thanh vien: giam 15% voi ma MEMBER15 cho don tu 800K, "
                + "chi ap dung den het tuan nay!", "PROMOTION");
    }
}

package com.FinalProject.group3.model;

import com.google.firebase.firestore.DocumentId;

import java.util.Date;

/**
 * Voucher trong kho của user (LA.Voucher) — lưu ở customers/{uid}/vouchers.
 * type: "DISCOUNT" (giảm giá) | "SHIPPING" (vận chuyển).
 * code khớp với mã dùng ở màn Thanh toán (FREESHIP / GIAM10 / GIAM50K).
 */
public class Voucher {

    @DocumentId
    private String voucherId;
    private String code;
    private String title;
    private String type;
    private double minOrder;   // đơn tối thiểu để áp dụng
    private Date expireAt;

    public Voucher() {}

    public Voucher(String code, String title, String type, double minOrder, Date expireAt) {
        this.code = code;
        this.title = title;
        this.type = type;
        this.minOrder = minOrder;
        this.expireAt = expireAt;
    }

    /** Số ngày còn lại trước khi hết hạn (tối thiểu 0). */
    public long daysLeft() {
        if (expireAt == null) return 0;
        long ms = expireAt.getTime() - System.currentTimeMillis();
        return Math.max(0, (long) Math.ceil(ms / (24.0 * 60 * 60 * 1000)));
    }

    public String getVoucherId() { return voucherId; }
    public void setVoucherId(String voucherId) { this.voucherId = voucherId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getMinOrder() { return minOrder; }
    public void setMinOrder(double minOrder) { this.minOrder = minOrder; }

    public Date getExpireAt() { return expireAt; }
    public void setExpireAt(Date expireAt) { this.expireAt = expireAt; }
}

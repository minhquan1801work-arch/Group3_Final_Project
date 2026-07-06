package com.FinalProject.group3.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Order {
    @DocumentId
    private String orderId;
    private String customerId;
    private String orderCode;
    private double totalAmount;
    private double shippingFee;
    private double shipDiscount;
    private double voucherDiscount;
    private int usedPoints;
    private int earnedPoints;
    private String paymentMethod;  // "COD" hoặc "BANK_TRANSFER"
    private String paymentStatus;  // "PENDING", "PAID", "FAILED"
    private String orderStatus;    // "PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED"
    private String shippingAddress;
    private boolean reviewed;
    @ServerTimestamp
    private Date createdAt;

    public Order() {}

    public Order(String customerId, String orderCode, double totalAmount,
                 String paymentMethod, String shippingAddress) {
        this.customerId = customerId;
        this.orderCode = orderCode;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = "PENDING";
        this.orderStatus = "PENDING";
        this.shippingAddress = shippingAddress;
        this.reviewed = false;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getShippingFee() { return shippingFee; }
    public void setShippingFee(double shippingFee) { this.shippingFee = shippingFee; }

    public double getShipDiscount() { return shipDiscount; }
    public void setShipDiscount(double shipDiscount) { this.shipDiscount = shipDiscount; }

    public double getVoucherDiscount() { return voucherDiscount; }
    public void setVoucherDiscount(double voucherDiscount) { this.voucherDiscount = voucherDiscount; }

    public int getUsedPoints() { return usedPoints; }
    public void setUsedPoints(int usedPoints) { this.usedPoints = usedPoints; }

    public int getEarnedPoints() { return earnedPoints; }
    public void setEarnedPoints(int earnedPoints) { this.earnedPoints = earnedPoints; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public boolean isReviewed() { return reviewed; }
    public void setReviewed(boolean reviewed) { this.reviewed = reviewed; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}

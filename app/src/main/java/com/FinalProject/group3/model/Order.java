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
    private String paymentMethod;  // "COD" hoặc "BANK_TRANSFER"
    private String paymentStatus;  // "PENDING", "PAID", "FAILED"
    private String orderStatus;    // "PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED"
    private String shippingAddress;
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
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}

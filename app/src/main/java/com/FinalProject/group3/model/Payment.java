package com.FinalProject.group3.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Payment {
    @DocumentId
    private String paymentId;
    private String orderId;
    private String customerId;
    private String method;       // "COD", "BANK_TRANSFER"
    private String status;       // "PENDING", "SUCCESS", "FAILED"
    private String transactionId; // Mã giao dịch ngân hàng (nếu có)
    private double amount;
    @ServerTimestamp
    private Date createdAt;

    public Payment() {}

    public Payment(String orderId, String customerId, String method, double amount) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.method = method;
        this.amount = amount;
        this.status = "PENDING";
    }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}

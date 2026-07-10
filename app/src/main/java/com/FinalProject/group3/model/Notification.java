package com.FinalProject.group3.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Notification {
    @DocumentId
    private String notificationId;
    private String customerId;
    private String message;
    private String status; // "UNREAD", "READ"
    private String type;   // "ORDER", "PROMOTION", "SYSTEM"
    private String orderId; // chỉ có khi type=ORDER — dùng để nhảy thẳng OrderDetailActivity
    @ServerTimestamp
    private Date createdAt;

    public Notification() {}

    public Notification(String customerId, String message, String type) {
        this.customerId = customerId;
        this.message = message;
        this.status = "UNREAD";
        this.type = type;
    }

    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
}

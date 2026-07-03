package com.FinalProject.group3.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Customer {
    @DocumentId
    private String customerId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String loginProvider; // "email" hoặc "google"
    @ServerTimestamp
    private Date createdAt;

    // Required default constructor for Firestore
    public Customer() {}

    public Customer(String name, String email, String phone, String address, String loginProvider) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.loginProvider = loginProvider;
    }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getLoginProvider() { return loginProvider; }
    public void setLoginProvider(String loginProvider) { this.loginProvider = loginProvider; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}

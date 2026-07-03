package com.FinalProject.group3.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Favorite {
    @DocumentId
    private String favoriteId;
    private String customerId;
    private String productId;
    @ServerTimestamp
    private Date createdAt;

    // Để hiển thị UI
    private Product product;

    public Favorite() {}

    public Favorite(String customerId, String productId) {
        this.customerId = customerId;
        this.productId = productId;
    }

    public String getFavoriteId() { return favoriteId; }
    public void setFavoriteId(String favoriteId) { this.favoriteId = favoriteId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}

package com.FinalProject.group3.model;

import com.google.firebase.firestore.DocumentId;

public class CartDetail {
    @DocumentId
    private String cartDetailId;
    private String productId;
    private int quantity;
    private String color;

    // Thời điểm thêm vào giỏ (millis) — dùng sort cũ → mới
    private long addedAt;

    // Thêm để tiện hiển thị UI (không lưu Firestore)
    private Product product;

    public CartDetail() {}

    public CartDetail(String productId, int quantity, String color) {
        this.productId = productId;
        this.quantity = quantity;
        this.color = color;
    }

    public String getCartDetailId() { return cartDetailId; }
    public void setCartDetailId(String cartDetailId) { this.cartDetailId = cartDetailId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public long getAddedAt() { return addedAt; }
    public void setAddedAt(long addedAt) { this.addedAt = addedAt; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}

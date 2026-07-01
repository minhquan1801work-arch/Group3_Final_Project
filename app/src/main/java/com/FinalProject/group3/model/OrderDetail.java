package com.FinalProject.group3.model;

import com.google.firebase.firestore.DocumentId;

public class OrderDetail {
    @DocumentId
    private String orderDetailId;
    private String productId;
    private int quantity;
    private double price;
    private String color;

    // Để hiển thị UI
    private Product product;

    public OrderDetail() {}

    public OrderDetail(String productId, int quantity, double price, String color) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.color = color;
    }

    public String getOrderDetailId() { return orderDetailId; }
    public void setOrderDetailId(String orderDetailId) { this.orderDetailId = orderDetailId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}

package com.FinalProject.group3.model;

import com.google.firebase.firestore.DocumentId;
import java.util.List;

public class Product {
    @DocumentId
    private String productId;
    private String name;
    private double price;
    private int stock;
    private String description;
    private List<String> colors;
    private List<String> images; // Firebase Storage URLs
    private String categoryId;

    public Product() {}

    public Product(String name, double price, int stock, String description,
                   List<String> colors, List<String> images, String categoryId) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.colors = colors;
        this.images = images;
        this.categoryId = categoryId;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getColors() { return colors; }
    public void setColors(List<String> colors) { this.colors = colors; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
}

package com.FinalProject.group3.model;

import java.util.List;

public class ProductVariant {
    private String color;
    private String colorName; // tên hiển thị: "Hổ phách", "Đen bóng",...
    private int stock;
    private List<String> images;

    public ProductVariant() {}

    public ProductVariant(String color, int stock, List<String> images) {
        this.color = color;
        this.stock = stock;
        this.images = images;
    }

    public String getColor()  { return color; }
    public void setColor(String color) { this.color = color; }

    public String getColorName() { return colorName; }
    public void setColorName(String colorName) { this.colorName = colorName; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
}

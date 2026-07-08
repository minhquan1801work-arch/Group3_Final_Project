package com.FinalProject.group3.model;

import java.util.List;

public class ProductVariant {
    private String color;
    private String colorName;
    private List<String> images;
    private int stock;

    public ProductVariant() {}

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getColorName() { return colorName; }
    public void setColorName(String colorName) { this.colorName = colorName; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}

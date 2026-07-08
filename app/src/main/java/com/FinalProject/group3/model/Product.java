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
    private List<String> faceShapes; // ["tron","trai_xoan",...] — empty for non-glasses
    private String collection;       // BST: "Monochrome Collection" | "Essential Acetate" | "Sunlight Studio"
    private String gender;           // "nam" | "nu" | "unisex"
    private List<ProductVariant> variants; // mỗi variant có color + stock + images riêng

    public Product() {}

    public Product(String name, double price, int stock, String description,
                   List<String> colors, List<String> images, String categoryId,
                   List<String> faceShapes) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.colors = colors;
        this.images = images;
        this.categoryId = categoryId;
        this.faceShapes = faceShapes;
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

    // Fallback: neu chua co field images (data moi chi luu anh trong variants)
    // thi tra ve anh cua variant dau tien — cac man hinh cu (order, checkout,
    // review) van hien anh dung ma khong phai sua tung noi.
    public List<String> getImages() {
        if ((images == null || images.isEmpty())
                && variants != null && !variants.isEmpty()
                && variants.get(0).getImages() != null) {
            return variants.get(0).getImages();
        }
        return images;
    }
    public void setImages(List<String> images) { this.images = images; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public List<String> getFaceShapes() { return faceShapes; }
    public void setFaceShapes(List<String> faceShapes) { this.faceShapes = faceShapes; }

    public String getCollection() { return collection; }
    public void setCollection(String collection) { this.collection = collection; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public List<ProductVariant> getVariants() { return variants; }
    public void setVariants(List<ProductVariant> variants) { this.variants = variants; }

    // Trả về tổng stock (sum của tất cả variants, hoặc field stock cũ nếu chưa migrate)
    public int getTotalStock() {
        if (variants != null && !variants.isEmpty()) {
            int total = 0;
            for (ProductVariant v : variants) total += v.getStock();
            return total;
        }
        return stock;
    }
}

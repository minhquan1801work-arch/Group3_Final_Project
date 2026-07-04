package com.FinalProject.group3.model;

import com.google.firebase.firestore.DocumentId;

public class Category {
    @DocumentId
    private String categoryId;
    private String name;
    private String description;


    public Category() {}

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }


}

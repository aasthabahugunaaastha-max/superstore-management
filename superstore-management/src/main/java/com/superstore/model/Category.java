// Category.java
package com.superstore.model;
import java.io.Serializable;
import java.util.*;

public class Category implements Serializable{
    private String categoryId;
    private String categoryName;
    private Map<String, Subcategory> subcategories;
    
    public Category(String categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.subcategories = new HashMap<>();
    }
    
    public void addSubcategory(Subcategory subcategory) {
        subcategories.put(subcategory.getSubcategoryId(), subcategory);
    }
    
    public void removeSubcategory(String subcategoryId) {
        subcategories.remove(subcategoryId);
    }
    
    public Subcategory getSubcategory(String subcategoryId) {
        return subcategories.get(subcategoryId);
    }
    
    public List<Subcategory> getAllSubcategories() {
        return new ArrayList<>(subcategories.values());
    }
    
    public String getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { 
        this.categoryName = categoryName; 
    }
}


// Store.java
package com.superstore.domain;
import com.superstore.model.Category;
import com.superstore.model.Subcategory;
import com.superstore.model.Item;
import java.io.Serializable;

import java.util.*;

public class Store implements Serializable{
    private String storeId;
    private String storeName;
    private String linkedWarehouseId;
    private Map<String, Category> categories;
    
    public Store(String storeId, String storeName) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.categories = new HashMap<>();
    }
    
    public void linkWarehouse(String warehouseId) {
        this.linkedWarehouseId = warehouseId;
    }
    
    public void addCategory(Category category) {
        categories.put(category.getCategoryId(), category);
    }
    
    public void removeCategory(String categoryId) {
        categories.remove(categoryId);
    }
    
    public Category getCategory(String categoryId) {
        return categories.get(categoryId);
    }
    
    public List<Category> getAllCategories() {
        return new ArrayList<>(categories.values());
    }
    
    public Item getItem(String itemCode) {
        for (Category category : categories.values()) {
            for (Subcategory subcategory : category.getAllSubcategories()) {
                Item item = subcategory.getItem(itemCode);
                if (item != null) return item;
            }
        }
        return null;
    }
    
    public List<Item> getOutOfStockItems() {
        List<Item> outOfStock = new ArrayList<>();
        for (Category category : categories.values()) {
            for (Subcategory subcategory : category.getAllSubcategories()) {
                for (Item item : subcategory.getAllItems()) {
                    if (item.getCurrentInventoryLevel() == 0) {
                        outOfStock.add(item);
                    }
                }
            }
        }
        return outOfStock;
    }
    
    public String getStoreId() { return storeId; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public String getLinkedWarehouseId() { return linkedWarehouseId; }
}

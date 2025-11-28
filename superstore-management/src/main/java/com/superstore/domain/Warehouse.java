// Warehouse.java
package com.superstore.domain;
import com.superstore.model.Category;
import com.superstore.model.Subcategory;
import com.superstore.model.Item;
import com.superstore.domain.OrderMessage;
import java.io.Serializable;

import java.util.*;

public class Warehouse implements Serializable {
    private String warehouseId;
    private String warehouseName;
    private Map<String, Category> categories;
    private List<String> linkedStoreIds;
    private List<OrderMessage> receivedMessages;
    
    public Warehouse(String warehouseId, String warehouseName) {
        this.warehouseId = warehouseId;
        this.warehouseName = warehouseName;
        this.categories = new HashMap<>();
        this.linkedStoreIds = new ArrayList<>();
        this.receivedMessages = new ArrayList<>();
    }
    
    public void linkStore(String storeId) {
        if (!linkedStoreIds.contains(storeId)) {
            linkedStoreIds.add(storeId);
        }
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
    
    public List<Item> getItemsNeedingReorder() {
        List<Item> reorderItems = new ArrayList<>();
        for (Category category : categories.values()) {
            for (Subcategory subcategory : category.getAllSubcategories()) {
                for (Item item : subcategory.getAllItems()) {
                    if (item.needsReordering()) {
                        reorderItems.add(item);
                    }
                }
            }
        }
        // Sort by reorder point
        reorderItems.sort(Comparator.comparing(Item::calculateReorderPoint));
        return reorderItems;
    }
    
    public void receiveMessage(OrderMessage message) {
        receivedMessages.add(message);
    }
    
    public List<OrderMessage> getUnprocessedMessages() {
        List<OrderMessage> unprocessed = new ArrayList<>();
        for (OrderMessage msg : receivedMessages) {
            if (!msg.isProcessed()) {
                unprocessed.add(msg);
            }
        }
        return unprocessed;
    }
    
    public String getWarehouseId() { return warehouseId; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { 
        this.warehouseName = warehouseName; 
    }
    public List<String> getLinkedStoreIds() { return linkedStoreIds; }
}


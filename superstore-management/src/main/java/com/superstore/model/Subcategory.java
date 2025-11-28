// Subcategory.java
package com.superstore.model;
import java.io.Serializable;
import java.util.*;

public class Subcategory implements Serializable{
    private String subcategoryId;
    private String subcategoryName;
    private String categoryId;
    private Map<String, Item> items;
    
    public Subcategory(String subcategoryId, String subcategoryName, String categoryId) {
        this.subcategoryId = subcategoryId;
        this.subcategoryName = subcategoryName;
        this.categoryId = categoryId;
        this.items = new HashMap<>();
    }
    
    public void addItem(Item item) {
        items.put(item.getItemCode(), item);
    }
    
    public void removeItem(String itemCode) {
        items.remove(itemCode);
    }
    
    public Item getItem(String itemCode) {
        return items.get(itemCode);
    }
    
    public List<Item> getAllItems() {
        return new ArrayList<>(items.values());
    }
    
    public List<Item> searchItems(String searchTerm) {
        List<Item> results = new ArrayList<>();
        String lowerSearchTerm = searchTerm.toLowerCase();
        for (Item item : items.values()) {
            if (item.getItemName().toLowerCase().contains(lowerSearchTerm)) {
                results.add(item);
            }
        }
        return results;
    }
    
    public List<Item> getItemsSorted() {
        List<Item> itemList = new ArrayList<>(items.values());
        itemList.sort(Comparator.comparing(Item::getItemName));
        return itemList;
    }
    
    public String getSubcategoryId() { return subcategoryId; }
    public String getSubcategoryName() { return subcategoryName; }
    public void setSubcategoryName(String subcategoryName) { 
        this.subcategoryName = subcategoryName; 
    }
    public String getCategoryId() { return categoryId; }
}

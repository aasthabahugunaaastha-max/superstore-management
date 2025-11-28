// Item.java
package com.superstore.model;
import java.io.Serializable;

public class Item implements Serializable{
    private String itemCode;
    private String itemName;
    private String description;
    private double fixedCostPerQuarter; // D
    private double carryingCostPerUnitPerQuarter; // H
    private double demandUnitsPerQuarter; // K
    private int currentInventoryLevel;
    private double maxDailyUsage;
    private double avgDailyUsage;
    private double maxLeadTimeDays;
    private double avgLeadTimeDays;
    private String subcategoryId;
    
    public Item(String itemCode, String itemName, String subcategoryId) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.subcategoryId = subcategoryId;
        this.currentInventoryLevel = 0;
    }
    
    // Calculate Economic Order Quantity (EOQ)
    public double calculateEOQ() {
        if (carryingCostPerUnitPerQuarter == 0) return 0;
        return Math.sqrt((2 * fixedCostPerQuarter * demandUnitsPerQuarter) / carryingCostPerUnitPerQuarter);
    }
    
    // Calculate Safety Stock (for warehouses only)
    public double calculateSafetyStock() {
        return (maxDailyUsage * maxLeadTimeDays) - (avgDailyUsage * avgLeadTimeDays);
    }
    
    // Calculate Reorder Point
    public double calculateReorderPoint() {
        return (avgLeadTimeDays * avgDailyUsage) + calculateSafetyStock();
    }
    
    // Check if item needs reordering
    public boolean needsReordering() {
        return currentInventoryLevel <= calculateReorderPoint();
    }
    
    // Getters and Setters
    public String getItemCode() { return itemCode; }
    public String getItemName() { return itemName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public double getFixedCostPerQuarter() { return fixedCostPerQuarter; }
    public void setFixedCostPerQuarter(double fixedCostPerQuarter) { 
        this.fixedCostPerQuarter = fixedCostPerQuarter; 
    }
    
    public double getCarryingCostPerUnitPerQuarter() { return carryingCostPerUnitPerQuarter; }
    public void setCarryingCostPerUnitPerQuarter(double cost) { 
        this.carryingCostPerUnitPerQuarter = cost; 
    }
    
    public double getDemandUnitsPerQuarter() { return demandUnitsPerQuarter; }
    public void setDemandUnitsPerQuarter(double demand) { 
        this.demandUnitsPerQuarter = demand; 
    }
    
    public int getCurrentInventoryLevel() { return currentInventoryLevel; }
    public void setCurrentInventoryLevel(int level) { 
        this.currentInventoryLevel = level; 
    }
    
    public void addInventory(int units) { this.currentInventoryLevel += units; }
    public void removeInventory(int units) { this.currentInventoryLevel -= units; }
    
    public double getMaxDailyUsage() { return maxDailyUsage; }
    public void setMaxDailyUsage(double maxDailyUsage) { 
        this.maxDailyUsage = maxDailyUsage; 
    }
    
    public double getAvgDailyUsage() { return avgDailyUsage; }
    public void setAvgDailyUsage(double avgDailyUsage) { 
        this.avgDailyUsage = avgDailyUsage; 
    }
    
    public double getMaxLeadTimeDays() { return maxLeadTimeDays; }
    public void setMaxLeadTimeDays(double maxLeadTimeDays) { 
        this.maxLeadTimeDays = maxLeadTimeDays; 
    }
    
    public double getAvgLeadTimeDays() { return avgLeadTimeDays; }
    public void setAvgLeadTimeDays(double avgLeadTimeDays) { 
        this.avgLeadTimeDays = avgLeadTimeDays; 
    }
    
    public String getSubcategoryId() { return subcategoryId; }
    public void setSubcategoryId(String subcategoryId) { 
        this.subcategoryId = subcategoryId; 
    }
}


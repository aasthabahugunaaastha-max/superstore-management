// OrderMessage.java
package com.superstore.domain;
import java.io.Serializable;
import java.time.LocalDate;

public class OrderMessage implements Serializable{
    private String messageId;
    private String fromStoreId;
    private String toWarehouseId;
    private String itemName;
    private String itemCode;
    private int quantity;
    private LocalDate expectedArrivalDate;
    private LocalDate sentDate;
    private boolean processed;
    
    public OrderMessage(String messageId, String fromStoreId, String toWarehouseId,
                       String itemName, String itemCode, int quantity, 
                       LocalDate expectedArrivalDate) {
        this.messageId = messageId;
        this.fromStoreId = fromStoreId;
        this.toWarehouseId = toWarehouseId;
        this.itemName = itemName;
        this.itemCode = itemCode;
        this.quantity = quantity;
        this.expectedArrivalDate = expectedArrivalDate;
        this.sentDate = LocalDate.now();
        this.processed = false;
    }
    
    // Getters and Setters
    public String getMessageId() { return messageId; }
    public String getFromStoreId() { return fromStoreId; }
    public String getToWarehouseId() { return toWarehouseId; }
    public String getItemName() { return itemName; }
    public String getItemCode() { return itemCode; }
    public int getQuantity() { return quantity; }
    public LocalDate getExpectedArrivalDate() { return expectedArrivalDate; }
    public LocalDate getSentDate() { return sentDate; }
    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }
}


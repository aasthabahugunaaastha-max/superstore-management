// SuperstoreSystem.java
package com.superstore.core;

import java.util.*;
import java.io.*;
import java.time.LocalDateTime;
import com.superstore.domain.Store;
import com.superstore.domain.Warehouse;
import com.superstore.domain.OrderMessage;
import com.superstore.user.SuperUser;
import com.superstore.user.User;

public class SuperstoreSystem {
    private Map<String, Warehouse> warehouses;
    private Map<String, Store> stores;
    private Map<String, User> users;
    private SuperUser superUser;
    private static SuperstoreSystem instance;
    
    private SuperstoreSystem() {
        warehouses = new HashMap<>();
        stores = new HashMap<>();
        users = new HashMap<>();
        // Create default super user
        superUser = new SuperUser("SU001", "superadmin", "admin123");
        users.put(superUser.getUserId(), superUser);
    }
    
    public static synchronized SuperstoreSystem getInstance() {
        if (instance == null) {
            instance = new SuperstoreSystem();
            instance.loadData();  // ✅ Load persistent data on startup
        }
        return instance;
    }
    
    // Warehouse Management
    public void createWarehouse(String warehouseId, String warehouseName) {
        Warehouse warehouse = new Warehouse(warehouseId, warehouseName);
        warehouses.put(warehouseId, warehouse);
        logEvent("Warehouse created: " + warehouseId);
        saveData();  // ✅ Auto-save after changes
    }
    
    public Warehouse getWarehouse(String warehouseId) {
        return warehouses.get(warehouseId);
    }
    
    public List<Warehouse> getAllWarehouses() {
        return new ArrayList<>(warehouses.values());
    }
    
    // Store Management
    public void createStore(String storeId, String storeName, String warehouseId) {
        Store store = new Store(storeId, storeName);
        store.linkWarehouse(warehouseId);
        stores.put(storeId, store);
        
        Warehouse warehouse = warehouses.get(warehouseId);
        if (warehouse != null) {
            warehouse.linkStore(storeId);
        }
        logEvent("Store created: " + storeId + " linked to " + warehouseId);
        saveData();  // ✅ Auto-save after changes
    }
    
    public Store getStore(String storeId) {
        return stores.get(storeId);
    }
    
    public List<Store> getAllStores() {
        return new ArrayList<>(stores.values());
    }
    
    // ✅ NEW: REQUIRED FOR PERSISTENT ADMIN LISTS
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
    
    // User Management
    public void createUser(User user) {
        users.put(user.getUserId(), user);
        logEvent("User created: " + user.getUsername() + " (" + user.getUserType() + ")");
        saveData();  // ✅ Auto-save after user creation
    }
    
    // ✅ NEW: REQUIRED FOR DELETE FUNCTIONALITY
    public void deleteUser(User user) {
        if (user != null && users.containsKey(user.getUserId())) {
            users.remove(user.getUserId());
            logEvent("User deleted: " + user.getUsername() + " (" + user.getUserType() + ")");
            saveData();  // ✅ Auto-save after deletion
        }
    }
    
    public User authenticateUser(String username, String password) {
        for (User user : users.values()) {
            if (user.authenticate(username, password)) {
                logEvent("User logged in: " + username);
                return user;
            }
        }
        return null;
    }
    
    public User getUser(String userId) {
        return users.get(userId);
    }
    
    public SuperUser getSuperUser() {
        return superUser;
    }
    
    // Message handling
    public void sendOrderMessage(OrderMessage message) {
        Warehouse warehouse = warehouses.get(message.getToWarehouseId());
        if (warehouse != null) {
            warehouse.receiveMessage(message);
            logEvent("Order message sent from " + message.getFromStoreId() + 
                    " to " + message.getToWarehouseId());
        }
    }
    
    public void forwardOrderMessage(String fromWarehouseId, String toWarehouseId, 
                                   OrderMessage originalMessage) {
        OrderMessage forwardedMessage = new OrderMessage(
            UUID.randomUUID().toString(),
            fromWarehouseId,
            toWarehouseId,
            originalMessage.getItemName(),
            originalMessage.getItemCode(),
            originalMessage.getQuantity(),
            originalMessage.getExpectedArrivalDate()
        );
        Warehouse warehouse = warehouses.get(toWarehouseId);
        if (warehouse != null) {
            warehouse.receiveMessage(forwardedMessage);
            logEvent("Order message forwarded from " + fromWarehouseId + 
                    " to " + toWarehouseId);
        }
    }
    
    // Logging
    private void logEvent(String event) {
        try (FileWriter fw = new FileWriter("superstore.log", true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(LocalDateTime.now() + " - " + event);
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }
    
    public void logException(Exception e) {
        try (FileWriter fw = new FileWriter("superstore.log", true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(LocalDateTime.now() + " - EXCEPTION: " + e.getMessage());
            e.printStackTrace(pw);
        } catch (IOException ex) {
            System.err.println("Error writing exception to log: " + ex.getMessage());
        }
    }
    
    // Data Persistence
    public void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream("superstore_data.dat"))) {
            oos.writeObject(warehouses);
            oos.writeObject(stores);
            oos.writeObject(users);
            logEvent("Data saved successfully");
        } catch (IOException e) {
            logException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public void loadData() {
        File dataFile = new File("superstore_data.dat");
        if (!dataFile.exists()) {
            logEvent("No saved data found. Starting with fresh system.");
            return; // Empty collections already initialized
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))) {
            this.warehouses = (Map<String, Warehouse>) ois.readObject();
            this.stores = (Map<String, Store>) ois.readObject();
            this.users = (Map<String, User>) ois.readObject();
            
            // ✅ Ensure superUser is always available after load
            if (!users.containsKey(superUser.getUserId())) {
                users.put(superUser.getUserId(), superUser);
            }
            
            logEvent("Data loaded successfully.");
        } catch (Exception e) {
            logEvent("Failed to load data: " + e.getMessage());
            // Reset to empty on load failure
            warehouses.clear();
            stores.clear();
            users.clear();
            users.put(superUser.getUserId(), superUser); // Keep superuser
        }
    }
}


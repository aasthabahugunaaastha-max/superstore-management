// StoreTest.java
package com.superstore.domain;

import com.superstore.model.Category;
import com.superstore.model.Item;
import com.superstore.model.Subcategory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StoreTest {

    private Store createSampleStore() {
        Store store = new Store("S001", "City Store");

        Category grocery = new Category("C001", "Grocery");
        Subcategory grains = new Subcategory("SC001", "Grains", "C001");

        Item rice = new Item("I001", "Rice", "SC001");
        rice.setCurrentInventoryLevel(10);      // in stock

        Item wheat = new Item("I002", "Wheat", "SC001");
        wheat.setCurrentInventoryLevel(0);      // out of stock

        grains.addItem(rice);
        grains.addItem(wheat);
        grocery.addSubcategory(grains);
        store.addCategory(grocery);

        return store;
    }

    @Test
    void testGetOutOfStockItems() {
        Store store = createSampleStore();

        List<Item> outOfStock = store.getOutOfStockItems();

        assertEquals(1, outOfStock.size(), "There should be exactly one out-of-stock item");
        assertEquals("I002", outOfStock.get(0).getItemCode());
        assertEquals("Wheat", outOfStock.get(0).getItemName());
    }

    @Test
    void testGetItemByCode() {
        Store store = createSampleStore();

        Item rice = store.getItem("I001");
        Item none = store.getItem("NON_EXISTENT");

        assertNotNull(rice);
        assertEquals("Rice", rice.getItemName());
        assertNull(none, "Unknown item code should return null");
    }

    @Test
    void testStoreIdentityAndLink() {
        Store store = new Store("S002", "Suburb Store");
        store.linkWarehouse("W001");

        assertEquals("S002", store.getStoreId());
        assertEquals("Suburb Store", store.getStoreName());
        assertEquals("W001", store.getLinkedWarehouseId());
    }
}


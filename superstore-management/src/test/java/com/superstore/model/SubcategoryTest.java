// SubcategoryTest.java
package com.superstore.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class SubcategoryTest {

    @Test
    public void testAddAndRemoveItem() {
        Subcategory sub = new Subcategory("S001", "Beverages", "C001");
        Item item = new Item("I001", "Cola", "S001");

        sub.addItem(item);
        assertEquals(1, sub.getAllItems().size());
        assertEquals(item, sub.getItem("I001"));

        sub.removeItem("I001");
        assertNull(sub.getItem("I001"));
        assertEquals(0, sub.getAllItems().size());
    }

    @Test
    public void testSearchItemsPartialMatch() {
        Subcategory sub = new Subcategory("S001", "Beverages", "C001");
        sub.addItem(new Item("I001", "Orange Juice", "S001"));
        sub.addItem(new Item("I002", "Apple Juice", "S001"));
        sub.addItem(new Item("I003", "Milk", "S001"));

        List<Item> results = sub.searchItems("juice");
        assertEquals(2, results.size());
    }

    @Test
    public void testItemsSortedAlphabetically() {
        Subcategory sub = new Subcategory("S001", "Beverages", "C001");
        sub.addItem(new Item("I002", "Cola", "S001"));
        sub.addItem(new Item("I001", "Apple Juice", "S001"));

        List<Item> sorted = sub.getItemsSorted();
        assertEquals("Apple Juice", sorted.get(0).getItemName());
        assertEquals("Cola", sorted.get(1).getItemName());
    }
}


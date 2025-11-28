// WarehouseTest.java
package com.superstore.domain;
import com.superstore.model.Category;
import com.superstore.model.Subcategory;
import com.superstore.model.Item;
import com.superstore.domain.OrderMessage;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class WarehouseTest {

    private Warehouse createSampleWarehouse() {
        Warehouse w = new Warehouse("W001", "Central");
        Category c = new Category("C001", "Grocery");
        Subcategory s = new Subcategory("S001", "Grains", "C001");

        Item i1 = new Item("I001", "Rice", "S001");
        i1.setAvgDailyUsage(10);
        i1.setAvgLeadTimeDays(2);
        i1.setMaxDailyUsage(15);
        i1.setMaxLeadTimeDays(3);
        i1.setCurrentInventoryLevel(10); // should trigger reorder

        Item i2 = new Item("I002", "Wheat", "S001");
        i2.setAvgDailyUsage(5);
        i2.setAvgLeadTimeDays(1);
        i2.setMaxDailyUsage(6);
        i2.setMaxLeadTimeDays(1);
        i2.setCurrentInventoryLevel(100); // no reorder

        s.addItem(i1);
        s.addItem(i2);
        c.addSubcategory(s);
        w.addCategory(c);
        return w;
    }

    @Test
    public void testGetItemsNeedingReorder() {
        Warehouse w = createSampleWarehouse();
        List<Item> reorderItems = w.getItemsNeedingReorder();
        assertEquals(1, reorderItems.size());
        assertEquals("I001", reorderItems.get(0).getItemCode());
    }

    @Test
    public void testLinkStore() {
        Warehouse w = new Warehouse("W001", "Central");
        w.linkStore("S001");
        w.linkStore("S002");
        w.linkStore("S001"); // duplicate ignored

        assertEquals(2, w.getLinkedStoreIds().size());
        assertTrue(w.getLinkedStoreIds().contains("S001"));
        assertTrue(w.getLinkedStoreIds().contains("S002"));
    }
}


// ItemTest.java
package com.superstore.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ItemTest {

    @Test
    public void testEOQCalculation() {
        Item item = new Item("I001", "Rice", "S001");
        item.setFixedCostPerQuarter(100);  // D
        item.setDemandUnitsPerQuarter(1000); // K
        item.setCarryingCostPerUnitPerQuarter(2); // H

        double eoq = item.calculateEOQ();
        // EOQ = sqrt((2*D*K)/H) = sqrt((2*100*1000)/2) = sqrt(100000) ≈ 316.23
        assertEquals(316.23, eoq, 0.5);
    }

    @Test
    public void testSafetyStockCalculation() {
        Item item = new Item("I002", "Sugar", "S001");
        item.setMaxDailyUsage(50);
        item.setMaxLeadTimeDays(5);
        item.setAvgDailyUsage(30);
        item.setAvgLeadTimeDays(4);

        double safetyStock = item.calculateSafetyStock();
        // (50*5) - (30*4) = 250 - 120 = 130
        assertEquals(130.0, safetyStock, 0.0001);
    }

    @Test
    public void testReorderPointCalculation() {
        Item item = new Item("I003", "Salt", "S001");
        item.setAvgDailyUsage(20);
        item.setAvgLeadTimeDays(3);
        item.setMaxDailyUsage(25);
        item.setMaxLeadTimeDays(4);

        double rp = item.calculateReorderPoint();
        // safety = (25*4)-(20*3) = 100-60=40; RP = (3*20)+40 = 100
        assertEquals(100.0, rp, 0.0001);
    }

    @Test
    public void testNeedsReorderingTrue() {
        Item item = new Item("I004", "Oil", "S001");
        item.setAvgDailyUsage(10);
        item.setAvgLeadTimeDays(2);
        item.setMaxDailyUsage(15);
        item.setMaxLeadTimeDays(3);
        // RP = (2*10)+((15*3)-(10*2)) = 20 + (45-20) = 45
        item.setCurrentInventoryLevel(40);

        assertTrue(item.needsReordering());
    }

    @Test
    public void testInventoryInwardsOutwards() {
        Item item = new Item("I005", "Flour", "S001");
        item.setCurrentInventoryLevel(100);
        item.addInventory(20);
        assertEquals(120, item.getCurrentInventoryLevel());
        item.removeInventory(30);
        assertEquals(90, item.getCurrentInventoryLevel());
    }
}


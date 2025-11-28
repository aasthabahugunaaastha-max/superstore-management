// CategoryTest.java
package com.superstore.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CategoryTest {

    @Test
    public void testAddAndRemoveSubcategory() {
        Category category = new Category("C001", "Grocery");
        Subcategory sub = new Subcategory("S001", "Beverages", "C001");

        category.addSubcategory(sub);
        assertEquals(1, category.getAllSubcategories().size());
        assertEquals(sub, category.getSubcategory("S001"));

        category.removeSubcategory("S001");
        assertNull(category.getSubcategory("S001"));
        assertEquals(0, category.getAllSubcategories().size());
    }
}


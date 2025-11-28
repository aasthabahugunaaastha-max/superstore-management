// UserAuthTest.java
package com.superstore.user;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserAuthTest {

    @Test
    public void testAuthenticateSuccess() {
        WarehouseAdministrator admin = new WarehouseAdministrator(
                "U001", "whAdmin", "pass123", "W001");
        assertTrue(admin.authenticate("whAdmin", "pass123"));
    }

    @Test
    public void testAuthenticateFailure() {
        StoreAdministrator admin = new StoreAdministrator(
                "U002", "storeAdmin", "pass123", "S001");
        assertFalse(admin.authenticate("storeAdmin", "wrong"));
    }

    @Test
    public void testChangePassword() {
        Storekeeper keeper = new Storekeeper("U003", "keeper", "old", "S001");
        assertTrue(keeper.authenticate("keeper", "old"));
        keeper.changePassword("new");
        assertFalse(keeper.authenticate("keeper", "old"));
        assertTrue(keeper.authenticate("keeper", "new"));
    }
}


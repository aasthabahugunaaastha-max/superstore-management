// WarehouseAdministrator.java
package com.superstore.user;
import java.io.Serializable;

public class WarehouseAdministrator extends Administrator implements Serializable  {
    public WarehouseAdministrator(String userId, String username, 
                                  String password, String warehouseId) {
        super(userId, username, password, UserType.WAREHOUSE_ADMIN, warehouseId);
    }
}


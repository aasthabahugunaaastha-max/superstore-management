// WarehouseKeeper.java
package com.superstore.user;
import java.io.Serializable;

public class WarehouseKeeper extends Keeper implements Serializable  {
    public WarehouseKeeper(String userId, String username, 
                          String password, String warehouseId) {
        super(userId, username, password, UserType.WAREHOUSE_KEEPER, warehouseId);
    }
}


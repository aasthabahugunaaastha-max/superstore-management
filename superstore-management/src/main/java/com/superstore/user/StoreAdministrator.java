// StoreAdministrator.java
package com.superstore.user;
import java.io.Serializable;

public class StoreAdministrator extends Administrator implements Serializable {
    public StoreAdministrator(String userId, String username, 
                             String password, String storeId) {
        super(userId, username, password, UserType.STORE_ADMIN, storeId);
    }
}


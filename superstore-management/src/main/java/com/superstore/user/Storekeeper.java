// Storekeeper.java
package com.superstore.user;
import java.io.Serializable;

public class Storekeeper extends Keeper implements Serializable {
    public Storekeeper(String userId, String username, 
                      String password, String storeId) {
        super(userId, username, password, UserType.STOREKEEPER, storeId);
    }
}


// SuperUser.java
package com.superstore.user;
import java.io.Serializable;

public class SuperUser extends User implements Serializable {
    public SuperUser(String userId, String username, String password) {
        super(userId, username, password, UserType.SUPER_USER);
    }
}


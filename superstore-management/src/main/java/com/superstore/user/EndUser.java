// EndUser.java
package com.superstore.user;
import java.io.Serializable;

public class EndUser extends User implements Serializable  {
    public EndUser() {
        super("guest", "guest", "guest", UserType.END_USER);
    }
}


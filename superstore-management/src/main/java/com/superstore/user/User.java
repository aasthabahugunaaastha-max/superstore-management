// User.java
package com.superstore.user;
import java.io.Serializable;

public abstract class User implements Serializable {
    protected String userId;
    protected String username;
    protected String password;
    protected UserType userType;
    
    public enum UserType {
        SUPER_USER, WAREHOUSE_ADMIN, STORE_ADMIN, 
        WAREHOUSE_KEEPER, STOREKEEPER, END_USER
    }
    
    public User(String userId, String username, String password, UserType userType) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.userType = userType;
    }
    
    public boolean authenticate(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }
    
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }
    
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public UserType getUserType() { return userType; }
}


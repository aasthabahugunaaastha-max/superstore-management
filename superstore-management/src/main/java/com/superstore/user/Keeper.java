package com.superstore.user;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;


public abstract class Keeper extends User implements Serializable  {
    protected String facilityId;
    protected List<String> assignedCategories;

    public Keeper(String userId, String username, String password,
                  UserType userType, String facilityId) {
        super(userId, username, password, userType);
        this.facilityId = facilityId;
        this.assignedCategories = new ArrayList<>();
    }

    public String getFacilityId() {
        return facilityId;
    }

    public List<String> getAssignedCategories() {
        return assignedCategories;
    }

    public void assignCategory(String categoryId) {
        assignedCategories.add(categoryId);
    }
}


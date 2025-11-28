package com.superstore.user;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;


public abstract class Administrator extends User implements Serializable {
    protected String facilityId;               // warehouse or store ID
    protected List<String> assignedCategories;

    public Administrator(String userId, String username, String password,
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


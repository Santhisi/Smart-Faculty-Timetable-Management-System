package com.smartfaculty.dto;

import com.smartfaculty.enums.UserRole;

public class UserAccountResponse {

    private Long userId;

    private String username;

    private UserRole role;

    private Boolean enabled;


    public UserAccountResponse() {
    }


    public UserAccountResponse(
            Long userId,
            String username,
            UserRole role,
            Boolean enabled) {

        this.userId = userId;
        this.username = username;
        this.role = role;
        this.enabled = enabled;
    }


    // =====================================================
    // GET USER ID
    // =====================================================

    public Long getUserId() {
        return userId;
    }


    // =====================================================
    // SET USER ID
    // =====================================================

    public void setUserId(Long userId) {
        this.userId = userId;
    }


    // =====================================================
    // GET USERNAME
    // =====================================================

    public String getUsername() {
        return username;
    }


    // =====================================================
    // SET USERNAME
    // =====================================================

    public void setUsername(String username) {
        this.username = username;
    }


    // =====================================================
    // GET ROLE
    // =====================================================

    public UserRole getRole() {
        return role;
    }


    // =====================================================
    // SET ROLE
    // =====================================================

    public void setRole(UserRole role) {
        this.role = role;
    }


    // =====================================================
    // GET ENABLED
    // =====================================================

    public Boolean getEnabled() {
        return enabled;
    }


    // =====================================================
    // SET ENABLED
    // =====================================================

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
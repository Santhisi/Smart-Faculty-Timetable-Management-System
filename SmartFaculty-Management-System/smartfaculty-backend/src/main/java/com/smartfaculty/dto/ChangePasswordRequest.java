package com.smartfaculty.dto;

public class ChangePasswordRequest {

    private String currentPassword;

    private String newPassword;

    private String confirmNewPassword;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public ChangePasswordRequest() {
    }


    // =====================================================
    // CURRENT PASSWORD
    // =====================================================

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }


    // =====================================================
    // NEW PASSWORD
    // =====================================================

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }


    // =====================================================
    // CONFIRM NEW PASSWORD
    // =====================================================

    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }

    public void setConfirmNewPassword(
            String confirmNewPassword) {

        this.confirmNewPassword =
                confirmNewPassword;
    }


    // =====================================================
    // COMPATIBILITY GETTER
    // =====================================================
    // This allows existing controller code that uses
    // getConfirmPassword() to continue working.

    public String getConfirmPassword() {

        return confirmNewPassword;
    }


    // =====================================================
    // COMPATIBILITY SETTER
    // =====================================================

    public void setConfirmPassword(
            String confirmPassword) {

        this.confirmNewPassword =
                confirmPassword;
    }
}
package com.smartfaculty.dto;

public class AdminResetPasswordRequest {

    private String newPassword;

    private String confirmNewPassword;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public AdminResetPasswordRequest() {
    }


    // =====================================================
    // GET NEW PASSWORD
    // =====================================================

    public String getNewPassword() {
        return newPassword;
    }


    // =====================================================
    // SET NEW PASSWORD
    // =====================================================

    public void setNewPassword(
            String newPassword) {

        this.newPassword = newPassword;
    }


    // =====================================================
    // GET CONFIRM NEW PASSWORD
    // =====================================================

    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }


    // =====================================================
    // SET CONFIRM NEW PASSWORD
    // =====================================================

    public void setConfirmNewPassword(
            String confirmNewPassword) {

        this.confirmNewPassword =
                confirmNewPassword;
    }
}
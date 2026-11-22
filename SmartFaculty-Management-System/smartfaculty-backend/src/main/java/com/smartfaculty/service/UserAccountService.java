package com.smartfaculty.service;

import com.smartfaculty.dto.AdminResetPasswordRequest;
import com.smartfaculty.dto.UserAccountResponse;
import com.smartfaculty.enums.UserRole;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface UserAccountService {


    // =====================================================
    // GET ALL USER ACCOUNTS
    // =====================================================

    List<UserAccountResponse> getAllAccounts();


    // =====================================================
    // GET ACCOUNT BY ID
    // =====================================================

    UserAccountResponse getAccountById(
            Long userId
    );


    // =====================================================
    // ENABLE ACCOUNT
    // =====================================================

    UserAccountResponse enableAccount(
            Long userId
    );


    // =====================================================
    // DISABLE ACCOUNT
    // =====================================================

    UserAccountResponse disableAccount(
            Long userId,
            Authentication authentication
    );


    // =====================================================
    // ADMIN RESET USER PASSWORD
    // =====================================================

    UserAccountResponse resetPassword(
            Long userId,
            AdminResetPasswordRequest request,
            Authentication authentication
    );

    // ADMIN: change an existing user role.
    UserAccountResponse updateRole(
            Long userId,
            UserRole role,
            Authentication authentication
    );
}
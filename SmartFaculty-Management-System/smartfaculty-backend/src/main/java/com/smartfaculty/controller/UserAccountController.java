package com.smartfaculty.controller;

import com.smartfaculty.dto.AdminResetPasswordRequest;
import com.smartfaculty.dto.UserAccountResponse;
import com.smartfaculty.enums.UserRole;
import com.smartfaculty.service.UserAccountService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-accounts")

public class UserAccountController {


    @Autowired
    private UserAccountService userAccountService;


    // =====================================================
    // GET ALL USER ACCOUNTS
    // =====================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserAccountResponse>>
    getAllAccounts() {

        return ResponseEntity.ok(
                userAccountService.getAllAccounts()
        );
    }


    // =====================================================
    // GET ACCOUNT BY ID
    // =====================================================

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserAccountResponse>
    getAccountById(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                userAccountService.getAccountById(
                        userId
                )
        );
    }


    // =====================================================
    // ENABLE ACCOUNT
    // =====================================================

    @PutMapping("/{userId}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserAccountResponse>
    enableAccount(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                userAccountService.enableAccount(
                        userId
                )
        );
    }


    // =====================================================
    // DISABLE ACCOUNT
    // =====================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{userId}/disable")
    public ResponseEntity<UserAccountResponse> disableAccount(
            @PathVariable Long userId,
            Authentication authentication) {

        return ResponseEntity.ok(
                userAccountService.disableAccount(
                        userId,
                        authentication
                )
        );
    }


    // =====================================================
    // ADMIN CHANGE USER ROLE
    // =====================================================

    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserAccountResponse>
    updateRole(
            @PathVariable Long userId,
            @RequestParam UserRole role,
            Authentication authentication) {

        return ResponseEntity.ok(
                userAccountService.updateRole(
                        userId,
                        role,
                        authentication
                )
        );
    }


    // =====================================================
    // ADMIN RESET USER PASSWORD
    // =====================================================

    @PutMapping("/{userId}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserAccountResponse>
    resetPassword(
            @PathVariable Long userId,
            @RequestBody AdminResetPasswordRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                userAccountService.resetPassword(
                        userId,
                        request,
                        authentication
                )
        );
    }

}

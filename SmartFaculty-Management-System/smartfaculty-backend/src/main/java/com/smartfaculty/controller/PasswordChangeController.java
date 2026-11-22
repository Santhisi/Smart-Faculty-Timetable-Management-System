package com.smartfaculty.controller;

import com.smartfaculty.dto.ChangePasswordRequest;
import com.smartfaculty.service.PasswordChangeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")

public class PasswordChangeController {

    @Autowired
    private PasswordChangeService passwordChangeService;


    // =====================================================
    // CHANGE PASSWORD
    // AUTHENTICATED USERS ONLY
    // =====================================================

    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        // =================================================
        // CHECK AUTHENTICATION
        // =================================================

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication is required.");
        }


        // =================================================
        // CHANGE PASSWORD
        // =================================================

        try {

            passwordChangeService.changePassword(request);

            return ResponseEntity.ok(
                    "Password changed successfully."
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        }
    }
}
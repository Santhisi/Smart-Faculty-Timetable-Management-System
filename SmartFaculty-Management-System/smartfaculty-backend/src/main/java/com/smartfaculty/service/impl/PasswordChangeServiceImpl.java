package com.smartfaculty.service.impl;

import com.smartfaculty.dto.ChangePasswordRequest;
import com.smartfaculty.entity.User;
import com.smartfaculty.repository.UserRepository;
import com.smartfaculty.service.PasswordChangeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordChangeServiceImpl
        implements PasswordChangeService {


    @Autowired
    private UserRepository userRepository;


    @Autowired
    private PasswordEncoder passwordEncoder;


    // =====================================================
    // CHANGE PASSWORD
    // =====================================================

    @Override
    @Transactional
    public void changePassword(
            ChangePasswordRequest request) {


        // =================================================
        // VALIDATE REQUEST
        // =================================================

        if (request == null) {

            throw new IllegalArgumentException(
                    "Password change data cannot be null."
            );
        }


        if (isBlank(
                request.getCurrentPassword()
        )) {

            throw new IllegalArgumentException(
                    "Current password is required."
            );
        }


        if (isBlank(
                request.getNewPassword()
        )) {

            throw new IllegalArgumentException(
                    "New password is required."
            );
        }


        if (isBlank(
                request.getConfirmPassword()
        )) {

            throw new IllegalArgumentException(
                    "Confirm password is required."
            );
        }


        // =================================================
        // CHECK NEW PASSWORD LENGTH
        // =================================================

        if (request.getNewPassword().trim().length() < 6) {

            throw new IllegalArgumentException(
                    "New password must contain at least 6 characters."
            );
        }


        // =================================================
        // CHECK PASSWORD CONFIRMATION
        // =================================================

        if (!request.getNewPassword().equals(
                request.getConfirmPassword()
        )) {

            throw new IllegalArgumentException(
                    "New password and confirm password do not match."
            );
        }


        // =================================================
        // GET LOGGED-IN USER
        // =================================================

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "User is not authenticated."
            );
        }


        String username =
                authentication.getName();


        // =================================================
        // FIND USER
        // =================================================

        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User account not found."
                                )
                        );


        // =================================================
        // CHECK CURRENT PASSWORD
        // =================================================

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword()
        )) {

            throw new IllegalArgumentException(
                    "Current password is incorrect."
            );
        }


        // =================================================
        // PREVENT SAME PASSWORD
        // =================================================

        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPassword()
        )) {

            throw new IllegalArgumentException(
                    "New password must be different from the current password."
            );
        }


        // =================================================
        // SAVE NEW PASSWORD
        // =================================================

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );


        userRepository.save(user);
    }


    // =====================================================
    // BLANK CHECK
    // =====================================================

    private boolean isBlank(
            String value) {

        return value == null ||
                value.trim().isEmpty();
    }
}
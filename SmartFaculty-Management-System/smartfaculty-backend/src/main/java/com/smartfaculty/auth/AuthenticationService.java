package com.smartfaculty.auth;

import com.smartfaculty.dto.ChangePasswordRequest;
import com.smartfaculty.dto.LoginRequest;
import com.smartfaculty.dto.LoginResponse;
import com.smartfaculty.entity.User;
import com.smartfaculty.repository.UserRepository;
import com.smartfaculty.security.JwtService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    // =====================================================
    // LOGIN
    // =====================================================

    public LoginResponse login(LoginRequest request) {

        // -------------------------------------------------
        // VALIDATE REQUEST
        // -------------------------------------------------

        if (request == null ||
                request.getUsername() == null ||
                request.getUsername().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Username is required."
            );
        }

        if (request.getPassword() == null ||
                request.getPassword().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Password is required."
            );
        }


        // -------------------------------------------------
        // FIND USER
        // -------------------------------------------------

        String username =
                request.getUsername().trim();

        User user =
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid username or password."
                                )
                        );


        // -------------------------------------------------
        // VALIDATE ACCOUNT STATUS
        // -------------------------------------------------

        if (user.getEnabled() == null) {

            throw new IllegalStateException(
                    "User account status is invalid."
            );
        }

        if (!user.getEnabled()) {

            throw new IllegalStateException(
                    "Your account has been disabled. Please contact the administrator."
            );
        }


        // -------------------------------------------------
        // VALIDATE USER ROLE
        // -------------------------------------------------

        if (user.getRole() == null) {

            throw new IllegalStateException(
                    "User account role is invalid."
            );
        }


        // -------------------------------------------------
        // CHECK PASSWORD
        // -------------------------------------------------

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new IllegalArgumentException(
                    "Invalid username or password."
            );
        }


        // -------------------------------------------------
        // GENERATE JWT
        // -------------------------------------------------

        String token =
                jwtService.generateToken(
                        user.getUsername()
                );


        // -------------------------------------------------
        // RETURN LOGIN RESPONSE
        // -------------------------------------------------

        return new LoginResponse(
                token,
                user.getUsername(),
                user.getRole().name()
        );
    }


    // =====================================================
    // CHANGE PASSWORD
    // =====================================================

    public void changePassword(
            String username,
            ChangePasswordRequest request) {

        // -------------------------------------------------
        // VALIDATE USERNAME
        // -------------------------------------------------

        if (username == null ||
                username.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Username is required."
            );
        }


        // -------------------------------------------------
        // VALIDATE REQUEST
        // -------------------------------------------------

        if (request == null) {

            throw new IllegalArgumentException(
                    "Password data cannot be null."
            );
        }


        // -------------------------------------------------
        // FIND USER
        // -------------------------------------------------

        User user =
                userRepository.findByUsername(
                        username
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found."
                        )
                );


        // -------------------------------------------------
        // CHECK ACCOUNT
        // -------------------------------------------------

        if (user.getEnabled() == null ||
                !user.getEnabled()) {

            throw new IllegalStateException(
                    "Your account has been disabled."
            );
        }


        // -------------------------------------------------
        // CHECK CURRENT PASSWORD
        // -------------------------------------------------

        if (request.getCurrentPassword() == null ||
                request.getCurrentPassword().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Current password is required."
            );
        }

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword())) {

            throw new IllegalStateException(
                    "Current password is incorrect."
            );
        }


        // -------------------------------------------------
        // CHECK NEW PASSWORD
        // -------------------------------------------------

        if (request.getNewPassword() == null ||
                request.getNewPassword()
                        .trim()
                        .isEmpty()) {

            throw new IllegalArgumentException(
                    "New password is required."
            );
        }


        // -------------------------------------------------
        // CHECK CONFIRM PASSWORD
        // -------------------------------------------------

        if (request.getConfirmNewPassword() == null ||
                request.getConfirmNewPassword()
                        .trim()
                        .isEmpty()) {

            throw new IllegalArgumentException(
                    "Please confirm your new password."
            );
        }


        // -------------------------------------------------
        // MATCH PASSWORDS
        // -------------------------------------------------

        if (!request.getNewPassword()
                .equals(
                        request.getConfirmNewPassword()
                )) {

            throw new IllegalArgumentException(
                    "New password and confirmation password do not match."
            );
        }


        // -------------------------------------------------
        // PREVENT SAME PASSWORD
        // -------------------------------------------------

        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPassword())) {

            throw new IllegalArgumentException(
                    "New password must be different from your current password."
            );
        }


        // -------------------------------------------------
        // SAVE NEW PASSWORD
        // -------------------------------------------------

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);
    }
}
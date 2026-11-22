package com.smartfaculty.service.impl;

import com.smartfaculty.entity.User;
import com.smartfaculty.repository.UserRepository;
import com.smartfaculty.service.UserService;
import com.smartfaculty.enums.UserRole;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    // =====================================================
    // CREATE USER
    // =====================================================

    @Override
    public User saveUser(User user) {

        // -------------------------------------------------
        // VALIDATE USER
        // -------------------------------------------------

        if (user == null) {
            throw new IllegalArgumentException(
                    "User data cannot be null."
            );
        }


        // -------------------------------------------------
        // VALIDATE USERNAME
        // -------------------------------------------------

        if (user.getUsername() == null ||
                user.getUsername().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Username is required."
            );
        }


        String username =
                user.getUsername().trim();


        // -------------------------------------------------
        // CHECK DUPLICATE USERNAME
        // -------------------------------------------------

        if (userRepository.existsByUsername(username)) {

            throw new IllegalArgumentException(
                    "Username already exists."
            );
        }

        // -------------------------------------------------
        // VALIDATE USER ROLE
        // -------------------------------------------------

        if (user.getRole() == null) {

            throw new IllegalArgumentException(
                    "User role is required."
            );
        }


        // -------------------------------------------------
        // VALIDATE PASSWORD
        // -------------------------------------------------

        if (user.getPassword() == null ||
                user.getPassword().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Password is required."
            );
        }


        String password =
                user.getPassword().trim();


        if (password.length() < 6) {

            throw new IllegalArgumentException(
                    "Password must contain at least 6 characters."
            );
        }


        // -------------------------------------------------
        // NORMALIZE USERNAME
        // -------------------------------------------------

        user.setUsername(username);


        // -------------------------------------------------
        // ENCRYPT PASSWORD
        // -------------------------------------------------

        user.setPassword(
                passwordEncoder.encode(password)
        );


        return userRepository.save(user);
    }


    // =====================================================
    // GET ALL USERS
    // =====================================================

    @Override
    public List<User> getAllUsers() {

        return userRepository.findAll();
    }


    // =====================================================
    // GET USER BY ID
    // =====================================================

    @Override
    public User getUserById(Long id) {

        return userRepository
                .findById(id)
                .orElse(null);
    }


    // =====================================================
    // UPDATE USER
    // =====================================================

    @Override
    public User updateUser(
            Long id,
            User user) {

        if (id == null) {

            throw new IllegalArgumentException(
                    "User ID is required."
            );
        }


        if (user == null) {

            throw new IllegalArgumentException(
                    "User data cannot be null."
            );
        }


        User existingUser =
                userRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User not found with id: " + id
                                )
                        );


        // -------------------------------------------------
        // VALIDATE USERNAME
        // -------------------------------------------------

        if (user.getUsername() == null ||
                user.getUsername().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Username is required."
            );
        }


        String username =
                user.getUsername().trim();


        // -------------------------------------------------
        // CHECK DUPLICATE USERNAME
        // -------------------------------------------------
        //
        // If the username belongs to another user,
        // reject the update.
        //
        // The current user's own username is allowed.
        // -------------------------------------------------

        if (!username.equals(
                existingUser.getUsername())) {

            if (userRepository.existsByUsername(username)) {

                throw new IllegalArgumentException(
                        "Username already exists."
                );
            }
        }


        existingUser.setUsername(username);


        // -------------------------------------------------
        // VALIDATE ROLE
        // -------------------------------------------------

        if (user.getRole() == null) {

            throw new IllegalArgumentException(
                    "User role is required."
            );
        }


        // -------------------------------------------------
        // PREVENT ADMIN ROLE FROM BEING CHANGED
        // -------------------------------------------------

        if (existingUser.getRole() == UserRole.ADMIN &&
                user.getRole() != UserRole.ADMIN) {

            throw new IllegalStateException(
                    "Administrator account role cannot be changed."
            );
        }


        existingUser.setRole(
                user.getRole()
        );


        // -------------------------------------------------
        // ACCOUNT STATUS
        // -------------------------------------------------

        if (user.getEnabled() != null) {

            // -------------------------------------------------
            // PREVENT ADMIN ACCOUNT FROM BEING DISABLED
            // -------------------------------------------------

            if (existingUser.getRole() == UserRole.ADMIN &&
                    !user.getEnabled()) {

                throw new IllegalStateException(
                        "Administrator accounts cannot be disabled."
                );
            }


            existingUser.setEnabled(
                    user.getEnabled()
            );
        }


        // -------------------------------------------------
        // PASSWORD
        // -------------------------------------------------
        //
        // Password is optional during an update.
        // If no new password is supplied, the existing
        // password remains unchanged.
        //

        if (user.getPassword() != null &&
                !user.getPassword().trim().isEmpty()) {

            String password =
                    user.getPassword().trim();


            if (password.length() < 6) {

                throw new IllegalArgumentException(
                        "Password must contain at least 6 characters."
                );
            }


            existingUser.setPassword(
                    passwordEncoder.encode(password)
            );
        }


        return userRepository.save(
                existingUser
        );
    }


    // =====================================================
    // DELETE USER
    // =====================================================

    @Override
    public void deleteUser(
            Long id,
            Authentication authentication) {

        // -------------------------------------------------
        // CHECK USER ID
        // -------------------------------------------------

        if (id == null) {

            throw new IllegalArgumentException(
                    "User ID is required."
            );
        }


        // -------------------------------------------------
        // CHECK AUTHENTICATION
        // -------------------------------------------------

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "Authentication is required."
            );
        }


        // -------------------------------------------------
        // FIND USER
        // -------------------------------------------------

        User user =
                userRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User not found with id: " + id
                                )
                        );
        // -------------------------------------------------
        // PREVENT ADMIN ACCOUNT FROM BEING DELETED
        // -------------------------------------------------

        if (user.getRole() == UserRole.ADMIN) {

            throw new IllegalStateException(
                    "Administrator accounts cannot be deleted."
            );
        }


         // -------------------------------------------------
         // PREVENT SELF-DELETION
         // -------------------------------------------------

        if (user.getUsername() != null &&
                user.getUsername().equals(
                        authentication.getName()
                )) {

            throw new IllegalStateException(
                    "You cannot delete your own account."
            );
        }


        // -------------------------------------------------
        // DELETE USER
        // -------------------------------------------------

        userRepository.delete(user);
    }
}
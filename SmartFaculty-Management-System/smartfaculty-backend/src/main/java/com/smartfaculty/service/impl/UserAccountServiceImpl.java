package com.smartfaculty.service.impl;

import com.smartfaculty.dto.AdminResetPasswordRequest;
import com.smartfaculty.dto.UserAccountResponse;
import com.smartfaculty.entity.User;
import com.smartfaculty.entity.Lecturer;
import com.smartfaculty.enums.UserRole;
import com.smartfaculty.repository.UserRepository;
import com.smartfaculty.repository.LecturerRepository;
import com.smartfaculty.repository.StudentRepository;
import com.smartfaculty.service.UserAccountService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserAccountServiceImpl
        implements UserAccountService {


    // =====================================================
    // USER REPOSITORY
    // =====================================================

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LecturerRepository lecturerRepository;

    @Autowired
    private StudentRepository studentRepository;


    // =====================================================
    // PASSWORD ENCODER
    // =====================================================

    @Autowired
    private PasswordEncoder passwordEncoder;


    // =====================================================
    // GET ALL USER ACCOUNTS
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<UserAccountResponse> getAllAccounts() {

        // -------------------------------------------------
        // ADMIN AUTHORIZATION
        // -------------------------------------------------

        validateAdminAuthorization(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );


        return userRepository
                .findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }


    // =====================================================
    // GET ACCOUNT BY ID
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public UserAccountResponse getAccountById(
            Long userId) {

        // -------------------------------------------------
        // ADMIN AUTHORIZATION
        // -------------------------------------------------

        validateAdminAuthorization(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );


        // -------------------------------------------------
        // CHECK USER ID
        // -------------------------------------------------

        if (userId == null) {

            throw new IllegalArgumentException(
                    "User ID is required."
            );
        }


        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User account not found."
                                )
                        );


        return convertToResponse(
                user
        );
    }


    // =====================================================
    // ENABLE ACCOUNT
    // =====================================================

    @Override
    @Transactional
    public UserAccountResponse enableAccount(
            Long userId) {

        // -------------------------------------------------
        // ADMIN AUTHORIZATION
        // -------------------------------------------------

        validateAdminAuthorization(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );


        // -------------------------------------------------
        // CHECK USER ID
        // -------------------------------------------------

        if (userId == null) {

            throw new IllegalArgumentException(
                    "User ID is required."
            );
        }


        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User account not found."
                                )
                        );


        // -------------------------------------------------
        // VALIDATE USER ROLE
        // -------------------------------------------------

        if (user.getRole() == null) {

            throw new IllegalStateException(
                    "User account has no valid role."
            );
        }


        // -------------------------------------------------
        // ENABLE ACCOUNT
        // -------------------------------------------------

        user.setEnabled(true);


        User updatedUser =
                userRepository.save(
                        user
                );


        return convertToResponse(
                updatedUser
        );
    }


    // =====================================================
    // DISABLE ACCOUNT
    // =====================================================

    @Override
    @Transactional
    public UserAccountResponse disableAccount(
            Long userId,
            Authentication authentication) {

        // -------------------------------------------------
        // ADMIN AUTHORIZATION
        // -------------------------------------------------

        validateAdminAuthorization(
                authentication
        );


        // -------------------------------------------------
        // CHECK USER ID
        // -------------------------------------------------

        if (userId == null) {

            throw new IllegalArgumentException(
                    "User ID is required."
            );
        }


        // -------------------------------------------------
        // FIND USER ACCOUNT
        // -------------------------------------------------

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User account not found."
                                )
                        );


        // -------------------------------------------------
        // PREVENT ADMIN ACCOUNT FROM BEING DISABLED
        // -------------------------------------------------

        if (user.getRole() == UserRole.ADMIN) {

            throw new IllegalStateException(
                    "Administrator accounts cannot be disabled."
            );
        }


        // -------------------------------------------------
        // PREVENT CURRENT USER FROM DISABLING OWN ACCOUNT
        // -------------------------------------------------

        if (user.getUsername() != null &&
                user.getUsername().equals(
                        authentication.getName()
                )) {

            throw new IllegalStateException(
                    "You cannot disable your own account."
            );
        }


        // -------------------------------------------------
        // DISABLE ACCOUNT
        // -------------------------------------------------

        user.setEnabled(false);


        User updatedUser =
                userRepository.save(
                        user
                );


        // -------------------------------------------------
        // RETURN RESPONSE
        // -------------------------------------------------

        return convertToResponse(
                updatedUser
        );
    }


    // =====================================================
    // ADMIN RESET USER PASSWORD
    // =====================================================

    @Override
    @Transactional
    public UserAccountResponse resetPassword(
            Long userId,
            AdminResetPasswordRequest request,
            Authentication authentication) {

        // -------------------------------------------------
        // ADMIN AUTHORIZATION
        // -------------------------------------------------

        validateAdminAuthorization(
                authentication
        );


        // -------------------------------------------------
        // CHECK USER ID
        // -------------------------------------------------

        if (userId == null) {

            throw new IllegalArgumentException(
                    "User ID is required."
            );
        }


        // -------------------------------------------------
        // CHECK REQUEST
        // -------------------------------------------------

        if (request == null) {

            throw new IllegalArgumentException(
                    "Password data cannot be null."
            );
        }


        // -------------------------------------------------
        // GET NEW PASSWORD
        // -------------------------------------------------

        String newPassword =
                request.getNewPassword();


        // -------------------------------------------------
        // GET CONFIRM PASSWORD
        // -------------------------------------------------

        String confirmNewPassword =
                request.getConfirmNewPassword();


        // -------------------------------------------------
        // CHECK NEW PASSWORD
        // -------------------------------------------------

        if (newPassword == null ||
                newPassword.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "New password is required."
            );
        }


        // -------------------------------------------------
        // CHECK CONFIRM PASSWORD
        // -------------------------------------------------

        if (confirmNewPassword == null ||
                confirmNewPassword.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Please confirm the new password."
            );
        }


        // -------------------------------------------------
        // TRIM PASSWORD VALUES
        // -------------------------------------------------

        newPassword =
                newPassword.trim();

        confirmNewPassword =
                confirmNewPassword.trim();


        // -------------------------------------------------
        // PASSWORD LENGTH
        // -------------------------------------------------

        if (newPassword.length() < 6) {

            throw new IllegalArgumentException(
                    "New password must contain at least 6 characters."
            );
        }


        // -------------------------------------------------
        // CHECK PASSWORD MATCH
        // -------------------------------------------------

        if (!newPassword.equals(
                confirmNewPassword
        )) {

            throw new IllegalArgumentException(
                    "New password and confirmation password do not match."
            );
        }


        // -------------------------------------------------
        // FIND USER
        // -------------------------------------------------

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User account not found."
                                )
                        );


        // -------------------------------------------------
        // PREVENT REUSING CURRENT PASSWORD
        // -------------------------------------------------

        if (passwordEncoder.matches(
                newPassword,
                user.getPassword()
        )) {

            throw new IllegalArgumentException(
                    "New password must be different from the current password."
            );
        }


        // -------------------------------------------------
        // ENCODE PASSWORD
        // -------------------------------------------------

        user.setPassword(
                passwordEncoder.encode(
                        newPassword
                )
        );


        // -------------------------------------------------
        // SAVE USER
        // -------------------------------------------------

        User updatedUser =
                userRepository.save(
                        user
                );


        // -------------------------------------------------
        // RETURN RESPONSE
        // -------------------------------------------------

        return convertToResponse(
                updatedUser
        );
    }


    // =====================================================
    // ADMIN CHANGE USER ROLE
    // =====================================================

// =====================================================
// ADMIN CHANGE USER ROLE
// =====================================================

    @Override
    @Transactional
    public UserAccountResponse updateRole(
            Long userId,
            UserRole role,
            Authentication authentication) {

        // -------------------------------------------------
        // ADMIN AUTHORIZATION
        // -------------------------------------------------

        validateAdminAuthorization(authentication);


        // -------------------------------------------------
        // CHECK USER ID
        // -------------------------------------------------

        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID is required."
            );
        }


        // -------------------------------------------------
        // CHECK ROLE
        // -------------------------------------------------

        if (role == null) {
            throw new IllegalArgumentException(
                    "A valid role is required."
            );
        }


        // -------------------------------------------------
        // FIND USER ACCOUNT
        // -------------------------------------------------

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User account not found."
                                )
                        );


        // -------------------------------------------------
        // PREVENT ADMIN ROLE FROM BEING CHANGED
        // -------------------------------------------------

        if (user.getRole() == UserRole.ADMIN) {
            throw new IllegalStateException(
                    "Administrator roles cannot be changed."
            );
        }


        // -------------------------------------------------
        // PREVENT CURRENT ADMIN FROM CHANGING OWN ROLE
        // -------------------------------------------------

        if (authentication != null &&
                authentication.getName() != null &&
                authentication.getName().equals(
                        user.getUsername()
                )) {

            throw new IllegalStateException(
                    "You cannot change your own role."
            );
        }


        // -------------------------------------------------
        // ADMIN CANNOT PROMOTE USER TO ADMIN
        // -------------------------------------------------

        if (role == UserRole.ADMIN) {
            throw new IllegalArgumentException(
                    "A user account cannot be promoted to ADMIN from Account Management."
            );
        }


        // -------------------------------------------------
        // FIND LECTURER RECORD
        // -------------------------------------------------

        Lecturer lecturer = null;

        if (role == UserRole.HOD ||
                role == UserRole.LECTURER) {

            lecturer =
                    lecturerRepository
                            .findByUser_Username(
                                    user.getUsername()
                            )
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Only a lecturer account can have the HOD or LECTURER role."
                                    )
                            );
        }


        // -------------------------------------------------
        // ONE HOD PER DEPARTMENT
        // -------------------------------------------------

        if (role == UserRole.HOD) {

            if (lecturer.getDepartment() == null ||
                    lecturer.getDepartment().getDepartmentId() == null) {

                throw new IllegalStateException(
                        "The lecturer is not assigned to a valid department."
                );
            }


            Long departmentId =
                    lecturer
                            .getDepartment()
                            .getDepartmentId();


            List<Lecturer> departmentLecturers =
                    lecturerRepository
                            .findByDepartment_DepartmentId(
                                    departmentId
                            );


            boolean anotherHodExists =
                    departmentLecturers
                            .stream()
                            .anyMatch(departmentLecturer ->
                                    departmentLecturer != null &&
                                            departmentLecturer.getUser() != null &&
                                            departmentLecturer.getUser().getRole() == UserRole.HOD &&
                                            !departmentLecturer.getUser().getUserId().equals(
                                                    user.getUserId()
                                            )
                            );


            if (anotherHodExists) {

                String departmentName =
                        lecturer
                                .getDepartment()
                                .getDepartmentName();

                throw new IllegalStateException(
                        "The department "
                                + departmentName
                                + " already has an HOD. Only one HOD is allowed per department."
                );
            }
        }


        // -------------------------------------------------
        // STUDENT ROLE VALIDATION
        // -------------------------------------------------

        if (role == UserRole.STUDENT) {

            boolean hasStudent =
                    studentRepository
                            .findByUser_Username(
                                    user.getUsername()
                            )
                            .isPresent();

            boolean hasLecturer =
                    lecturerRepository
                            .findByUser_Username(
                                    user.getUsername()
                            )
                            .isPresent();


            if (!hasStudent && hasLecturer) {

                throw new IllegalStateException(
                        "This account belongs to a lecturer. Use LECTURER or HOD."
                );
            }


            if (!hasStudent) {

                throw new IllegalStateException(
                        "Only a student account can have the STUDENT role."
                );
            }
        }


        // -------------------------------------------------
        // UPDATE ROLE
        // -------------------------------------------------

        user.setRole(role);


        // -------------------------------------------------
        // SAVE USER ACCOUNT
        // -------------------------------------------------

        User updatedUser =
                userRepository.save(user);


        // -------------------------------------------------
        // RETURN UPDATED ACCOUNT
        // -------------------------------------------------

        return convertToResponse(
                updatedUser
        );
    }


    // =====================================================
    // ADMIN AUTHORIZATION VALIDATION
    // =====================================================

    private void validateAdminAuthorization(
            Authentication authentication) {

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
        // CHECK ADMIN ROLE
        // -------------------------------------------------

        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority != null &&
                                        "ROLE_ADMIN".equals(
                                                authority.getAuthority()
                                        )
                        );


        if (!isAdmin) {

            throw new IllegalStateException(
                    "Administrator authorization is required."
            );
        }
    }


    // =====================================================
    // CONVERT USER ENTITY → RESPONSE DTO
    // =====================================================

    private UserAccountResponse convertToResponse(
            User user) {

        // -------------------------------------------------
        // VALIDATE USER ACCOUNT
        // -------------------------------------------------

        if (user == null) {

            throw new IllegalStateException(
                    "User account data is invalid."
            );
        }


        if (user.getRole() == null) {

            throw new IllegalStateException(
                    "User account has no valid role."
            );
        }


        UserAccountResponse response =
                new UserAccountResponse();


        // -------------------------------------------------
        // USER ID
        // -------------------------------------------------

        response.setUserId(
                user.getUserId()
        );


        // -------------------------------------------------
        // USERNAME
        // -------------------------------------------------

        response.setUsername(
                user.getUsername()
        );


        // -------------------------------------------------
        // ROLE
        // -------------------------------------------------

        response.setRole(
                user.getRole()
        );


        // -------------------------------------------------
        // ENABLED
        // -------------------------------------------------

        response.setEnabled(
                user.getEnabled()
        );


        return response;
    }
}
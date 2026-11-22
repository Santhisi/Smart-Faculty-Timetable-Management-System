package com.smartfaculty.service.impl;

import com.smartfaculty.entity.Department;
import com.smartfaculty.entity.Lecturer;
import com.smartfaculty.entity.User;
import com.smartfaculty.enums.UserRole;
import com.smartfaculty.repository.DepartmentRepository;
import com.smartfaculty.repository.LecturerRepository;
import com.smartfaculty.repository.UserRepository;
import com.smartfaculty.service.LecturerService;
import com.smartfaculty.repository.LecturerAllocationRepository;
import com.smartfaculty.repository.TimetableEntryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LecturerServiceImpl
        implements LecturerService {

    @Autowired
    private LecturerRepository lecturerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LecturerAllocationRepository lecturerAllocationRepository;

    @Autowired
    private TimetableEntryRepository timetableEntryRepository;


    // =====================================================
    // CREATE LECTURER + USER ACCOUNT
    // ADMIN → ANY DEPARTMENT
    // HOD   → OWN DEPARTMENT ONLY
    // =====================================================

    @Override
    @Transactional
    public Lecturer saveLecturer(
            Lecturer lecturer,
            Authentication authentication) {

        validateAuthentication(
                authentication
        );

        // Admin can create lecturers in any department.
        // HOD can create lecturers only in the HOD's own department.
        if (!hasRole(authentication, "ROLE_ADMIN") &&
                !hasRole(authentication, "ROLE_HOD")) {

            throw new IllegalStateException(
                    "You are not authorized to create lecturers."
            );
        }


        if (lecturer == null) {

            throw new IllegalArgumentException(
                    "Lecturer data cannot be null."
            );
        }


        validateLecturer(
                lecturer
        );


        String lecturerCode =
                lecturer.getLecturerCode().trim();

        String email =
                lecturer.getEmail().trim();


        // =================================================
        // CHECK LECTURER CODE
        // =================================================

        if (lecturerRepository
                .findAll()
                .stream()
                .anyMatch(existing ->
                        existing != null &&
                                existing.getLecturerCode() != null &&
                                existing.getLecturerCode()
                                        .equalsIgnoreCase(
                                                lecturerCode
                                        ))) {

            throw new IllegalStateException(
                    "A lecturer with this lecturer code already exists."
            );
        }


        // =================================================
        // CHECK EMAIL
        // =================================================

        if (lecturerRepository
                .findAll()
                .stream()
                .anyMatch(existing ->
                        existing != null &&
                                existing.getEmail() != null &&
                                existing.getEmail()
                                        .equalsIgnoreCase(
                                                email
                                        ))) {

            throw new IllegalStateException(
                    "A lecturer with this email already exists."
            );
        }


        // =================================================
        // CHECK USERNAME
        // =================================================

        if (userRepository.existsByUsername(
                lecturerCode
        )) {

            throw new IllegalStateException(
                    "A user account with this lecturer code already exists."
            );
        }


        // =================================================
        // CREATE USER ACCOUNT
        // =================================================

        User user =
                new User();

        user.setUsername(
                lecturerCode
        );


        if (lecturer.getUser() == null ||
                lecturer.getUser().getPassword() == null ||
                lecturer.getUser()
                        .getPassword()
                        .trim()
                        .isEmpty()) {

            throw new IllegalArgumentException(
                    "Initial password is required when creating a lecturer."
            );
        }


        user.setPassword(
                passwordEncoder.encode(
                        lecturer.getUser()
                                .getPassword()
                )
        );


        user.setRole(
                UserRole.LECTURER
        );


        user.setEnabled(
                true
        );


        User savedUser =
                userRepository.save(
                        user
                );


        // =================================================
        // LOAD DEPARTMENT
        // =================================================

        Department department =
                lecturer.getDepartment();


        if (department == null ||
                department.getDepartmentId() == null) {

            throw new IllegalArgumentException(
                    "Department is required."
            );
        }


        Department savedDepartment =
                departmentRepository
                        .findById(
                                department.getDepartmentId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Department not found."
                                )
                        );


        // =================================================
        // HOD DEPARTMENT AUTHORIZATION
        // =================================================

        if (hasRole(
                authentication,
                "ROLE_HOD"
        )) {

            Long hodDepartmentId =
                    getAuthenticatedHODDepartmentId(
                            authentication
                    );

            if (!hodDepartmentId.equals(
                    savedDepartment.getDepartmentId()
            )) {

                throw new IllegalStateException(
                        "You are not authorized to create a lecturer in another department."
                );
            }
        }


        // =================================================
        // CREATE LECTURER
        // =================================================

        lecturer.setLecturerCode(
                lecturerCode
        );

        lecturer.setLecturerName(
                lecturer.getLecturerName()
                        .trim()
        );

        lecturer.setEmail(
                email
        );

        lecturer.setDepartment(
                savedDepartment
        );

        lecturer.setUser(
                savedUser
        );


        return lecturerRepository.save(
                lecturer
        );
    }


    // =====================================================
    // GET ALL LECTURERS
    // ADMIN → ALL
    // HOD   → OWN DEPARTMENT
    // =====================================================

    @Override
    public List<Lecturer> getAllLecturers(
            Authentication authentication) {

        validateAuthentication(
                authentication
        );


        // =================================================
        // ADMIN
        // =================================================

        if (hasRole(
                authentication,
                "ROLE_ADMIN"
        )) {

            return lecturerRepository.findAll();
        }


        // =================================================
        // HOD
        // =================================================

        if (hasRole(
                authentication,
                "ROLE_HOD"
        )) {

            Long hodDepartmentId =
                    getAuthenticatedHODDepartmentId(
                            authentication
                    );


            return lecturerRepository
                    .findAll()
                    .stream()
                    .filter(lecturer ->
                            isLecturerInDepartment(
                                    lecturer,
                                    hodDepartmentId
                            )
                    )
                    .collect(Collectors.toList());
        }


        throw new IllegalStateException(
                "You are not authorized to access lecturer management."
        );
    }


    // =====================================================
    // GET LECTURER BY ID
    // ADMIN → ANY
    // HOD   → OWN DEPARTMENT
    // LECTURER → OWN PROFILE
    // =====================================================

    @Override
    public Lecturer getLecturerById(
            Long id,
            Authentication authentication) {

        validateAuthentication(
                authentication
        );


        if (id == null) {

            throw new IllegalArgumentException(
                    "Lecturer ID is required."
            );
        }


        Lecturer lecturer =
                lecturerRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Lecturer not found."
                                )
                        );


        // =================================================
        // ADMIN
        // =================================================

        if (hasRole(
                authentication,
                "ROLE_ADMIN"
        )) {

            return lecturer;
        }


        // =================================================
        // HOD
        // =================================================

        if (hasRole(
                authentication,
                "ROLE_HOD"
        )) {

            Long hodDepartmentId =
                    getAuthenticatedHODDepartmentId(
                            authentication
                    );


            validateHODLecturerAccess(
                    lecturer,
                    hodDepartmentId
            );


            return lecturer;
        }


        // =================================================
        // LECTURER
        // =================================================

        if (hasRole(
                authentication,
                "ROLE_LECTURER"
        )) {

            String username =
                    authentication.getName();


            if (username == null ||
                    username.trim().isEmpty()) {

                throw new IllegalStateException(
                        "Authenticated username is invalid."
                );
            }


            Lecturer authenticatedLecturer =
                    lecturerRepository
                            .findByUser_Username(
                                    username.trim()
                            )
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Lecturer profile not found."
                                    )
                            );


            if (authenticatedLecturer
                    .getLecturerId() == null) {

                throw new IllegalStateException(
                        "Lecturer profile ID is invalid."
                );
            }


            if (!authenticatedLecturer
                    .getLecturerId()
                    .equals(id)) {

                throw new IllegalStateException(
                        "You are not authorized to access this lecturer profile."
                );
            }


            return lecturer;
        }


        throw new IllegalStateException(
                "You are not authorized to access lecturer profiles."
        );
    }


    // =====================================================
    // UPDATE LECTURER
    // ADMIN → ANY
    // HOD   → OWN DEPARTMENT
    // =====================================================

    @Override
    @Transactional
    public Lecturer updateLecturer(
            Long id,
            Lecturer lecturer,
            Authentication authentication) {

        validateAuthentication(
                authentication
        );


        if (id == null) {

            throw new IllegalArgumentException(
                    "Lecturer ID is required."
            );
        }


        if (lecturer == null) {

            throw new IllegalArgumentException(
                    "Lecturer data cannot be null."
            );
        }


        Lecturer existing =
                lecturerRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Lecturer not found."
                                )
                        );


        // =================================================
        // HOD DEPARTMENT AUTHORIZATION
        // =================================================

        Long hodDepartmentId =
                null;


        if (hasRole(
                authentication,
                "ROLE_HOD"
        )) {

            hodDepartmentId =
                    getAuthenticatedHODDepartmentId(
                            authentication
                    );


            validateHODLecturerAccess(
                    existing,
                    hodDepartmentId
            );
        }


        // =================================================
        // ONLY ADMIN OR HOD
        // =================================================

        if (!hasRole(
                authentication,
                "ROLE_ADMIN"
        ) &&
                !hasRole(
                        authentication,
                        "ROLE_HOD"
                )) {

            throw new IllegalStateException(
                    "You are not authorized to update lecturers."
            );
        }


        validateLecturer(
                lecturer
        );


        String newCode =
                lecturer.getLecturerCode().trim();

        String newEmail =
                lecturer.getEmail().trim();


        // =================================================
        // CHECK CODE CHANGE
        // =================================================

        if (!newCode.equalsIgnoreCase(
                existing.getLecturerCode()
        )) {

            boolean duplicateCode =
                    lecturerRepository
                            .findAll()
                            .stream()
                            .anyMatch(other ->
                                    other != null &&
                                            other.getLecturerId() != null &&
                                            !other.getLecturerId()
                                                    .equals(id) &&
                                            other.getLecturerCode() != null &&
                                            other.getLecturerCode()
                                                    .equalsIgnoreCase(
                                                            newCode
                                                    )
                            );


            if (duplicateCode) {

                throw new IllegalStateException(
                        "Another lecturer already uses this lecturer code."
                );
            }


            User linkedUser =
                    existing.getUser();


            if (linkedUser != null &&
                    userRepository.existsByUsername(
                            newCode
                    )) {

                throw new IllegalStateException(
                        "Another user account already uses this lecturer code."
                );
            }
        }


        // =================================================
        // CHECK EMAIL CHANGE
        // =================================================

        if (!newEmail.equalsIgnoreCase(
                existing.getEmail()
        )) {

            boolean duplicateEmail =
                    lecturerRepository
                            .findAll()
                            .stream()
                            .anyMatch(other ->
                                    other != null &&
                                            other.getLecturerId() != null &&
                                            !other.getLecturerId()
                                                    .equals(id) &&
                                            other.getEmail() != null &&
                                            other.getEmail()
                                                    .equalsIgnoreCase(
                                                            newEmail
                                                    )
                            );


            if (duplicateEmail) {

                throw new IllegalStateException(
                        "Another lecturer already uses this email."
                );
            }
        }


        // =================================================
        // LOAD REQUESTED DEPARTMENT
        // =================================================

        Department requestedDepartment =
                lecturer.getDepartment();


        if (requestedDepartment == null ||
                requestedDepartment.getDepartmentId() == null) {

            throw new IllegalArgumentException(
                    "Department is required."
            );
        }


        Department savedDepartment =
                departmentRepository
                        .findById(
                                requestedDepartment
                                        .getDepartmentId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Department not found."
                                )
                        );


        // =================================================
        // HOD CANNOT MOVE LECTURER OUTSIDE HIS/HER
        // DEPARTMENT
        // =================================================

        if (hasRole(
                authentication,
                "ROLE_HOD"
        )) {

            if (!hodDepartmentId.equals(
                    savedDepartment.getDepartmentId()
            )) {

                throw new IllegalStateException(
                        "You are not authorized to assign a lecturer to another department."
                );
            }
        }


        // =================================================
        // UPDATE LECTURER
        // =================================================

        existing.setLecturerCode(
                newCode
        );

        existing.setLecturerName(
                lecturer.getLecturerName()
                        .trim()
        );

        existing.setEmail(
                newEmail
        );

        existing.setDepartment(
                savedDepartment
        );


        // =================================================
        // UPDATE LINKED USER
        // =================================================

        User user =
                existing.getUser();


        if (user != null) {

            user.setUsername(
                    newCode
            );


            if (lecturer.getUser() != null &&
                    lecturer.getUser().getPassword() != null &&
                    !lecturer.getUser()
                            .getPassword()
                            .trim()
                            .isEmpty()) {

                user.setPassword(
                        passwordEncoder.encode(
                                lecturer.getUser()
                                        .getPassword()
                        ));
            }


            userRepository.save(
                    user
            );
        }


        return lecturerRepository.save(
                existing
        );
    }


    // =====================================================
    // DELETE LECTURER
    // ADMIN → ANY
    // HOD   → OWN DEPARTMENT
    // =====================================================

    @Override
    @Transactional
    public void deleteLecturer(
            Long id,
            Authentication authentication) {

        validateAuthentication(
                authentication
        );


        if (id == null) {

            throw new IllegalArgumentException(
                    "Lecturer ID is required."
            );
        }


        Lecturer lecturer =
                lecturerRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Lecturer not found."
                                )
                        );


        // =================================================
        // HOD DEPARTMENT AUTHORIZATION
        // =================================================

        if (hasRole(
                authentication,
                "ROLE_HOD"
        )) {

            Long hodDepartmentId =
                    getAuthenticatedHODDepartmentId(
                            authentication
                    );


            validateHODLecturerAccess(
                    lecturer,
                    hodDepartmentId
            );
        }


        // =================================================
        // ONLY ADMIN OR HOD
        // =================================================

        if (!hasRole(
                authentication,
                "ROLE_ADMIN"
        ) &&
                !hasRole(
                        authentication,
                        "ROLE_HOD"
                )) {

            throw new IllegalStateException(
                    "You are not authorized to delete lecturers."
            );
        }


        User user = lecturer.getUser();

// Check existing lecturer allocations
        boolean hasAllocations =
                lecturerAllocationRepository
                        .existsByLecturerLecturerId(id);

// Check existing timetable entries
        boolean hasTimetableEntries =
                timetableEntryRepository
                        .existsByLecturerLecturerId(id);

        if (hasAllocations || hasTimetableEntries) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete this lecturer because existing lecturer allocations or timetable entries are linked to this lecturer. Please manage those records first."
            );
        }


        // =================================================
        // DELETE LECTURER
        // =================================================

        lecturerRepository.delete(
                lecturer
        );


        // =================================================
        // DELETE USER ACCOUNT
        // =================================================

        if (user != null) {

            userRepository.delete(
                    user
            );
        }
    }


    // =====================================================
    // AUTHENTICATION VALIDATION
    // =====================================================

    private void validateAuthentication(
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "Authentication is required."
            );
        }
    }


    // =====================================================
    // ADMIN AUTHORIZATION
    // =====================================================

    private void validateAdminAuthorization(
            Authentication authentication) {

        validateAuthentication(
                authentication
        );


        if (!hasRole(
                authentication,
                "ROLE_ADMIN"
        )) {

            throw new IllegalStateException(
                    "Administrator authorization is required."
            );
        }
    }


    // =====================================================
    // ROLE CHECK
    // =====================================================

    private boolean hasRole(
            Authentication authentication,
            String role) {

        if (authentication == null ||
                role == null) {

            return false;
        }


        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority != null &&
                                role.equals(
                                        authority.getAuthority()
                                )
                );
    }


    // =====================================================
    // GET AUTHENTICATED HOD DEPARTMENT
    // =====================================================

    private Long getAuthenticatedHODDepartmentId(
            Authentication authentication) {

        validateAuthentication(
                authentication
        );


        if (!hasRole(
                authentication,
                "ROLE_HOD"
        )) {

            throw new IllegalStateException(
                    "HOD authorization is required."
            );
        }


        String username =
                authentication
                        .getName();


        if (username == null ||
                username.trim().isEmpty()) {

            throw new IllegalStateException(
                    "Authenticated username is invalid."
            );
        }


        Lecturer hod =
                lecturerRepository
                        .findByUser_Username(
                                username.trim()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "HOD lecturer profile not found."
                                )
                        );


        if (hod.getLecturerId() == null) {

            throw new IllegalStateException(
                    "HOD lecturer profile ID is invalid."
            );
        }


        Department department =
                hod.getDepartment();


        if (department == null ||
                department.getDepartmentId() == null) {

            throw new IllegalStateException(
                    "HOD department information is invalid."
            );
        }


        return department.getDepartmentId();
    }


    // =====================================================
    // CHECK LECTURER DEPARTMENT
    // =====================================================

    private boolean isLecturerInDepartment(
            Lecturer lecturer,
            Long departmentId) {

        if (lecturer == null ||
                departmentId == null) {

            return false;
        }


        if (lecturer.getDepartment() == null ||
                lecturer.getDepartment()
                        .getDepartmentId() == null) {

            return false;
        }


        return departmentId.equals(
                lecturer.getDepartment()
                        .getDepartmentId()
        );
    }


    // =====================================================
    // VALIDATE HOD LECTURER ACCESS
    // =====================================================

    private void validateHODLecturerAccess(
            Lecturer lecturer,
            Long hodDepartmentId) {

        if (lecturer == null) {

            throw new IllegalStateException(
                    "Lecturer information is invalid."
            );
        }


        if (hodDepartmentId == null) {

            throw new IllegalStateException(
                    "HOD department information is invalid."
            );
        }


        if (!isLecturerInDepartment(
                lecturer,
                hodDepartmentId
        )) {

            throw new IllegalStateException(
                    "You are not authorized to access lecturers outside your department."
            );
        }
    }


    // =====================================================
    // VALIDATION
    // =====================================================

    private void validateLecturer(
            Lecturer lecturer) {

        if (lecturer.getLecturerCode() == null ||
                lecturer.getLecturerCode()
                        .trim()
                        .isEmpty()) {

            throw new IllegalArgumentException(
                    "Lecturer code is required."
            );
        }


        if (lecturer.getLecturerName() == null ||
                lecturer.getLecturerName()
                        .trim()
                        .isEmpty()) {

            throw new IllegalArgumentException(
                    "Lecturer name is required."
            );
        }


        if (lecturer.getEmail() == null ||
                lecturer.getEmail()
                        .trim()
                        .isEmpty()) {

            throw new IllegalArgumentException(
                    "Lecturer email is required."
            );
        }


        if (lecturer.getDepartment() == null ||
                lecturer.getDepartment()
                        .getDepartmentId() == null) {

            throw new IllegalArgumentException(
                    "Department is required."
            );
        }
    }
}
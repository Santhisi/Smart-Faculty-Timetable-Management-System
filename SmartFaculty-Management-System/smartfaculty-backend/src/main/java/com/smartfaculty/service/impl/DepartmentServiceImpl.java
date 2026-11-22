package com.smartfaculty.service.impl;

import com.smartfaculty.entity.Department;
import com.smartfaculty.repository.DepartmentRepository;
import com.smartfaculty.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;


    // =====================================================
    // CREATE
    // ADMIN ONLY
    // =====================================================

    @Override
    @Transactional
    public Department saveDepartment(
            Department department,
            Authentication authentication) {

        validateAdmin(authentication);

        if (department == null) {
            throw new IllegalArgumentException(
                    "Department data cannot be null."
            );
        }

        if (department.getDepartmentCode() == null ||
                department.getDepartmentCode().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Department code is required."
            );
        }

        if (department.getDepartmentName() == null ||
                department.getDepartmentName().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Department name is required."
            );
        }

        department.setDepartmentCode(
                department.getDepartmentCode().trim()
        );

        department.setDepartmentName(
                department.getDepartmentName().trim()
        );

        boolean duplicateCode =
                departmentRepository.findAll()
                        .stream()
                        .anyMatch(existing ->
                                existing.getDepartmentCode() != null &&
                                        existing.getDepartmentCode()
                                                .equalsIgnoreCase(
                                                        department.getDepartmentCode()
                                                )
                        );

        if (duplicateCode) {
            throw new IllegalStateException(
                    "Another department already uses this department code."
            );
        }

        return departmentRepository.save(department);
    }


    // =====================================================
    // GET ALL
    // AUTHENTICATED USERS
    // =====================================================

    @Override
    public List<Department> getAllDepartments(
            Authentication authentication) {

        validateAuthenticated(authentication);

        return departmentRepository.findAll();
    }


    // =====================================================
    // GET BY ID
    // AUTHENTICATED USERS
    // =====================================================

    @Override
    public Department getDepartmentById(
            Long id,
            Authentication authentication) {

        validateAuthenticated(authentication);

        if (id == null) {
            throw new IllegalArgumentException(
                    "Department ID is required."
            );
        }

        return departmentRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Department not found with id: " + id
                        )
                );
    }


    // =====================================================
    // UPDATE
    // ADMIN ONLY
    // =====================================================

    @Override
    @Transactional
    public Department updateDepartment(
            Long id,
            Department department,
            Authentication authentication) {

        validateAdmin(authentication);

        if (id == null) {
            throw new IllegalArgumentException(
                    "Department ID is required."
            );
        }

        if (department == null) {
            throw new IllegalArgumentException(
                    "Department data cannot be null."
            );
        }

        Department existingDepartment =
                departmentRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Department not found with id: " + id
                                )
                        );


        // -------------------------------------------------
        // VALIDATE DEPARTMENT NAME
        // -------------------------------------------------

        if (department.getDepartmentName() == null ||
                department.getDepartmentName().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Department name is required."
            );
        }


        // -------------------------------------------------
        // UPDATE CODE IF PROVIDED
        // -------------------------------------------------

        if (department.getDepartmentCode() != null &&
                !department.getDepartmentCode().trim().isEmpty()) {

            String newCode =
                    department.getDepartmentCode().trim();

            boolean duplicateCode =
                    departmentRepository.findAll()
                            .stream()
                            .anyMatch(other ->
                                    !other.getDepartmentId()
                                            .equals(id) &&
                                            other.getDepartmentCode() != null &&
                                            other.getDepartmentCode()
                                                    .equalsIgnoreCase(newCode)
                            );

            if (duplicateCode) {
                throw new IllegalStateException(
                        "Another department already uses this department code."
                );
            }

            existingDepartment.setDepartmentCode(newCode);
        }


        existingDepartment.setDepartmentName(
                department.getDepartmentName().trim()
        );

        return departmentRepository.save(existingDepartment);
    }


    // =====================================================
    // DELETE
    // ADMIN ONLY
    // =====================================================

    @Override
    @Transactional
    public void deleteDepartment(
            Long id,
            Authentication authentication) {

        validateAdmin(authentication);

        if (id == null) {
            throw new IllegalArgumentException(
                    "Department ID is required."
            );
        }

        Department existingDepartment =
                departmentRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Department not found with id: " + id
                                )
                        );

        departmentRepository.delete(existingDepartment);
    }


    // =====================================================
    // AUTHORIZATION HELPERS
    // =====================================================

    private void validateAuthenticated(
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new SecurityException(
                    "Authentication is required."
            );
        }
    }


    private void validateAdmin(
            Authentication authentication) {

        validateAuthenticated(authentication);

        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                "ROLE_ADMIN".equals(
                                        authority.getAuthority()
                                )
                        );

        if (!isAdmin) {
            throw new SecurityException(
                    "Admin authorization is required."
            );
        }
    }
}

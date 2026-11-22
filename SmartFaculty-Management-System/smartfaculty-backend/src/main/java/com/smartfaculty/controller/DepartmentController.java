package com.smartfaculty.controller;

import com.smartfaculty.entity.Department;
import com.smartfaculty.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin("*")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;


    // =====================================================
    // CREATE DEPARTMENT
    // ADMIN ONLY
    // =====================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Department saveDepartment(
            @RequestBody Department department,
            Authentication authentication) {

        return departmentService.saveDepartment(
                department,
                authentication
        );
    }


    // =====================================================
    // GET ALL DEPARTMENTS
    // AUTHENTICATED USERS
    // =====================================================

    @GetMapping
    public List<Department> getAllDepartments(
            Authentication authentication) {

        return departmentService.getAllDepartments(
                authentication
        );
    }


    // =====================================================
    // GET DEPARTMENT BY ID
    // AUTHENTICATED USERS
    // =====================================================

    @GetMapping("/{id}")
    public Department getDepartmentById(
            @PathVariable Long id,
            Authentication authentication) {

        return departmentService.getDepartmentById(
                id,
                authentication
        );
    }


    // =====================================================
    // UPDATE DEPARTMENT
    // ADMIN ONLY
    // =====================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Department updateDepartment(
            @PathVariable Long id,
            @RequestBody Department department,
            Authentication authentication) {

        return departmentService.updateDepartment(
                id,
                department,
                authentication
        );
    }


    // =====================================================
    // DELETE DEPARTMENT
    // ADMIN ONLY
    // =====================================================

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public String deleteDepartment(
            @PathVariable Long id,
            Authentication authentication) {

        departmentService.deleteDepartment(
                id,
                authentication
        );

        return "Department deleted successfully.";
    }
}

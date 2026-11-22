package com.smartfaculty.service;

import com.smartfaculty.entity.Department;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface DepartmentService {

    Department saveDepartment(
            Department department,
            Authentication authentication
    );

    List<Department> getAllDepartments(
            Authentication authentication
    );

    Department getDepartmentById(
            Long id,
            Authentication authentication
    );

    Department updateDepartment(
            Long id,
            Department department,
            Authentication authentication
    );

    void deleteDepartment(
            Long id,
            Authentication authentication
    );
}


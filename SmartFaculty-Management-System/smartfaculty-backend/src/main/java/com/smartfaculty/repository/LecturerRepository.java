package com.smartfaculty.repository;

import com.smartfaculty.entity.Department;
import com.smartfaculty.entity.Lecturer;
import com.smartfaculty.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LecturerRepository extends JpaRepository<Lecturer, Long> {

    // =========================================================
    // FIND LECTURER BY USER
    // =========================================================

    Optional<Lecturer> findByUser(User user);


    // =========================================================
    // FIND LECTURER BY USERNAME
    // =========================================================

    Optional<Lecturer> findByUser_Username(String username);


    // =========================================================
    // GET ALL LECTURERS OF A DEPARTMENT
    // =========================================================

    List<Lecturer> findByDepartment_DepartmentId(
            Long departmentId
    );


    // =========================================================
    // CHECK WHETHER LECTURER BELONGS TO DEPARTMENT
    // =========================================================

    boolean existsByLecturerIdAndDepartment_DepartmentId(
            Long lecturerId,
            Long departmentId
    );
}
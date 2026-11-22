package com.smartfaculty.service;

import com.smartfaculty.entity.LecturerAllocation;

import org.springframework.security.core.Authentication;

import java.util.List;

public interface LecturerAllocationService {

    List<LecturerAllocation> getAllAllocations();

    LecturerAllocation getAllocationById(Long id);

    List<LecturerAllocation> getAllocationsByLecturerId(
            Long lecturerId
    );

    List<LecturerAllocation> getAllocationsByDepartmentId(
            Long departmentId
    );

    LecturerAllocation saveAllocation(
            LecturerAllocation allocation,
            Authentication authentication
    );

    LecturerAllocation updateAllocation(
            Long id,
            LecturerAllocation allocation,
            Authentication authentication
    );

    void deleteAllocation(
            Long id,
            Authentication authentication
    );
}
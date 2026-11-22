package com.smartfaculty.service.impl;

import com.smartfaculty.entity.Department;
import com.smartfaculty.entity.Lecturer;
import com.smartfaculty.entity.LecturerAllocation;
import com.smartfaculty.entity.Subject;
import com.smartfaculty.enums.UserRole;
import com.smartfaculty.repository.LecturerAllocationRepository;
import com.smartfaculty.repository.LecturerRepository;
import com.smartfaculty.repository.SubjectRepository;
import com.smartfaculty.service.LecturerAllocationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LecturerAllocationServiceImpl
        implements LecturerAllocationService {

    @Autowired
    private LecturerAllocationRepository
            lecturerAllocationRepository;

    @Autowired
    private LecturerRepository
            lecturerRepository;

    @Autowired
    private SubjectRepository
            subjectRepository;


    // =========================================================
    // GET ALL
    // =========================================================

    @Override
    public List<LecturerAllocation>
    getAllAllocations() {

        return lecturerAllocationRepository.findAll();
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    @Override
    public LecturerAllocation
    getAllocationById(Long id) {

        return lecturerAllocationRepository
                .findById(id)
                .orElse(null);
    }


    // =========================================================
    // GET BY LECTURER
    // =========================================================

    @Override
    public List<LecturerAllocation>
    getAllocationsByLecturerId(
            Long lecturerId) {

        return lecturerAllocationRepository
                .findByLecturer_LecturerId(
                        lecturerId
                );
    }


    // =========================================================
    // GET BY DEPARTMENT
    // =========================================================

    @Override
    public List<LecturerAllocation>
    getAllocationsByDepartmentId(
            Long departmentId) {

        return lecturerAllocationRepository
                .findByLecturer_Department_DepartmentId(
                        departmentId
                );
    }


    // =========================================================
    // CREATE ALLOCATION
    // =========================================================

    @Override
    public LecturerAllocation saveAllocation(
            LecturerAllocation allocation,
            Authentication authentication) {

        validateAuthentication(authentication);

        validateAdminOrHOD(authentication);

        validateAllocation(allocation);

        Long lecturerId =
                allocation
                        .getLecturer()
                        .getLecturerId();

        Long subjectId =
                allocation
                        .getSubject()
                        .getSubjectId();


        // -----------------------------------------------------
        // LOAD REAL DATABASE ENTITIES
        // -----------------------------------------------------

        Lecturer lecturer =
                lecturerRepository
                        .findById(lecturerId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Lecturer not found."
                                )
                        );


        Subject subject =
                subjectRepository
                        .findById(subjectId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Subject not found."
                                )
                        );


        // -----------------------------------------------------
        // CHECK DEPARTMENT
        // -----------------------------------------------------

        Department lecturerDepartment =
                lecturer.getDepartment();

        Department subjectDepartment =
                subject.getDepartment();


        if (lecturerDepartment == null) {

            throw new IllegalArgumentException(
                    "Lecturer does not belong to a department."
            );
        }


        if (subjectDepartment == null) {

            throw new IllegalArgumentException(
                    "Subject does not belong to a department."
            );
        }


        if (lecturerDepartment.getDepartmentId() == null ||
                subjectDepartment.getDepartmentId() == null) {

            throw new IllegalArgumentException(
                    "Lecturer and subject must have valid departments."
            );
        }


        if (!lecturerDepartment
                .getDepartmentId()
                .equals(
                        subjectDepartment
                                .getDepartmentId()
                )) {

            throw new IllegalArgumentException(
                    "Lecturer and subject must belong to "
                            + "the same department."
            );
        }


        // -----------------------------------------------------
        // HOD DEPARTMENT AUTHORIZATION
        // -----------------------------------------------------

        validateHODDepartmentAccess(
                authentication,
                lecturer,
                subject
        );


        // -----------------------------------------------------
        // CHECK DUPLICATE
        // -----------------------------------------------------

        boolean exists =
                lecturerAllocationRepository
                        .existsByLecturer_LecturerIdAndSubject_SubjectIdAndAcademicYearAndSemester(
                                lecturerId,
                                subjectId,
                                allocation.getAcademicYear(),
                                allocation.getSemester()
                        );


        if (exists) {

            throw new IllegalStateException(
                    "This lecturer is already allocated "
                            + "to this subject for the selected "
                            + "academic year and semester."
            );
        }


        // -----------------------------------------------------
        // USE REAL ENTITIES
        // -----------------------------------------------------

        allocation.setLecturer(lecturer);
        allocation.setSubject(subject);


        return lecturerAllocationRepository
                .save(allocation);
    }


    // =========================================================
    // UPDATE ALLOCATION
    // =========================================================

    @Override
    public LecturerAllocation updateAllocation(
            Long id,
            LecturerAllocation allocation,
            Authentication authentication) {

        validateAuthentication(authentication);

        validateAdminOrHOD(authentication);

        if (id == null) {

            throw new IllegalArgumentException(
                    "Allocation ID is required."
            );
        }


        LecturerAllocation existing =
                lecturerAllocationRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Lecturer allocation not found."
                                )
                        );


        // -----------------------------------------------------
        // CHECK EXISTING ALLOCATION OWNERSHIP
        // -----------------------------------------------------

        validateExistingAllocationAccess(
                authentication,
                existing
        );


        validateAllocation(allocation);


        Long lecturerId =
                allocation
                        .getLecturer()
                        .getLecturerId();

        Long subjectId =
                allocation
                        .getSubject()
                        .getSubjectId();


        Lecturer lecturer =
                lecturerRepository
                        .findById(lecturerId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Lecturer not found."
                                )
                        );


        Subject subject =
                subjectRepository
                        .findById(subjectId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Subject not found."
                                )
                        );


        // -----------------------------------------------------
        // CHECK DEPARTMENT
        // -----------------------------------------------------

        if (lecturer.getDepartment() == null ||
                subject.getDepartment() == null) {

            throw new IllegalArgumentException(
                    "Lecturer and subject must belong "
                            + "to a department."
            );
        }


        if (lecturer.getDepartment().getDepartmentId() == null ||
                subject.getDepartment().getDepartmentId() == null) {

            throw new IllegalArgumentException(
                    "Lecturer and subject must have valid departments."
            );
        }


        if (!lecturer
                .getDepartment()
                .getDepartmentId()
                .equals(
                        subject
                                .getDepartment()
                                .getDepartmentId()
                )) {

            throw new IllegalArgumentException(
                    "Lecturer and subject must belong "
                            + "to the same department."
            );
        }


        // -----------------------------------------------------
        // HOD DEPARTMENT AUTHORIZATION
        // -----------------------------------------------------

        validateHODDepartmentAccess(
                authentication,
                lecturer,
                subject
        );


        // -----------------------------------------------------
        // CHECK DUPLICATE
        // -----------------------------------------------------

        boolean exists =
                lecturerAllocationRepository
                        .existsByLecturer_LecturerIdAndSubject_SubjectIdAndAcademicYearAndSemesterAndAllocationIdNot(
                                lecturerId,
                                subjectId,
                                allocation.getAcademicYear(),
                                allocation.getSemester(),
                                id
                        );


        if (exists) {

            throw new IllegalStateException(
                    "Another allocation already exists "
                            + "with the same lecturer, subject, "
                            + "academic year and semester."
            );
        }


        // -----------------------------------------------------
        // UPDATE
        // -----------------------------------------------------

        existing.setLecturer(lecturer);

        existing.setSubject(subject);

        existing.setAcademicYear(
                allocation.getAcademicYear()
        );

        existing.setSemester(
                allocation.getSemester()
        );


        return lecturerAllocationRepository
                .save(existing);
    }


    // =========================================================
    // DELETE
    // =========================================================

    @Override
    public void deleteAllocation(
            Long id,
            Authentication authentication) {

        validateAuthentication(authentication);

        validateAdminOrHOD(authentication);

        if (id == null) {

            throw new IllegalArgumentException(
                    "Allocation ID is required."
            );
        }


        LecturerAllocation allocation =
                lecturerAllocationRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Lecturer allocation not found."
                                )
                        );


        // -----------------------------------------------------
        // CHECK OWNERSHIP
        // -----------------------------------------------------

        validateExistingAllocationAccess(
                authentication,
                allocation
        );


        lecturerAllocationRepository
                .delete(allocation);
    }


    // =========================================================
    // AUTHENTICATION VALIDATION
    // =========================================================

    private void validateAuthentication(
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "Authentication is required."
            );
        }
    }


    // =========================================================
    // ADMIN / HOD VALIDATION
    // =========================================================

    private void validateAdminOrHOD(
            Authentication authentication) {

        boolean hasAdminRole =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority != null &&
                                        "ROLE_ADMIN".equals(
                                                authority.getAuthority()
                                        )
                        );


        boolean hasHODRole =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority != null &&
                                        "ROLE_HOD".equals(
                                                authority.getAuthority()
                                        )
                        );


        if (!hasAdminRole && !hasHODRole) {

            throw new IllegalStateException(
                    "Only administrators and HODs can modify "
                            + "lecturer allocations."
            );
        }
    }


    // =========================================================
    // EXISTING ALLOCATION ACCESS
    // =========================================================

    private void validateExistingAllocationAccess(
            Authentication authentication,
            LecturerAllocation allocation) {

        if (allocation == null) {

            throw new IllegalArgumentException(
                    "Lecturer allocation not found."
            );
        }


        if (allocation.getLecturer() == null ||
                allocation.getLecturer().getDepartment() == null ||
                allocation.getLecturer()
                        .getDepartment()
                        .getDepartmentId() == null) {

            throw new IllegalStateException(
                    "Allocation has invalid department information."
            );
        }


        if (isAdmin(authentication)) {
            return;
        }


        Lecturer hod =
                getAuthenticatedLecturer(authentication);


        if (hod == null ||
                hod.getDepartment() == null ||
                hod.getDepartment().getDepartmentId() == null) {

            throw new IllegalStateException(
                    "HOD is not assigned to a department."
            );
        }


        Long hodDepartmentId =
                hod.getDepartment()
                        .getDepartmentId();


        Long allocationDepartmentId =
                allocation.getLecturer()
                        .getDepartment()
                        .getDepartmentId();


        if (!hodDepartmentId.equals(
                allocationDepartmentId)) {

            throw new IllegalStateException(
                    "You are not authorized to modify "
                            + "another department's allocation."
            );
        }
    }


    // =========================================================
    // HOD DEPARTMENT ACCESS
    // =========================================================

    private void validateHODDepartmentAccess(
            Authentication authentication,
            Lecturer lecturer,
            Subject subject) {

        if (isAdmin(authentication)) {
            return;
        }


        Lecturer hod =
                getAuthenticatedLecturer(authentication);


        if (hod == null ||
                hod.getDepartment() == null ||
                hod.getDepartment().getDepartmentId() == null) {

            throw new IllegalStateException(
                    "HOD is not assigned to a department."
            );
        }


        if (lecturer == null ||
                lecturer.getDepartment() == null ||
                lecturer.getDepartment()
                        .getDepartmentId() == null) {

            throw new IllegalStateException(
                    "Lecturer department information is invalid."
            );
        }


        if (subject == null ||
                subject.getDepartment() == null ||
                subject.getDepartment()
                        .getDepartmentId() == null) {

            throw new IllegalStateException(
                    "Subject department information is invalid."
            );
        }


        Long hodDepartmentId =
                hod.getDepartment()
                        .getDepartmentId();


        Long lecturerDepartmentId =
                lecturer.getDepartment()
                        .getDepartmentId();


        Long subjectDepartmentId =
                subject.getDepartment()
                        .getDepartmentId();


        if (!hodDepartmentId.equals(
                lecturerDepartmentId)) {

            throw new IllegalStateException(
                    "You can only manage allocations "
                            + "for lecturers in your department."
            );
        }


        if (!hodDepartmentId.equals(
                subjectDepartmentId)) {

            throw new IllegalStateException(
                    "You can only manage allocations "
                            + "for subjects in your department."
            );
        }
    }


    // =========================================================
    // GET AUTHENTICATED LECTURER
    // =========================================================

    private Lecturer getAuthenticatedLecturer(
            Authentication authentication) {

        if (authentication == null ||
                authentication.getName() == null ||
                authentication.getName()
                        .trim()
                        .isEmpty()) {

            return null;
        }


        return lecturerRepository
                .findByUser_Username(
                        authentication.getName()
                )
                .orElse(null);
    }


    // =========================================================
    // CHECK ADMIN
    // =========================================================

    private boolean isAdmin(
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return false;
        }


        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority != null &&
                                "ROLE_ADMIN".equals(
                                        authority.getAuthority()
                                )
                );
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateAllocation(
            LecturerAllocation allocation) {

        if (allocation == null) {

            throw new IllegalArgumentException(
                    "Allocation cannot be null."
            );
        }


        if (allocation.getLecturer() == null ||
                allocation.getLecturer()
                        .getLecturerId() == null) {

            throw new IllegalArgumentException(
                    "Lecturer is required."
            );
        }


        if (allocation.getSubject() == null ||
                allocation.getSubject()
                        .getSubjectId() == null) {

            throw new IllegalArgumentException(
                    "Subject is required."
            );
        }


        if (allocation.getAcademicYear() == null) {

            throw new IllegalArgumentException(
                    "Academic year is required."
            );
        }


        if (allocation.getSemester() == null) {

            throw new IllegalArgumentException(
                    "Semester is required."
            );
        }


        if (allocation.getAcademicYear() < 1 ||
                allocation.getAcademicYear() > 4) {

            throw new IllegalArgumentException(
                    "Academic year must be between 1 and 4."
            );
        }


        if (allocation.getSemester() < 1 ||
                allocation.getSemester() > 2) {

            throw new IllegalArgumentException(
                    "Semester must be either 1 or 2."
            );
        }


        // Year 4 only has Semester 1
        if (allocation.getAcademicYear() == 4 &&
                allocation.getSemester() != 1) {

            throw new IllegalArgumentException(
                    "Year 4 allocations can only be Semester 1."
            );
        }
    }
}
package com.smartfaculty.service.impl;

import com.smartfaculty.entity.Department;
import com.smartfaculty.entity.Lecturer;
import com.smartfaculty.entity.Specialization;
import com.smartfaculty.entity.Subject;
import com.smartfaculty.repository.DepartmentRepository;
import com.smartfaculty.repository.LecturerRepository;
import com.smartfaculty.repository.SpecializationRepository;
import com.smartfaculty.repository.SubjectRepository;
import com.smartfaculty.service.SubjectService;
import com.smartfaculty.repository.StudentSubjectRegistrationRepository;
import com.smartfaculty.repository.LecturerAllocationRepository;
import com.smartfaculty.repository.TimetableEntryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SubjectServiceImpl
        implements SubjectService {

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private LecturerRepository lecturerRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private SpecializationRepository specializationRepository;

    @Autowired
    private StudentSubjectRegistrationRepository registrationRepository;

    @Autowired
    private LecturerAllocationRepository lecturerAllocationRepository;

    @Autowired
    private TimetableEntryRepository timetableEntryRepository;


    // =====================================================
    // CREATE SUBJECT
    // =====================================================

    @Override
    @Transactional
    public Subject saveSubject(
            Subject subject,
            Authentication authentication) {

        validateAuthentication(authentication);

        if (subject == null) {
            throw new IllegalArgumentException(
                    "Subject data cannot be null."
            );
        }

        boolean isAdmin =
                hasRole(authentication, "ROLE_ADMIN");

        boolean isHOD =
                hasRole(authentication, "ROLE_HOD");

        if (!isAdmin && !isHOD) {
            throw new IllegalStateException(
                    "You are not authorized to create subjects."
            );
        }

        validateSubject(subject);

        Department department =
                resolveDepartment(subject);

        Specialization specialization =
                resolveSpecialization(subject);

        /*
         * The specialization must belong to the
         * same department as the subject.
         */
        validateSpecializationDepartment(
                specialization,
                department
        );

        /*
         * HOD can create subjects only inside
         * the HOD's own department.
         */
        if (isHOD) {

            Long hodDepartmentId =
                    getAuthenticatedHODDepartmentId(
                            authentication
                    );

            if (!hodDepartmentId.equals(
                    department.getDepartmentId())) {

                throw new IllegalStateException(
                        "You are not authorized to create a subject "
                                + "for another department."
                );
            }
        }

        /*
         * Use the actual database entities instead of
         * trusting IDs supplied by the client.
         */
        subject.setDepartment(department);
        subject.setSpecialization(specialization);

        return subjectRepository.save(subject);
    }


    // =====================================================
    // GET ALL SUBJECTS
    // ADMIN = ALL
    // HOD = OWN DEPARTMENT
    // =====================================================

    @Override
    public List<Subject> getAllSubjects(
            Authentication authentication) {

        validateAuthentication(authentication);

        boolean isAdmin =
                hasRole(authentication, "ROLE_ADMIN");

        boolean isHOD =
                hasRole(authentication, "ROLE_HOD");

        if (isAdmin) {
            return subjectRepository.findAll();
        }

        if (isHOD) {

            Long hodDepartmentId =
                    getAuthenticatedHODDepartmentId(
                            authentication
                    );

            return subjectRepository
                    .findByDepartment_DepartmentId(
                            hodDepartmentId
                    );
        }

        throw new IllegalStateException(
                "You are not authorized to access subjects."
        );
    }


    // =====================================================
    // GET SUBJECT BY ID
    // ADMIN = ANY
    // HOD = OWN DEPARTMENT
    // =====================================================

    @Override
    public Subject getSubjectById(
            Long id,
            Authentication authentication) {

        validateAuthentication(authentication);

        if (id == null) {
            throw new IllegalArgumentException(
                    "Subject ID is required."
            );
        }

        Subject subject =
                subjectRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Subject not found."
                                )
                        );

        boolean isAdmin =
                hasRole(authentication, "ROLE_ADMIN");

        boolean isHOD =
                hasRole(authentication, "ROLE_HOD");

        if (isAdmin) {
            return subject;
        }

        if (isHOD) {

            Long hodDepartmentId =
                    getAuthenticatedHODDepartmentId(
                            authentication
                    );

            validateHODSubjectAccess(
                    subject,
                    hodDepartmentId
            );

            return subject;
        }

        throw new IllegalStateException(
                "You are not authorized to access this subject."
        );
    }


    // =====================================================
    // UPDATE SUBJECT
    // =====================================================

    @Override
    @Transactional
    public Subject updateSubject(
            Long id,
            Subject subject,
            Authentication authentication) {

        validateAuthentication(authentication);

        if (id == null) {
            throw new IllegalArgumentException(
                    "Subject ID is required."
            );
        }

        if (subject == null) {
            throw new IllegalArgumentException(
                    "Subject data cannot be null."
            );
        }

        boolean isAdmin =
                hasRole(authentication, "ROLE_ADMIN");

        boolean isHOD =
                hasRole(authentication, "ROLE_HOD");

        if (!isAdmin && !isHOD) {
            throw new IllegalStateException(
                    "You are not authorized to update subjects."
            );
        }

        Subject existing =
                subjectRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Subject not found."
                                )
                        );

        validateSubject(subject);

        /*
         * HOD can update only a subject belonging
         * to the HOD's own department.
         */
        Long hodDepartmentId = null;

        if (isHOD) {

            hodDepartmentId =
                    getAuthenticatedHODDepartmentId(
                            authentication
                    );

            validateHODSubjectAccess(
                    existing,
                    hodDepartmentId
            );
        }

        Department department =
                resolveDepartment(subject);

        Specialization specialization =
                resolveSpecialization(subject);

        /*
         * Department and specialization must always
         * belong together.
         */
        validateSpecializationDepartment(
                specialization,
                department
        );

        /*
         * ADMIN can change the department.
         *
         * HOD cannot move a subject to another department.
         */
        if (isHOD &&
                !hodDepartmentId.equals(
                        department.getDepartmentId())) {

            throw new IllegalStateException(
                    "HOD cannot change a subject "
                            + "to another department."
            );
        }

        /*
         * HOD cannot assign a specialization from
         * another department either.
         */
        if (isHOD) {

            Long specializationDepartmentId =
                    specialization
                            .getDepartment()
                            .getDepartmentId();

            if (!hodDepartmentId.equals(
                    specializationDepartmentId)) {

                throw new IllegalStateException(
                        "HOD cannot assign a specialization "
                                + "from another department."
                );
            }
        }

        // =================================================
        // UPDATE BASIC INFORMATION
        // =================================================

        existing.setSubjectCode(
                subject.getSubjectCode().trim()
        );

        existing.setSubjectName(
                subject.getSubjectName().trim()
        );


        // =================================================
        // UPDATE DEPARTMENT
        // =================================================

        existing.setDepartment(
                department
        );


        // =================================================
        // UPDATE SPECIALIZATION
        // =================================================

        existing.setSpecialization(
                specialization
        );


        // =================================================
        // UPDATE YEAR
        // =================================================

        existing.setAcademicYear(
                subject.getAcademicYear()
        );


        // =================================================
        // UPDATE SEMESTER
        // =================================================

        existing.setSemester(
                subject.getSemester()
        );


        // =================================================
        // UPDATE CREDITS
        // =================================================

        existing.setCredits(
                subject.getCredits()
        );


        // =================================================
        // UPDATE COMPULSORY
        // =================================================

        existing.setCompulsory(
                subject.getCompulsory()
        );

        return subjectRepository.save(
                existing
        );
    }


    // =====================================================
    // DELETE SUBJECT
    // =====================================================

    @Override
    @Transactional
    public String deleteSubject(
            Long id,
            Authentication authentication) {

        validateAuthentication(authentication);

        if (id == null) {
            throw new IllegalArgumentException(
                    "Subject ID is required."
            );
        }

        boolean isAdmin =
                hasRole(authentication, "ROLE_ADMIN");

        boolean isHOD =
                hasRole(authentication, "ROLE_HOD");

        if (!isAdmin && !isHOD) {
            throw new IllegalStateException(
                    "You are not authorized to delete subjects."
            );
        }

        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Subject not found."
                        )
                );

        if (isHOD) {
            Long hodDepartmentId =
                    getAuthenticatedHODDepartmentId(authentication);

            validateHODSubjectAccess(
                    subject,
                    hodDepartmentId
            );
        }

        boolean hasRegistrations =
                registrationRepository
                        .existsBySubjectSubjectId(id);

        boolean hasAllocations =
                lecturerAllocationRepository
                        .existsBySubjectSubjectId(id);

        boolean hasTimetableEntries =
                timetableEntryRepository
                        .existsBySubjectSubjectId(id);

        if (hasRegistrations ||
                hasAllocations ||
                hasTimetableEntries) {

            subject.setActive(false);
            subjectRepository.save(subject);

            if (hasRegistrations) {
                return "Students have already registered for this subject. "
                        + "The subject has been deactivated successfully. "
                        + "Existing registration records have been preserved.";
            }

            return "This subject has existing academic records. "
                    + "It has been deactivated successfully.";
        }

        subjectRepository.delete(subject);

        return "Subject permanently deleted successfully.";
    }

    // =====================================================
    // RESOLVE DEPARTMENT
    // =====================================================

    private Department resolveDepartment(
            Subject subject) {

        if (subject.getDepartment() == null ||
                subject.getDepartment().getDepartmentId() == null) {

            throw new IllegalArgumentException(
                    "Department is required."
            );
        }

        Long departmentId =
                subject.getDepartment()
                        .getDepartmentId();

        return departmentRepository
                .findById(departmentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Department not found with id: "
                                        + departmentId
                        )
                );
    }


    // =====================================================
    // RESOLVE SPECIALIZATION
    // =====================================================

    private Specialization resolveSpecialization(
            Subject subject) {

        if (subject.getSpecialization() == null ||
                subject.getSpecialization()
                        .getSpecializationId() == null) {

            throw new IllegalArgumentException(
                    "Specialization is required."
            );
        }

        Long specializationId =
                subject.getSpecialization()
                        .getSpecializationId();

        return specializationRepository
                .findById(specializationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Specialization not found with id: "
                                        + specializationId
                        )
                );
    }


    // =====================================================
    // VALIDATE SPECIALIZATION DEPARTMENT
    // =====================================================

    private void validateSpecializationDepartment(
            Specialization specialization,
            Department department) {

        if (specialization == null) {
            throw new IllegalArgumentException(
                    "Specialization is required."
            );
        }

        if (department == null ||
                department.getDepartmentId() == null) {

            throw new IllegalArgumentException(
                    "Department is required."
            );
        }

        if (specialization.getDepartment() == null ||
                specialization.getDepartment()
                        .getDepartmentId() == null) {

            throw new IllegalArgumentException(
                    "Specialization department information is invalid."
            );
        }

        Long specializationDepartmentId =
                specialization.getDepartment()
                        .getDepartmentId();

        if (!department.getDepartmentId()
                .equals(specializationDepartmentId)) {

            throw new IllegalArgumentException(
                    "The selected specialization does not "
                            + "belong to the selected department."
            );
        }
    }


    // =====================================================
    // HOD SUBJECT ACCESS
    // =====================================================

    private void validateHODSubjectAccess(
            Subject subject,
            Long hodDepartmentId) {

        if (subject == null) {
            throw new IllegalStateException(
                    "Subject information is invalid."
            );
        }

        if (hodDepartmentId == null) {
            throw new IllegalStateException(
                    "HOD department information is invalid."
            );
        }

        if (subject.getDepartment() == null ||
                subject.getDepartment()
                        .getDepartmentId() == null) {

            throw new IllegalStateException(
                    "Subject department information is invalid."
            );
        }

        Long subjectDepartmentId =
                subject.getDepartment()
                        .getDepartmentId();

        if (!hodDepartmentId.equals(
                subjectDepartmentId)) {

            throw new IllegalStateException(
                    "You are not authorized to access "
                            + "a subject outside your department."
            );
        }
    }


    // =====================================================
    // GET AUTHENTICATED HOD DEPARTMENT
    // =====================================================

    private Long getAuthenticatedHODDepartmentId(
            Authentication authentication) {

        validateAuthentication(authentication);

        if (!hasRole(authentication, "ROLE_HOD")) {
            throw new IllegalStateException(
                    "HOD authorization is required."
            );
        }

        String username =
                authentication.getName().trim();

        Lecturer hod =
                lecturerRepository
                        .findByUser_Username(username)
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

        if (hod.getDepartment() == null ||
                hod.getDepartment().getDepartmentId() == null) {

            throw new IllegalStateException(
                    "HOD department information is invalid."
            );
        }

        return hod.getDepartment().getDepartmentId();
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

        if (authentication.getName() == null ||
                authentication.getName()
                        .trim()
                        .isEmpty()) {

            throw new IllegalStateException(
                    "Authenticated username is invalid."
            );
        }
    }


    // =====================================================
    // ROLE CHECK
    // =====================================================

    private boolean hasRole(
            Authentication authentication,
            String role) {

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority != null &&
                                role.equals(
                                        authority.getAuthority()
                                )
                );
    }


    // =====================================================
    // VALIDATE SUBJECT
    // =====================================================

    private void validateSubject(
            Subject subject) {

        if (subject.getSubjectCode() == null ||
                subject.getSubjectCode()
                        .trim()
                        .isEmpty()) {

            throw new IllegalArgumentException(
                    "Subject code is required."
            );
        }

        if (subject.getSubjectName() == null ||
                subject.getSubjectName()
                        .trim()
                        .isEmpty()) {

            throw new IllegalArgumentException(
                    "Subject name is required."
            );
        }

        if (subject.getDepartment() == null) {

            throw new IllegalArgumentException(
                    "Department is required."
            );
        }

        if (subject.getSpecialization() == null) {

            throw new IllegalArgumentException(
                    "Specialization is required."
            );
        }

        if (subject.getAcademicYear() == null ||
                subject.getAcademicYear() < 1 ||
                subject.getAcademicYear() > 4) {

            throw new IllegalArgumentException(
                    "Academic year must be between 1 and 4."
            );
        }

        if (subject.getSemester() == null ||
                subject.getSemester() < 1 ||
                subject.getSemester() > 2) {

            throw new IllegalArgumentException(
                    "Semester must be either 1 or 2."
            );
        }

        if (subject.getAcademicYear() == 4 &&
                subject.getSemester() != 1) {

            throw new IllegalArgumentException(
                    "Year 4 subjects can only be registered "
                            + "for Semester 1."
            );
        }

        if (subject.getCredits() == null ||
                subject.getCredits() <= 0) {

            throw new IllegalArgumentException(
                    "Credits must be greater than zero."
            );
        }

        if (subject.getCompulsory() == null) {
            subject.setCompulsory(false);
        }
    }
}

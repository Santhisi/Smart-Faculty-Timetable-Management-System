package com.smartfaculty.service.impl;

import com.smartfaculty.entity.Lecturer;
import com.smartfaculty.entity.Specialization;
import com.smartfaculty.entity.Student;
import com.smartfaculty.entity.StudentSubjectRegistration;
import com.smartfaculty.entity.Subject;
import com.smartfaculty.enums.DegreeType;
import com.smartfaculty.repository.LecturerRepository;
import com.smartfaculty.repository.StudentRepository;
import com.smartfaculty.repository.StudentSubjectRegistrationRepository;
import com.smartfaculty.repository.SubjectRepository;
import com.smartfaculty.repository.SpecializationRepository;
import com.smartfaculty.service.StudentSubjectRegistrationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class StudentSubjectRegistrationServiceImpl
        implements StudentSubjectRegistrationService {

    @Autowired
    private StudentSubjectRegistrationRepository
            registrationRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private LecturerRepository lecturerRepository;

    @Autowired
    private SpecializationRepository specializationRepository;


    // =====================================================
    // CREATE REGISTRATION
    // =====================================================

    @Override
    public StudentSubjectRegistration saveRegistration(
            StudentSubjectRegistration registration,
            Authentication authentication) {

        validateAuthentication(authentication);

        if (registration == null) {

            throw new IllegalArgumentException(
                    "Registration cannot be null."
            );
        }

        boolean isAdmin =
                hasRole(authentication, "ROLE_ADMIN");

        boolean isStudent =
                hasRole(authentication, "ROLE_STUDENT");

        if (!isAdmin && !isStudent) {

            throw new IllegalStateException(
                    "You are not authorized to create a registration."
            );
        }


        // -------------------------------------------------
        // STUDENT OWNERSHIP
        // -------------------------------------------------

        if (isStudent) {

            Student authenticatedStudent =
                    getAuthenticatedStudent(
                            authentication
                    );

            registration.setStudent(
                    authenticatedStudent
            );
        }


        // -------------------------------------------------
        // CHECK STUDENT
        // -------------------------------------------------

        if (registration.getStudent() == null ||
                registration.getStudent().getStudentId() == null) {

            throw new IllegalArgumentException(
                    "Student is required."
            );
        }


        // -------------------------------------------------
        // CHECK SUBJECT
        // -------------------------------------------------

        if (registration.getSubject() == null ||
                registration.getSubject().getSubjectId() == null) {

            throw new IllegalArgumentException(
                    "Subject is required."
            );
        }


        // -------------------------------------------------
        // CHECK YEAR
        // -------------------------------------------------

        if (registration.getAcademicYear() == null) {

            throw new IllegalArgumentException(
                    "Academic year is required."
            );
        }


        // -------------------------------------------------
        // CHECK SEMESTER
        // -------------------------------------------------

        if (registration.getSemester() == null) {

            throw new IllegalArgumentException(
                    "Semester is required."
            );
        }


        // -------------------------------------------------
        // VALIDATE YEAR
        // -------------------------------------------------

        if (registration.getAcademicYear() < 1 ||
                registration.getAcademicYear() > 4) {

            throw new IllegalArgumentException(
                    "Academic year must be between 1 and 4."
            );
        }


        // -------------------------------------------------
        // VALIDATE SEMESTER
        // -------------------------------------------------

        if (registration.getSemester() < 1 ||
                registration.getSemester() > 2) {

            throw new IllegalArgumentException(
                    "Semester must be either 1 or 2."
            );
        }


        // -------------------------------------------------
        // YEAR 4 SEMESTER CHECK
        // -------------------------------------------------

        if (registration.getAcademicYear() == 4 &&
                registration.getSemester() != 1) {

            throw new IllegalArgumentException(
                    "Year 4 students can only register for Semester 1."
            );
        }


        // -------------------------------------------------
        // GET STUDENT
        // -------------------------------------------------

        Long studentId =
                registration
                        .getStudent()
                        .getStudentId();

        Student student =
                studentRepository
                        .findById(studentId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Student not found with ID: "
                                                + studentId
                                )
                        );


        // -------------------------------------------------
        // GET SUBJECT
        // -------------------------------------------------

        Long subjectId =
                registration
                        .getSubject()
                        .getSubjectId();

        Subject subject =
                subjectRepository
                        .findById(subjectId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Subject not found with ID: "
                                                + subjectId
                                )
                        );


        // -------------------------------------------------
        // STUDENT YEAR CHECK
        // -------------------------------------------------

        if (!registration
                .getAcademicYear()
                .equals(student.getYearLevel())) {

            throw new IllegalArgumentException(
                    "Student can only register for subjects "
                            + "belonging to their current academic year."
            );
        }


        // -------------------------------------------------
        // STUDENT SEMESTER CHECK
        // -------------------------------------------------

        if (!registration
                .getSemester()
                .equals(student.getSemester())) {

            throw new IllegalArgumentException(
                    "Student can only register for subjects "
                            + "belonging to their current semester."
            );
        }


        // -------------------------------------------------
        // SUBJECT YEAR CHECK
        // -------------------------------------------------

        if (!registration
                .getAcademicYear()
                .equals(subject.getAcademicYear())) {

            throw new IllegalArgumentException(
                    "The selected subject does not belong "
                            + "to the selected academic year."
            );
        }


        // -------------------------------------------------
        // SUBJECT SEMESTER CHECK
        // -------------------------------------------------

        if (!registration
                .getSemester()
                .equals(subject.getSemester())) {

            throw new IllegalArgumentException(
                    "The selected subject does not belong "
                            + "to the selected semester."
            );
        }


        // -------------------------------------------------
        // CHECK SUBJECT SPECIALIZATION
        // -------------------------------------------------

        boolean specializationAllowed =
                isSubjectAllowedForStudent(
                        student,
                        subject
                );

        if (!specializationAllowed) {

            throw new IllegalArgumentException(
                    "This subject is not available "
                            + "for the student's specialization."
            );
        }


        // -------------------------------------------------
        // CHECK DUPLICATE
        // -------------------------------------------------

        boolean alreadyRegistered =
                registrationRepository
                        .existsByStudentStudentIdAndSubjectSubjectIdAndAcademicYearAndSemester(
                                studentId,
                                subjectId,
                                registration.getAcademicYear(),
                                registration.getSemester()
                        );

        if (alreadyRegistered) {

            throw new IllegalStateException(
                    "The student has already registered "
                            + "for this subject."
            );
        }


        // -------------------------------------------------
        // USE DATABASE ENTITIES
        // -------------------------------------------------

        registration.setStudent(student);
        registration.setSubject(subject);


        // -------------------------------------------------
        // SAVE
        // -------------------------------------------------

        return registrationRepository.save(
                registration
        );
    }


    // =====================================================
    // UPDATE REGISTRATION
    // =====================================================

    @Override
    public StudentSubjectRegistration updateRegistration(
            Long id,
            StudentSubjectRegistration registration,
            Authentication authentication) {

        validateAuthentication(authentication);

        if (id == null) {

            throw new IllegalArgumentException(
                    "Registration ID is required."
            );
        }

        if (registration == null) {

            throw new IllegalArgumentException(
                    "Registration cannot be null."
            );
        }

        boolean isAdmin =
                hasRole(authentication, "ROLE_ADMIN");

        boolean isStudent =
                hasRole(authentication, "ROLE_STUDENT");

        if (!isAdmin && !isStudent) {

            throw new IllegalStateException(
                    "You are not authorized to update registrations."
            );
        }


        StudentSubjectRegistration existingRegistration =
                registrationRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Registration not found with ID: "
                                                + id
                                )
                        );


        // -------------------------------------------------
        // STUDENT OWNERSHIP
        // -------------------------------------------------

        if (isStudent) {

            Student authenticatedStudent =
                    getAuthenticatedStudent(
                            authentication
                    );

            if (existingRegistration.getStudent() == null ||
                    !authenticatedStudent
                            .getStudentId()
                            .equals(
                                    existingRegistration
                                            .getStudent()
                                            .getStudentId()
                            )) {

                throw new IllegalStateException(
                        "You are not authorized to update another student's registration."
                );
            }

            registration.setStudent(
                    authenticatedStudent
            );
        }


        // -------------------------------------------------
        // ADMIN MUST PROVIDE STUDENT
        // -------------------------------------------------

        if (registration.getStudent() == null ||
                registration.getStudent().getStudentId() == null) {

            throw new IllegalArgumentException(
                    "Student is required."
            );
        }


        // -------------------------------------------------
        // CHECK SUBJECT
        // -------------------------------------------------

        if (registration.getSubject() == null ||
                registration.getSubject().getSubjectId() == null) {

            throw new IllegalArgumentException(
                    "Subject is required."
            );
        }


        // -------------------------------------------------
        // CHECK YEAR
        // -------------------------------------------------

        if (registration.getAcademicYear() == null) {

            throw new IllegalArgumentException(
                    "Academic year is required."
            );
        }


        // -------------------------------------------------
        // CHECK SEMESTER
        // -------------------------------------------------

        if (registration.getSemester() == null) {

            throw new IllegalArgumentException(
                    "Semester is required."
            );
        }


        // -------------------------------------------------
        // VALIDATE YEAR
        // -------------------------------------------------

        if (registration.getAcademicYear() < 1 ||
                registration.getAcademicYear() > 4) {

            throw new IllegalArgumentException(
                    "Academic year must be between 1 and 4."
            );
        }


        // -------------------------------------------------
        // VALIDATE SEMESTER
        // -------------------------------------------------

        if (registration.getSemester() < 1 ||
                registration.getSemester() > 2) {

            throw new IllegalArgumentException(
                    "Semester must be either 1 or 2."
            );
        }


        // -------------------------------------------------
        // YEAR 4 SEMESTER CHECK
        // -------------------------------------------------

        if (registration.getAcademicYear() == 4 &&
                registration.getSemester() != 1) {

            throw new IllegalArgumentException(
                    "Year 4 students can only use Semester 1."
            );
        }


        // -------------------------------------------------
        // GET DATABASE STUDENT
        // -------------------------------------------------

        Long studentId =
                registration
                        .getStudent()
                        .getStudentId();

        Student student =
                studentRepository
                        .findById(studentId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Student not found with ID: "
                                                + studentId
                                )
                        );


        // -------------------------------------------------
        // GET DATABASE SUBJECT
        // -------------------------------------------------

        Long subjectId =
                registration
                        .getSubject()
                        .getSubjectId();

        Subject subject =
                subjectRepository
                        .findById(subjectId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Subject not found with ID: "
                                                + subjectId
                                )
                        );


        // -------------------------------------------------
        // STUDENT YEAR CHECK
        // -------------------------------------------------

        if (!registration
                .getAcademicYear()
                .equals(student.getYearLevel())) {

            throw new IllegalArgumentException(
                    "Student can only register for subjects "
                            + "belonging to their current academic year."
            );
        }


        // -------------------------------------------------
        // STUDENT SEMESTER CHECK
        // -------------------------------------------------

        if (!registration
                .getSemester()
                .equals(student.getSemester())) {

            throw new IllegalArgumentException(
                    "Student can only register for subjects "
                            + "belonging to their current semester."
            );
        }


        // -------------------------------------------------
        // SUBJECT YEAR CHECK
        // -------------------------------------------------

        if (!registration
                .getAcademicYear()
                .equals(subject.getAcademicYear())) {

            throw new IllegalArgumentException(
                    "The selected subject does not belong "
                            + "to the selected academic year."
            );
        }


        // -------------------------------------------------
        // SUBJECT SEMESTER CHECK
        // -------------------------------------------------

        if (!registration
                .getSemester()
                .equals(subject.getSemester())) {

            throw new IllegalArgumentException(
                    "The selected subject does not belong "
                            + "to the selected semester."
            );
        }


        // -------------------------------------------------
        // SPECIALIZATION CHECK
        // -------------------------------------------------

        if (!isSubjectAllowedForStudent(
                student,
                subject
        )) {

            throw new IllegalArgumentException(
                    "This subject is not available "
                            + "for the student's specialization."
            );
        }


        // -------------------------------------------------
        // DUPLICATE CHECK
        // -------------------------------------------------

        boolean duplicate =
                registrationRepository
                        .existsByStudentStudentIdAndSubjectSubjectIdAndAcademicYearAndSemester(
                                studentId,
                                subjectId,
                                registration.getAcademicYear(),
                                registration.getSemester()
                        );


        boolean sameRegistration =
                existingRegistration
                        .getStudent() != null &&
                        existingRegistration
                                .getSubject() != null &&
                        existingRegistration
                                .getStudent()
                                .getStudentId()
                                .equals(studentId) &&
                        existingRegistration
                                .getSubject()
                                .getSubjectId()
                                .equals(subjectId) &&
                        existingRegistration
                                .getAcademicYear()
                                .equals(
                                        registration
                                                .getAcademicYear()
                                ) &&
                        existingRegistration
                                .getSemester()
                                .equals(
                                        registration
                                                .getSemester()
                                );


        if (duplicate && !sameRegistration) {

            throw new IllegalStateException(
                    "The student has already registered "
                            + "for this subject."
            );
        }


        // -------------------------------------------------
        // UPDATE USING DATABASE ENTITIES
        // -------------------------------------------------

        existingRegistration.setStudent(student);
        existingRegistration.setSubject(subject);

        existingRegistration.setAcademicYear(
                registration.getAcademicYear()
        );

        existingRegistration.setSemester(
                registration.getSemester()
        );


        return registrationRepository.save(
                existingRegistration
        );
    }


    // =====================================================
    // GET ALL REGISTRATIONS
    // =====================================================

    @Override
    public List<StudentSubjectRegistration>
    getAllRegistrations(
            Authentication authentication) {

        validateAuthentication(authentication);

        boolean isAdmin =
                hasRole(authentication, "ROLE_ADMIN");

        boolean isHOD =
                hasRole(authentication, "ROLE_HOD");

        if (!isAdmin && !isHOD) {

            throw new IllegalStateException(
                    "You are not authorized to view all registrations."
            );
        }

        List<StudentSubjectRegistration> registrations =
                registrationRepository.findAll();

        if (isAdmin) {
            return registrations;
        }

        Long hodDepartmentId =
                getAuthenticatedHODDepartmentId(
                        authentication
                );

        return filterRegistrationsByDepartment(
                registrations,
                hodDepartmentId
        );
    }


    // =====================================================
    // GET REGISTRATION BY ID
    // =====================================================

    @Override
    public StudentSubjectRegistration
    getRegistrationById(
            Long id,
            Authentication authentication) {

        validateAuthentication(authentication);

        if (id == null) {

            throw new IllegalArgumentException(
                    "Registration ID is required."
            );
        }

        StudentSubjectRegistration registration =
                registrationRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Registration not found with ID: "
                                                + id
                                )
                        );


        // -------------------------------------------------
        // ADMIN
        // -------------------------------------------------

        if (hasRole(authentication, "ROLE_ADMIN")) {
            return registration;
        }


        // -------------------------------------------------
        // HOD
        // -------------------------------------------------

        if (hasRole(authentication, "ROLE_HOD")) {

            Long hodDepartmentId =
                    getAuthenticatedHODDepartmentId(
                            authentication
                    );

            validateRegistrationDepartmentAccess(
                    registration,
                    hodDepartmentId
            );

            return registration;
        }


        // -------------------------------------------------
        // STUDENT
        // -------------------------------------------------

        if (hasRole(authentication, "ROLE_STUDENT")) {

            Student student =
                    getAuthenticatedStudent(
                            authentication
                    );

            if (registration.getStudent() == null ||
                    !student.getStudentId()
                            .equals(
                                    registration
                                            .getStudent()
                                            .getStudentId()
                            )) {

                throw new IllegalStateException(
                        "You are not authorized to access another student's registration."
                );
            }

            return registration;
        }


        throw new IllegalStateException(
                "You are not authorized to access this registration."
        );
    }


    // =====================================================
    // GET REGISTRATIONS BY STUDENT
    // =====================================================

    @Override
    public List<StudentSubjectRegistration>
    getRegistrationsByStudentId(
            Long studentId,
            Authentication authentication) {

        validateAuthentication(authentication);

        if (studentId == null) {

            throw new IllegalArgumentException(
                    "Student ID is required."
            );
        }


        // -------------------------------------------------
        // ADMIN
        // -------------------------------------------------

        if (hasRole(authentication, "ROLE_ADMIN")) {

            validateStudentExists(studentId);

            return registrationRepository
                    .findByStudentStudentId(studentId);
        }


        // -------------------------------------------------
        // HOD
        // -------------------------------------------------

        if (hasRole(authentication, "ROLE_HOD")) {

            validateStudentExists(studentId);

            Long hodDepartmentId =
                    getAuthenticatedHODDepartmentId(
                            authentication
                    );

            List<StudentSubjectRegistration> registrations =
                    registrationRepository
                            .findByStudentStudentId(studentId);

            return filterRegistrationsByDepartment(
                    registrations,
                    hodDepartmentId
            );
        }


        // -------------------------------------------------
        // STUDENT
        // -------------------------------------------------

        if (hasRole(authentication, "ROLE_STUDENT")) {

            Student student =
                    getAuthenticatedStudent(
                            authentication
                    );

            if (!student
                    .getStudentId()
                    .equals(studentId)) {

                throw new IllegalStateException(
                        "You are not authorized to access another student's registrations."
                );
            }

            return registrationRepository
                    .findByStudentStudentId(studentId);
        }


        throw new IllegalStateException(
                "You are not authorized to access student registrations."
        );
    }


    // =====================================================
    // GET REGISTRATIONS BY STUDENT + YEAR + SEMESTER
    // =====================================================

    @Override
    public List<StudentSubjectRegistration>
    getRegistrationsByStudentAndYearAndSemester(
            Long studentId,
            Integer academicYear,
            Integer semester,
            Authentication authentication) {

        validateAuthentication(authentication);

        if (studentId == null) {

            throw new IllegalArgumentException(
                    "Student ID is required."
            );
        }

        if (academicYear == null) {

            throw new IllegalArgumentException(
                    "Academic year is required."
            );
        }

        if (semester == null) {

            throw new IllegalArgumentException(
                    "Semester is required."
            );
        }


        if (academicYear < 1 ||
                academicYear > 4) {

            throw new IllegalArgumentException(
                    "Academic year must be between 1 and 4."
            );
        }


        if (semester < 1 ||
                semester > 2) {

            throw new IllegalArgumentException(
                    "Semester must be either 1 or 2."
            );
        }


        if (academicYear == 4 &&
                semester != 1) {

            throw new IllegalArgumentException(
                    "Year 4 can only use Semester 1."
            );
        }


        // -------------------------------------------------
        // ADMIN
        // -------------------------------------------------

        if (hasRole(authentication, "ROLE_ADMIN")) {

            validateStudentExists(studentId);

            return registrationRepository
                    .findByStudentStudentIdAndAcademicYearAndSemester(
                            studentId,
                            academicYear,
                            semester
                    );
        }


        // -------------------------------------------------
        // HOD
        // -------------------------------------------------

        if (hasRole(authentication, "ROLE_HOD")) {

            validateStudentExists(studentId);

            Long hodDepartmentId =
                    getAuthenticatedHODDepartmentId(
                            authentication
                    );

            List<StudentSubjectRegistration> registrations =
                    registrationRepository
                            .findByStudentStudentIdAndAcademicYearAndSemester(
                                    studentId,
                                    academicYear,
                                    semester
                            );

            return filterRegistrationsByDepartment(
                    registrations,
                    hodDepartmentId
            );
        }


        // -------------------------------------------------
        // STUDENT
        // -------------------------------------------------

        if (hasRole(authentication, "ROLE_STUDENT")) {

            Student student =
                    getAuthenticatedStudent(
                            authentication
                    );

            if (!student
                    .getStudentId()
                    .equals(studentId)) {

                throw new IllegalStateException(
                        "You are not authorized to access another student's registrations."
                );
            }

            return registrationRepository
                    .findByStudentStudentIdAndAcademicYearAndSemester(
                            studentId,
                            academicYear,
                            semester
                    );
        }


        throw new IllegalStateException(
                "You are not authorized to access student registrations."
        );
    }


    // =====================================================
    // GET REGISTRATIONS BY SUBJECT
    // =====================================================

    @Override
    public List<StudentSubjectRegistration>
    getRegistrationsBySubjectId(
            Long subjectId,
            Authentication authentication) {

        validateAuthentication(authentication);

        if (subjectId == null) {

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
                    "You are not authorized to view registrations by subject."
            );
        }


        Subject subject =
                subjectRepository
                        .findById(subjectId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Subject not found with ID: "
                                                + subjectId
                                )
                        );


        // -------------------------------------------------
        // HOD DEPARTMENT CHECK
        // -------------------------------------------------

        if (isHOD) {

            Long hodDepartmentId =
                    getAuthenticatedHODDepartmentId(
                            authentication
                    );

            validateSubjectDepartmentAccess(
                    subject,
                    hodDepartmentId
            );
        }


        return registrationRepository
                .findBySubjectSubjectId(subjectId);
    }


    // =====================================================
    // GET AVAILABLE SUBJECTS FOR STUDENT
    // =====================================================
    // PART 15O
    // =====================================================

    @Override
    public List<Subject>
    getAvailableSubjectsForStudent(
            Long studentId,
            Authentication authentication) {

        validateAuthentication(authentication);

        if (studentId == null) {

            throw new IllegalArgumentException(
                    "Student ID is required."
            );
        }


        // -------------------------------------------------
        // STUDENT OWNERSHIP
        // -------------------------------------------------

        if (hasRole(authentication, "ROLE_STUDENT")) {

            Student authenticatedStudent =
                    getAuthenticatedStudent(
                            authentication
                    );

            if (!authenticatedStudent
                    .getStudentId()
                    .equals(studentId)) {

                throw new IllegalStateException(
                        "You are not authorized to access another student's available subjects."
                );
            }
        }


        // -------------------------------------------------
        // ADMIN / HOD / STUDENT AUTHORIZATION
        // -------------------------------------------------

        boolean isAdmin =
                hasRole(authentication, "ROLE_ADMIN");

        boolean isHOD =
                hasRole(authentication, "ROLE_HOD");

        boolean isStudent =
                hasRole(authentication, "ROLE_STUDENT");

        if (!isAdmin && !isHOD && !isStudent) {

            throw new IllegalStateException(
                    "You are not authorized to view available subjects."
            );
        }


        // -------------------------------------------------
        // GET STUDENT
        // -------------------------------------------------

        Student student =
                studentRepository
                        .findById(studentId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Student not found with ID: "
                                                + studentId
                                )
                        );


        // -------------------------------------------------
        // GET CURRENT YEAR + SEMESTER
        // -------------------------------------------------

        Integer year =
                student.getYearLevel();

        Integer semester =
                student.getSemester();


        if (year == null ||
                semester == null) {

            throw new IllegalArgumentException(
                    "Student year and semester must be defined."
            );
        }


        if (year < 1 ||
                year > 4) {

            throw new IllegalArgumentException(
                    "Student academic year must be between 1 and 4."
            );
        }


        if (semester < 1 ||
                semester > 2) {

            throw new IllegalArgumentException(
                    "Student semester must be either 1 or 2."
            );
        }


        if (year == 4 &&
                (student.getDegreeType() != DegreeType.SPECIAL ||
                        semester < 1 ||
                        semester > 2)) {

            throw new IllegalArgumentException(
                    "Year 4 is available only for Special Degree students."
            );
        }


        // -------------------------------------------------
        // GET STUDENT SPECIALIZATIONS
        // -------------------------------------------------

        List<Specialization> specializations =
                new ArrayList<>();


        if (student.getMajorSpecialization() != null) {

            specializations.add(
                    student.getMajorSpecialization()
            );
        }


        if (student.getMinorSpecialization() != null) {

            if (!specializations.contains(
                    student.getMinorSpecialization()
            )) {

                specializations.add(
                        student.getMinorSpecialization()
                );
            }
        }


        // -------------------------------------------------
        // COLLECT AVAILABLE SUBJECTS
        // -------------------------------------------------

        List<Subject> availableSubjects =
                new ArrayList<>();


        // -------------------------------------------------
        // YEAR 1
        // -------------------------------------------------

        if (year == 1) {

            availableSubjects.addAll(
                    subjectRepository
                            .findByAcademicYearAndSemester(
                                    year,
                                    semester
                            )
            );

        } else {

            availableSubjects.addAll(
                    subjectRepository
                            .findByCompulsoryTrueAndAcademicYearAndSemester(
                                    year,
                                    semester
                            )
            );

            availableSubjects.addAll(
                    getEligibleSpecializationSubjects(
                            student,
                            year,
                            semester
                    )
            );
        }


        // -------------------------------------------------
        // REMOVE DUPLICATE SUBJECTS
        // -------------------------------------------------

        Set<Long> subjectIds =
                new HashSet<>();

        List<Subject> uniqueSubjects =
                new ArrayList<>();


        for (Subject subject :
                availableSubjects) {

            if (subject == null ||
                    subject.getSubjectId() == null) {

                continue;
            }


            if (subjectIds.add(
                    subject.getSubjectId()
            )) {

                uniqueSubjects.add(
                        subject
                );
            }
        }


        // -------------------------------------------------
        // REMOVE COMPULSORY SUBJECTS
        // -------------------------------------------------

        uniqueSubjects.removeIf(
                subject ->
                        Boolean.TRUE.equals(
                                subject.getCompulsory()
                        )
        );


        // -------------------------------------------------
        // HOD DEPARTMENT FILTER
        // -------------------------------------------------

        if (isHOD) {

            Long hodDepartmentId =
                    getAuthenticatedHODDepartmentId(
                            authentication
                    );

            List<Subject> departmentSubjects =
                    new ArrayList<>();


            for (Subject subject :
                    uniqueSubjects) {

                if (subject == null) {
                    continue;
                }

                if (subject.getDepartment() == null) {
                    continue;
                }

                if (subject.getDepartment()
                        .getDepartmentId() == null) {

                    continue;
                }

                Long subjectDepartmentId =
                        subject.getDepartment()
                                .getDepartmentId();


                if (hodDepartmentId.equals(
                        subjectDepartmentId
                )) {

                    departmentSubjects.add(
                            subject
                    );
                }
            }


            uniqueSubjects =
                    departmentSubjects;
        }


        // -------------------------------------------------
        // GET ALREADY REGISTERED SUBJECTS
        // -------------------------------------------------

        List<StudentSubjectRegistration>
                registeredSubjects =
                registrationRepository
                        .findByStudentStudentIdAndAcademicYearAndSemester(
                                studentId,
                                year,
                                semester
                        );


        // -------------------------------------------------
        // STORE REGISTERED SUBJECT IDS
        // -------------------------------------------------

        Set<Long> registeredSubjectIds =
                new HashSet<>();


        for (StudentSubjectRegistration registration :
                registeredSubjects) {

            if (registration.getSubject() != null &&
                    registration.getSubject().getSubjectId() != null) {

                registeredSubjectIds.add(
                        registration
                                .getSubject()
                                .getSubjectId()
                );
            }
        }


        // -------------------------------------------------
        // REMOVE ALREADY REGISTERED SUBJECTS
        // -------------------------------------------------

        uniqueSubjects.removeIf(
                subject ->
                        registeredSubjectIds.contains(
                                subject.getSubjectId()
                        )
        );


        return uniqueSubjects;
    }


    // =====================================================
    // CHECK WHETHER SUBJECT IS ALLOWED
    // =====================================================

    private boolean isSubjectAllowedForStudent(
            Student student,
            Subject subject) {

        if (Boolean.TRUE.equals(
                subject.getCompulsory())) {

            return true;
        }

        if (student.getYearLevel() == null ||
                student.getSemester() == null ||
                subject.getAcademicYear() == null ||
                subject.getSemester() == null ||
                !student.getYearLevel()
                        .equals(subject.getAcademicYear()) ||
                !student.getSemester()
                        .equals(subject.getSemester())) {

            return false;
        }

        if (student.getYearLevel() == 1) {
            return true;
        }

        return getEligibleSpecializationSubjects(
                student,
                student.getYearLevel(),
                student.getSemester()
        )
                .stream()
                .anyMatch(
                        s ->
                                s.getSubjectId() != null &&
                                        s.getSubjectId()
                                                .equals(
                                                        subject.getSubjectId()
                                                )
                );
    }


    // =====================================================
    // REGISTRATION STATUS
    // =====================================================

    @Override
    public Map<String, Object> getRegistrationStatus(
            Long studentId,
            Authentication authentication) {

        validateAuthentication(authentication);

        Student student =
                getAuthenticatedStudent(
                        authentication
                );

        if (!student.getStudentId().equals(studentId)) {

            throw new IllegalStateException(
                    "You are not authorized to access another student's registration status."
            );
        }

        Integer year =
                student.getYearLevel();

        Integer semester =
                student.getSemester();

        if (year == null ||
                semester == null) {

            throw new IllegalArgumentException(
                    "Student year and semester must be defined."
            );
        }


        // =================================================
        // IMPORTANT LEGACY STUDENT CHECK
        // =================================================
        //
        // Students who completed registration BEFORE the
        // registrationCompleted field was introduced may
        // have NULL/FALSE in that field.
        //
        // Their old registration records are therefore
        // checked once.
        //
        // If the existing records prove that registration
        // was already completed, we permanently mark the
        // student as completed.
        //
        // This prevents old students from being sent back
        // to the registration page.
        // =================================================

        boolean complete =
                Boolean.TRUE.equals(
                        student.getRegistrationCompleted()
                );


        if (!complete &&
                hasCompletedLegacyRegistration(
                        student
                )) {

            student.setRegistrationCompleted(
                    true
            );

            studentRepository.save(
                    student
            );

            complete = true;
        }


        // -------------------------------------------------
        // GET CURRENT REGISTRATIONS
        // -------------------------------------------------

        List<StudentSubjectRegistration> registrations =
                registrationRepository
                        .findByStudentStudentIdAndAcademicYearAndSemester(
                                studentId,
                                year,
                                semester
                        );


        // -------------------------------------------------
        // REGISTERED SUBJECT IDS
        // -------------------------------------------------

        Set<Long> registeredIds =
                new HashSet<>();

        for (StudentSubjectRegistration r :
                registrations) {

            if (r != null &&
                    r.getSubject() != null &&
                    r.getSubject().getSubjectId() != null) {

                registeredIds.add(
                        r.getSubject()
                                .getSubjectId()
                );
            }
        }


        // -------------------------------------------------
        // COMPULSORY SUBJECT INFORMATION
        // -------------------------------------------------

        List<Subject> compulsory =
                subjectRepository
                        .findByCompulsoryTrueAndAcademicYearAndSemester(
                                year,
                                semester
                        );

        int compulsoryCount =
                compulsory.size();

        int compulsoryRegistered =
                0;

        for (Subject subject :
                compulsory) {

            if (subject.getSubjectId() != null &&
                    registeredIds.contains(
                            subject.getSubjectId()
                    )) {

                compulsoryRegistered++;
            }
        }


        // -------------------------------------------------
        // PREFERRED SUBJECT INFORMATION
        // -------------------------------------------------

        int preferredRequired =
                year == 1 ? 3 : 0;

        int preferredRegistered =
                0;

        for (StudentSubjectRegistration r :
                registrations) {

            if (r == null ||
                    r.getSubject() == null ||
                    Boolean.TRUE.equals(
                            r.getSubject().getCompulsory()
                    )) {

                continue;
            }

            preferredRegistered++;
        }


        // -------------------------------------------------
        // RETURN STATUS
        // -------------------------------------------------

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put(
                "complete",
                complete
        );

        result.put(
                "studentId",
                studentId
        );

        result.put(
                "yearLevel",
                year
        );

        result.put(
                "semester",
                semester
        );

        result.put(
                "degreeType",
                student.getDegreeType() != null
                        ? student.getDegreeType().name()
                        : null
        );

        result.put(
                "compulsoryRequired",
                compulsoryCount
        );

        result.put(
                "compulsoryRegistered",
                compulsoryRegistered
        );

        result.put(
                "preferredRequired",
                preferredRequired
        );

        result.put(
                "preferredRegistered",
                preferredRegistered
        );

        result.put(
                "majorSpecializationId",
                student.getMajorSpecialization() != null
                        ? student.getMajorSpecialization()
                        .getSpecializationId()
                        : null
        );

        result.put(
                "minorSpecializationId",
                student.getMinorSpecialization() != null
                        ? student.getMinorSpecialization()
                        .getSpecializationId()
                        : null
        );

        return result;
    }


    // =====================================================
    // LEGACY REGISTRATION COMPLETION CHECK
    // =====================================================
    //
    // This method is ONLY used for students whose new
    // registrationCompleted flag is not yet true.
    //
    // It identifies students who completed registration
    // under the old system.
    // =====================================================

    private boolean hasCompletedLegacyRegistration(
            Student student) {

        if (student == null ||
                student.getStudentId() == null ||
                student.getYearLevel() == null ||
                student.getSemester() == null) {

            return false;
        }


        Integer year =
                student.getYearLevel();

        Integer semester =
                student.getSemester();


        // -------------------------------------------------
        // GET EXISTING REGISTRATIONS
        // -------------------------------------------------

        List<StudentSubjectRegistration> registrations =
                registrationRepository
                        .findByStudentStudentIdAndAcademicYearAndSemester(
                                student.getStudentId(),
                                year,
                                semester
                        );


        if (registrations == null ||
                registrations.isEmpty()) {

            return false;
        }


        // -------------------------------------------------
        // COLLECT REGISTERED SUBJECT IDS
        // -------------------------------------------------

        Set<Long> registeredIds =
                new HashSet<>();


        int nonCompulsoryRegistered =
                0;


        for (StudentSubjectRegistration registration :
                registrations) {

            if (registration == null ||
                    registration.getSubject() == null ||
                    registration.getSubject().getSubjectId() == null) {

                continue;
            }


            Subject subject =
                    registration.getSubject();


            registeredIds.add(
                    subject.getSubjectId()
            );


            if (!Boolean.TRUE.equals(
                    subject.getCompulsory()
            )) {

                nonCompulsoryRegistered++;
            }
        }


        // =================================================
        // YEAR 1
        // =================================================
        //
        // Old Year 1 completion required:
        //
        // 1. All compulsory subjects registered
        // 2. Exactly 3 preferred subjects registered
        //
        // =================================================

        if (year == 1) {

            List<Subject> compulsory =
                    subjectRepository
                            .findByCompulsoryTrueAndAcademicYearAndSemester(
                                    year,
                                    semester
                            );


            for (Subject subject :
                    compulsory) {

                if (subject.getSubjectId() == null ||
                        !registeredIds.contains(
                                subject.getSubjectId()
                        )) {

                    return false;
                }
            }


            return nonCompulsoryRegistered == 3;
        }


        // =================================================
        // YEAR 2 / YEAR 3
        // =================================================
        //
        // Old completion required:
        //
        // 1. Major specialization exists
        // 2. Minor specialization exists
        // 3. Major and minor are different
        // 4. Degree type exists
        // 5. All eligible subjects are registered
        //
        // =================================================

        if (year == 2 ||
                year == 3) {

            if (student.getDegreeType() == null) {
                return false;
            }

            if (student.getMajorSpecialization() == null ||
                    student.getMinorSpecialization() == null) {

                return false;
            }

            if (student.getMajorSpecialization()
                    .getSpecializationId() == null ||
                    student.getMinorSpecialization()
                            .getSpecializationId() == null) {

                return false;
            }

            if (student.getMajorSpecialization()
                    .getSpecializationId()
                    .equals(
                            student.getMinorSpecialization()
                                    .getSpecializationId()
                    )) {

                return false;
            }


            return allEligibleSubjectsRegistered(
                    student,
                    registeredIds
            );
        }


        // =================================================
        // YEAR 4
        // =================================================
        //
        // Old completion required:
        //
        // 1. Special Degree
        // 2. Major specialization exists
        // 3. No minor
        // 4. All eligible subjects are registered
        //
        // =================================================

        if (year == 4) {

            if (student.getDegreeType() !=
                    DegreeType.SPECIAL) {

                return false;
            }

            if (student.getMajorSpecialization() == null ||
                    student.getMajorSpecialization()
                            .getSpecializationId() == null) {

                return false;
            }

            if (student.getMinorSpecialization() != null) {

                return false;
            }


            return allEligibleSubjectsRegistered(
                    student,
                    registeredIds
            );
        }


        return false;
    }


    // =====================================================
    // COMPLETE CURRENT-SEMESTER REGISTRATION
    // =====================================================

    @Override
    @org.springframework.transaction.annotation.Transactional
    public List<StudentSubjectRegistration> completeRegistration(
            Long studentId,
            Long majorSpecializationId,
            Long minorSpecializationId,
            DegreeType degreeType,
            List<Long> preferredSubjectIds,
            Authentication authentication) {

        validateAuthentication(authentication);

        Student student =
                getAuthenticatedStudent(
                        authentication
                );

        if (!student.getStudentId().equals(studentId)) {

            throw new IllegalStateException(
                    "You are not authorized to register subjects for another student."
            );
        }

        Integer year =
                student.getYearLevel();

        Integer semester =
                student.getSemester();

        if (year == null ||
                semester == null) {

            throw new IllegalArgumentException(
                    "Student year and semester must be defined."
            );
        }


        // -------------------------------------------------
        // YEAR 4 SEMESTER 2 / DEGREE CHECK
        // -------------------------------------------------

        if (year == 4 &&
                semester == 2 &&
                degreeType != DegreeType.SPECIAL &&
                student.getDegreeType() != DegreeType.SPECIAL) {

            throw new IllegalArgumentException(
                    "Only Special Degree students can register for Year 4 Semester 2."
            );
        }


        // -------------------------------------------------
        // DEGREE TYPE VALIDATION
        // -------------------------------------------------

        if (year == 1) {

            if (degreeType != null) {

                throw new IllegalArgumentException(
                        "Year 1 students do not select a degree type yet."
                );
            }

        } else if (year == 4) {

            if (degreeType != DegreeType.SPECIAL &&
                    student.getDegreeType() != DegreeType.SPECIAL) {

                throw new IllegalArgumentException(
                        "Year 4 is only available for Special Degree students."
                );
            }

        } else {

            if (degreeType == null &&
                    student.getDegreeType() == null) {

                throw new IllegalArgumentException(
                        "Please select Special Degree or General Degree."
                );
            }

            if (degreeType != null) {

                student.setDegreeType(
                        degreeType
                );

                studentRepository.save(
                        student
                );
            }
        }


        // -------------------------------------------------
        // USE EXISTING STUDENT DEGREE TYPE IF NECESSARY
        // -------------------------------------------------

        if (degreeType == null) {

            degreeType =
                    student.getDegreeType();
        }


        // -------------------------------------------------
        // GET CURRENT REGISTRATIONS
        // -------------------------------------------------

        List<StudentSubjectRegistration> current =
                registrationRepository
                        .findByStudentStudentIdAndAcademicYearAndSemester(
                                studentId,
                                year,
                                semester
                        );


        // -------------------------------------------------
        // PREVENT SECOND REGISTRATION CYCLE
        // -------------------------------------------------

        if (isRegistrationAlreadyComplete(student)) {

            return current;
        }


        // -------------------------------------------------
        // YEAR 1
        // EXACTLY 3 PREFERRED SUBJECTS
        // -------------------------------------------------

        if (year == 1) {

            if (majorSpecializationId != null ||
                    minorSpecializationId != null) {

                throw new IllegalArgumentException(
                        "Year 1 students cannot select major or minor specializations."
                );
            }


            List<Long> ids =
                    preferredSubjectIds == null
                            ? new ArrayList<>()
                            : preferredSubjectIds
                            .stream()
                            .distinct()
                            .toList();


            if (ids.size() != 3) {

                throw new IllegalArgumentException(
                        "Year 1 students must select exactly 3 preferred subjects."
                );
            }


            // Validate preferred subjects first.
            for (Long subjectId :
                    ids) {

                Subject subject =
                        subjectRepository
                                .findById(subjectId)
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Selected subject not found: "
                                                        + subjectId
                                        )
                                );

                validateSelectableSubject(
                        student,
                        subject,
                        false
                );
            }


            // -------------------------------------------------
            // AUTOMATIC COMPULSORY SUBJECTS
            // -------------------------------------------------

            List<Subject> compulsorySubjects =
                    subjectRepository
                            .findByCompulsoryTrueAndAcademicYearAndSemester(
                                    year,
                                    semester
                            );

            for (Subject subject :
                    compulsorySubjects) {

                registerIfMissing(
                        student,
                        subject,
                        year,
                        semester
                );
            }


            // -------------------------------------------------
            // PREFERRED SUBJECTS
            // -------------------------------------------------

            for (Long subjectId :
                    ids) {

                registerIfMissing(
                        student,
                        subjectRepository
                                .findById(subjectId)
                                .orElseThrow(),
                        year,
                        semester
                );
            }
        }


        // -------------------------------------------------
        // YEAR 2 / YEAR 3
        // MAJOR + MINOR
        // -------------------------------------------------

        if (year == 2 ||
                year == 3) {

            if (majorSpecializationId == null ||
                    minorSpecializationId == null) {

                throw new IllegalArgumentException(
                        "Year " + year +
                                " students must select both major and minor specializations."
                );
            }


            if (majorSpecializationId.equals(
                    minorSpecializationId
            )) {

                throw new IllegalArgumentException(
                        "Major and minor specializations must be different."
                );
            }


            Specialization major =
                    specializationRepository
                            .findById(
                                    majorSpecializationId
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Major specialization not found."
                                    )
                            );


            Specialization minor =
                    specializationRepository
                            .findById(
                                    minorSpecializationId
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Minor specialization not found."
                                    )
                            );


            student.setMajorSpecialization(
                    major
            );

            student.setMinorSpecialization(
                    minor
            );

            studentRepository.save(
                    student
            );


            registerAutomaticSubjects(
                    student,
                    year,
                    semester
            );
        }


        // -------------------------------------------------
        // YEAR 4
        // MAJOR ONLY
        // -------------------------------------------------

        if (year == 4) {

            if (majorSpecializationId == null) {

                throw new IllegalArgumentException(
                        "Year 4 students must select a major specialization."
                );
            }


            if (minorSpecializationId != null) {

                throw new IllegalArgumentException(
                        "Year 4 students cannot select a minor specialization."
                );
            }


            Specialization major =
                    specializationRepository
                            .findById(
                                    majorSpecializationId
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Major specialization not found."
                                    )
                            );


            student.setMajorSpecialization(
                    major
            );

            student.setMinorSpecialization(
                    null
            );

            studentRepository.save(
                    student
            );


            registerAutomaticSubjects(
                    student,
                    year,
                    semester
            );
        }


        // =================================================
        // MARK FIRST-TIME REGISTRATION AS COMPLETED
        // =================================================
        //
        // This is saved only after the complete registration
        // process has succeeded.
        // =================================================

        student.setRegistrationCompleted(
                true
        );

        studentRepository.save(
                student
        );


        // -------------------------------------------------
        // RETURN CURRENT REGISTRATIONS
        // -------------------------------------------------

        return registrationRepository
                .findByStudentStudentIdAndAcademicYearAndSemester(
                        studentId,
                        year,
                        semester
                );
    }


    // =====================================================
    // REGISTER AUTOMATIC SUBJECTS
    // =====================================================

    private void registerAutomaticSubjects(
            Student student,
            Integer year,
            Integer semester) {

        List<Subject> compulsory =
                subjectRepository
                        .findByCompulsoryTrueAndAcademicYearAndSemester(
                                year,
                                semester
                        );


        for (Subject subject :
                compulsory) {

            registerIfMissing(
                    student,
                    subject,
                    year,
                    semester
            );
        }


        for (Subject subject :
                getEligibleSpecializationSubjects(
                        student,
                        year,
                        semester
                )) {

            registerIfMissing(
                    student,
                    subject,
                    year,
                    semester
            );
        }
    }


    // =====================================================
    // GET ELIGIBLE SPECIALIZATION SUBJECTS
    // =====================================================

    private List<Subject> getEligibleSpecializationSubjects(
            Student student,
            Integer year,
            Integer semester) {

        List<Specialization> specializations =
                new ArrayList<>();


        if (student.getMajorSpecialization() != null) {

            specializations.add(
                    student.getMajorSpecialization()
            );
        }


        if (student.getMinorSpecialization() != null &&
                (student.getMajorSpecialization() == null ||
                        !student.getMinorSpecialization()
                                .getSpecializationId()
                                .equals(
                                        student.getMajorSpecialization()
                                                .getSpecializationId()
                                ))) {

            specializations.add(
                    student.getMinorSpecialization()
            );
        }


        if (specializations.isEmpty()) {

            return new ArrayList<>();
        }


        List<Subject> all =
                subjectRepository
                        .findByAcademicYearAndSemesterAndSpecializationIn(
                                year,
                                semester,
                                specializations
                        )
                        .stream()
                        .filter(
                                s ->
                                        !Boolean.TRUE.equals(
                                                s.getCompulsory()
                                        )
                        )
                        .sorted(
                                java.util.Comparator.comparing(
                                        Subject::getSubjectCode,
                                        String.CASE_INSENSITIVE_ORDER
                                )
                        )
                        .toList();


        List<Subject> result =
                new ArrayList<>();


        for (Specialization specialization :
                specializations) {

            List<Subject> bySpec =
                    all.stream()
                            .filter(
                                    s ->
                                            s.getSpecialization() != null &&
                                                    specialization
                                                            .getSpecializationId()
                                                            .equals(
                                                                    s.getSpecialization()
                                                                            .getSpecializationId()
                                                            )
                            )
                            .toList();


            boolean isMajor =
                    student.getMajorSpecialization() != null &&
                            specialization
                                    .getSpecializationId()
                                    .equals(
                                            student.getMajorSpecialization()
                                                    .getSpecializationId()
                                    );


            // -------------------------------------------------
            // YEAR 3 SEMESTER 2 HAS EXPLICIT DEGREE SUBJECTS
            // -------------------------------------------------
            // Special Degree:
            //   Major -> 321, 322, 323, 324, 325
            //   Minor -> 321
            //
            // General Degree:
            //   Major -> 321, 322, 323, 326
            //   Minor -> 321
            //
            // Do NOT select these subjects using a generic
            // first-N limit because 326 is General-only while
            // 324 and 325 are Special-only.
            // -------------------------------------------------
            if (year == 3 && semester == 2) {

                DegreeType degreeType =
                        student.getDegreeType();

                if (degreeType == null) {
                    continue;
                }

                for (Subject subject : bySpec) {

                    String subjectCode =
                            subject.getSubjectCode();

                    if (subjectCode == null) {
                        continue;
                    }

                    String normalizedCode =
                            subjectCode.trim()
                                    .toUpperCase();

                    boolean allowed = false;

                    if (degreeType == DegreeType.SPECIAL) {

                        if (isMajor) {
                            allowed =
                                    hasSubjectCodeSuffix(
                                            normalizedCode,
                                            "321",
                                            "322",
                                            "323",
                                            "324",
                                            "325"
                                    );
                        } else {
                            allowed =
                                    hasSubjectCodeSuffix(
                                            normalizedCode,
                                            "321"
                                    );
                        }

                    } else if (degreeType == DegreeType.GENERAL) {

                        if (isMajor) {
                            allowed =
                                    hasSubjectCodeSuffix(
                                            normalizedCode,
                                            "321",
                                            "322",
                                            "323",
                                            "326"
                                    );
                        } else {
                            allowed =
                                    hasSubjectCodeSuffix(
                                            normalizedCode,
                                            "321"
                                    );
                        }
                    }

                    if (allowed) {
                        result.add(subject);
                    }
                }

                continue;
            }


            int limit =
                    getDegreeSubjectLimit(
                            student.getDegreeType(),
                            year,
                            semester,
                            isMajor
                    );


            if (limit < 0) {

                limit =
                        bySpec.size();
            }


            result.addAll(
                    bySpec.subList(
                            0,
                            Math.min(
                                    limit,
                                    bySpec.size()
                            )
                    )
            );
        }


        return result;
    }


    // =====================================================
    // CHECK SUBJECT CODE SUFFIX
    // =====================================================

    private boolean hasSubjectCodeSuffix(
            String subjectCode,
            String... allowedSuffixes) {

        if (subjectCode == null ||
                subjectCode.trim().isEmpty() ||
                allowedSuffixes == null) {

            return false;
        }

        String normalizedCode =
                subjectCode.trim()
                        .toUpperCase();

        for (String suffix : allowedSuffixes) {

            if (suffix != null &&
                    normalizedCode.endsWith(
                            suffix.toUpperCase()
                    )) {

                return true;
            }
        }

        return false;
    }


    // =====================================================
    // DEGREE SUBJECT LIMIT
    // =====================================================

    private int getDegreeSubjectLimit(
            DegreeType degreeType,
            Integer year,
            Integer semester,
            boolean major) {

        if (degreeType == null) {

            return 0;
        }


        if (year == 2) {

            if (degreeType ==
                    DegreeType.SPECIAL) {

                return major
                        ? 3
                        : 1;
            }

            return major
                    ? 2
                    : 2;
        }


        if (year == 3 &&
                semester == 1) {

            if (degreeType ==
                    DegreeType.SPECIAL) {

                return major
                        ? 5
                        : 1;
            }

            return major
                    ? 3
                    : 1;
        }


        if (year == 3 &&
                semester == 2) {

            if (degreeType ==
                    DegreeType.GENERAL) {

                return major
                        ? 4
                        : 1;
            }

            return -1;
        }


        if (year == 4 &&
                semester == 1) {

            return degreeType ==
                    DegreeType.SPECIAL &&
                    major
                    ? 5
                    : 0;
        }


        return 0;
    }


    // =====================================================
    // REGISTER IF MISSING
    // =====================================================

    private void registerIfMissing(
            Student student,
            Subject subject,
            Integer year,
            Integer semester) {

        boolean exists =
                registrationRepository
                        .existsByStudentStudentIdAndSubjectSubjectIdAndAcademicYearAndSemester(
                                student.getStudentId(),
                                subject.getSubjectId(),
                                year,
                                semester
                        );


        if (!exists) {

            StudentSubjectRegistration registration =
                    new StudentSubjectRegistration();

            registration.setStudent(
                    student
            );

            registration.setSubject(
                    subject
            );

            registration.setAcademicYear(
                    year
            );

            registration.setSemester(
                    semester
            );


            registrationRepository.save(
                    registration
            );
        }
    }


    // =====================================================
    // VALIDATE SELECTABLE SUBJECT
    // =====================================================

    private void validateSelectableSubject(
            Student student,
            Subject subject,
            boolean allowCompulsory) {

        if (subject.getAcademicYear() == null ||
                !subject.getAcademicYear()
                        .equals(
                                student.getYearLevel()
                        ) ||
                subject.getSemester() == null ||
                !subject.getSemester()
                        .equals(
                                student.getSemester()
                        )) {

            throw new IllegalArgumentException(
                    "Selected subject does not belong to the student's current year and semester."
            );
        }


        if (Boolean.TRUE.equals(
                subject.getCompulsory()
        ) &&
                !allowCompulsory) {

            throw new IllegalArgumentException(
                    "Compulsory subjects are registered automatically. Please select only preferred subjects."
            );
        }
    }


    // =====================================================
    // CHECK REGISTRATION COMPLETION
    // =====================================================

    private boolean isRegistrationAlreadyComplete(
            Student student) {

        return Boolean.TRUE.equals(
                student.getRegistrationCompleted()
        );
    }


    // =====================================================
    // CHECK ALL ELIGIBLE SUBJECTS REGISTERED
    // =====================================================

    private boolean allEligibleSubjectsRegistered(
            Student student,
            Set<Long> registeredIds) {

        List<Subject> compulsory =
                subjectRepository
                        .findByCompulsoryTrueAndAcademicYearAndSemester(
                                student.getYearLevel(),
                                student.getSemester()
                        );


        for (Subject subject :
                compulsory) {

            if (subject.getSubjectId() == null ||
                    !registeredIds.contains(
                            subject.getSubjectId()
                    )) {

                return false;
            }
        }


        for (Subject subject :
                getEligibleSpecializationSubjects(
                        student,
                        student.getYearLevel(),
                        student.getSemester()
                )) {

            if (subject.getSubjectId() != null &&
                    !registeredIds.contains(
                            subject.getSubjectId()
                    )) {

                return false;
            }
        }


        return true;
    }


    // =====================================================
    // DELETE REGISTRATION
    // =====================================================

    @Override
    public void deleteRegistration(
            Long id,
            Authentication authentication) {

        validateAuthentication(authentication);

        if (id == null) {

            throw new IllegalArgumentException(
                    "Registration ID is required."
            );
        }


        boolean isAdmin =
                hasRole(
                        authentication,
                        "ROLE_ADMIN"
                );

        boolean isStudent =
                hasRole(
                        authentication,
                        "ROLE_STUDENT"
                );


        if (!isAdmin && !isStudent) {

            throw new IllegalStateException(
                    "You are not authorized to delete registrations."
            );
        }


        StudentSubjectRegistration registration =
                registrationRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Registration not found with ID: "
                                                + id
                                )
                        );


        // -------------------------------------------------
        // STUDENT OWNERSHIP
        // -------------------------------------------------

        if (isStudent) {

            Student authenticatedStudent =
                    getAuthenticatedStudent(
                            authentication
                    );

            if (registration.getStudent() == null ||
                    !authenticatedStudent
                            .getStudentId()
                            .equals(
                                    registration
                                            .getStudent()
                                            .getStudentId()
                            )) {

                throw new IllegalStateException(
                        "You are not authorized to delete another student's registration."
                );
            }
        }


        registrationRepository.delete(
                registration
        );
    }


    // =====================================================
    // AUTHENTICATION HELPERS
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
                authentication.getName().trim().isEmpty()) {

            throw new IllegalStateException(
                    "Authenticated username is invalid."
            );
        }
    }


    private boolean hasRole(
            Authentication authentication,
            String role) {

        if (authentication == null ||
                authentication.getAuthorities() == null) {

            return false;
        }

        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(
                        authority ->
                                authority != null &&
                                        role.equals(
                                                authority.getAuthority()
                                        )
                );
    }


    private Student getAuthenticatedStudent(
            Authentication authentication) {

        validateAuthentication(
                authentication
        );

        String username =
                authentication
                        .getName()
                        .trim();


        return studentRepository
                .findByUser_Username(username)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Student profile not found."
                        )
                );
    }


    private void validateStudentExists(
            Long studentId) {

        if (studentId == null) {

            throw new IllegalArgumentException(
                    "Student ID is required."
            );
        }

        if (!studentRepository.existsById(
                studentId
        )) {

            throw new IllegalArgumentException(
                    "Student not found with ID: "
                            + studentId
            );
        }
    }


    // =====================================================
    // HOD DEPARTMENT HELPERS
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
                        .getName()
                        .trim();


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
                hod.getDepartment()
                        .getDepartmentId() == null) {

            throw new IllegalStateException(
                    "HOD department information is invalid."
            );
        }


        return hod
                .getDepartment()
                .getDepartmentId();
    }


    private void validateRegistrationDepartmentAccess(
            StudentSubjectRegistration registration,
            Long hodDepartmentId) {

        if (registration == null) {

            throw new IllegalStateException(
                    "Registration data is invalid."
            );
        }


        if (hodDepartmentId == null) {

            throw new IllegalStateException(
                    "HOD department information is invalid."
            );
        }


        if (registration.getSubject() == null) {

            throw new IllegalStateException(
                    "Registration subject information is invalid."
            );
        }


        validateSubjectDepartmentAccess(
                registration.getSubject(),
                hodDepartmentId
        );
    }


    private void validateSubjectDepartmentAccess(
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


        if (!subject
                .getDepartment()
                .getDepartmentId()
                .equals(
                        hodDepartmentId
                )) {

            throw new IllegalStateException(
                    "You are not authorized to access registrations outside your department."
            );
        }
    }


    private List<StudentSubjectRegistration>
    filterRegistrationsByDepartment(
            List<StudentSubjectRegistration> registrations,
            Long hodDepartmentId) {

        if (registrations == null ||
                registrations.isEmpty()) {

            return new ArrayList<>();
        }


        if (hodDepartmentId == null) {

            throw new IllegalStateException(
                    "HOD department information is invalid."
            );
        }


        List<StudentSubjectRegistration>
                filteredRegistrations =
                new ArrayList<>();


        for (StudentSubjectRegistration registration :
                registrations) {

            if (registration == null ||
                    registration.getSubject() == null ||
                    registration.getSubject()
                            .getDepartment() == null ||
                    registration.getSubject()
                            .getDepartment()
                            .getDepartmentId() == null) {

                continue;
            }


            Long registrationDepartmentId =
                    registration
                            .getSubject()
                            .getDepartment()
                            .getDepartmentId();


            if (hodDepartmentId.equals(
                    registrationDepartmentId
            )) {

                filteredRegistrations.add(
                        registration
                );
            }
        }


        return filteredRegistrations;
    }
}
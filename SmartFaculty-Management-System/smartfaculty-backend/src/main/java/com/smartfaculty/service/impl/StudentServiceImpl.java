package com.smartfaculty.service.impl;

import com.smartfaculty.dto.StudentRegistrationRequest;
import com.smartfaculty.dto.StudentResponse;
import com.smartfaculty.entity.Department;
import com.smartfaculty.entity.Lecturer;
import com.smartfaculty.entity.Specialization;
import com.smartfaculty.entity.Student;
import com.smartfaculty.entity.User;
import com.smartfaculty.entity.Subject;
import com.smartfaculty.entity.StudentSubjectRegistration;
import com.smartfaculty.enums.UserRole;
import com.smartfaculty.enums.DegreeType;
import com.smartfaculty.repository.LecturerRepository;
import com.smartfaculty.repository.SpecializationRepository;
import com.smartfaculty.repository.StudentRepository;
import com.smartfaculty.repository.UserRepository;
import com.smartfaculty.repository.SubjectRepository;
import com.smartfaculty.repository.StudentSubjectRegistrationRepository;
import com.smartfaculty.service.StudentService;
import com.smartfaculty.repository.NotificationRepository;
import org.springframework.transaction.annotation.Transactional;

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
public class StudentServiceImpl
        implements StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpecializationRepository specializationRepository;

    @Autowired
    private LecturerRepository lecturerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private StudentSubjectRegistrationRepository registrationRepository;

    @Autowired
    private NotificationRepository notificationRepository;


    // =====================================================
    // REGISTER STUDENT
    // =====================================================

    @Override
    @Transactional
    public StudentResponse registerStudent(
            StudentRegistrationRequest request) {

        validateStudentRequest(request);


        String registrationNumber =
                request.getRegistrationNumber().trim();

        String email =
                request.getEmail().trim();


        // =================================================
        // CHECK DUPLICATES
        // =================================================

        if (studentRepository.existsByRegistrationNumber(
                registrationNumber
        )) {

            throw new IllegalStateException(
                    "A student with this registration number already exists."
            );
        }


        if (studentRepository.existsByEmail(email)) {

            throw new IllegalStateException(
                    "A student with this email already exists."
            );
        }


        if (userRepository.existsByUsername(
                registrationNumber
        )) {

            throw new IllegalStateException(
                    "A user account with this registration number already exists."
            );
        }


        // =================================================
        // LOAD SPECIALIZATIONS
        // =================================================

        Specialization major =
                loadMajorSpecialization(request);

        Specialization minor =
                loadMinorSpecialization(request);


        // =================================================
        // CREATE USER ACCOUNT
        // =================================================

        User user = new User();

        user.setUsername(
                registrationNumber
        );

        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        user.setRole(
                UserRole.STUDENT
        );

        user.setEnabled(true);


        User savedUser =
                userRepository.save(user);


        // =================================================
        // CREATE STUDENT
        // =================================================

        Student student =
                new Student();

        student.setRegistrationNumber(
                registrationNumber
        );

        student.setStudentName(
                request.getStudentName().trim()
        );

        student.setEmail(
                email
        );

        student.setYearLevel(
                request.getYearLevel()
        );

        student.setSemester(
                request.getSemester()
        );

        student.setDegreeType(
                request.getDegreeType()
        );

        student.setMajorSpecialization(
                major
        );

        student.setMinorSpecialization(
                minor
        );

        student.setUser(
                savedUser
        );

        /*
         * New students have NOT completed the first-time
         * subject registration process yet.
         */
        student.setRegistrationCompleted(false);


        Student savedStudent =
                studentRepository.save(student);


        // Automatically register compulsory subjects for the
        // student's current academic year and semester.
        autoRegisterCompulsorySubjects(savedStudent);


        return convertToResponse(
                savedStudent
        );
    }


    // =====================================================
    // AUTO-REGISTER COMPULSORY SUBJECTS
    // =====================================================

    private void autoRegisterCompulsorySubjects(
            Student student) {

        if (student == null ||
                student.getStudentId() == null ||
                student.getYearLevel() == null ||
                student.getSemester() == null) {
            return;
        }

        List<Subject> compulsorySubjects =
                subjectRepository
                        .findByCompulsoryTrueAndAcademicYearAndSemester(
                                student.getYearLevel(),
                                student.getSemester()
                        );

        for (Subject subject : compulsorySubjects) {

            if (subject == null ||
                    subject.getSubjectId() == null) {
                continue;
            }

            boolean exists =
                    registrationRepository
                            .existsByStudentStudentIdAndSubjectSubjectIdAndAcademicYearAndSemester(
                                    student.getStudentId(),
                                    subject.getSubjectId(),
                                    student.getYearLevel(),
                                    student.getSemester()
                            );

            if (exists) {
                continue;
            }

            StudentSubjectRegistration registration =
                    new StudentSubjectRegistration();

            registration.setStudent(student);
            registration.setSubject(subject);
            registration.setAcademicYear(student.getYearLevel());
            registration.setSemester(student.getSemester());

            registrationRepository.save(registration);
        }
    }


    // =====================================================
    // GET ALL STUDENTS
    // =====================================================

    @Override
    public List<StudentResponse> getAllStudents(
            Authentication authentication) {

        validateAuthentication(authentication);


        // =================================================
        // ADMIN
        // =================================================

        if (hasRole(
                authentication,
                "ROLE_ADMIN"
        )) {

            return studentRepository
                    .findAll()
                    .stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
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

            return studentRepository
                    .findAll()
                    .stream()
                    .filter(student ->
                            isStudentInDepartment(
                                    student,
                                    hodDepartmentId
                            )
                    )
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        }


        // =================================================
        // STUDENT - OWN RECORD ONLY
        // =================================================

        if (hasRole(
                authentication,
                "ROLE_STUDENT"
        )) {

            String username = authentication.getName();

            Student ownStudent =
                    studentRepository
                            .findByUser_Username(username)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Student account not found."
                                    )
                            );

            return List.of(
                    convertToResponse(ownStudent)
            );
        }


        // =================================================
        // OTHER ROLES
        // =================================================

        throw new IllegalStateException(
                "You are not authorized to access student management."
        );
    }


    // =====================================================
    // GET STUDENT BY ID
    // =====================================================

    @Override
    public StudentResponse getStudentById(
            Long id,
            Authentication authentication) {

        validateAuthentication(authentication);


        if (id == null) {

            throw new IllegalArgumentException(
                    "Student ID is required."
            );
        }


        Student student =
                studentRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Student not found."
                                )
                        );


        // =================================================
        // ADMIN
        // =================================================

        if (hasRole(
                authentication,
                "ROLE_ADMIN"
        )) {

            return convertToResponse(
                    student
            );
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

            validateHODStudentAccess(
                    student,
                    hodDepartmentId
            );

            return convertToResponse(
                    student
            );
        }


        // =================================================
        // STUDENT - OWN RECORD ONLY
        // =================================================

        if (hasRole(
                authentication,
                "ROLE_STUDENT"
        )) {

            String username = authentication.getName();

            Student ownStudent =
                    studentRepository
                            .findByUser_Username(username)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Student account not found."
                                    )
                            );

            if (!ownStudent.getStudentId().equals(student.getStudentId())) {
                throw new IllegalStateException(
                        "You are not authorized to access another student's details."
                );
            }

            return convertToResponse(student);
        }


        // =================================================
        // OTHER ROLES
        // =================================================

        throw new IllegalStateException(
                "You are not authorized to access student management."
        );
    }


    // =====================================================
    // UPDATE STUDENT
    // =====================================================

    @Override
    @Transactional
    public StudentResponse updateStudent(
            Long id,
            StudentRegistrationRequest request,
            Authentication authentication) {

        validateAuthentication(authentication);


        if (id == null) {

            throw new IllegalArgumentException(
                    "Student ID is required."
            );
        }


        validateStudentRequestForUpdate(
                request
        );


        Student student =
                studentRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Student not found."
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

            validateHODStudentAccess(
                    student,
                    hodDepartmentId
            );
        }


        // =================================================
        // ONLY ADMIN OR HOD CAN CONTINUE
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
                    "You are not authorized to update students."
            );
        }


        String registrationNumber =
                request.getRegistrationNumber().trim();

        String email =
                request.getEmail().trim();


        // =================================================
        // CHECK REGISTRATION NUMBER
        // =================================================

        if (!registrationNumber.equals(
                student.getRegistrationNumber()
        )) {

            if (studentRepository.existsByRegistrationNumber(
                    registrationNumber
            )) {

                throw new IllegalStateException(
                        "Another student already uses this registration number."
                );
            }


            if (userRepository.existsByUsername(
                    registrationNumber
            )) {

                User currentUser =
                        student.getUser();

                if (currentUser == null ||
                        !registrationNumber.equals(
                                currentUser.getUsername()
                        )) {

                    throw new IllegalStateException(
                            "A user account with this registration number already exists."
                    );
                }
            }
        }


        // =================================================
        // CHECK EMAIL
        // =================================================

        if (!email.equals(
                student.getEmail()
        )) {

            if (studentRepository.existsByEmail(
                    email
            )) {

                throw new IllegalStateException(
                        "Another student already uses this email."
                );
            }
        }


        // =================================================
        // LOAD SPECIALIZATIONS
        // =================================================

        Specialization major =
                loadMajorSpecialization(request);

        Specialization minor =
                loadMinorSpecialization(request);


        // =================================================
        // UPDATE STUDENT
        // =================================================

        student.setRegistrationNumber(
                registrationNumber
        );

        student.setStudentName(
                request.getStudentName().trim()
        );

        student.setEmail(
                email
        );

        student.setYearLevel(
                request.getYearLevel()
        );

        student.setSemester(
                request.getSemester()
        );

        if (request.getDegreeType() != null) {
            student.setDegreeType(request.getDegreeType());
        }

        student.setMajorSpecialization(
                major
        );

        student.setMinorSpecialization(
                minor
        );


        // =================================================
        // UPDATE USER ACCOUNT
        // =================================================

        User user =
                student.getUser();


        if (user != null) {

            user.setUsername(
                    registrationNumber
            );


            if (request.getPassword() != null &&
                    !request.getPassword()
                            .trim()
                            .isEmpty()) {

                user.setPassword(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                );
            }


            userRepository.save(user);
        }


        Student updatedStudent =
                studentRepository.save(
                        student
                );


        return convertToResponse(
                updatedStudent
        );
    }


    // =====================================================
    // DELETE STUDENT
    // =====================================================

    @Transactional
    @Override
    public void deleteStudent(Long id, Authentication authentication) {

        if (id == null) {
            throw new RuntimeException("Invalid student ID.");
        }

        Student student = studentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Student not found."));

        // Keep your existing HOD department authorization check here.
        // Keep your existing ADMIN/HOD role validation here.

        User user = student.getUser();

        // Check whether the student still has subject registrations
        // Check whether the student still has subject registrations
        if (registrationRepository.existsByStudentStudentId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete this student because subject registrations still exist. Please remove all subject registrations first."
            );
        }

        // Delete notifications linked to this student first
        notificationRepository.deleteByStudent_StudentId(id);

        // Delete the student
        studentRepository.delete(student);

        // Delete the associated user account
        if (user != null) {
            userRepository.delete(user);
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
    // CHECK STUDENT DEPARTMENT
    // =====================================================

    private boolean isStudentInDepartment(
            Student student,
            Long departmentId) {

        if (student == null ||
                departmentId == null) {

            return false;
        }


        // =================================================
        // CHECK MAJOR SPECIALIZATION
        // =================================================

        Specialization major =
                student.getMajorSpecialization();


        if (major != null &&
                major.getDepartment() != null &&
                major.getDepartment()
                        .getDepartmentId() != null) {

            if (departmentId.equals(
                    major.getDepartment()
                            .getDepartmentId()
            )) {

                return true;
            }
        }


        // =================================================
        // CHECK MINOR SPECIALIZATION
        // =================================================

        Specialization minor =
                student.getMinorSpecialization();


        if (minor != null &&
                minor.getDepartment() != null &&
                minor.getDepartment()
                        .getDepartmentId() != null) {

            if (departmentId.equals(
                    minor.getDepartment()
                            .getDepartmentId()
            )) {

                return true;
            }
        }


        return false;
    }


    // =====================================================
    // VALIDATE HOD STUDENT ACCESS
    // =====================================================

    private void validateHODStudentAccess(
            Student student,
            Long hodDepartmentId) {

        if (student == null) {

            throw new IllegalStateException(
                    "Student information is invalid."
            );
        }


        if (hodDepartmentId == null) {

            throw new IllegalStateException(
                    "HOD department information is invalid."
            );
        }


        if (!isStudentInDepartment(
                student,
                hodDepartmentId
        )) {

            throw new IllegalStateException(
                    "You are not authorized to access students outside your department."
            );
        }
    }


    // =====================================================
    // VALIDATE CREATE REQUEST
    // =====================================================

    private void validateStudentRequest(
            StudentRegistrationRequest request) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Student data cannot be null."
            );
        }


        if (isBlank(
                request.getRegistrationNumber()
        )) {

            throw new IllegalArgumentException(
                    "Registration number is required."
            );
        }


        if (isBlank(
                request.getStudentName()
        )) {

            throw new IllegalArgumentException(
                    "Student name is required."
            );
        }


        if (isBlank(
                request.getEmail()
        )) {

            throw new IllegalArgumentException(
                    "Email is required."
            );
        }


        if (isBlank(
                request.getPassword()
        )) {

            throw new IllegalArgumentException(
                    "Password is required."
            );
        }


        validateYearAndSemester(
                request
        );

        validateSpecializations(
                request
        );
    }


    // =====================================================
    // VALIDATE UPDATE REQUEST
    // =====================================================

    private void validateStudentRequestForUpdate(
            StudentRegistrationRequest request) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Student data cannot be null."
            );
        }


        if (isBlank(
                request.getRegistrationNumber()
        )) {

            throw new IllegalArgumentException(
                    "Registration number is required."
            );
        }


        if (isBlank(
                request.getStudentName()
        )) {

            throw new IllegalArgumentException(
                    "Student name is required."
            );
        }


        if (isBlank(
                request.getEmail()
        )) {

            throw new IllegalArgumentException(
                    "Email is required."
            );
        }


        validateYearAndSemester(
                request
        );

        validateSpecializations(
                request
        );
    }


    // =====================================================
    // YEAR + SEMESTER VALIDATION
    // =====================================================

    private void validateYearAndSemester(
            StudentRegistrationRequest request) {

        if (request.getYearLevel() == null ||
                request.getYearLevel() < 1 ||
                request.getYearLevel() > 4) {

            throw new IllegalArgumentException(
                    "Year level must be between 1 and 4."
            );
        }


        if (request.getSemester() == null ||
                request.getSemester() < 1 ||
                request.getSemester() > 2) {

            throw new IllegalArgumentException(
                    "Semester must be either 1 or 2."
            );
        }


        if (request.getYearLevel() == 4 &&
                request.getDegreeType() != null &&
                request.getDegreeType() != DegreeType.SPECIAL) {

            throw new IllegalArgumentException(
                    "Year 4 students can only follow the Special Degree."
            );
        }

        if (request.getYearLevel() == 4 &&
                request.getSemester() == 2 &&
                request.getDegreeType() != DegreeType.SPECIAL) {

            throw new IllegalArgumentException(
                    "Year 4 Semester 2 is available only for the Special Degree."
            );
        }
    }


    // =====================================================
    // SPECIALIZATION VALIDATION
    // =====================================================

    private void validateSpecializations(
            StudentRegistrationRequest request) {

        Integer year =
                request.getYearLevel();


        // =================================================
        // YEAR 1
        // =================================================

        if (year == 1) {

            if (request.getMajorSpecializationId() != null ||
                    request.getMinorSpecializationId() != null) {

                throw new IllegalArgumentException(
                        "Year 1 students do not have major or minor specializations."
                );
            }
        }


        // =================================================
        // YEAR 2
        // =================================================

        if (year == 2) {

            if (request.getMajorSpecializationId() == null) {

                throw new IllegalArgumentException(
                        "Year 2 students must select a major specialization."
                );
            }


            if (request.getMinorSpecializationId() == null) {

                throw new IllegalArgumentException(
                        "Year 2 students must select a minor specialization."
                );
            }


            if (request.getMajorSpecializationId()
                    .equals(
                            request.getMinorSpecializationId()
                    )) {

                throw new IllegalArgumentException(
                        "Major and minor specializations must be different."
                );
            }
        }


        // =================================================
        // YEAR 3
        // =================================================

        if (year == 3) {

            if (request.getMajorSpecializationId() == null) {

                throw new IllegalArgumentException(
                        "Year 3 students must select a major specialization."
                );
            }


            if (request.getMinorSpecializationId() == null) {

                throw new IllegalArgumentException(
                        "Year 3 students must select a minor specialization."
                );
            }


            if (request.getMajorSpecializationId()
                    .equals(
                            request.getMinorSpecializationId()
                    )) {

                throw new IllegalArgumentException(
                        "Major and minor specializations must be different."
                );
            }
        }


        // =================================================
        // YEAR 4
        // =================================================

        if (year == 4) {

            if (request.getDegreeType() != null &&
                    request.getDegreeType() != DegreeType.SPECIAL) {

                throw new IllegalArgumentException(
                        "Year 4 students must follow the Special Degree."
                );
            }

            if (request.getMajorSpecializationId() == null) {

                throw new IllegalArgumentException(
                        "Year 4 students must select a major specialization."
                );
            }


            if (request.getMinorSpecializationId() != null) {

                throw new IllegalArgumentException(
                        "Year 4 students do not have a minor specialization."
                );
            }
        }
    }


    // =====================================================
    // LOAD MAJOR SPECIALIZATION
    // =====================================================

    private Specialization loadMajorSpecialization(
            StudentRegistrationRequest request) {

        if (request.getMajorSpecializationId() == null) {

            return null;
        }


        return specializationRepository
                .findById(
                        request.getMajorSpecializationId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Major specialization not found."
                        )
                );
    }


    // =====================================================
    // LOAD MINOR SPECIALIZATION
    // =====================================================

    private Specialization loadMinorSpecialization(
            StudentRegistrationRequest request) {

        if (request.getMinorSpecializationId() == null) {

            return null;
        }


        return specializationRepository
                .findById(
                        request.getMinorSpecializationId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Minor specialization not found."
                        )
                );
    }


    // =====================================================
    // BLANK CHECK
    // =====================================================

    private boolean isBlank(
            String value) {

        return value == null ||
                value.trim().isEmpty();
    }


    // =====================================================
    // CONVERT ENTITY → RESPONSE DTO
    // =====================================================

    private StudentResponse convertToResponse(
            Student student) {

        StudentResponse response =
                new StudentResponse();


        response.setStudentId(
                student.getStudentId()
        );


        response.setRegistrationNumber(
                student.getRegistrationNumber()
        );


        response.setStudentName(
                student.getStudentName()
        );


        response.setEmail(
                student.getEmail()
        );


        response.setYearLevel(
                student.getYearLevel()
        );


        response.setSemester(
                student.getSemester()
        );


        response.setDegreeType(
                student.getDegreeType()
        );


        // =================================================
        // REGISTRATION COMPLETION STATUS
        // =================================================

        response.setRegistrationCompleted(
                Boolean.TRUE.equals(
                        student.getRegistrationCompleted()
                )
        );


        // =================================================
        // MAJOR
        // =================================================

        if (student.getMajorSpecialization() != null) {

            Specialization major =
                    student.getMajorSpecialization();


            response.setMajorSpecializationId(
                    major.getSpecializationId()
            );


            response.setMajorSpecializationCode(
                    major.getSpecializationCode()
            );


            response.setMajorSpecializationName(
                    major.getSpecializationName()
            );
        }


        // =================================================
        // MINOR
        // =================================================

        if (student.getMinorSpecialization() != null) {

            Specialization minor =
                    student.getMinorSpecialization();


            response.setMinorSpecializationId(
                    minor.getSpecializationId()
            );


            response.setMinorSpecializationCode(
                    minor.getSpecializationCode()
            );


            response.setMinorSpecializationName(
                    minor.getSpecializationName()
            );
        }


        return response;
    }
}
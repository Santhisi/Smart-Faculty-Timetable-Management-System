package com.smartfaculty.service.impl;

import com.smartfaculty.entity.AcademicYear;
import com.smartfaculty.entity.Semester;
import com.smartfaculty.entity.Student;
import com.smartfaculty.entity.StudentSubjectRegistration;
import com.smartfaculty.entity.Timetable;
import com.smartfaculty.entity.Lecturer;
import com.smartfaculty.entity.LecturerAllocation;
import com.smartfaculty.entity.Student;

import com.smartfaculty.repository.StudentRepository;
import com.smartfaculty.repository.StudentSubjectRegistrationRepository;
import com.smartfaculty.repository.TimetableRepository;
import com.smartfaculty.repository.LecturerRepository;
import com.smartfaculty.repository.LecturerAllocationRepository;
import com.smartfaculty.repository.TimetableChangeRequestRepository;

import com.smartfaculty.service.NotificationService;
import com.smartfaculty.service.TimetableService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TimetableServiceImpl
        implements TimetableService {


    @Autowired
    private TimetableRepository timetableRepository;

    @Autowired
    private TimetableChangeRequestRepository changeRequestRepository;


    @Autowired
    private StudentRepository studentRepository;


    @Autowired
    private StudentSubjectRegistrationRepository registrationRepository;

    @Autowired
    private LecturerRepository lecturerRepository;


    @Autowired
    private LecturerAllocationRepository allocationRepository;


    @Autowired
    private NotificationService notificationService;


    // =========================================================
    // SAVE / UPDATE
    // =========================================================

    @Override
    public Timetable saveTimetable(
            Timetable timetable
    ) {
        validateAdminAuthorization();

        if (timetable == null) {

            throw new RuntimeException(
                    "Timetable data cannot be null."
            );
        }


        if (timetable.getLecturerAllocation() == null
                || timetable
                .getLecturerAllocation()
                .getAllocationId() == null) {

            throw new RuntimeException(
                    "Lecturer allocation is required."
            );
        }


        if (timetable.getClassroom() == null
                || timetable
                .getClassroom()
                .getClassroomId() == null) {

            throw new RuntimeException(
                    "Classroom is required."
            );
        }


        if (timetable.getTimeSlot() == null
                || timetable
                .getTimeSlot()
                .getTimeSlotId() == null) {

            throw new RuntimeException(
                    "Time slot is required."
            );
        }


        if (timetable.getSpecialization() == null
                || timetable
                .getSpecialization()
                .getSpecializationId() == null) {

            throw new RuntimeException(
                    "Specialization is required."
            );
        }


        if (timetable.getYearLevel() == null) {

            throw new RuntimeException(
                    "Academic year is required."
            );
        }


        if (timetable.getSemester() == null) {

            throw new RuntimeException(
                    "Semester is required."
            );
        }


        Long timetableId =
                timetable.getTimetableId();


        // =====================================================
        // IMMUTABLE FIELDS DURING UPDATE
        // =====================================================

        // Subject / Lecturer are controlled by the HOD
        // through the lecturer allocation.
        // Specialization is permanent once the timetable entry
        // has been created.
        // Academic year and semester remain tied to the entry.
        if (timetableId != null) {

            Timetable existingTimetable =
                    timetableRepository
                            .findById(timetableId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Timetable not found with ID: "
                                                    + timetableId
                                    )
                            );

            Long existingAllocationId =
                    existingTimetable.getLecturerAllocation() != null
                            ? existingTimetable
                            .getLecturerAllocation()
                            .getAllocationId()
                            : null;

            Long newAllocationId =
                    timetable.getLecturerAllocation() != null
                            ? timetable
                            .getLecturerAllocation()
                            .getAllocationId()
                            : null;

            if (!java.util.Objects.equals(
                    existingAllocationId,
                    newAllocationId
            )) {
                throw new RuntimeException(
                        "Subject and lecturer are assigned by the HOD and cannot be changed."
                );
            }

            Long existingSpecializationId =
                    existingTimetable.getSpecialization() != null
                            ? existingTimetable
                            .getSpecialization()
                            .getSpecializationId()
                            : null;

            Long newSpecializationId =
                    timetable.getSpecialization() != null
                            ? timetable
                            .getSpecialization()
                            .getSpecializationId()
                            : null;

            if (!java.util.Objects.equals(
                    existingSpecializationId,
                    newSpecializationId
            )) {
                throw new RuntimeException(
                        "Specialization is permanent and cannot be changed."
                );
            }

            if (existingTimetable.getYearLevel() !=
                    timetable.getYearLevel()) {
                throw new RuntimeException(
                        "Academic year cannot be changed for an existing timetable entry."
                );
            }

            if (existingTimetable.getSemester() !=
                    timetable.getSemester()) {
                throw new RuntimeException(
                        "Semester cannot be changed for an existing timetable entry."
                );
            }
        }


        Long classroomId =
                timetable
                        .getClassroom()
                        .getClassroomId();


        Long timeSlotId =
                timetable
                        .getTimeSlot()
                        .getTimeSlotId();


        // =====================================================
        // LOAD LECTURER ALLOCATION FROM DATABASE
        // =====================================================
        // The frontend sends only the allocation ID. Do not trust
        // or dereference a nested Lecturer object from the request.
        // Load the complete allocation from the database instead.
        // This also prevents the null Lecturer error that occurred
        // when allocation.getLecturer() was not populated.
        // =====================================================

        Long allocationId =
                timetable
                        .getLecturerAllocation()
                        .getAllocationId();

        LecturerAllocation persistedAllocation =
                allocationRepository
                        .findById(allocationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Lecturer allocation not found with ID: "
                                                + allocationId
                                )
                        );

        if (persistedAllocation.getLecturer() == null
                || persistedAllocation.getLecturer().getLecturerId() == null) {

            throw new RuntimeException(
                    "The selected lecturer allocation is invalid: no lecturer is assigned to this allocation. "
                            + "Please create/fix the lecturer allocation before adding the timetable."
            );
        }

        if (persistedAllocation.getSubject() == null
                || persistedAllocation.getSubject().getSubjectId() == null) {

            throw new RuntimeException(
                    "The selected lecturer allocation is invalid: no subject is assigned to this allocation."
            );
        }

        // Always use the complete database entity for the timetable.
        timetable.setLecturerAllocation(persistedAllocation);

        Long lecturerId =
                persistedAllocation
                        .getLecturer()
                        .getLecturerId();


        if (lecturerId == null) {

            throw new RuntimeException(
                    "Lecturer is required."
            );
        }


        // =====================================================
        // CLASSROOM CONFLICT
        // =====================================================

        boolean classroomConflict;


        if (timetableId == null) {

            classroomConflict =
                    timetableRepository
                            .existsByClassroom_ClassroomIdAndTimeSlot_TimeSlotId(
                                    classroomId,
                                    timeSlotId
                            );

        } else {

            classroomConflict =
                    timetableRepository
                            .existsByClassroom_ClassroomIdAndTimeSlot_TimeSlotIdAndTimetableIdNot(
                                    classroomId,
                                    timeSlotId,
                                    timetableId
                            );
        }


        if (classroomConflict) {

            throw new RuntimeException(
                    "Classroom conflict: The selected classroom is already occupied during this time slot."
            );
        }


        // =====================================================
        // LECTURER CONFLICT
        // =====================================================

        boolean lecturerConflict;


        if (timetableId == null) {

            lecturerConflict =
                    timetableRepository
                            .existsByLecturerAllocation_Lecturer_LecturerIdAndTimeSlot_TimeSlotId(
                                    lecturerId,
                                    timeSlotId
                            );

        } else {

            lecturerConflict =
                    timetableRepository
                            .existsByLecturerAllocation_Lecturer_LecturerIdAndTimeSlot_TimeSlotIdAndTimetableIdNot(
                                    lecturerId,
                                    timeSlotId,
                                    timetableId
                            );
        }


        if (lecturerConflict) {

            throw new RuntimeException(
                    "Lecturer conflict: The selected lecturer is already assigned during this time slot."
            );
        }


        // =====================================================
        // STUDENT CLASH DETECTION
        // =====================================================
        // A student may register for subjects from different
        // departments/specializations. A timetable entry is a
        // clash only when the same registered student is actually
        // eligible for both subjects and both subjects use the
        // same time slot in the same academic year/semester.
        // =====================================================

        validateStudentClash(
                timetable,
                timetableId
        );


        // =====================================================
        // FIND OLD RECORD
        // =====================================================

        Timetable oldTimetable = null;


        if (timetableId != null) {

            oldTimetable =
                    timetableRepository
                            .findById(timetableId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Timetable not found with ID: "
                                                    + timetableId
                                    )
                            );
        }


        // =====================================================
        // SAVE
        // =====================================================

        Timetable savedTimetable =
                timetableRepository.save(timetable);


        // =====================================================
        // UPDATE NOTIFICATION
        // =====================================================

        if (oldTimetable != null) {

            boolean classroomChanged =
                    !sameClassroom(
                            oldTimetable,
                            savedTimetable
                    );


            boolean timeSlotChanged =
                    !sameTimeSlot(
                            oldTimetable,
                            savedTimetable
                    );


            boolean lecturerChanged =
                    !sameLecturer(
                            oldTimetable,
                            savedTimetable
                    );


            boolean specializationChanged =
                    !sameSpecialization(
                            oldTimetable,
                            savedTimetable
                    );


            if (classroomChanged
                    || timeSlotChanged
                    || lecturerChanged
                    || specializationChanged) {

                sendTimetableChangeNotification(
                        oldTimetable,
                        savedTimetable,
                        classroomChanged,
                        timeSlotChanged,
                        lecturerChanged
                );
            }
        }


        return savedTimetable;
    }


    // =========================================================
    // SAME CLASSROOM
    // =========================================================

    private boolean sameClassroom(
            Timetable oldTimetable,
            Timetable newTimetable
    ) {

        if (oldTimetable.getClassroom() == null
                || newTimetable.getClassroom() == null) {

            return oldTimetable.getClassroom()
                    == newTimetable.getClassroom();
        }


        return oldTimetable
                .getClassroom()
                .getClassroomId()
                .equals(
                        newTimetable
                                .getClassroom()
                                .getClassroomId()
                );
    }


    // =========================================================
    // SAME TIME SLOT
    // =========================================================

    private boolean sameTimeSlot(
            Timetable oldTimetable,
            Timetable newTimetable
    ) {

        if (oldTimetable.getTimeSlot() == null
                || newTimetable.getTimeSlot() == null) {

            return oldTimetable.getTimeSlot()
                    == newTimetable.getTimeSlot();
        }


        return oldTimetable
                .getTimeSlot()
                .getTimeSlotId()
                .equals(
                        newTimetable
                                .getTimeSlot()
                                .getTimeSlotId()
                );
    }


    // =========================================================
    // SAME LECTURER
    // =========================================================

    private boolean sameLecturer(
            Timetable oldTimetable,
            Timetable newTimetable
    ) {

        if (oldTimetable.getLecturerAllocation() == null
                || newTimetable
                .getLecturerAllocation() == null) {

            return oldTimetable
                    .getLecturerAllocation()
                    == newTimetable
                    .getLecturerAllocation();
        }


        if (oldTimetable
                .getLecturerAllocation()
                .getLecturer() == null
                || newTimetable
                .getLecturerAllocation()
                .getLecturer() == null) {

            return false;
        }


        return oldTimetable
                .getLecturerAllocation()
                .getLecturer()
                .getLecturerId()
                .equals(
                        newTimetable
                                .getLecturerAllocation()
                                .getLecturer()
                                .getLecturerId()
                );
    }


    // =========================================================
    // SAME SPECIALIZATION
    // =========================================================

    private boolean sameSpecialization(
            Timetable oldTimetable,
            Timetable newTimetable
    ) {

        if (oldTimetable.getSpecialization() == null
                || newTimetable.getSpecialization() == null) {

            return oldTimetable
                    .getSpecialization()
                    == newTimetable
                    .getSpecialization();
        }


        return oldTimetable
                .getSpecialization()
                .getSpecializationId()
                .equals(
                        newTimetable
                                .getSpecialization()
                                .getSpecializationId()
                );
    }


    // =========================================================
    // STUDENT CLASH VALIDATION
    // =========================================================
    // A clash exists only when the SAME student has actually
    // registered for BOTH subjects and the two timetable entries
    // use the same time slot in the same year/semester.
    //
    // This is intentionally based on StudentSubjectRegistration,
    // not only on major/minor eligibility. A student who is not
    // registered for the second subject must not create a clash.
    // =========================================================

    private void validateStudentClash(
            Timetable timetable,
            Long editingId
    ) {

        if (timetable == null
                || timetable.getLecturerAllocation() == null
                || timetable.getLecturerAllocation().getSubject() == null
                || timetable.getLecturerAllocation().getSubject().getSubjectId() == null
                || timetable.getTimeSlot() == null
                || timetable.getTimeSlot().getTimeSlotId() == null
                || timetable.getYearLevel() == null
                || timetable.getSemester() == null) {
            return;
        }

        Long newSubjectId =
                timetable.getLecturerAllocation()
                        .getSubject()
                        .getSubjectId();

        Long newTimeSlotId =
                timetable.getTimeSlot()
                        .getTimeSlotId();

        List<StudentSubjectRegistration> registrations =
                registrationRepository.findAll();

        List<Timetable> allTimetables =
                timetableRepository.findAll();

        for (StudentSubjectRegistration newRegistration : registrations) {

            if (newRegistration == null
                    || newRegistration.getStudent() == null
                    || newRegistration.getSubject() == null
                    || newRegistration.getSubject().getSubjectId() == null
                    || !newSubjectId.equals(
                    newRegistration.getSubject().getSubjectId()
            )) {
                continue;
            }

            Student student =
                    newRegistration.getStudent();

            Long studentId =
                    student.getStudentId();

            if (studentId == null) {
                continue;
            }

            // Find every subject actually registered by this same student.
            Set<Long> registeredSubjectIds =
                    new HashSet<>();

            for (StudentSubjectRegistration registration : registrations) {

                if (registration == null
                        || registration.getStudent() == null
                        || registration.getSubject() == null
                        || registration.getSubject().getSubjectId() == null) {
                    continue;
                }

                if (studentId.equals(
                        registration.getStudent().getStudentId()
                )) {
                    registeredSubjectIds.add(
                            registration.getSubject().getSubjectId()
                    );
                }
            }

            for (Timetable existing : allTimetables) {

                if (existing == null) {
                    continue;
                }

                if (editingId != null
                        && editingId.equals(existing.getTimetableId())) {
                    continue;
                }

                if (existing.getYearLevel() != timetable.getYearLevel()
                        || existing.getSemester() != timetable.getSemester()) {
                    continue;
                }

                if (existing.getTimeSlot() == null
                        || existing.getTimeSlot().getTimeSlotId() == null
                        || !newTimeSlotId.equals(
                        existing.getTimeSlot().getTimeSlotId()
                )) {
                    continue;
                }

                if (existing.getLecturerAllocation() == null
                        || existing.getLecturerAllocation().getSubject() == null
                        || existing.getLecturerAllocation().getSubject().getSubjectId() == null) {
                    continue;
                }

                Long existingSubjectId =
                        existing.getLecturerAllocation()
                                .getSubject()
                                .getSubjectId();

                // Same subject is not a student clash.
                if (newSubjectId.equals(existingSubjectId)) {
                    continue;
                }

                // IMPORTANT: the student must actually be registered
                // for the existing subject as well.
                if (!registeredSubjectIds.contains(existingSubjectId)) {
                    continue;
                }

                String studentName =
                        student.getStudentName() != null
                                ? student.getStudentName()
                                : "Student ID " + studentId;

                String newCode =
                        timetable.getLecturerAllocation()
                                .getSubject()
                                .getSubjectCode();

                String existingCode =
                        existing.getLecturerAllocation()
                                .getSubject()
                                .getSubjectCode();

                throw new RuntimeException(
                        "Student timetable clash: "
                                + studentName
                                + " is registered for "
                                + newCode
                                + " and "
                                + existingCode
                                + ". Both subjects are scheduled in the same time slot. "
                                + "The timetable entry cannot be added."
                );
            }
        }
    }


    // =========================================================
    // STUDENT SUBJECT ELIGIBILITY
    // =========================================================

    private boolean isStudentEligibleForSubject(
            Student student,
            com.smartfaculty.entity.Subject subject
    ) {

        if (student == null || subject == null) {
            return false;
        }

        // Year 1 subjects are common for first-year students.
        if (student.getYearLevel() != null
                && student.getYearLevel() == 1) {
            return true;
        }

        // Compulsory subjects are available to the relevant student group.
        if (Boolean.TRUE.equals(subject.getCompulsory())) {
            return true;
        }

        if (subject.getSpecialization() == null
                || subject.getSpecialization().getSpecializationId() == null) {
            return false;
        }

        Long subjectSpecializationId =
                subject.getSpecialization().getSpecializationId();

        if (student.getMajorSpecialization() != null
                && student.getMajorSpecialization().getSpecializationId() != null
                && student.getMajorSpecialization()
                .getSpecializationId()
                .equals(subjectSpecializationId)) {
            return true;
        }

        return student.getMinorSpecialization() != null
                && student.getMinorSpecialization().getSpecializationId() != null
                && student.getMinorSpecialization()
                .getSpecializationId()
                .equals(subjectSpecializationId);
    }


    // =========================================================
    // SEND NOTIFICATION
    // =========================================================

    private void sendTimetableChangeNotification(
            Timetable oldTimetable,
            Timetable newTimetable,
            boolean classroomChanged,
            boolean timeSlotChanged,
            boolean lecturerChanged
    ) {

        if (newTimetable
                .getLecturerAllocation() == null
                || newTimetable
                .getLecturerAllocation()
                .getSubject() == null) {

            return;
        }


        Long subjectId =
                newTimetable
                        .getLecturerAllocation()
                        .getSubject()
                        .getSubjectId();


        String subjectCode =
                newTimetable
                        .getLecturerAllocation()
                        .getSubject()
                        .getSubjectCode();


        String title =
                "Timetable Update - "
                        + subjectCode;


        StringBuilder message =
                new StringBuilder();


        message.append(
                subjectCode
        );


        message.append(
                " timetable has been updated."
        );


        if (classroomChanged) {

            message.append(
                    " Classroom: "
            );


            message.append(
                    getClassroomCode(
                            oldTimetable
                    )
            );


            message.append(
                    " → "
            );


            message.append(
                    getClassroomCode(
                            newTimetable
                    )
            );
        }


        if (timeSlotChanged) {

            message.append(
                    " Time: "
            );


            message.append(
                    getTimeSlotDetails(
                            oldTimetable
                    )
            );


            message.append(
                    " → "
            );


            message.append(
                    getTimeSlotDetails(
                            newTimetable
                    )
            );
        }


        if (lecturerChanged) {

            message.append(
                    " Lecturer: "
            );


            message.append(
                    getLecturerName(
                            oldTimetable
                    )
            );


            message.append(
                    " → "
            );


            message.append(
                    getLecturerName(
                            newTimetable
                    )
            );
        }


        notificationService
                .notifyAllStudentsForSubject(
                        subjectId,
                        title,
                        message.toString(),
                        "TIMETABLE_UPDATE"
                );
    }


    // =========================================================
    // CLASSROOM CODE
    // =========================================================

    private String getClassroomCode(
            Timetable timetable
    ) {

        if (timetable == null
                || timetable.getClassroom() == null) {

            return "Not Assigned";
        }


        String roomCode =
                timetable
                        .getClassroom()
                        .getRoomCode();


        return roomCode != null
                ? roomCode
                : "Not Assigned";
    }


    // =========================================================
    // TIME DETAILS
    // =========================================================

    private String getTimeSlotDetails(
            Timetable timetable
    ) {

        if (timetable == null
                || timetable.getTimeSlot() == null) {

            return "Not Assigned";
        }


        return timetable
                .getTimeSlot()
                .getDay()
                .toString()
                + " "
                + timetable
                .getTimeSlot()
                .getStartTime()
                .toString()
                + " - "
                + timetable
                .getTimeSlot()
                .getEndTime()
                .toString();
    }


    // =========================================================
    // LECTURER NAME
    // =========================================================

    private String getLecturerName(
            Timetable timetable
    ) {

        if (timetable == null
                || timetable.getLecturerAllocation() == null
                || timetable
                .getLecturerAllocation()
                .getLecturer() == null) {

            return "Not Assigned";
        }


        String name =
                timetable
                        .getLecturerAllocation()
                        .getLecturer()
                        .getLecturerName();


        return name != null
                ? name
                : "Not Assigned";
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @Override
    public List<Timetable> getAllTimetables() {

        return timetableRepository.findAll();
    }

    // =========================================================
    // VALIDATE TIMETABLE ID ACCESS
    // =========================================================

    private void validateTimetableAccess(
            Timetable timetable
    ) {

        if (timetable == null) {

            throw new IllegalArgumentException(
                    "Timetable not found."
            );
        }

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "Authentication is required."
            );
        }

        // =====================================================
        // ADMIN
        // =====================================================

        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority != null &&
                                        "ROLE_ADMIN".equals(
                                                authority.getAuthority()
                                        )
                        );

        if (isAdmin) {
            return;
        }

        // =====================================================
        // HOD
        // =====================================================

        boolean isHOD =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority != null &&
                                        "ROLE_HOD".equals(
                                                authority.getAuthority()
                                        )
                        );

        if (isHOD) {

            String username =
                    authentication.getName();

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

            if (hod.getDepartment() == null ||
                    hod.getDepartment().getDepartmentId() == null) {

                throw new IllegalStateException(
                        "HOD department is not configured."
                );
            }

            if (timetable.getLecturerAllocation() == null ||
                    timetable.getLecturerAllocation().getLecturer() == null ||
                    timetable.getLecturerAllocation()
                            .getLecturer()
                            .getDepartment() == null ||
                    timetable.getLecturerAllocation()
                            .getLecturer()
                            .getDepartment()
                            .getDepartmentId() == null) {

                throw new IllegalStateException(
                        "Timetable department information is invalid."
                );
            }

            Long hodDepartmentId =
                    hod.getDepartment()
                            .getDepartmentId();

            Long timetableDepartmentId =
                    timetable.getLecturerAllocation()
                            .getLecturer()
                            .getDepartment()
                            .getDepartmentId();

            if (!hodDepartmentId.equals(
                    timetableDepartmentId
            )) {

                throw new IllegalStateException(
                        "You are not authorized to access a timetable outside your department."
                );
            }

            return;
        }

        // =====================================================
        // LECTURER
        // =====================================================

        boolean isLecturer =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority != null &&
                                        "ROLE_LECTURER".equals(
                                                authority.getAuthority()
                                        )
                        );

        if (isLecturer) {

            String username =
                    authentication.getName();

            if (username == null ||
                    username.trim().isEmpty()) {

                throw new IllegalStateException(
                        "Authenticated username is invalid."
                );
            }

            Lecturer lecturer =
                    lecturerRepository
                            .findByUser_Username(
                                    username.trim()
                            )
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Lecturer profile not found."
                                    )
                            );

            if (lecturer.getLecturerId() == null) {

                throw new IllegalStateException(
                        "Lecturer profile ID is invalid."
                );
            }

            if (timetable.getLecturerAllocation() == null ||
                    timetable.getLecturerAllocation().getLecturer() == null ||
                    timetable.getLecturerAllocation()
                            .getLecturer()
                            .getLecturerId() == null) {

                throw new IllegalStateException(
                        "Timetable lecturer information is invalid."
                );
            }

            Long timetableLecturerId =
                    timetable.getLecturerAllocation()
                            .getLecturer()
                            .getLecturerId();

            if (!lecturer.getLecturerId()
                    .equals(timetableLecturerId)) {

                throw new IllegalStateException(
                        "You are not authorized to access another lecturer's timetable."
                );
            }

            return;
        }

        // =====================================================
        // STUDENT
        // =====================================================

        boolean isStudent =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority != null &&
                                        "ROLE_STUDENT".equals(
                                                authority.getAuthority()
                                        )
                        );

        if (isStudent) {

            String username =
                    authentication.getName();

            if (username == null ||
                    username.trim().isEmpty()) {

                throw new IllegalStateException(
                        "Authenticated username is invalid."
                );
            }

            Student student =
                    studentRepository
                            .findByUser_Username(
                                    username.trim()
                            )
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Student profile not found."
                                    )
                            );

            if (student.getYearLevel() == null ||
                    student.getSemester() == null) {

                throw new IllegalStateException(
                        "Student academic information is incomplete."
                );
            }

            if (timetable.getYearLevel() == null ||
                    timetable.getSemester() == null) {

                throw new IllegalStateException(
                        "Timetable academic information is invalid."
                );
            }

            AcademicYear studentAcademicYear =
                    AcademicYear.valueOf(
                            "YEAR_" + student.getYearLevel()
                    );

            Semester studentSemester =
                    Semester.valueOf(
                            "SEMESTER_" + student.getSemester()
                    );

            if (timetable.getYearLevel()
                    != studentAcademicYear) {

                throw new IllegalStateException(
                        "You are not authorized to access this timetable."
                );
            }

            if (timetable.getSemester()
                    != studentSemester) {

                throw new IllegalStateException(
                        "You are not authorized to access this timetable."
                );
            }

            if (timetable.getLecturerAllocation() == null ||
                    timetable.getLecturerAllocation().getSubject() == null) {

                throw new IllegalStateException(
                        "Timetable subject information is invalid."
                );
            }

            if (!isStudentEligibleForSubject(
                    student,
                    timetable.getLecturerAllocation().getSubject()
            )) {

                throw new IllegalStateException(
                        "You are not authorized to access this timetable."
                );
            }

            return;
        }

        throw new IllegalStateException(
                "You are not authorized to access this timetable."
        );
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    @Override
    public Timetable getTimetableById(
            Long id
    ) {

        if (id == null) {

            throw new IllegalArgumentException(
                    "Timetable ID is required."
            );
        }

        Timetable timetable =
                timetableRepository
                        .findById(id)
                        .orElse(null);

        if (timetable == null) {
            return null;
        }

        validateTimetableAccess(
                timetable
        );

        return timetable;
    }


    // =========================================================
    // GET BY YEAR + SEMESTER
    // =========================================================

    @Override
    public List<Timetable> getByYearAndSemester(
            AcademicYear yearLevel,
            Semester semester
    ) {

        return timetableRepository
                .findByYearLevelAndSemester(
                        yearLevel,
                        semester
                );
    }

    // =========================================================
    // ACTIVE YEAR + SEMESTER
    // =========================================================

    @Override
    public List<Timetable> getActiveByYearAndSemester(
            AcademicYear yearLevel,
            Semester semester
    ) {

        if (yearLevel == null || semester == null) {
            return new ArrayList<>();
        }

        List<Timetable> timetables =
                timetableRepository
                        .findByYearLevelAndSemesterAndActiveTrue(
                                yearLevel,
                                semester
                        );

        /*
         * IMPORTANT:
         * The repository query is already supposed to filter by
         * Academic Year + Semester.
         *
         * This second check guarantees that a timetable belonging
         * to another semester can NEVER be returned to the Home page.
         *
         * Example:
         *
         * Request:
         * YEAR_1 + SEMESTER_2
         *
         * If a SEMESTER_1 record somehow appears in the repository
         * result, it is removed here.
         */

        return timetables
                .stream()
                .filter(timetable ->
                        timetable != null
                                && yearLevel.equals(
                                timetable.getYearLevel()
                        )
                                && semester.equals(
                                timetable.getSemester()
                        )
                )
                .collect(Collectors.toList());
    }


    // =========================================================
    // GET BY SPECIALIZATION
    // =========================================================

    @Override
    public List<Timetable> getBySpecialization(
            Long specializationId
    ) {

        return timetableRepository
                .findBySpecialization_SpecializationId(
                        specializationId
                );
    }

    // =========================================================
    // VALIDATE STUDENT TIMETABLE ACCESS
    // =========================================================

    private void validateStudentTimetableAccess(
            Long studentId
    ) {

        if (studentId == null) {

            throw new IllegalArgumentException(
                    "Student ID is required."
            );
        }

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "Authentication is required."
            );
        }

        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority != null &&
                                        "ROLE_ADMIN".equals(
                                                authority.getAuthority()
                                        )
                        );

        boolean isHOD =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority != null &&
                                        "ROLE_HOD".equals(
                                                authority.getAuthority()
                                        )
                        );

        // ADMIN and HOD access remains allowed.
        if (isAdmin || isHOD) {
            return;
        }

        boolean isStudent =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority != null &&
                                        "ROLE_STUDENT".equals(
                                                authority.getAuthority()
                                        )
                        );

        if (!isStudent) {

            throw new IllegalStateException(
                    "You are not authorized to access student timetables."
            );
        }

        String username =
                authentication.getName();

        if (username == null ||
                username.trim().isEmpty()) {

            throw new IllegalStateException(
                    "Authenticated username is invalid."
            );
        }

        Student authenticatedStudent =
                studentRepository
                        .findByUser_Username(
                                username.trim()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Student profile not found."
                                )
                        );

        if (authenticatedStudent.getStudentId() == null) {

            throw new IllegalStateException(
                    "Student profile ID is invalid."
            );
        }

        if (!authenticatedStudent.getStudentId()
                .equals(studentId)) {

            throw new IllegalStateException(
                    "You are not authorized to access another student's timetable."
            );
        }
    }


    // =========================================================
    // STUDENT TIMETABLE
    // =========================================================

    @Override
    public List<Timetable> getByStudentId(
            Long studentId
    ) {

        validateStudentTimetableAccess(studentId);

        Student student =
                studentRepository
                        .findById(studentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student not found with ID: "
                                                + studentId
                                )
                        );

        if (student.getYearLevel() == null ||
                student.getSemester() == null) {
            return new ArrayList<>();
        }

        AcademicYear academicYear =
                AcademicYear.valueOf(
                        "YEAR_" + student.getYearLevel()
                );

        Semester semester =
                Semester.valueOf(
                        "SEMESTER_" + student.getSemester()
                );

        // IMPORTANT: A student must see timetable entries only
        // for subjects actually registered by that student.
        // This also makes compulsory subjects appear because
        // they are auto-registered when the student is created.
        List<StudentSubjectRegistration> registrations =
                registrationRepository
                        .findByStudentStudentIdAndAcademicYearAndSemester(
                                studentId,
                                student.getYearLevel(),
                                student.getSemester()
                        );

        Set<Long> registeredSubjectIds =
                new HashSet<>();

        for (StudentSubjectRegistration registration : registrations) {
            if (registration != null &&
                    registration.getSubject() != null &&
                    registration.getSubject().getSubjectId() != null) {

                registeredSubjectIds.add(
                        registration.getSubject().getSubjectId()
                );
            }
        }

        if (registeredSubjectIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Fetch all timetable entries for the student's current
        // year/semester, then filter by registered subject ID.
        // This avoids accidentally showing ICT111 or another
        // subject merely because it belongs to the same
        // specialization/year/semester.
        List<Timetable> timetables =
                timetableRepository
                        .findByYearLevelAndSemester(
                                academicYear,
                                semester
                        );

        timetables.removeIf(timetable ->
                timetable == null ||
                        timetable.getLecturerAllocation() == null ||
                        timetable.getLecturerAllocation().getSubject() == null ||
                        timetable.getLecturerAllocation()
                                .getSubject().getSubjectId() == null ||
                        !registeredSubjectIds.contains(
                                timetable.getLecturerAllocation()
                                        .getSubject().getSubjectId()
                        )
        );

        return timetables;
    }


    // =========================================================
    // VALIDATE LECTURER TIMETABLE ACCESS
    // =========================================================

    private void validateLecturerTimetableAccess(
            Long lecturerId
    ) {

        if (lecturerId == null) {

            throw new IllegalArgumentException(
                    "Lecturer ID is required."
            );
        }

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "Authentication is required."
            );
        }

        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority != null &&
                                        "ROLE_ADMIN".equals(
                                                authority.getAuthority()
                                        )
                        );

        boolean isHOD =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority != null &&
                                        "ROLE_HOD".equals(
                                                authority.getAuthority()
                                        )
                        );

        boolean isLecturer =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority != null &&
                                        "ROLE_LECTURER".equals(
                                                authority.getAuthority()
                                        )
                        );

        // ADMIN and HOD access is already controlled
        // by the controller-level authorization.
        if (isAdmin || isHOD) {
            return;
        }

        if (!isLecturer) {

            throw new IllegalStateException(
                    "You are not authorized to access lecturer timetables."
            );
        }

        String username =
                authentication.getName();

        if (username == null ||
                username.trim().isEmpty()) {

            throw new IllegalStateException(
                    "Authenticated username is invalid."
            );
        }

        Lecturer lecturer =
                lecturerRepository
                        .findByUser_Username(username.trim())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Lecturer profile not found."
                                )
                        );

        if (lecturer.getLecturerId() == null) {

            throw new IllegalStateException(
                    "Lecturer profile ID is invalid."
            );
        }

        if (!lecturer.getLecturerId()
                .equals(lecturerId)) {

            throw new IllegalStateException(
                    "You are not authorized to access another lecturer's timetable."
            );
        }
    }


    // =========================================================
    // LECTURER TIMETABLE
    // =========================================================

    @Override
    public List<Timetable> getByLecturerId(
            Long lecturerId
    ) {

        validateLecturerTimetableAccess(
                lecturerId
        );

        return timetableRepository
                .findByLecturerAllocation_Lecturer_LecturerId(
                        lecturerId
                );
    }

    // =========================================================
    // VALIDATE ADMIN AUTHORIZATION
    // =========================================================

    private void validateAdminAuthorization() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "Authentication is required."
            );
        }

        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority != null &&
                                        "ROLE_ADMIN".equals(
                                                authority.getAuthority()
                                        )
                        );

        if (!isAdmin) {

            throw new IllegalStateException(
                    "Administrator authorization is required."
            );
        }
    }


    // =========================================================
    // DELETE
    // =========================================================

    // =========================================================
// DELETE / DEACTIVATE TIMETABLE
// =========================================================

    @Override
    public String deleteTimetable(Long id) {

        validateAdminAuthorization();

        if (id == null) {
            throw new IllegalArgumentException("Timetable ID is required.");
        }

        Timetable timetable = timetableRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Timetable not found with ID: " + id)
                );

        boolean hasChangeRequests =
                changeRequestRepository.existsByTimetable_TimetableId(id);

        if (hasChangeRequests) {

            timetable.setActive(false);
            timetableRepository.save(timetable);

            return "This timetable has existing change requests. " +
                    "It has been deactivated successfully, and its history has been preserved.";
        }

        timetableRepository.delete(timetable);

        return "Timetable deleted permanently successfully.";
    }
}
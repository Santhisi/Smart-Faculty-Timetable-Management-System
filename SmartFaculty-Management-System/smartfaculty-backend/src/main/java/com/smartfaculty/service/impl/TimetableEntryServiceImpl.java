package com.smartfaculty.service.impl;

import com.smartfaculty.entity.Lecturer;
import com.smartfaculty.entity.LecturerAllocation;
import com.smartfaculty.entity.Subject;
import com.smartfaculty.entity.TimetableEntry;
import com.smartfaculty.repository.LecturerAllocationRepository;
import com.smartfaculty.repository.LecturerRepository;
import com.smartfaculty.repository.SubjectRepository;
import com.smartfaculty.repository.TimetableEntryRepository;
import com.smartfaculty.service.TimetableEntryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TimetableEntryServiceImpl
        implements TimetableEntryService {


    @Autowired
    private TimetableEntryRepository timetableRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private LecturerRepository lecturerRepository;

    @Autowired
    private LecturerAllocationRepository allocationRepository;


    // =====================================================
    // CREATE
    // =====================================================

    @Override
    public TimetableEntry saveTimetableEntry(
            TimetableEntry timetableEntry,
            Authentication authentication) {

        validateAdminAuthorization(authentication);

        validateTimetableEntry(
                timetableEntry,
                null
        );

        return timetableRepository.save(
                timetableEntry
        );
    }


    // =====================================================
    // GET ALL
    // =====================================================

    @Override
    public List<TimetableEntry>
    getAllTimetableEntries(
            Authentication authentication) {

        validateReadAuthorization(authentication);

        List<TimetableEntry> entries =
                timetableRepository.findAll();

        if (isHOD(authentication)) {
            return filterByHODDepartment(entries, authentication);
        }

        return entries;
    }


    // =====================================================
    // GET BY ID
    // =====================================================

    @Override
    public TimetableEntry
    getTimetableEntryById(
            Long id,
            Authentication authentication) {

        validateReadAuthorization(authentication);

        if (id == null) {

            throw new IllegalArgumentException(
                    "Timetable ID is required."
            );
        }

        TimetableEntry entry =
                timetableRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Timetable entry not found with ID: "
                                                + id
                                )
                        );

        validateEntryReadAccess(entry, authentication);

        return entry;
    }


    // =====================================================
    // GET BY YEAR + SEMESTER
    // =====================================================

    @Override
    public List<TimetableEntry>
    getTimetableByYearAndSemester(
            Integer academicYear,
            Integer semester,
            Authentication authentication) {

        validateReadAuthorization(authentication);

        validateYearAndSemester(
                academicYear,
                semester
        );

        List<TimetableEntry> entries =
                timetableRepository
                        .findByAcademicYearAndSemester(
                                academicYear,
                                semester
                        );

        if (isHOD(authentication)) {
            return filterByHODDepartment(entries, authentication);
        }

        return entries;
    }


    // =====================================================
    // GET BY LECTURER
    // =====================================================

    @Override
    public List<TimetableEntry>
    getTimetableByLecturer(
            Long lecturerId,
            Authentication authentication) {

        validateReadAuthorization(authentication);
        validateLecturerTargetAccess(lecturerId, authentication);

        if (lecturerId == null) {

            throw new IllegalArgumentException(
                    "Lecturer ID is required."
            );
        }

        return timetableRepository
                .findByLecturerLecturerId(
                        lecturerId
                );
    }


    // =====================================================
    // GET BY SUBJECT
    // =====================================================

    @Override
    public List<TimetableEntry>
    getTimetableBySubject(
            Long subjectId,
            Authentication authentication) {

        validateReadAuthorization(authentication);

        if (subjectId == null) {

            throw new IllegalArgumentException(
                    "Subject ID is required."
            );
        }

        List<TimetableEntry> entries =
                timetableRepository
                        .findBySubjectSubjectId(
                                subjectId
                        );

        if (isHOD(authentication)) {
            return filterByHODDepartment(entries, authentication);
        }

        if (isLecturer(authentication)) {
            Long loggedInLecturerId =
                    getAuthenticatedLecturer(authentication)
                            .getLecturerId();

            return entries.stream()
                    .filter(entry ->
                            entry != null &&
                                    entry.getLecturer() != null &&
                                    loggedInLecturerId.equals(
                                            entry.getLecturer().getLecturerId()
                                    ))
                    .toList();
        }

        return entries;
    }


    // =====================================================
    // UPDATE
    // =====================================================

    @Override
    public TimetableEntry updateTimetableEntry(
            Long id,
            TimetableEntry timetableEntry,
            Authentication authentication) {

        validateAdminAuthorization(authentication);

        if (id == null) {

            throw new IllegalArgumentException(
                    "Timetable ID is required."
            );
        }

        TimetableEntry existing =
                timetableRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Timetable entry not found."
                                )
                        );


        validateTimetableEntry(
                timetableEntry,
                id
        );


        existing.setSubject(
                timetableEntry.getSubject()
        );

        existing.setLecturer(
                timetableEntry.getLecturer()
        );

        existing.setAcademicYear(
                timetableEntry.getAcademicYear()
        );

        existing.setSemester(
                timetableEntry.getSemester()
        );

        existing.setDay(
                timetableEntry.getDay()
        );

        existing.setStartTime(
                timetableEntry.getStartTime()
        );

        existing.setEndTime(
                timetableEntry.getEndTime()
        );

        existing.setRoom(
                timetableEntry.getRoom()
        );


        return timetableRepository.save(
                existing
        );
    }


    // =====================================================
    // DELETE
    // =====================================================

    @Override
    public void deleteTimetableEntry(
            Long id,
            Authentication authentication) {

        validateAdminAuthorization(authentication);

        if (id == null) {

            throw new IllegalArgumentException(
                    "Timetable ID is required."
            );
        }

        if (!timetableRepository.existsById(id)) {

            throw new IllegalArgumentException(
                    "Timetable entry not found with ID: "
                            + id
            );
        }

        timetableRepository.deleteById(id);
    }


    // =====================================================
    // VALIDATION
    // =====================================================

    private void validateTimetableEntry(
            TimetableEntry timetableEntry,
            Long editingId) {


        if (timetableEntry == null) {

            throw new IllegalArgumentException(
                    "Timetable data cannot be null."
            );
        }


        // =================================================
        // YEAR
        // =================================================

        validateYearAndSemester(
                timetableEntry.getAcademicYear(),
                timetableEntry.getSemester()
        );


        // =================================================
        // DAY
        // =================================================

        if (timetableEntry.getDay() == null ||
                timetableEntry.getDay().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Day is required."
            );
        }


        // =================================================
        // START TIME
        // =================================================

        if (timetableEntry.getStartTime() == null ||
                timetableEntry.getStartTime().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Start time is required."
            );
        }


        // =================================================
        // END TIME
        // =================================================

        if (timetableEntry.getEndTime() == null ||
                timetableEntry.getEndTime().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "End time is required."
            );
        }


        // =================================================
        // TIME VALIDATION
        // =================================================

        if (!isValidTime(
                timetableEntry.getStartTime()
        ) ||
                !isValidTime(
                        timetableEntry.getEndTime()
                )) {

            throw new IllegalArgumentException(
                    "Time must use HH:mm format."
            );
        }


        if (timetableEntry.getStartTime()
                .compareTo(
                        timetableEntry.getEndTime()
                ) >= 0) {

            throw new IllegalArgumentException(
                    "End time must be after start time."
            );
        }


        // =================================================
        // ROOM
        // =================================================

        if (timetableEntry.getRoom() == null ||
                timetableEntry.getRoom().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Room is required."
            );
        }


        // =================================================
        // SUBJECT
        // =================================================

        if (timetableEntry.getSubject() == null ||
                timetableEntry.getSubject().getSubjectId() == null) {

            throw new IllegalArgumentException(
                    "Subject is required."
            );
        }


        // =================================================
        // LECTURER
        // =================================================

        if (timetableEntry.getLecturer() == null ||
                timetableEntry.getLecturer().getLecturerId() == null) {

            throw new IllegalArgumentException(
                    "Lecturer is required."
            );
        }


        // =================================================
        // LOAD DATABASE SUBJECT
        // =================================================

        Long subjectId =
                timetableEntry
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


        // =================================================
        // LOAD DATABASE LECTURER
        // =================================================

        Long lecturerId =
                timetableEntry
                        .getLecturer()
                        .getLecturerId();

        Lecturer lecturer =
                lecturerRepository
                        .findById(lecturerId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Lecturer not found with ID: "
                                                + lecturerId
                                )
                        );


        // =================================================
        // SUBJECT YEAR
        // =================================================

        if (!timetableEntry
                .getAcademicYear()
                .equals(subject.getAcademicYear())) {

            throw new IllegalArgumentException(
                    "The subject does not belong to "
                            + "the selected academic year."
            );
        }


        // =================================================
        // SUBJECT SEMESTER
        // =================================================

        if (!timetableEntry
                .getSemester()
                .equals(subject.getSemester())) {

            throw new IllegalArgumentException(
                    "The subject does not belong to "
                            + "the selected semester."
            );
        }


        // =================================================
        // CHECK LECTURER ALLOCATION
        // =================================================

        List<LecturerAllocation> allocations =
                allocationRepository
                        .findByLecturerLecturerIdAndSubjectSubjectIdAndAcademicYearAndSemester(
                                lecturerId,
                                subjectId,
                                timetableEntry.getAcademicYear(),
                                timetableEntry.getSemester()
                        );


        if (allocations == null ||
                allocations.isEmpty()) {

            throw new IllegalArgumentException(
                    "This lecturer is not allocated "
                            + "to this subject for the selected "
                            + "academic year and semester."
            );
        }


        // =================================================
        // LECTURER CONFLICT
        // =================================================

        List<TimetableEntry> lecturerEntries =
                timetableRepository
                        .findByLecturerLecturerIdAndAcademicYearAndSemesterAndDay(
                                lecturerId,
                                timetableEntry.getAcademicYear(),
                                timetableEntry.getSemester(),
                                timetableEntry.getDay()
                        );


        for (TimetableEntry entry :
                lecturerEntries) {

            if (editingId != null &&
                    entry.getTimetableId()
                            .equals(editingId)) {

                continue;
            }


            if (timesOverlap(
                    timetableEntry.getStartTime(),
                    timetableEntry.getEndTime(),
                    entry.getStartTime(),
                    entry.getEndTime()
            )) {

                throw new IllegalArgumentException(
                        "This lecturer already has another "
                                + "class at the selected time."
                );
            }
        }


        // =================================================
        // ROOM CONFLICT
        // =================================================

        List<TimetableEntry> roomEntries =
                timetableRepository
                        .findByRoomAndAcademicYearAndSemesterAndDay(
                                timetableEntry.getRoom()
                                        .trim(),
                                timetableEntry.getAcademicYear(),
                                timetableEntry.getSemester(),
                                timetableEntry.getDay()
                        );


        for (TimetableEntry entry :
                roomEntries) {

            if (editingId != null &&
                    entry.getTimetableId()
                            .equals(editingId)) {

                continue;
            }


            if (timesOverlap(
                    timetableEntry.getStartTime(),
                    timetableEntry.getEndTime(),
                    entry.getStartTime(),
                    entry.getEndTime()
            )) {

                throw new IllegalArgumentException(
                        "The selected room is already occupied "
                                + "at the selected time."
                );
            }
        }


        // =================================================
        // YEAR GROUP CONFLICT
        // =================================================

        List<TimetableEntry> yearEntries =
                timetableRepository
                        .findByAcademicYearAndSemesterAndDay(
                                timetableEntry.getAcademicYear(),
                                timetableEntry.getSemester(),
                                timetableEntry.getDay()
                        );


        for (TimetableEntry entry :
                yearEntries) {

            if (editingId != null &&
                    entry.getTimetableId()
                            .equals(editingId)) {

                continue;
            }


            if (timesOverlap(
                    timetableEntry.getStartTime(),
                    timetableEntry.getEndTime(),
                    entry.getStartTime(),
                    entry.getEndTime()
            )) {

                throw new IllegalArgumentException(
                        "This academic year already has another "
                                + "subject scheduled at the selected time."
                );
            }
        }


        // =================================================
        // USE DATABASE ENTITIES
        // =================================================

        timetableEntry.setSubject(subject);
        timetableEntry.setLecturer(lecturer);
        timetableEntry.setDay(
                timetableEntry.getDay().trim()
        );
        timetableEntry.setRoom(
                timetableEntry.getRoom().trim()
        );
    }


    // =====================================================
    // YEAR + SEMESTER VALIDATION
    // =====================================================

    private void validateYearAndSemester(
            Integer academicYear,
            Integer semester) {

        if (academicYear == null) {

            throw new IllegalArgumentException(
                    "Academic year is required."
            );
        }


        if (academicYear < 1 ||
                academicYear > 4) {

            throw new IllegalArgumentException(
                    "Academic year must be between 1 and 4."
            );
        }


        if (semester == null) {

            throw new IllegalArgumentException(
                    "Semester is required."
            );
        }


        if (semester < 1 ||
                semester > 2) {

            throw new IllegalArgumentException(
                    "Semester must be either 1 or 2."
            );
        }


        // =================================================
        // YEAR 4 ONLY HAS SEMESTER 1
        // =================================================

        if (academicYear == 4 &&
                semester == 2) {

            throw new IllegalArgumentException(
                    "Year 4 currently has only Semester 1."
            );
        }
    }


    // =====================================================
    // TIME FORMAT
    // =====================================================

    private boolean isValidTime(
            String time) {

        return time != null &&
                time.matches(
                        "^([01]\\d|2[0-3]):[0-5]\\d$"
                );
    }


    // =====================================================
    // TIME OVERLAP
    // =====================================================

    private boolean timesOverlap(
            String start1,
            String end1,
            String start2,
            String end2) {

        return start1.compareTo(end2) < 0 &&
                end1.compareTo(start2) > 0;
    }

    // =====================================================
    // PART 15X - SERVICE-LEVEL AUTHORIZATION
    // =====================================================

    private Authentication getCurrentAuthentication() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getName() == null) {

            throw new SecurityException(
                    "Authentication is required."
            );
        }

        return authentication;
    }


    private void validateAdminAuthorization(
            Authentication authentication) {

        Authentication current =
                authentication != null
                        ? authentication
                        : getCurrentAuthentication();

        boolean admin =
                current.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                "ROLE_ADMIN".equals(
                                        authority.getAuthority()
                                )
                        );

        if (!admin) {
            throw new SecurityException(
                    "Only administrators are authorized to perform this operation."
            );
        }
    }


    private void validateReadAuthorization(
            Authentication authentication) {

        boolean allowed =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                "ROLE_ADMIN".equals(
                                        authority.getAuthority()
                                ) ||
                                        "ROLE_HOD".equals(
                                                authority.getAuthority()
                                        ) ||
                                        "ROLE_LECTURER".equals(
                                                authority.getAuthority()
                                        )
                        );

        if (!allowed) {
            throw new SecurityException(
                    "You are not authorized to access timetable entries."
            );
        }
    }


    private boolean isAdmin(
            Authentication authentication) {

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN".equals(
                                authority.getAuthority()
                        )
                );
    }


    private boolean isHOD(
            Authentication authentication) {

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_HOD".equals(
                                authority.getAuthority()
                        )
                );
    }


    private boolean isLecturer(
            Authentication authentication) {

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_LECTURER".equals(
                                authority.getAuthority()
                        )
                );
    }


    private Lecturer getAuthenticatedLecturer(
            Authentication authentication) {

        return lecturerRepository
                .findByUser_Username(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new SecurityException(
                                "Lecturer account not found."
                        )
                );
    }


    private void validateLecturerTargetAccess(
            Long lecturerId,
            Authentication authentication) {

        if (lecturerId == null) {
            throw new IllegalArgumentException(
                    "Lecturer ID is required."
            );
        }

        if (isAdmin(authentication)) {
            return;
        }

        Lecturer loggedInLecturer =
                getAuthenticatedLecturer(authentication);

        // A lecturer can access only their own timetable.
        if (isLecturer(authentication)) {

            if (!loggedInLecturer
                    .getLecturerId()
                    .equals(lecturerId)) {

                throw new SecurityException(
                        "You can only access your own timetable."
                );
            }

            return;
        }

        // HOD can access lecturers in the HOD's own department.
        if (isHOD(authentication)) {

            Lecturer requestedLecturer =
                    lecturerRepository
                            .findById(lecturerId)
                            .orElseThrow(() ->
                                    new SecurityException(
                                            "Requested lecturer not found."
                                    )
                            );

            if (loggedInLecturer.getDepartment() == null ||
                    requestedLecturer.getDepartment() == null ||
                    loggedInLecturer.getDepartment().getDepartmentId() == null ||
                    requestedLecturer.getDepartment().getDepartmentId() == null ||
                    !loggedInLecturer
                            .getDepartment()
                            .getDepartmentId()
                            .equals(
                                    requestedLecturer
                                            .getDepartment()
                                            .getDepartmentId()
                            )) {

                throw new SecurityException(
                        "You can only access timetables within your department."
                );
            }
        }
    }


    private void validateEntryReadAccess(
            TimetableEntry entry,
            Authentication authentication) {

        if (entry == null ||
                entry.getLecturer() == null ||
                entry.getLecturer().getLecturerId() == null) {

            throw new SecurityException(
                    "Timetable entry owner could not be determined."
            );
        }

        if (isAdmin(authentication)) {
            return;
        }

        Lecturer loggedInLecturer =
                getAuthenticatedLecturer(authentication);

        if (isLecturer(authentication)) {

            if (!loggedInLecturer
                    .getLecturerId()
                    .equals(
                            entry.getLecturer()
                                    .getLecturerId()
                    )) {

                throw new SecurityException(
                        "You can only access your own timetable entries."
                );
            }

            return;
        }

        if (isHOD(authentication)) {

            if (loggedInLecturer.getDepartment() == null ||
                    loggedInLecturer.getDepartment().getDepartmentId() == null ||
                    entry.getLecturer().getDepartment() == null ||
                    entry.getLecturer().getDepartment().getDepartmentId() == null ||
                    !loggedInLecturer
                            .getDepartment()
                            .getDepartmentId()
                            .equals(
                                    entry.getLecturer()
                                            .getDepartment()
                                            .getDepartmentId()
                            )) {

                throw new SecurityException(
                        "You can only access timetable entries within your department."
                );
            }

            return;
        }

        throw new SecurityException(
                "You are not authorized to access this timetable entry."
        );
    }


    private List<TimetableEntry> filterByHODDepartment(
            List<TimetableEntry> entries,
            Authentication authentication) {

        Lecturer hod =
                getAuthenticatedLecturer(authentication);

        if (hod.getDepartment() == null ||
                hod.getDepartment().getDepartmentId() == null) {

            throw new SecurityException(
                    "HOD department could not be determined."
            );
        }

        Long departmentId =
                hod.getDepartment().getDepartmentId();

        return entries.stream()
                .filter(entry ->
                        entry != null &&
                                entry.getLecturer() != null &&
                                entry.getLecturer().getDepartment() != null &&
                                entry.getLecturer()
                                        .getDepartment()
                                        .getDepartmentId() != null &&
                                departmentId.equals(
                                        entry.getLecturer()
                                                .getDepartment()
                                                .getDepartmentId()
                                ))
                .toList();
    }

}


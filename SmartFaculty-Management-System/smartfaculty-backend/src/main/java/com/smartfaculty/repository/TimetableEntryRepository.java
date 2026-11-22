package com.smartfaculty.repository;

import com.smartfaculty.entity.TimetableEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimetableEntryRepository
        extends JpaRepository<TimetableEntry, Long> {
    boolean existsByLecturerLecturerId(Long lecturerId);


    // =====================================================
    // GET TIMETABLE BY YEAR + SEMESTER
    // =====================================================

    List<TimetableEntry>
    findByAcademicYearAndSemester(
            Integer academicYear,
            Integer semester
    );


    // =====================================================
    // GET TIMETABLE BY LECTURER
    // =====================================================

    List<TimetableEntry>
    findByLecturerLecturerId(
            Long lecturerId
    );


    // =====================================================
    // GET TIMETABLE BY SUBJECT
    // =====================================================

    List<TimetableEntry>
    findBySubjectSubjectId(
            Long subjectId
    );


    // =====================================================
    // CHECK LECTURER CONFLICT
    // =====================================================

    List<TimetableEntry>
    findByLecturerLecturerIdAndAcademicYearAndSemesterAndDay(
            Long lecturerId,
            Integer academicYear,
            Integer semester,
            String day
    );


    // =====================================================
    // CHECK ROOM CONFLICT
    // =====================================================

    List<TimetableEntry>
    findByRoomAndAcademicYearAndSemesterAndDay(
            String room,
            Integer academicYear,
            Integer semester,
            String day
    );


    // =====================================================
    // CHECK YEAR GROUP CONFLICT
    // =====================================================

    List<TimetableEntry>
    findByAcademicYearAndSemesterAndDay(
            Integer academicYear,
            Integer semester,
            String day
    );

    boolean existsBySubjectSubjectId(Long subjectId);
}


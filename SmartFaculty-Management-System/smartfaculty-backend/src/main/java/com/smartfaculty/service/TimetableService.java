package com.smartfaculty.service;

import com.smartfaculty.entity.AcademicYear;
import com.smartfaculty.entity.Semester;
import com.smartfaculty.entity.Timetable;

import java.util.List;

public interface TimetableService {


    // =========================================================
    // SAVE / UPDATE
    // =========================================================

    Timetable saveTimetable(
            Timetable timetable
    );


    // =========================================================
    // GET ALL
    // =========================================================

    List<Timetable> getAllTimetables();


    // =========================================================
    // GET BY ID
    // =========================================================

    Timetable getTimetableById(
            Long id
    );


    // =========================================================
    // YEAR + SEMESTER
    // =========================================================

    List<Timetable> getByYearAndSemester(
            AcademicYear yearLevel,
            Semester semester
    );

    List<Timetable> getActiveByYearAndSemester(
            AcademicYear yearLevel,
            Semester semester
    );


    // =========================================================
    // SPECIALIZATION
    // =========================================================

    List<Timetable> getBySpecialization(
            Long specializationId
    );


    // =========================================================
    // STUDENT
    // =========================================================

    List<Timetable> getByStudentId(
            Long studentId
    );


    // =========================================================
    // LECTURER
    // =========================================================

    List<Timetable> getByLecturerId(
            Long lecturerId
    );


    // =========================================================
    // DELETE
    // =========================================================

    String deleteTimetable(Long id);
}
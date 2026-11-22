package com.smartfaculty.service;

import com.smartfaculty.dto.HODDashboardResponse;
import com.smartfaculty.entity.Lecturer;
import com.smartfaculty.entity.Subject;

import java.util.List;

public interface HODDashboardService {

    HODDashboardResponse getDashboard(
            String username
    );


    List<Lecturer> getDepartmentLecturers(
            String username
    );


    List<Subject> getDepartmentSubjects(
            String username
    );


    List<Subject> getDepartmentSubjectsByYearAndSemester(
            String username,
            Integer academicYear,
            Integer semester
    );
}
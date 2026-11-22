package com.smartfaculty.dto;

import com.smartfaculty.entity.Department;
import com.smartfaculty.entity.Lecturer;
import com.smartfaculty.entity.LecturerAllocation;
import com.smartfaculty.entity.Subject;

import java.util.List;

public class HODDashboardResponse {

    private Department department;

    private List<Lecturer> lecturers;

    private List<Subject> subjects;

    private List<LecturerAllocation> allocations;


    public HODDashboardResponse() {
    }


    public HODDashboardResponse(
            Department department,
            List<Lecturer> lecturers,
            List<Subject> subjects,
            List<LecturerAllocation> allocations) {

        this.department = department;
        this.lecturers = lecturers;
        this.subjects = subjects;
        this.allocations = allocations;
    }


    public Department getDepartment() {
        return department;
    }


    public void setDepartment(
            Department department) {

        this.department = department;
    }


    public List<Lecturer> getLecturers() {

        return lecturers;
    }


    public void setLecturers(
            List<Lecturer> lecturers) {

        this.lecturers = lecturers;
    }


    public List<Subject> getSubjects() {

        return subjects;
    }


    public void setSubjects(
            List<Subject> subjects) {

        this.subjects = subjects;
    }


    public List<LecturerAllocation> getAllocations() {

        return allocations;
    }


    public void setAllocations(
            List<LecturerAllocation> allocations) {

        this.allocations = allocations;
    }
}
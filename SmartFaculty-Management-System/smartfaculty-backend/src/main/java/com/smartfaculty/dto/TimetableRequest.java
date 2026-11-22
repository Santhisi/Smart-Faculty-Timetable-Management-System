package com.smartfaculty.dto;

public class TimetableRequest {

    private Long lecturerAllocationId;
    private Long classroomId;
    private Long timeSlotId;
    private Long specializationId;

    private Integer academicYear;
    private Integer semester;


    public TimetableRequest() {
    }


    public Long getLecturerAllocationId() {
        return lecturerAllocationId;
    }

    public void setLecturerAllocationId(Long lecturerAllocationId) {
        this.lecturerAllocationId = lecturerAllocationId;
    }


    public Long getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(Long classroomId) {
        this.classroomId = classroomId;
    }


    public Long getTimeSlotId() {
        return timeSlotId;
    }

    public void setTimeSlotId(Long timeSlotId) {
        this.timeSlotId = timeSlotId;
    }


    public Long getSpecializationId() {
        return specializationId;
    }

    public void setSpecializationId(Long specializationId) {
        this.specializationId = specializationId;
    }


    public Integer getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(Integer academicYear) {
        this.academicYear = academicYear;
    }


    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }
}
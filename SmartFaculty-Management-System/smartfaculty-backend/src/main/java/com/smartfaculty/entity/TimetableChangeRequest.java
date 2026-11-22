package com.smartfaculty.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "timetable_change_requests")
public class TimetableChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    // Lecturer who submitted the request
    @ManyToOne
    @JoinColumn(name = "lecturer_id", nullable = false)
    private Lecturer lecturer;

    // Timetable that needs to be changed
    @ManyToOne
    @JoinColumn(name = "timetable_id", nullable = false)
    private Timetable timetable;

    // Reason given by lecturer
    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    // Requested new day
    private String requestedDay;

    // Requested new start time
    private String requestedStartTime;

    // Requested new end time
    private String requestedEndTime;

    // Requested new classroom
    private String requestedClassroom;

    // PENDING / APPROVED / REJECTED
    @Column(nullable = false)
    private String status = "PENDING";

    // Admin/HOD response
    @Column(columnDefinition = "TEXT")
    private String adminResponse;

    // Request created date
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Request processed date
    private LocalDateTime processedAt;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public TimetableChangeRequest() {
    }


    // ==========================================
    // GETTERS AND SETTERS
    // ==========================================

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Lecturer getLecturer() {
        return lecturer;
    }

    public void setLecturer(Lecturer lecturer) {
        this.lecturer = lecturer;
    }

    public Timetable getTimetable() {
        return timetable;
    }

    public void setTimetable(Timetable timetable) {
        this.timetable = timetable;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRequestedDay() {
        return requestedDay;
    }

    public void setRequestedDay(String requestedDay) {
        this.requestedDay = requestedDay;
    }

    public String getRequestedStartTime() {
        return requestedStartTime;
    }

    public void setRequestedStartTime(String requestedStartTime) {
        this.requestedStartTime = requestedStartTime;
    }

    public String getRequestedEndTime() {
        return requestedEndTime;
    }

    public void setRequestedEndTime(String requestedEndTime) {
        this.requestedEndTime = requestedEndTime;
    }

    public String getRequestedClassroom() {
        return requestedClassroom;
    }

    public void setRequestedClassroom(String requestedClassroom) {
        this.requestedClassroom = requestedClassroom;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminResponse() {
        return adminResponse;
    }

    public void setAdminResponse(String adminResponse) {
        this.adminResponse = adminResponse;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
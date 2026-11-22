package com.smartfaculty.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reschedule_requests")
public class RescheduleRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    // Lecturer who submitted the request
    @ManyToOne
    @JoinColumn(name = "lecturer_id", nullable = false)
    private Lecturer lecturer;

    // Timetable that needs to be rescheduled
    @ManyToOne
    @JoinColumn(name = "timetable_id", nullable = false)
    private Timetable timetable;

    // Requested classroom
    @ManyToOne
    @JoinColumn(name = "requested_classroom_id")
    private Classroom requestedClassroom;

    // Requested time slot
    @ManyToOne
    @JoinColumn(name = "requested_time_slot_id")
    private TimeSlot requestedTimeSlot;

    // Reason given by lecturer
    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    // PENDING / APPROVED / REJECTED
    @Column(nullable = false)
    private String status = "PENDING";

    // Admin/HOD comment
    @Column(columnDefinition = "TEXT")
    private String adminComment;

    // Request created date
    @Column(nullable = false)
    private LocalDateTime requestedAt;

    // Request processed date
    private LocalDateTime processedAt;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public RescheduleRequest() {
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

    public Classroom getRequestedClassroom() {
        return requestedClassroom;
    }

    public void setRequestedClassroom(Classroom requestedClassroom) {
        this.requestedClassroom = requestedClassroom;
    }

    public TimeSlot getRequestedTimeSlot() {
        return requestedTimeSlot;
    }

    public void setRequestedTimeSlot(TimeSlot requestedTimeSlot) {
        this.requestedTimeSlot = requestedTimeSlot;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminComment() {
        return adminComment;
    }

    public void setAdminComment(String adminComment) {
        this.adminComment = adminComment;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
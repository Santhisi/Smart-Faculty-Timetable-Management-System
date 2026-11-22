package com.smartfaculty.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "timetable_entries",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "academic_year",
                                "semester",
                                "day",
                                "start_time",
                                "end_time",
                                "room"
                        }
                )
        }
)
public class TimetableEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long timetableId;


    // =====================================================
    // SUBJECT
    // =====================================================

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;


    // =====================================================
    // LECTURER
    // =====================================================

    @ManyToOne
    @JoinColumn(name = "lecturer_id", nullable = false)
    private Lecturer lecturer;


    // =====================================================
    // ACADEMIC YEAR
    // =====================================================

    @Column(name = "academic_year", nullable = false)
    private Integer academicYear;


    // =====================================================
    // SEMESTER
    // =====================================================

    @Column(nullable = false)
    private Integer semester;


    // =====================================================
    // DAY
    // =====================================================

    @Column(nullable = false)
    private String day;


    // =====================================================
    // START TIME
    // =====================================================

    @Column(name = "start_time", nullable = false)
    private String startTime;


    // =====================================================
    // END TIME
    // =====================================================

    @Column(name = "end_time", nullable = false)
    private String endTime;


    // =====================================================
    // ROOM
    // =====================================================

    @Column(nullable = false)
    private String room;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public TimetableEntry() {
    }


    // =====================================================
    // GETTERS AND SETTERS
    // =====================================================

    public Long getTimetableId() {
        return timetableId;
    }

    public void setTimetableId(Long timetableId) {
        this.timetableId = timetableId;
    }


    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }


    public Lecturer getLecturer() {
        return lecturer;
    }

    public void setLecturer(Lecturer lecturer) {
        this.lecturer = lecturer;
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


    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }


    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }


    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }


    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }
}


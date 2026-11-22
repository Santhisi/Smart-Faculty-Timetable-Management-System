package com.smartfaculty.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "timetables",
        uniqueConstraints = {

                @UniqueConstraint(
                        columnNames = {
                                "classroom_id",
                                "time_slot_id"
                        }
                ),

                @UniqueConstraint(
                        columnNames = {
                                "lecturer_allocation_id",
                                "time_slot_id"
                        }
                )
        }
)
public class Timetable {


    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long timetableId;


    // =========================================================
    // LECTURER ALLOCATION
    // =========================================================
    //
    // This comes from HOD's allocation.
    //
    // It determines:
    //
    //     Lecturer
    //     Subject
    //
    // Admin should NOT change this during editing.
    //
    // =========================================================

    @ManyToOne
    @JoinColumn(
            name = "lecturer_allocation_id",
            nullable = false
    )
    private LecturerAllocation lecturerAllocation;


    // =========================================================
    // CLASSROOM
    // =========================================================
    //
    // Admin can change this.
    //
    // =========================================================

    @ManyToOne
    @JoinColumn(
            name = "classroom_id",
            nullable = false
    )
    private Classroom classroom;


    // =========================================================
    // TIME SLOT
    // =========================================================
    //
    // Admin can change this.
    //
    // =========================================================

    @ManyToOne
    @JoinColumn(
            name = "time_slot_id",
            nullable = false
    )
    private TimeSlot timeSlot;


    // =========================================================
    // SPECIALIZATION
    // =========================================================
    //
    // This is permanent for the timetable entry.
    //
    // =========================================================

    @ManyToOne
    @JoinColumn(
            name = "specialization_id",
            nullable = false
    )
    private Specialization specialization;


    // =========================================================
    // YEAR LEVEL
    // =========================================================

    @Enumerated(EnumType.STRING)
    @Column(
            name = "year_level",
            nullable = false
    )
    private AcademicYear yearLevel;


    // =========================================================
    // SEMESTER
    // =========================================================

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false
    )
    private Semester semester;


    // =========================================================
    // ACTIVE
    // =========================================================

    @Column(
            nullable = false
    )
    private Boolean active = true;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public Timetable() {
    }


    // =========================================================
    // GET TIMETABLE ID
    // =========================================================

    public Long getTimetableId() {

        return timetableId;
    }


    public void setTimetableId(
            Long timetableId
    ) {

        this.timetableId =
                timetableId;
    }


    // =========================================================
    // GET LECTURER ALLOCATION
    // =========================================================

    public LecturerAllocation
    getLecturerAllocation() {

        return lecturerAllocation;
    }


    public void setLecturerAllocation(
            LecturerAllocation lecturerAllocation
    ) {

        this.lecturerAllocation =
                lecturerAllocation;
    }


    // =========================================================
    // GET CLASSROOM
    // =========================================================

    public Classroom getClassroom() {

        return classroom;
    }


    public void setClassroom(
            Classroom classroom
    ) {

        this.classroom =
                classroom;
    }


    // =========================================================
    // GET TIME SLOT
    // =========================================================

    public TimeSlot getTimeSlot() {

        return timeSlot;
    }


    public void setTimeSlot(
            TimeSlot timeSlot
    ) {

        this.timeSlot =
                timeSlot;
    }


    // =========================================================
    // GET SPECIALIZATION
    // =========================================================

    public Specialization
    getSpecialization() {

        return specialization;
    }


    public void setSpecialization(
            Specialization specialization
    ) {

        this.specialization =
                specialization;
    }


    // =========================================================
    // GET YEAR LEVEL
    // =========================================================

    public AcademicYear getYearLevel() {

        return yearLevel;
    }


    public void setYearLevel(
            AcademicYear yearLevel
    ) {

        this.yearLevel =
                yearLevel;
    }


    // =========================================================
    // GET SEMESTER
    // =========================================================

    public Semester getSemester() {

        return semester;
    }


    public void setSemester(
            Semester semester
    ) {

        this.semester =
                semester;
    }


    // =========================================================
    // GET ACTIVE
    // =========================================================

    public Boolean getActive() {

        return active;
    }


    public void setActive(
            Boolean active
    ) {

        this.active =
                active;
    }
}
package com.smartfaculty.service.impl;

import com.smartfaculty.entity.Classroom;
import com.smartfaculty.entity.Lecturer;
import com.smartfaculty.entity.RescheduleRequest;
import com.smartfaculty.entity.Subject;
import com.smartfaculty.entity.TimeSlot;
import com.smartfaculty.entity.Timetable;

import com.smartfaculty.repository.LecturerRepository;
import com.smartfaculty.repository.RescheduleRequestRepository;
import com.smartfaculty.repository.TimetableRepository;

import com.smartfaculty.service.NotificationService;
import com.smartfaculty.service.RescheduleRequestService;
import com.smartfaculty.service.TimetableConflictService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RescheduleRequestServiceImpl
        implements RescheduleRequestService {

    @Autowired
    private RescheduleRequestRepository requestRepository;

    @Autowired
    private TimetableRepository timetableRepository;

    @Autowired
    private LecturerRepository lecturerRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TimetableConflictService timetableConflictService;


    // =====================================================
    // CREATE REQUEST
    // =====================================================

    @Override
    public RescheduleRequest createRequest(
            RescheduleRequest request) {

        Authentication authentication =
                getCurrentAuthentication();

        boolean isLecturer =
                hasRole(authentication, "ROLE_LECTURER");

        boolean isHOD =
                hasRole(authentication, "ROLE_HOD");

        if (!isLecturer && !isHOD) {
            throw new IllegalStateException(
                    "Lecturer or HOD authorization is required."
            );
        }

        if (request == null) {
            throw new IllegalArgumentException(
                    "Request data is required."
            );
        }

        Lecturer authenticatedLecturer =
                getAuthenticatedLecturer();

        if (request.getTimetable() == null) {
            throw new IllegalArgumentException(
                    "Timetable is required."
            );
        }

        Timetable timetable = request.getTimetable();

        if (timetable.getTimetableId() == null) {
            throw new IllegalArgumentException(
                    "Timetable ID is required."
            );
        }

        Timetable actualTimetable =
                timetableRepository.findById(
                        timetable.getTimetableId()
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "Timetable not found with ID: "
                                        + timetable.getTimetableId()
                        )
                );

        if (actualTimetable.getLecturerAllocation() == null ||
                actualTimetable.getLecturerAllocation().getLecturer() == null ||
                actualTimetable.getLecturerAllocation().getLecturer().getLecturerId() == null) {

            throw new IllegalStateException(
                    "Timetable lecturer information is invalid."
            );
        }

        Long timetableLecturerId =
                actualTimetable.getLecturerAllocation()
                        .getLecturer()
                        .getLecturerId();

        if (authenticatedLecturer.getLecturerId() == null ||
                !authenticatedLecturer.getLecturerId()
                        .equals(timetableLecturerId)) {

            throw new IllegalStateException(
                    "You can only submit a reschedule request for your own timetable."
            );
        }

        if (request.getReason() == null ||
                request.getReason().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Reason is required."
            );
        }

        request.setLecturer(authenticatedLecturer);
        request.setTimetable(actualTimetable);
        request.setStatus("PENDING");
        request.setAdminComment(null);
        request.setProcessedAt(null);

        if (request.getRequestedAt() == null) {
            request.setRequestedAt(LocalDateTime.now());
        }

        return requestRepository.save(request);
    }


    // =====================================================
    // GET ALL REQUESTS
    // =====================================================

    @Override
    public List<RescheduleRequest>
    getAllRequests() {

        validateAdminAuthorization();

        return requestRepository
                .findAllByOrderByRequestedAtDesc();
    }


    // =====================================================
    // GET LECTURER REQUESTS
    // =====================================================

    @Override
    public List<RescheduleRequest>
    getLecturerRequests(Long lecturerId) {

        Authentication authentication =
                getCurrentAuthentication();

        if (lecturerId == null) {
            throw new IllegalArgumentException(
                    "Lecturer ID is required."
            );
        }

        if (hasRole(authentication, "ROLE_ADMIN")) {
            return requestRepository
                    .findByLecturer_LecturerIdOrderByRequestedAtDesc(
                            lecturerId
                    );
        }

        Lecturer requestedLecturer =
                lecturerRepository.findById(lecturerId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Lecturer not found with ID: "
                                                + lecturerId
                                )
                        );

        if (hasRole(authentication, "ROLE_LECTURER")) {

            Long authenticatedLecturerId =
                    getAuthenticatedLecturer().getLecturerId();

            if (authenticatedLecturerId == null ||
                    !authenticatedLecturerId.equals(lecturerId)) {

                throw new IllegalStateException(
                        "You are not authorized to access another lecturer's reschedule requests."
                );
            }
        } else if (hasRole(authentication, "ROLE_HOD")) {

            Long hodDepartmentId =
                    getAuthenticatedDepartmentId();

            if (requestedLecturer.getDepartment() == null ||
                    requestedLecturer.getDepartment().getDepartmentId() == null ||
                    !hodDepartmentId.equals(
                            requestedLecturer.getDepartment().getDepartmentId()
                    )) {

                throw new IllegalStateException(
                        "You are not authorized to access requests outside your department."
                );
            }
        } else {
            throw new IllegalStateException(
                    "You are not authorized to access timetable reschedule requests."
            );
        }

        return requestRepository
                .findByLecturer_LecturerIdOrderByRequestedAtDesc(
                        lecturerId
                );
    }


    // =====================================================
    // GET REQUEST BY ID
    // =====================================================

    @Override
    public RescheduleRequest
    getRequestById(Long requestId) {

        getCurrentAuthentication();

        if (requestId == null) {
            throw new IllegalArgumentException(
                    "Request ID is required."
            );
        }

        RescheduleRequest request =
                requestRepository
                        .findById(requestId)
                        .orElse(null);

        if (request == null) {
            return null;
        }

        validateRequestAccess(request);

        return request;
    }


    // =====================================================
    // GET REQUESTS BY STATUS
    // =====================================================

    @Override
    public List<RescheduleRequest>
    getRequestsByStatus(String status) {

        validateAdminAuthorization();

        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Request status is required."
            );
        }

        return requestRepository
                .findByStatusOrderByRequestedAtDesc(
                        status.trim()
                );
    }


    // =====================================================
    // APPROVE REQUEST
    // =====================================================

    @Override
    public RescheduleRequest approveRequest(
            Long requestId,
            String adminComment) {

        validateAdminAuthorization();

        if (requestId == null) {
            throw new IllegalArgumentException(
                    "Request ID is required."
            );
        }

        // -------------------------------------------------
        // FIND REQUEST
        // -------------------------------------------------

        RescheduleRequest request =
                requestRepository
                        .findById(requestId)
                        .orElse(null);

        if (request == null) {
            return null;
        }


        // -------------------------------------------------
        // PREVENT DOUBLE APPROVAL
        // -------------------------------------------------

        if ("APPROVED".equalsIgnoreCase(
                request.getStatus())) {

            return request;
        }


        // -------------------------------------------------
        // GET TIMETABLE
        // -------------------------------------------------

        Timetable timetable =
                request.getTimetable();

        if (timetable == null) {

            throw new IllegalArgumentException(
                    "Timetable associated with this request was not found."
            );
        }


        // =================================================
        // GET REQUESTED CLASSROOM
        // =================================================

        Classroom requestedClassroom =
                request.getRequestedClassroom();


        // =================================================
        // GET REQUESTED TIME SLOT
        // =================================================

        TimeSlot requestedTimeSlot =
                request.getRequestedTimeSlot();


        // =================================================
        // DETERMINE FINAL CLASSROOM
        // =================================================

        Classroom finalClassroom =
                timetable.getClassroom();

        if (requestedClassroom != null) {

            finalClassroom =
                    requestedClassroom;
        }


        // =================================================
        // DETERMINE FINAL TIME SLOT
        // =================================================

        TimeSlot finalTimeSlot =
                timetable.getTimeSlot();

        if (requestedTimeSlot != null) {

            finalTimeSlot =
                    requestedTimeSlot;
        }


        // =================================================
        // VALIDATE CLASSROOM
        // =================================================

        if (finalClassroom == null) {

            throw new IllegalArgumentException(
                    "Classroom cannot be empty."
            );
        }


        // =================================================
        // VALIDATE TIME SLOT
        // =================================================

        if (finalTimeSlot == null) {

            throw new IllegalArgumentException(
                    "Time slot cannot be empty."
            );
        }


        // =================================================
        // CHECK FOR CONFLICT
        // =================================================

        String conflictMessage =
                timetableConflictService.checkConflict(
                        timetable,
                        finalClassroom.getClassroomId(),
                        finalTimeSlot
                );


        // -------------------------------------------------
        // STOP IF CONFLICT EXISTS
        // -------------------------------------------------

        if (conflictMessage != null) {

            throw new IllegalStateException(
                    conflictMessage
            );
        }


        // =================================================
        // APPLY NEW CLASSROOM
        // =================================================

        timetable.setClassroom(
                finalClassroom
        );


        // =================================================
        // APPLY NEW TIME SLOT
        // =================================================

        timetable.setTimeSlot(
                finalTimeSlot
        );


        // =================================================
        // SAVE UPDATED TIMETABLE
        // =================================================

        timetableRepository.save(
                timetable
        );


        // =================================================
        // UPDATE REQUEST
        // =================================================

        request.setStatus(
                "APPROVED"
        );

        request.setAdminComment(
                adminComment
        );

        request.setProcessedAt(
                LocalDateTime.now()
        );


        RescheduleRequest savedRequest =
                requestRepository.save(
                        request
                );


        // =================================================
        // NOTIFY STUDENTS
        // =================================================

        try {

            if (timetable.getLecturerAllocation() != null &&
                    timetable
                            .getLecturerAllocation()
                            .getSubject() != null) {

                Subject subject =
                        timetable
                                .getLecturerAllocation()
                                .getSubject();


                Long subjectId =
                        subject.getSubjectId();


                String subjectCode =
                        subject.getSubjectCode();


                String title =
                        "Timetable Rescheduled";


                String message =
                        "The timetable for subject "
                                + subjectCode
                                + " has been rescheduled. "
                                + "Please check your timetable "
                                + "for the updated schedule.";


                notificationService
                        .notifyAllStudentsForSubject(
                                subjectId,
                                title,
                                message,
                                "TIMETABLE_RESCHEDULE"
                        );
            }

        } catch (Exception e) {

            System.out.println(
                    "Reschedule student notification "
                            + "could not be sent: "
                            + e.getMessage()
            );
        }


        // =================================================
        // RETURN APPROVED REQUEST
        // =================================================

        return savedRequest;
    }


    // =====================================================
    // REJECT REQUEST
    // =====================================================

    @Override
    public RescheduleRequest rejectRequest(
            Long requestId,
            String adminComment) {

        validateAdminAuthorization();

        if (requestId == null) {
            throw new IllegalArgumentException(
                    "Request ID is required."
            );
        }

        RescheduleRequest request =
                requestRepository
                        .findById(requestId)
                        .orElse(null);

        if (request == null) {
            return null;
        }


        // Prevent double rejection

        if ("REJECTED".equalsIgnoreCase(
                request.getStatus())) {

            return request;
        }


        request.setStatus(
                "REJECTED"
        );

        request.setAdminComment(
                adminComment
        );

        request.setProcessedAt(
                LocalDateTime.now()
        );


        return requestRepository.save(
                request
        );
    }


    // =====================================================
    // DELETE REQUEST
    // =====================================================

    @Override
    public void deleteRequest(
            Long requestId) {

        validateAdminAuthorization();

        if (requestId == null) {
            throw new IllegalArgumentException(
                    "Request ID is required."
            );
        }

        if (!requestRepository.existsById(requestId)) {
            throw new IllegalArgumentException(
                    "Reschedule request not found with ID: "
                            + requestId
            );
        }

        requestRepository.deleteById(
                requestId
        );
    }


    // =====================================================
    // PART 15V — SECURITY HELPERS
    // =====================================================

    private Authentication getCurrentAuthentication() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "Authentication is required."
            );
        }

        if (authentication.getName() == null ||
                authentication.getName().trim().isEmpty()) {

            throw new IllegalStateException(
                    "Authenticated username is invalid."
            );
        }

        return authentication;
    }


    private boolean hasRole(
            Authentication authentication,
            String role) {

        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(
                        authority ->
                                authority != null &&
                                        role.equals(
                                                authority.getAuthority()
                                        )
                );
    }


    private void validateAdminAuthorization() {

        Authentication authentication =
                getCurrentAuthentication();

        if (!hasRole(
                authentication,
                "ROLE_ADMIN"
        )) {

            throw new IllegalStateException(
                    "Administrator authorization is required."
            );
        }
    }


    private Lecturer getAuthenticatedLecturer() {

        Authentication authentication =
                getCurrentAuthentication();

        return lecturerRepository
                .findByUser_Username(
                        authentication.getName().trim()
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Lecturer profile not found."
                        )
                );
    }


    private Long getAuthenticatedDepartmentId() {

        Lecturer lecturer =
                getAuthenticatedLecturer();

        if (lecturer.getDepartment() == null ||
                lecturer.getDepartment().getDepartmentId() == null) {

            throw new IllegalStateException(
                    "Lecturer department information is invalid."
            );
        }

        return lecturer
                .getDepartment()
                .getDepartmentId();
    }


    private void validateRequestAccess(
            RescheduleRequest request) {

        if (request.getLecturer() == null ||
                request.getLecturer().getLecturerId() == null) {

            throw new IllegalStateException(
                    "Request lecturer information is invalid."
            );
        }

        Authentication authentication =
                getCurrentAuthentication();

        if (hasRole(authentication, "ROLE_ADMIN")) {
            return;
        }

        Lecturer requestLecturer =
                request.getLecturer();

        if (hasRole(authentication, "ROLE_LECTURER")) {

            Long authenticatedLecturerId =
                    getAuthenticatedLecturer()
                            .getLecturerId();

            if (authenticatedLecturerId == null ||
                    !authenticatedLecturerId.equals(
                            requestLecturer.getLecturerId()
                    )) {

                throw new IllegalStateException(
                        "You are not authorized to access another lecturer's reschedule request."
                );
            }

            return;
        }

        if (hasRole(authentication, "ROLE_HOD")) {

            Long hodDepartmentId =
                    getAuthenticatedDepartmentId();

            if (requestLecturer.getDepartment() == null ||
                    requestLecturer.getDepartment().getDepartmentId() == null ||
                    !hodDepartmentId.equals(
                            requestLecturer.getDepartment().getDepartmentId()
                    )) {

                throw new IllegalStateException(
                        "You are not authorized to access requests outside your department."
                );
            }

            return;
        }

        throw new IllegalStateException(
                "You are not authorized to access timetable reschedule requests."
        );
    }
}
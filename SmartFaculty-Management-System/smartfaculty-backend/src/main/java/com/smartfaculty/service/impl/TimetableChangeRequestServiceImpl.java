package com.smartfaculty.service.impl;

import com.smartfaculty.entity.Classroom;
import com.smartfaculty.entity.DayOfWeek;
import com.smartfaculty.entity.TimeSlot;
import com.smartfaculty.entity.Timetable;
import com.smartfaculty.entity.TimetableChangeRequest;
import com.smartfaculty.entity.Lecturer;
import com.smartfaculty.entity.LecturerNotification;
import com.smartfaculty.enums.UserRole;
import com.smartfaculty.repository.ClassroomRepository;
import com.smartfaculty.repository.TimeSlotRepository;
import com.smartfaculty.repository.TimetableChangeRequestRepository;
import com.smartfaculty.repository.TimetableRepository;
import com.smartfaculty.repository.LecturerRepository;
import com.smartfaculty.repository.LecturerNotificationRepository;
import com.smartfaculty.service.NotificationService;
import com.smartfaculty.service.TimetableChangeRequestService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class TimetableChangeRequestServiceImpl
        implements TimetableChangeRequestService {

    @Autowired
    private TimetableChangeRequestRepository requestRepository;

    @Autowired
    private TimetableRepository timetableRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private LecturerRepository lecturerRepository;

    @Autowired
    private LecturerNotificationRepository lecturerNotificationRepository;


    // ==========================================
    // CREATE REQUEST
    // ==========================================

    @Override
    public TimetableChangeRequest createRequest(
            TimetableChangeRequest request) {

        validateLecturerOrHODAuthorization();

        if (request == null) {
            throw new IllegalArgumentException("Request data is required.");
        }

        Lecturer authenticatedLecturer =
                getAuthenticatedLecturer();

        validateTimetableOwnershipForCreation(request);

        // Never trust lecturer information supplied by the frontend.
        request.setLecturer(authenticatedLecturer);

        if (request.getStatus() == null ||
                request.getStatus().isBlank()) {

            request.setStatus("PENDING");
        }

        if (request.getCreatedAt() == null) {

            request.setCreatedAt(
                    LocalDateTime.now()
            );
        }

        TimetableChangeRequest savedRequest =
                requestRepository.save(request);

        String subjectCode =
                getRequestSubjectCode(savedRequest);

        // Notify the relevant HOD after the request is saved.
        notifyRelevantHOD(
                savedRequest,
                "New Timetable Change Request",
                "Lecturer "
                        + getLecturerDisplayName(savedRequest.getLecturer())
                        + " submitted a timetable change request for subject "
                        + subjectCode
                        + ". Please review the requested change."
        );

        return savedRequest;
    }


    // ==========================================
    // GET ALL REQUESTS
    // ==========================================

    @Override
    public List<TimetableChangeRequest>
    getAllRequests() {

        validateAdminAuthorization();

        return requestRepository
                .findAllByOrderByCreatedAtDesc();
    }


    // ==========================================
    // GET LECTURER REQUESTS
    // ==========================================

    @Override
    public List<TimetableChangeRequest>
    getLecturerRequests(Long lecturerId) {

        validateAuthentication();

        if (lecturerId == null) {
            throw new IllegalArgumentException("Lecturer ID is required.");
        }

        Authentication authentication = getCurrentAuthentication();

        // ADMIN can view requests for any lecturer.
        if (hasRole(authentication, "ROLE_ADMIN")) {
            return requestRepository
                    .findByLecturer_LecturerIdOrderByCreatedAtDesc(lecturerId);
        }

        Lecturer requestedLecturer =
                lecturerRepository.findById(lecturerId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Lecturer not found with ID: " + lecturerId
                                ));

        // A lecturer can view only their own requests.
        if (hasRole(authentication, "ROLE_LECTURER")) {
            Long authenticatedLecturerId = getAuthenticatedLecturerId();

            if (!authenticatedLecturerId.equals(lecturerId)) {
                throw new IllegalStateException(
                        "You are not authorized to access another lecturer's requests."
                );
            }
        }

        // An HOD can view requests only from their department.
        else if (hasRole(authentication, "ROLE_HOD")) {
            Long hodDepartmentId = getAuthenticatedDepartmentId();

            if (requestedLecturer.getDepartment() == null ||
                    requestedLecturer.getDepartment().getDepartmentId() == null) {
                throw new IllegalStateException(
                        "Lecturer department information is invalid."
                );
            }

            if (!hodDepartmentId.equals(
                    requestedLecturer.getDepartment().getDepartmentId())) {
                throw new IllegalStateException(
                        "You are not authorized to access requests outside your department."
                );
            }
        } else {
            throw new IllegalStateException(
                    "You are not authorized to access timetable change requests."
            );
        }

        return requestRepository
                .findByLecturer_LecturerIdOrderByCreatedAtDesc(
                        lecturerId
                );
    }


    // ==========================================
    // GET REQUEST BY ID
    // ==========================================

    @Override
    public TimetableChangeRequest
    getRequestById(Long requestId) {

        validateAuthentication();

        if (requestId == null) {
            throw new IllegalArgumentException("Request ID is required.");
        }

        TimetableChangeRequest request =
                requestRepository.findById(requestId).orElse(null);

        if (request == null) {
            return null;
        }

        validateRequestOwnership(request);

        return request;
    }


    // ==========================================
    // GET REQUESTS BY STATUS
    // ==========================================

    @Override
    public List<TimetableChangeRequest>
    getRequestsByStatus(String status) {

        validateAdminAuthorization();

        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Request status is required.");
        }

        return requestRepository
                .findByStatusOrderByCreatedAtDesc(
                        status.trim()
                );
    }


    // =====================================================
    // PART 15U - AUTHORIZATION HELPERS
    // =====================================================

    private Authentication getCurrentAuthentication() {

        validateAuthentication();

        return SecurityContextHolder
                .getContext()
                .getAuthentication();
    }


    private void validateAuthentication() {

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
    }


    private boolean hasRole(
            Authentication authentication,
            String role) {

        return authentication != null &&
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority != null &&
                                        role.equals(
                                                authority.getAuthority()
                                        )
                        );
    }


    private void validateAdminAuthorization() {

        Authentication authentication =
                getCurrentAuthentication();

        if (!hasRole(authentication, "ROLE_ADMIN")) {

            throw new IllegalStateException(
                    "Administrator authorization is required."
            );
        }
    }


    private void validateLecturerOrHODAuthorization() {

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
    }


    private Lecturer getAuthenticatedLecturer() {

        Authentication authentication =
                getCurrentAuthentication();

        String username =
                authentication.getName().trim();

        return lecturerRepository
                .findByUser_Username(username)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Lecturer profile not found."
                        )
                );
    }


    private Long getAuthenticatedLecturerId() {

        Lecturer lecturer =
                getAuthenticatedLecturer();

        if (lecturer.getLecturerId() == null) {
            throw new IllegalStateException(
                    "Authenticated lecturer profile ID is invalid."
            );
        }

        return lecturer.getLecturerId();
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

        return lecturer.getDepartment().getDepartmentId();
    }


    private void validateRequestOwnership(
            TimetableChangeRequest request) {

        if (request == null) {
            throw new IllegalStateException(
                    "Timetable change request is invalid."
            );
        }

        if (request.getLecturer() == null ||
                request.getLecturer().getLecturerId() == null) {

            throw new IllegalStateException(
                    "Request lecturer information is invalid."
            );
        }

        Authentication authentication =
                getCurrentAuthentication();

        // ADMIN can access any request.
        if (hasRole(authentication, "ROLE_ADMIN")) {
            return;
        }

        Lecturer authenticatedLecturer =
                getAuthenticatedLecturer();

        Long requestLecturerId =
                request.getLecturer().getLecturerId();

        // LECTURER can access only own requests.
        if (hasRole(authentication, "ROLE_LECTURER")) {

            if (!authenticatedLecturer.getLecturerId()
                    .equals(requestLecturerId)) {

                throw new IllegalStateException(
                        "You are not authorized to access another lecturer's request."
                );
            }

            return;
        }

        // HOD can access requests only from own department.
        if (hasRole(authentication, "ROLE_HOD")) {

            Long hodDepartmentId =
                    getAuthenticatedDepartmentId();

            if (request.getLecturer().getDepartment() == null ||
                    request.getLecturer()
                            .getDepartment()
                            .getDepartmentId() == null) {

                throw new IllegalStateException(
                        "Request lecturer department information is invalid."
                );
            }

            Long requestDepartmentId =
                    request.getLecturer()
                            .getDepartment()
                            .getDepartmentId();

            if (!hodDepartmentId.equals(requestDepartmentId)) {

                throw new IllegalStateException(
                        "You are not authorized to access requests outside your department."
                );
            }

            return;
        }

        throw new IllegalStateException(
                "You are not authorized to access timetable change requests."
        );
    }


    private void validateTimetableOwnershipForCreation(
            TimetableChangeRequest request) {

        if (request.getTimetable() == null) {
            throw new IllegalArgumentException(
                    "Timetable is required."
            );
        }

        Timetable timetable =
                request.getTimetable();

        if (timetable.getTimetableId() == null) {
            throw new IllegalArgumentException(
                    "Timetable ID is required."
            );
        }

        Timetable realTimetable =
                timetableRepository.findById(
                        timetable.getTimetableId()
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "Timetable not found with ID: "
                                        + timetable.getTimetableId()
                        )
                );

        if (realTimetable.getLecturerAllocation() == null ||
                realTimetable.getLecturerAllocation().getLecturer() == null ||
                realTimetable.getLecturerAllocation()
                        .getLecturer().getLecturerId() == null) {

            throw new IllegalStateException(
                    "Timetable lecturer information is invalid."
            );
        }

        Long authenticatedLecturerId =
                getAuthenticatedLecturerId();

        Long timetableLecturerId =
                realTimetable.getLecturerAllocation()
                        .getLecturer()
                        .getLecturerId();

        if (!authenticatedLecturerId.equals(timetableLecturerId)) {

            throw new IllegalStateException(
                    "You can only submit a timetable change request for your own timetable."
            );
        }

        // Replace the client-supplied timetable object with the real DB entity.
        request.setTimetable(realTimetable);
    }


    // =====================================================
    // FIND AND NOTIFY RELEVANT HOD
    // =====================================================

    private void notifyRelevantHOD(
            TimetableChangeRequest request,
            String title,
            String message) {

        try {

            Lecturer requestingLecturer =
                    request != null
                            ? request.getLecturer()
                            : null;

            if (requestingLecturer == null ||
                    requestingLecturer.getDepartment() == null ||
                    requestingLecturer.getDepartment().getDepartmentId() == null) {
                return;
            }

            Long departmentId =
                    requestingLecturer
                            .getDepartment()
                            .getDepartmentId();

            Lecturer hod =
                    lecturerRepository.findAll()
                            .stream()
                            .filter(lecturer ->
                                    lecturer != null &&
                                            lecturer.getDepartment() != null &&
                                            lecturer.getDepartment().getDepartmentId() != null &&
                                            departmentId.equals(
                                                    lecturer.getDepartment().getDepartmentId()
                                            )
                            )
                            .filter(lecturer ->
                                    lecturer.getUser() != null &&
                                            lecturer.getUser().getRole() == UserRole.HOD
                            )
                            .findFirst()
                            .orElse(null);

            if (hod == null || hod.getLecturerId() == null) {
                System.out.println(
                        "No HOD found for department ID: " + departmentId
                );
                return;
            }

            notificationService.notifyLecturer(
                    hod.getLecturerId(),
                    title,
                    message,
                    "TIMETABLE_CHANGE_REQUEST"
            );

        } catch (Exception e) {

            // Notification failure must not prevent the request from being saved.
            System.out.println(
                    "HOD notification could not be sent: "
                            + e.getMessage()
            );
        }
    }


    // =====================================================
    // GET REQUEST SUBJECT CODE
    // =====================================================

    private String getRequestSubjectCode(
            TimetableChangeRequest request) {

        try {

            if (request == null || request.getTimetable() == null ||
                    request.getTimetable().getLecturerAllocation() == null ||
                    request.getTimetable().getLecturerAllocation().getSubject() == null) {
                return "Unknown Subject";
            }

            String subjectCode =
                    request.getTimetable()
                            .getLecturerAllocation()
                            .getSubject()
                            .getSubjectCode();

            return subjectCode == null || subjectCode.isBlank()
                    ? "Unknown Subject"
                    : subjectCode;

        } catch (Exception e) {

            return "Unknown Subject";
        }
    }


    // =====================================================
    // GET LECTURER DISPLAY NAME
    // =====================================================

    private String getLecturerDisplayName(
            Lecturer lecturer) {

        if (lecturer == null) {
            return "Unknown Lecturer";
        }

        if (lecturer.getLecturerName() != null &&
                !lecturer.getLecturerName().isBlank()) {
            return lecturer.getLecturerName();
        }

        if (lecturer.getLecturerCode() != null &&
                !lecturer.getLecturerCode().isBlank()) {
            return lecturer.getLecturerCode();
        }

        return "Unknown Lecturer";
    }
    // ==========================================
// UPDATE REQUEST
// ==========================================

    @Override
    @Transactional
    public TimetableChangeRequest updateRequest(
            Long requestId,
            TimetableChangeRequest updatedRequest) {

        if (requestId == null) {
            throw new IllegalArgumentException("Request ID is required.");
        }

        if (updatedRequest == null) {
            throw new IllegalArgumentException("Updated request data is required.");
        }

        TimetableChangeRequest existingRequest =
                requestRepository.findById(requestId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Timetable change request not found."
                                )
                        );

        // ADMIN can update any request.
        // LECTURER can update own requests.
        // HOD can update requests from their department.
        validateRequestOwnership(existingRequest);

        if (!"PENDING".equalsIgnoreCase(existingRequest.getStatus())) {
            throw new IllegalStateException(
                    "Only pending requests can be edited."
            );
        }

        if (updatedRequest.getReason() == null ||
                updatedRequest.getReason().isBlank()) {
            throw new IllegalArgumentException(
                    "Reason is required."
            );
        }

        // Update only editable fields.
        // Do not change lecturer, timetable, status, ID,
        // admin response, or request creation date.
        existingRequest.setRequestedDay(
                updatedRequest.getRequestedDay()
        );

        existingRequest.setRequestedStartTime(
                updatedRequest.getRequestedStartTime()
        );

        existingRequest.setRequestedEndTime(
                updatedRequest.getRequestedEndTime()
        );

        existingRequest.setRequestedClassroom(
                updatedRequest.getRequestedClassroom()
        );

        existingRequest.setReason(
                updatedRequest.getReason().trim()
        );

        return requestRepository.save(existingRequest);
    }


    // =====================================================
    // APPROVE REQUEST
    // =====================================================

    @Override
    @Transactional
    public TimetableChangeRequest approveRequest(
            Long requestId,
            String adminResponse) {

        // ------------------------------------------
        // ADMIN AUTHORIZATION
        // ------------------------------------------

        validateAdminAuthorization();

        if (requestId == null) {
            throw new IllegalArgumentException("Request ID is required.");
        }

        // ------------------------------------------
        // FIND REQUEST
        // ------------------------------------------

        TimetableChangeRequest request =
                requestRepository
                        .findById(requestId)
                        .orElse(null);

        if (request == null) {
            return null;
        }

        if (!"PENDING".equalsIgnoreCase(
                request.getStatus())) {
            return request;
        }


        // ------------------------------------------
        // RELOAD THE REAL TIMETABLE FROM DATABASE
        // ------------------------------------------
        // Do not update the timetable object supplied with the
        // request. Reload the actual database record so that the
        // approved change is applied to the same Timetable record
        // used by both lecturer and student timetable endpoints.

        if (request.getTimetable() == null ||
                request.getTimetable().getTimetableId() == null) {
            throw new IllegalStateException(
                    "Timetable information is missing from the change request."
            );
        }

        Long timetableId =
                request.getTimetable().getTimetableId();

        Timetable timetable =
                timetableRepository
                        .findById(timetableId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Timetable not found with ID: "
                                                + timetableId
                                )
                        );


        // ==========================================
        // CHANGE CLASSROOM
        // ==========================================

        if (request.getRequestedClassroom() != null &&
                !request.getRequestedClassroom().isBlank()) {

            String requestedRoomCode =
                    request.getRequestedClassroom().trim();

            Classroom classroom =
                    classroomRepository
                            .findAll()
                            .stream()
                            .filter(c ->
                                    c != null &&
                                            c.getRoomCode() != null &&
                                            c.getRoomCode()
                                                    .equalsIgnoreCase(
                                                            requestedRoomCode
                                                    )
                            )
                            .findFirst()
                            .orElse(null);

            if (classroom != null) {

                timetable.setClassroom(
                        classroom
                );

            } else {

                throw new IllegalStateException(
                        "Requested classroom not found: "
                                + requestedRoomCode
                );
            }
        }


        // ==========================================
        // CHANGE DAY / TIME
        // ==========================================

        boolean hasDayChange =
                request.getRequestedDay() != null &&
                        !request.getRequestedDay().isBlank();

        boolean hasStartTimeChange =
                request.getRequestedStartTime() != null &&
                        !request.getRequestedStartTime().isBlank();

        boolean hasEndTimeChange =
                request.getRequestedEndTime() != null &&
                        !request.getRequestedEndTime().isBlank();


        if (hasDayChange ||
                hasStartTimeChange ||
                hasEndTimeChange) {

            TimeSlot currentTimeSlot =
                    timetable.getTimeSlot();

            if (currentTimeSlot == null) {
                throw new IllegalStateException(
                        "Current timetable time slot is missing."
                );
            }

            DayOfWeek newDay =
                    currentTimeSlot.getDay();

            LocalTime newStartTime =
                    currentTimeSlot.getStartTime();

            LocalTime newEndTime =
                    currentTimeSlot.getEndTime();


            // ----------------------------------
            // NEW DAY
            // ----------------------------------

            if (hasDayChange) {

                try {

                    newDay =
                            DayOfWeek.valueOf(
                                    request
                                            .getRequestedDay()
                                            .trim()
                                            .toUpperCase()
                            );

                } catch (IllegalArgumentException e) {

                    throw new IllegalArgumentException(
                            "Invalid requested day: "
                                    + request.getRequestedDay()
                    );
                }
            }


            // ----------------------------------
            // NEW START TIME
            // ----------------------------------

            if (hasStartTimeChange) {

                try {

                    newStartTime =
                            LocalTime.parse(
                                    request
                                            .getRequestedStartTime()
                                            .trim()
                            );

                } catch (Exception e) {

                    throw new IllegalArgumentException(
                            "Invalid requested start time: "
                                    + request.getRequestedStartTime()
                    );
                }
            }


            // ----------------------------------
            // NEW END TIME
            // ----------------------------------

            if (hasEndTimeChange) {

                try {

                    newEndTime =
                            LocalTime.parse(
                                    request
                                            .getRequestedEndTime()
                                            .trim()
                            );

                } catch (Exception e) {

                    throw new IllegalArgumentException(
                            "Invalid requested end time: "
                                    + request.getRequestedEndTime()
                    );
                }
            }


            // ----------------------------------
            // VALIDATE TIME RANGE
            // ----------------------------------

            if (newStartTime == null ||
                    newEndTime == null ||
                    !newStartTime.isBefore(newEndTime)) {

                throw new IllegalArgumentException(
                        "Requested start time must be before requested end time."
                );
            }


            // ----------------------------------
            // FIND MATCHING TIME SLOT
            // ----------------------------------

            final DayOfWeek finalNewDay = newDay;
            final LocalTime finalNewStartTime = newStartTime;
            final LocalTime finalNewEndTime = newEndTime;

            TimeSlot matchingTimeSlot =
                    timeSlotRepository
                            .findAll()
                            .stream()
                            .filter(slot ->
                                    slot != null &&
                                            slot.getDay() == finalNewDay
                            )
                            .filter(slot ->
                                    slot.getStartTime() != null &&
                                            slot.getStartTime()
                                                    .equals(finalNewStartTime)
                            )
                            .filter(slot ->
                                    slot.getEndTime() != null &&
                                            slot.getEndTime()
                                                    .equals(finalNewEndTime)
                            )
                            .findFirst()
                            .orElse(null);


            // ----------------------------------
            // APPLY NEW TIME SLOT
            // ----------------------------------

            if (matchingTimeSlot == null) {

                throw new IllegalStateException(
                        "No matching TimeSlot found for "
                                + newDay
                                + " "
                                + newStartTime
                                + "-"
                                + newEndTime
                );
            }

            timetable.setTimeSlot(
                    matchingTimeSlot
            );
        }


        // ==========================================
        // SAVE UPDATED TIMETABLE
        // ==========================================
        // This is the single shared timetable record. Updating it
        // automatically changes what the lecturer and registered
        // students receive from their timetable API endpoints.

        Timetable updatedTimetable =
                timetableRepository.save(
                        timetable
                );


        // ==========================================
        // UPDATE REQUEST
        // ==========================================

        request.setStatus("APPROVED");

        request.setAdminResponse(
                adminResponse
        );

        request.setProcessedAt(
                LocalDateTime.now()
        );


        TimetableChangeRequest savedRequest =
                requestRepository.save(
                        request
                );


        // ==========================================
        // NOTIFY STUDENTS
        // ==========================================

        try {

            if (updatedTimetable.getLecturerAllocation() != null &&
                    updatedTimetable.getLecturerAllocation().getSubject() != null &&
                    updatedTimetable.getLecturerAllocation()
                            .getSubject().getSubjectId() != null) {

                Long subjectId =
                        updatedTimetable
                                .getLecturerAllocation()
                                .getSubject()
                                .getSubjectId();

                String subjectCode =
                        updatedTimetable
                                .getLecturerAllocation()
                                .getSubject()
                                .getSubjectCode();

                String title =
                        "Timetable Updated";

                String message =
                        "The timetable for subject "
                                + subjectCode
                                + " has been updated. "
                                + "Please check your timetable for the new schedule.";

                notificationService
                        .notifyAllStudentsForSubject(
                                subjectId,
                                title,
                                message,
                                "TIMETABLE_CHANGE"
                        );
            }

        } catch (Exception e) {

            System.out.println(
                    "Notification could not be sent: "
                            + e.getMessage()
            );
        }


        // ==========================================
        // NOTIFY LECTURER
        // ==========================================

        sendLecturerNotification(
                request.getLecturer(),
                "Timetable Change Request Approved",
                "Your timetable change request for "
                        + getRequestSubjectCode(request)
                        + " has been approved. Please check your timetable for the updated schedule.",
                "TIMETABLE_CHANGE_REQUEST"
        );


        // ==========================================
        // NOTIFY HOD
        // ==========================================

        notifyRelevantHOD(
                request,
                "Timetable Change Request Approved",
                "The timetable change request for subject "
                        + getRequestSubjectCode(request)
                        + " submitted by "
                        + getLecturerDisplayName(request.getLecturer())
                        + " has been approved by the administrator."
        );


        return savedRequest;
    }


    // =====================================================
    // SEND DIRECT LECTURER NOTIFICATION
    // =====================================================

    private void sendLecturerNotification(
            Lecturer lecturer,
            String title,
            String message,
            String notificationType) {

        try {

            if (lecturer == null || lecturer.getLecturerId() == null) {
                System.out.println(
                        "Lecturer notification skipped: lecturer not found."
                );
                return;
            }

            LecturerNotification notification =
                    new LecturerNotification();

            notification.setLecturer(lecturer);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setNotificationType(notificationType);
            notification.setIsRead(false);
            notification.setCreatedAt(LocalDateTime.now());

            lecturerNotificationRepository.save(notification);

        } catch (Exception e) {

            System.out.println(
                    "Lecturer notification could not be sent: "
                            + e.getMessage()
            );
        }
    }


    // ==========================================
    // REJECT REQUEST
    // ==========================================

    @Override
    public TimetableChangeRequest rejectRequest(
            Long requestId,
            String adminResponse) {

        validateAdminAuthorization();

        if (requestId == null) {
            throw new IllegalArgumentException("Request ID is required.");
        }

        TimetableChangeRequest request =
                requestRepository
                        .findById(requestId)
                        .orElse(null);

        if (request == null) {
            return null;
        }

        if (!"PENDING".equalsIgnoreCase(
                request.getStatus())) {
            return request;
        }

        request.setStatus("REJECTED");

        request.setAdminResponse(
                adminResponse
        );

        request.setProcessedAt(
                LocalDateTime.now()
        );

        TimetableChangeRequest savedRequest =
                requestRepository.save(
                        request
                );


        // ==========================================
        // NOTIFY LECTURER
        // ==========================================

        sendLecturerNotification(
                request.getLecturer(),
                "Timetable Change Request Rejected",
                "Your timetable change request for subject "
                        + getRequestSubjectCode(request)
                        + " has been rejected by the administrator. "
                        + (adminResponse == null || adminResponse.isBlank()
                        ? "Please review the administrator response."
                        : adminResponse),
                "TIMETABLE_CHANGE_REQUEST"
        );


        // ==========================================
        // NOTIFY HOD
        // ==========================================

        notifyRelevantHOD(
                request,
                "Timetable Change Request Rejected",
                "The timetable change request for subject "
                        + getRequestSubjectCode(request)
                        + " submitted by "
                        + getLecturerDisplayName(request.getLecturer())
                        + " has been rejected by the administrator."
        );


        return savedRequest;
    }


    // ==========================================
    // DELETE REQUEST
    // ==========================================

    @Override
    @Transactional
    public void deleteRequest(Long requestId) {

        if (requestId == null) {
            throw new IllegalArgumentException(
                    "Request ID is required."
            );
        }

        TimetableChangeRequest request =
                requestRepository.findById(requestId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Timetable change request not found."
                                )
                        );

        // ADMIN: any request
        // LECTURER: own requests only
        // HOD: requests from own department only
        validateRequestOwnership(request);

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            throw new IllegalStateException(
                    "Only pending requests can be deleted."
            );
        }

        requestRepository.delete(request);
    }
}

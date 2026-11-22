package com.smartfaculty.service.impl;

import com.smartfaculty.entity.Lecturer;
import com.smartfaculty.entity.Notification;
import com.smartfaculty.entity.Student;
import com.smartfaculty.entity.StudentSubjectRegistration;
import com.smartfaculty.enums.UserRole;
import com.smartfaculty.repository.LecturerRepository;
import com.smartfaculty.repository.NotificationRepository;
import com.smartfaculty.repository.StudentRepository;
import com.smartfaculty.service.NotificationService;
import com.smartfaculty.service.StudentSubjectRegistrationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationServiceImpl
        implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private StudentSubjectRegistrationService registrationService;

    @Autowired
    private LecturerRepository lecturerRepository;

    @Autowired
    private StudentRepository studentRepository;


    // =========================================================
    // AUTHENTICATION HELPERS
    // =========================================================

    private Authentication getAuthentication() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required."
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
                .anyMatch(authority ->
                        role.equals(
                                authority.getAuthority()
                        )
                );
    }


    private boolean isAdmin(
            Authentication authentication) {

        return hasRole(
                authentication,
                "ROLE_ADMIN"
        );
    }


    private boolean isStudent(
            Authentication authentication) {

        return hasRole(
                authentication,
                "ROLE_STUDENT"
        );
    }


    private boolean isLecturer(
            Authentication authentication) {

        return hasRole(
                authentication,
                "ROLE_LECTURER"
        );
    }


    private boolean isHOD(
            Authentication authentication) {

        return hasRole(
                authentication,
                "ROLE_HOD"
        );
    }


    // =========================================================
    // LOGGED-IN STUDENT
    // =========================================================

    private Student getLoggedInStudent(
            Authentication authentication) {

        return studentRepository
                .findByUser_Username(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.FORBIDDEN,
                                "Authenticated student account was not found."
                        )
                );
    }


    // =========================================================
    // LOGGED-IN LECTURER / HOD
    // =========================================================

    private Lecturer getLoggedInLecturer(
            Authentication authentication) {

        return lecturerRepository
                .findByUser_Username(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.FORBIDDEN,
                                "Authenticated lecturer account was not found."
                        )
                );
    }


    // =========================================================
    // STUDENT OWNERSHIP
    // =========================================================

    private void validateStudentOwnership(
            Long requestedStudentId,
            Authentication authentication) {

        if (requestedStudentId == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Student ID is required."
            );
        }

        if (isAdmin(authentication)) {
            return;
        }

        if (!isStudent(authentication)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access denied."
            );
        }

        Student student =
                getLoggedInStudent(authentication);

        if (student.getStudentId() == null
                || !student.getStudentId()
                .equals(requestedStudentId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access denied. You can only access your own notifications."
            );
        }
    }


    // =========================================================
    // LECTURER OWNERSHIP
    // =========================================================

    private void validateLecturerOwnership(
            Long requestedLecturerId,
            Authentication authentication) {

        if (requestedLecturerId == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Lecturer ID is required."
            );
        }

        if (isAdmin(authentication)) {
            return;
        }

        if (!isLecturer(authentication)
                && !isHOD(authentication)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access denied."
            );
        }

        Lecturer lecturer =
                getLoggedInLecturer(authentication);

        if (lecturer.getLecturerId() == null
                || !lecturer.getLecturerId()
                .equals(requestedLecturerId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access denied. You can only access your own notifications."
            );
        }
    }


    // =========================================================
    // HOD OWNERSHIP
    // =========================================================

    private void validateHODOwnership(
            Long requestedLecturerId,
            Authentication authentication) {

        if (requestedLecturerId == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Lecturer ID is required."
            );
        }

        if (isAdmin(authentication)) {
            return;
        }

        if (!isHOD(authentication)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only HOD users can access HOD notifications."
            );
        }

        Lecturer hod =
                getLoggedInLecturer(authentication);

        if (hod.getLecturerId() == null
                || !hod.getLecturerId()
                .equals(requestedLecturerId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access denied. You can only access your own HOD notifications."
            );
        }
    }


    // =========================================================
    // NOTIFICATION OWNERSHIP
    // =========================================================

    private void validateNotificationOwnership(
            Notification notification,
            Authentication authentication) {

        if (notification == null) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Notification not found."
            );
        }

        // -----------------------------------------------------
        // ADMIN
        // -----------------------------------------------------

        if (isAdmin(authentication)) {
            return;
        }


        // -----------------------------------------------------
        // STUDENT NOTIFICATION
        // -----------------------------------------------------

        if (notification.getStudent() != null) {

            if (!isStudent(authentication)) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Access denied."
                );
            }

            Student student =
                    getLoggedInStudent(authentication);

            Long notificationStudentId =
                    notification
                            .getStudent()
                            .getStudentId();

            if (student.getStudentId() == null
                    || notificationStudentId == null
                    || !student.getStudentId()
                    .equals(notificationStudentId)) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Access denied. You can only access your own notification."
                );
            }

            return;
        }


        // -----------------------------------------------------
        // LECTURER / HOD NOTIFICATION
        // -----------------------------------------------------

        if (notification.getLecturer() != null) {

            if (!isLecturer(authentication)
                    && !isHOD(authentication)) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Access denied."
                );
            }

            Lecturer lecturer =
                    getLoggedInLecturer(authentication);

            Long notificationLecturerId =
                    notification
                            .getLecturer()
                            .getLecturerId();

            if (lecturer.getLecturerId() == null
                    || notificationLecturerId == null
                    || !lecturer.getLecturerId()
                    .equals(notificationLecturerId)) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Access denied. You can only access your own notification."
                );
            }

            return;
        }


        // -----------------------------------------------------
        // UNKNOWN NOTIFICATION OWNER
        // -----------------------------------------------------

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Access denied."
        );
    }


    // =========================================================
    // SAVE NOTIFICATION
    // =========================================================

    @Override
    public Notification saveNotification(
            Notification notification) {

        if (notification == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Notification cannot be null."
            );
        }

        if (notification.getCreatedAt() == null) {

            notification.setCreatedAt(
                    LocalDateTime.now()
            );
        }

        if (notification.getIsRead() == null) {

            notification.setIsRead(false);
        }

        return notificationRepository.save(
                notification
        );
    }


    // =========================================================
    // STUDENT NOTIFICATIONS
    // =========================================================

    @Override
    public List<Notification>
    getStudentNotifications(
            Long studentId) {

        Authentication authentication =
                getAuthentication();

        validateStudentOwnership(
                studentId,
                authentication
        );

        return notificationRepository
                .findByStudent_StudentIdOrderByCreatedAtDesc(
                        studentId
                );
    }


    @Override
    public List<Notification>
    getUnreadNotifications(
            Long studentId) {

        Authentication authentication =
                getAuthentication();

        validateStudentOwnership(
                studentId,
                authentication
        );

        return notificationRepository
                .findByStudent_StudentIdAndIsReadFalseOrderByCreatedAtDesc(
                        studentId
                );
    }


    @Override
    public void markAllAsRead(
            Long studentId) {

        Authentication authentication =
                getAuthentication();

        validateStudentOwnership(
                studentId,
                authentication
        );

        List<Notification> notifications =
                notificationRepository
                        .findByStudent_StudentIdAndIsReadFalseOrderByCreatedAtDesc(
                                studentId
                        );

        for (Notification notification :
                notifications) {

            notification.setIsRead(true);
        }

        notificationRepository.saveAll(
                notifications
        );
    }


    // =========================================================
    // LECTURER NOTIFICATIONS
    // =========================================================

    @Override
    public List<Notification>
    getLecturerNotifications(
            Long lecturerId) {

        Authentication authentication =
                getAuthentication();

        validateLecturerOwnership(
                lecturerId,
                authentication
        );

        return notificationRepository
                .findByLecturer_LecturerIdOrderByCreatedAtDesc(
                        lecturerId
                );
    }


    @Override
    public List<Notification>
    getUnreadLecturerNotifications(
            Long lecturerId) {

        Authentication authentication =
                getAuthentication();

        validateLecturerOwnership(
                lecturerId,
                authentication
        );

        return notificationRepository
                .findByLecturer_LecturerIdAndIsReadFalseOrderByCreatedAtDesc(
                        lecturerId
                );
    }


    @Override
    public void markAllLecturerNotificationsAsRead(
            Long lecturerId) {

        Authentication authentication =
                getAuthentication();

        validateLecturerOwnership(
                lecturerId,
                authentication
        );

        List<Notification> notifications =
                notificationRepository
                        .findByLecturer_LecturerIdAndIsReadFalseOrderByCreatedAtDesc(
                                lecturerId
                        );

        for (Notification notification :
                notifications) {

            notification.setIsRead(true);
        }

        notificationRepository.saveAll(
                notifications
        );
    }


    // =========================================================
    // HOD NOTIFICATIONS
    // =========================================================

    @Override
    public List<Notification>
    getHODNotifications(
            Long lecturerId) {

        Authentication authentication =
                getAuthentication();

        validateHODOwnership(
                lecturerId,
                authentication
        );

        Lecturer lecturer =
                lecturerRepository
                        .findById(lecturerId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "HOD lecturer account not found."
                                )
                        );

        if (lecturer.getUser() == null
                || lecturer.getUser().getRole()
                != UserRole.HOD) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "The specified lecturer is not an HOD."
            );
        }

        return notificationRepository
                .findByLecturer_LecturerIdOrderByCreatedAtDesc(
                        lecturerId
                );
    }


    @Override
    public List<Notification>
    getUnreadHODNotifications(
            Long lecturerId) {

        Authentication authentication =
                getAuthentication();

        validateHODOwnership(
                lecturerId,
                authentication
        );

        Lecturer lecturer =
                lecturerRepository
                        .findById(lecturerId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "HOD lecturer account not found."
                                )
                        );

        if (lecturer.getUser() == null
                || lecturer.getUser().getRole()
                != UserRole.HOD) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "The specified lecturer is not an HOD."
            );
        }

        return notificationRepository
                .findByLecturer_LecturerIdAndIsReadFalseOrderByCreatedAtDesc(
                        lecturerId
                );
    }


    @Override
    public void markAllHODNotificationsAsRead(
            Long lecturerId) {

        Authentication authentication =
                getAuthentication();

        validateHODOwnership(
                lecturerId,
                authentication
        );

        Lecturer lecturer =
                lecturerRepository
                        .findById(lecturerId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "HOD lecturer account not found."
                                )
                        );

        if (lecturer.getUser() == null
                || lecturer.getUser().getRole()
                != UserRole.HOD) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "The specified lecturer is not an HOD."
            );
        }

        List<Notification> notifications =
                notificationRepository
                        .findByLecturer_LecturerIdAndIsReadFalseOrderByCreatedAtDesc(
                                lecturerId
                        );

        for (Notification notification :
                notifications) {

            notification.setIsRead(true);
        }

        notificationRepository.saveAll(
                notifications
        );
    }


    @Override
    public Notification notifyHOD(
            Long lecturerId,
            String title,
            String message,
            String notificationType) {

        Lecturer lecturer =
                lecturerRepository
                        .findById(lecturerId)
                        .orElse(null);

        if (lecturer == null) {
            return null;
        }

        if (lecturer.getUser() == null) {
            return null;
        }

        if (lecturer.getUser().getRole()
                != UserRole.HOD) {

            return null;
        }

        Notification notification =
                new Notification();

        notification.setStudent(null);

        notification.setLecturer(
                lecturer
        );

        notification.setTitle(
                title
        );

        notification.setMessage(
                message
        );

        notification.setNotificationType(
                notificationType
        );

        notification.setIsRead(false);

        notification.setCreatedAt(
                LocalDateTime.now()
        );

        return notificationRepository.save(
                notification
        );
    }


    // =========================================================
    // GET NOTIFICATION BY ID
    // =========================================================

    @Override
    public Notification getNotificationById(
            Long notificationId) {

        Authentication authentication =
                getAuthentication();

        if (notificationId == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Notification ID is required."
            );
        }

        Notification notification =
                notificationRepository
                        .findById(notificationId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Notification not found."
                                )
                        );

        validateNotificationOwnership(
                notification,
                authentication
        );

        return notification;
    }


    // =========================================================
    // MARK ONE AS READ
    // =========================================================

    @Override
    public Notification markAsRead(
            Long notificationId) {

        Authentication authentication =
                getAuthentication();

        if (notificationId == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Notification ID is required."
            );
        }

        Notification notification =
                notificationRepository
                        .findById(notificationId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Notification not found."
                                )
                        );

        validateNotificationOwnership(
                notification,
                authentication
        );

        notification.setIsRead(true);

        return notificationRepository.save(
                notification
        );
    }


    // =========================================================
    // DELETE NOTIFICATION
    // =========================================================

    @Override
    public void deleteNotification(
            Long notificationId) {

        Authentication authentication =
                getAuthentication();

        if (notificationId == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Notification ID is required."
            );
        }

        Notification notification =
                notificationRepository
                        .findById(notificationId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Notification not found."
                                )
                        );

        validateNotificationOwnership(
                notification,
                authentication
        );

        notificationRepository.delete(
                notification
        );
    }


    // =========================================================
    // NOTIFY ALL STUDENTS FOR SUBJECT
    // =========================================================

    @Override
    public List<Notification>
    notifyAllStudentsForSubject(
            Long subjectId,
            String title,
            String message,
            String notificationType) {

        Authentication authentication =
                getAuthentication();

        /*
         * This operation is normally triggered internally by
         * authenticated administrative/business processes.
         *
         * Do not allow ordinary Student/Lecturer/HOD users
         * to generate notifications for an entire subject.
         */

        if (!isAdmin(authentication)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only administrators can generate notifications for all students of a subject."
            );
        }

        List<StudentSubjectRegistration> registrations =
                registrationService
                        .getRegistrationsBySubjectId(
                                subjectId,
                                authentication
                        );

        List<Notification> notifications =
                new ArrayList<>();

        for (StudentSubjectRegistration registration :
                registrations) {

            Notification notification =
                    new Notification();

            notification.setStudent(
                    registration.getStudent()
            );

            notification.setLecturer(null);

            notification.setTitle(
                    title
            );

            notification.setMessage(
                    message
            );

            notification.setNotificationType(
                    notificationType
            );

            notification.setIsRead(false);

            notification.setCreatedAt(
                    LocalDateTime.now()
            );

            notifications.add(
                    notification
            );
        }

        return notificationRepository.saveAll(
                notifications
        );
    }


    // =========================================================
    // NOTIFY ONE LECTURER
    // =========================================================

    @Override
    public Notification notifyLecturer(
            Long lecturerId,
            String title,
            String message,
            String notificationType) {

        Authentication authentication =
                getAuthentication();

        /*
         * Notification generation for another lecturer is an
         * administrative/business operation.
         *
         * Students, lecturers and HODs must not manipulate the
         * lecturerId and generate notifications themselves.
         */

        if (!isAdmin(authentication)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only administrators can generate notifications for lecturers."
            );
        }

        Lecturer lecturer =
                lecturerRepository
                        .findById(lecturerId)
                        .orElse(null);

        if (lecturer == null) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Lecturer not found."
            );
        }

        Notification notification =
                new Notification();

        notification.setStudent(null);

        notification.setLecturer(
                lecturer
        );

        notification.setTitle(
                title
        );

        notification.setMessage(
                message
        );

        notification.setNotificationType(
                notificationType
        );

        notification.setIsRead(false);

        notification.setCreatedAt(
                LocalDateTime.now()
        );

        return notificationRepository.save(
                notification
        );
    }
}
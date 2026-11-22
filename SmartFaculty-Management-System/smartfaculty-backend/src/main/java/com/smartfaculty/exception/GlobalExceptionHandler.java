package com.smartfaculty.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =====================================================
    // 401 - AUTHENTICATION REQUIRED
    // =====================================================

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationException(
            AuthenticationException exception) {

        Map<String, String> response = new HashMap<>();

        response.put(
                "message",
                "Authentication is required."
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }


    // =====================================================
    // 403 - ACCESS DENIED
    // =====================================================

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(
            AccessDeniedException exception) {

        Map<String, String> response = new HashMap<>();

        response.put(
                "message",
                "You do not have permission to access this resource."
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }


    // =====================================================
    // 400 - ILLEGAL ARGUMENT
    // =====================================================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(
            IllegalArgumentException exception) {

        Map<String, String> response = new HashMap<>();

        response.put(
                "message",
                exception.getMessage() != null
                        ? exception.getMessage()
                        : "Invalid request."
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }


    // =====================================================
    // 400 - ILLEGAL STATE
    // =====================================================

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(
            IllegalStateException exception) {

        Map<String, String> response = new HashMap<>();

        response.put(
                "message",
                exception.getMessage() != null
                        ? exception.getMessage()
                        : "The requested operation cannot be performed."
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // =====================================================
// HANDLE RESPONSE STATUS EXCEPTIONS
// =====================================================

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(
            ResponseStatusException exception) {

        Map<String, String> response = new HashMap<>();

        response.put(
                "message",
                exception.getReason() != null
                        ? exception.getReason()
                        : "The requested operation could not be completed."
        );

        return ResponseEntity
                .status(exception.getStatusCode())
                .body(response);
    }


    // =====================================================
    // 500 - GENERAL SERVER ERROR
    // =====================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(
            Exception exception) {

        // Print the real error in IntelliJ console.
        // This is important during development so that
        // the actual cause is not hidden.
        exception.printStackTrace();

        Map<String, String> response = new HashMap<>();

        response.put(
                "message",
                "An unexpected server error occurred."
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}

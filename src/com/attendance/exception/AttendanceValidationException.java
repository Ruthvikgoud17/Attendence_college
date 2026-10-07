package com.attendance.exception;

/**
 * Exception thrown when attendance record validation rules are violated.
 */
public class AttendanceValidationException extends RuntimeException {
    public AttendanceValidationException(String message) {
        super("Attendance Validation Error: " + message);
    }

    public AttendanceValidationException(String message, Throwable cause) {
        super("Attendance Validation Error: " + message, cause);
    }
}

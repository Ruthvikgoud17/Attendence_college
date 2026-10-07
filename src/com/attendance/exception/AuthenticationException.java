package com.attendance.exception;

/**
 * Exception thrown when authentication fails for a teacher or student.
 */
public class AuthenticationException extends Exception {
    private final String attemptedIdentifier;

    public AuthenticationException(String attemptedIdentifier, String message) {
        super("Authentication Failed for [" + attemptedIdentifier + "]: " + message);
        this.attemptedIdentifier = attemptedIdentifier;
    }

    public String getAttemptedIdentifier() {
        return attemptedIdentifier;
    }
}

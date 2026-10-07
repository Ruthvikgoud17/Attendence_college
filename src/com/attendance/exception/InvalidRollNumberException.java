package com.attendance.exception;

/**
 * Exception thrown when a roll number format or value is invalid.
 */
public class InvalidRollNumberException extends Exception {
    private final String invalidRollNumber;

    public InvalidRollNumberException(String invalidRollNumber) {
        super("Invalid Roll Number: '" + invalidRollNumber + "'. Roll numbers must be numeric (e.g., 101 to 130).");
        this.invalidRollNumber = invalidRollNumber;
    }

    public InvalidRollNumberException(String invalidRollNumber, String customMessage) {
        super(customMessage);
        this.invalidRollNumber = invalidRollNumber;
    }

    public String getInvalidRollNumber() {
        return invalidRollNumber;
    }
}

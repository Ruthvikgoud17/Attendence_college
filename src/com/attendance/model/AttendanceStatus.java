package com.attendance.model;

/**
 * Represents the status of student attendance for a lecture.
 */
public enum AttendanceStatus {
    PRESENT("P", "Present"),
    ABSENT("A", "Absent"),
    ON_LEAVE("L", "Leave");

    private final String code;
    private final String displayName;

    AttendanceStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static AttendanceStatus fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return PRESENT;
        }
        String normalized = text.trim().toUpperCase();
        return switch (normalized) {
            case "P", "PRESENT" -> PRESENT;
            case "A", "ABSENT" -> ABSENT;
            case "L", "LEAVE", "ON_LEAVE" -> ON_LEAVE;
            default -> null;
        };
    }
}

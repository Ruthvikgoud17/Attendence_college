package com.attendance.model;

/**
 * Model representing an academic Subject with its assigned Faculty and Login Password.
 */
public class Subject {
    private String subjectCode;
    private String subjectName;
    private String facultyName;
    private String password;

    public Subject(String subjectCode, String subjectName, String facultyName, String password) {
        this.subjectCode = subjectCode != null ? subjectCode.trim().toUpperCase() : "";
        this.subjectName = subjectName != null ? subjectName.trim() : "";
        this.facultyName = facultyName != null ? facultyName.trim() : "";
        this.password = password != null ? password.trim().toLowerCase() : "";
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public String getPassword() {
        return password;
    }

    public boolean verifyPassword(String inputPassword) {
        if (inputPassword == null) return false;
        return this.password.equalsIgnoreCase(inputPassword.trim());
    }

    public String toCsv() {
        return subjectCode + "," + subjectName + "," + facultyName + "," + password;
    }

    public static Subject fromCsv(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length >= 4) {
            return new Subject(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim());
        } else if (parts.length >= 2) {
            // Fallback for older format
            String code = parts[0].trim();
            return new Subject(code, parts[1].trim(), "Faculty In-Charge", code.toLowerCase());
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (Teacher: %s)", subjectCode, subjectName, facultyName);
    }
}

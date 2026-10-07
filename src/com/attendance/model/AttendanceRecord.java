package com.attendance.model;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * Model representing attendance for a student in a specific subject on a date.
 */
public class AttendanceRecord {
    private String rollNo;
    private String subjectCode;
    private LocalDate date;
    private String status; // "Present" or "Absent"

    public AttendanceRecord(String rollNo, String subjectCode, LocalDate date, String status) {
        this.rollNo = rollNo != null ? rollNo.trim().toUpperCase() : "";
        this.subjectCode = subjectCode != null ? subjectCode.trim().toUpperCase() : "";
        this.date = date != null ? date : LocalDate.now();
        this.status = normalizeStatus(status);
    }

    private String normalizeStatus(String st) {
        if (st == null) return "Present";
        String s = st.trim().toUpperCase();
        if (s.startsWith("A")) return "Absent";
        return "Present";
    }

    public String getRecordId() {
        return (rollNo + "#" + subjectCode + "#" + date).toUpperCase();
    }

    public String getRollNo() {
        return rollNo;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = normalizeStatus(status);
    }

    public boolean isPresent() {
        return "Present".equalsIgnoreCase(status);
    }

    public String toCsv() {
        return rollNo + "," + subjectCode + "," + date.toString() + "," + status;
    }

    public static AttendanceRecord fromCsv(String csvLine) {
        String[] tokens = csvLine.split(",", -1);
        if (tokens.length >= 4) {
            String roll = tokens[0].trim();
            String sub = tokens[1].trim();
            LocalDate dt = LocalDate.now();
            try {
                dt = LocalDate.parse(tokens[2].trim());
            } catch (DateTimeParseException ignored) {}
            String st = tokens[3].trim();
            return new AttendanceRecord(roll, sub, dt, st);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttendanceRecord that = (AttendanceRecord) o;
        return Objects.equals(getRecordId(), that.getRecordId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRecordId());
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s", date, subjectCode, rollNo, status);
    }
}

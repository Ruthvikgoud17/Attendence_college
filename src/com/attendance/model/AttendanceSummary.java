package com.attendance.model;

/**
 * Summary statistics of student attendance in a subject.
 */
public class AttendanceSummary {
    private String subjectCode;
    private String subjectName;
    private String facultyName;
    private int totalClasses;
    private int presentClasses;
    private int absentClasses;
    private double percentage;
    private String status;
    private int classesNeededFor75;

    public AttendanceSummary(String subjectCode, String subjectName, int totalClasses, int presentClasses, int absentClasses) {
        this(subjectCode, subjectName, "", totalClasses, presentClasses, absentClasses);
    }

    public AttendanceSummary(String subjectCode, String subjectName, String facultyName, int totalClasses, int presentClasses, int absentClasses) {
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.facultyName = facultyName != null ? facultyName : "";
        this.totalClasses = totalClasses;
        this.presentClasses = presentClasses;
        this.absentClasses = absentClasses;

        if (totalClasses > 0) {
            this.percentage = ((double) presentClasses / totalClasses) * 100.0;
        } else {
            this.percentage = 0.0;
        }

        if (totalClasses == 0) {
            this.status = "No Lectures Held";
            this.classesNeededFor75 = 0;
        } else if (this.percentage >= 75.0) {
            this.status = "Eligible (>= 75%)";
            this.classesNeededFor75 = 0;
        } else {
            this.status = "Shortage (< 75%)";
            // Recovery formula: ceil((75 * Total - 100 * Present) / 25)
            double needed = Math.ceil(((75.0 * totalClasses) - (100.0 * presentClasses)) / 25.0);
            this.classesNeededFor75 = (int) Math.max(0, needed);
        }
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

    public int getTotalClasses() {
        return totalClasses;
    }

    public int getPresentClasses() {
        return presentClasses;
    }

    public int getAbsentClasses() {
        return absentClasses;
    }

    public double getPercentage() {
        return percentage;
    }

    public String getStatus() {
        return status;
    }

    public int getClassesNeededFor75() {
        return classesNeededFor75;
    }

    public boolean isEligible() {
        return totalClasses > 0 && percentage >= 75.0;
    }
}

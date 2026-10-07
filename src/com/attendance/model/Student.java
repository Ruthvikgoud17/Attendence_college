package com.attendance.model;

import java.util.Objects;

/**
 * Model representing a Student.
 */
public class Student {
    private String rollNo;
    private String name;
    private String department;
    private String yearSection;

    public Student(String rollNo, String name, String department, String yearSection) {
        this.rollNo = rollNo != null ? rollNo.trim().toUpperCase() : "";
        this.name = name != null ? name.trim() : "";
        this.department = department != null ? department.trim().toUpperCase() : "";
        this.yearSection = yearSection != null ? yearSection.trim().toUpperCase() : "";
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo != null ? rollNo.trim().toUpperCase() : "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : "";
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department != null ? department.trim().toUpperCase() : "";
    }

    public String getYearSection() {
        return yearSection;
    }

    public void setYearSection(String yearSection) {
        this.yearSection = yearSection != null ? yearSection.trim().toUpperCase() : "";
    }

    public String toCsv() {
        return rollNo + "," + name + "," + department + "," + yearSection;
    }

    public static Student fromCsv(String csvLine) {
        String[] tokens = csvLine.split(",", -1);
        if (tokens.length >= 4) {
            return new Student(tokens[0].trim(), tokens[1].trim(), tokens[2].trim(), tokens[3].trim());
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(rollNo, student.rollNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rollNo);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s - %s)", rollNo, name, department, yearSection);
    }
}

package com.attendance.service;

import com.attendance.model.AttendanceRecord;
import com.attendance.model.AttendanceSummary;
import com.attendance.model.Student;
import com.attendance.model.Subject;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AttendanceService managing business logic for:
 * - 30 Students
 * - 5 Subjects (Java, Maths, Python, DSA, Web)
 * - Teacher attendance entry by roll numbers
 * - Student personal dashboard (all 5 subjects view)
 */
public class AttendanceService {

    private final Map<String, Student> students = new LinkedHashMap<>();
    private final Map<String, Subject> subjects = new LinkedHashMap<>();
    private final List<AttendanceRecord> attendanceRecords = new ArrayList<>();
    private final DataPersistenceService persistenceService;

    public AttendanceService(DataPersistenceService persistenceService) {
        this.persistenceService = persistenceService;
        loadData();
    }

    public void loadData() {
        students.clear();
        for (Student s : persistenceService.loadStudents()) {
            students.put(s.getRollNo().toUpperCase(), s);
        }

        subjects.clear();
        for (Subject sub : persistenceService.loadSubjects()) {
            subjects.put(sub.getSubjectCode().toUpperCase(), sub);
        }

        attendanceRecords.clear();
        attendanceRecords.addAll(persistenceService.loadAttendance());
    }

    public void persistAll() {
        persistenceService.saveStudents(getAllStudents());
        persistenceService.saveSubjects(getAllSubjects());
        persistenceService.saveAttendance(getAllAttendanceRecords());
    }

    // ==========================================
    // Student & Subject Operations
    // ==========================================

    public boolean addStudent(Student student) {
        if (student == null || student.getRollNo().isEmpty()) return false;
        String roll = student.getRollNo().toUpperCase();
        students.put(roll, student);
        persistenceService.saveStudents(getAllStudents());
        return true;
    }

    public Optional<Student> getStudent(String rollNo) {
        if (rollNo == null) return Optional.empty();
        return Optional.ofNullable(students.get(rollNo.trim().toUpperCase()));
    }

    public List<Student> getAllStudents() {
        return new ArrayList<>(students.values());
    }

    public boolean addSubject(Subject subject) {
        if (subject == null || subject.getSubjectCode().isEmpty()) return false;
        String code = subject.getSubjectCode().toUpperCase();
        subjects.put(code, subject);
        persistenceService.saveSubjects(getAllSubjects());
        return true;
    }

    public Optional<Subject> getSubject(String code) {
        if (code == null) return Optional.empty();
        return Optional.ofNullable(subjects.get(code.trim().toUpperCase()));
    }

    public List<Subject> getAllSubjects() {
        return new ArrayList<>(subjects.values());
    }

    // ==========================================
    // Teacher Attendance Posting
    // ==========================================

    public boolean markAttendance(String rollNo, String subjectCode, LocalDate date, String status) {
        String roll = rollNo.trim().toUpperCase();
        String sub = subjectCode.trim().toUpperCase();
        if (!students.containsKey(roll) || !subjects.containsKey(sub)) {
            return false;
        }

        AttendanceRecord record = new AttendanceRecord(roll, sub, date, status);
        attendanceRecords.removeIf(r -> r.getRecordId().equalsIgnoreCase(record.getRecordId()));
        attendanceRecords.add(record);
        persistenceService.saveAttendance(attendanceRecords);
        return true;
    }

    public List<AttendanceRecord> getAllAttendanceRecords() {
        return new ArrayList<>(attendanceRecords);
    }

    public List<AttendanceRecord> getAttendanceForDateAndSubject(String subjectCode, LocalDate date) {
        String sub = subjectCode.trim().toUpperCase();
        return attendanceRecords.stream()
                .filter(r -> r.getSubjectCode().equalsIgnoreCase(sub) && r.getDate().equals(date))
                .collect(Collectors.toList());
    }

    // ==========================================
    // Student Attendance Analytics (All 5 Subjects)
    // ==========================================

    /**
     * Calculates attendance summary for a student in a specific subject.
     */
    public AttendanceSummary getStudentSubjectSummary(String rollNo, String subjectCode) {
        String roll = rollNo.trim().toUpperCase();
        String subCode = subjectCode.trim().toUpperCase();

        Subject sub = subjects.get(subCode);
        String subName = (sub != null) ? sub.getSubjectName() : subCode;

        // Count unique dates when this subject had lectures
        Set<LocalDate> dates = attendanceRecords.stream()
                .filter(r -> r.getSubjectCode().equalsIgnoreCase(subCode))
                .map(AttendanceRecord::getDate)
                .collect(Collectors.toSet());

        int totalClasses = dates.size();
        int presentClasses = 0;
        int absentClasses = 0;

        for (AttendanceRecord r : attendanceRecords) {
            if (r.getRollNo().equalsIgnoreCase(roll) && r.getSubjectCode().equalsIgnoreCase(subCode)) {
                if (r.isPresent()) presentClasses++;
                else absentClasses++;
            }
        }

        // Account for any unrecorded days as absent
        int recorded = presentClasses + absentClasses;
        if (recorded < totalClasses) {
            absentClasses += (totalClasses - recorded);
        }

        return new AttendanceSummary(subCode, subName, totalClasses, presentClasses, absentClasses);
    }

    /**
     * Retrieves all 5 subjects attendance for a student.
     */
    public List<AttendanceSummary> getStudentAllSubjectsSummary(String rollNo) {
        List<AttendanceSummary> list = new ArrayList<>();
        for (Subject sub : subjects.values()) {
            list.add(getStudentSubjectSummary(rollNo, sub.getSubjectCode()));
        }
        return list;
    }

    /**
     * Gets detailed date-by-date attendance records for a student in a specific subject.
     */
    public List<AttendanceRecord> getStudentSubjectHistory(String rollNo, String subjectCode) {
        String roll = rollNo.trim().toUpperCase();
        String sub = subjectCode.trim().toUpperCase();
        return attendanceRecords.stream()
                .filter(r -> r.getRollNo().equalsIgnoreCase(roll) && r.getSubjectCode().equalsIgnoreCase(sub))
                .sorted(Comparator.comparing(AttendanceRecord::getDate))
                .collect(Collectors.toList());
    }

    // ==========================================
    // Reports & Defaulter Analysis
    // ==========================================

    /**
     * Returns list of students with attendance below threshold (< 75%) in a subject.
     */
    public List<Map<String, Object>> getSubjectDefaulters(String subjectCode, double threshold) {
        List<Map<String, Object>> defaulters = new ArrayList<>();
        for (Student s : students.values()) {
            AttendanceSummary sum = getStudentSubjectSummary(s.getRollNo(), subjectCode);
            if (sum.getTotalClasses() > 0 && sum.getPercentage() < threshold) {
                Map<String, Object> map = new HashMap<>();
                map.put("student", s);
                map.put("summary", sum);
                defaulters.add(map);
            }
        }
        return defaulters;
    }
}

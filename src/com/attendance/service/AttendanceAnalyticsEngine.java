package com.attendance.service;

import com.attendance.model.AttendanceSummary;
import com.attendance.model.Student;
import com.attendance.model.Subject;
import com.attendance.util.TableFormatter;

import java.util.*;
import java.util.stream.Collectors;

import static com.attendance.util.TableFormatter.*;

/**
 * High-Performance Analytics & Stream Reporting Engine.
 * Analyzes attendance data structures using Java Collections and functional Stream pipelines.
 * Computes class aggregates, distribution statistics, and identifies defaulters (< 75%).
 * Course Faculty: Dr. Naga Padmaja
 */
public class AttendanceAnalyticsEngine {

    public static void runAnalytics(AttendanceService service) {
        System.out.println(CYAN + BOLD);
        System.out.println("==========================================================================");
        System.out.println("        ATTENDANCE ANALYTICS ENGINE & STREAM REPORTING PIPELINE           ");
        System.out.println("        Collections Architecture • Functional Streams • Defaulters (< 75%)");
        System.out.println("              Course Faculty: Dr. Naga Padmaja (Java Programming)         ");
        System.out.println("==========================================================================" + RESET);

        // Section 1: Collections Architecture
        System.out.println("\n" + YELLOW + BOLD + "[1] CORE DATA STRUCTURES & COLLECTIONS" + RESET);
        List<Student> studentList = service.getAllStudents();
        System.out.printf("  • Enrolled Student Roster (List)         : %d students%n", studentList.size());

        Set<String> uniqueSubjectCodes = service.getAllSubjects().stream()
                .map(Subject::getSubjectCode)
                .collect(Collectors.toCollection(TreeSet::new));
        System.out.printf("  • Registered Curriculum Codes (Set)      : %s%n", uniqueSubjectCodes);

        Map<String, String> facultyMap = service.getAllSubjects().stream()
                .collect(Collectors.toMap(Subject::getSubjectCode, Subject::getFacultyName));
        System.out.println("  • Faculty Assignment Registry (Map)      : Subject -> In-Charge:");
        facultyMap.forEach((code, name) -> System.out.printf("      [%s] -> %s%n", code, name));

        // Section 2: Defaulter Pipeline
        System.out.println("\n" + YELLOW + BOLD + "[2] HIGH-PERFORMANCE STREAM FILTER: ATTENDANCE SHORTAGE (< 75%)" + RESET);

        List<StudentDefaulterDTO> defaulters = studentList.stream()
                .map(student -> {
                    List<AttendanceSummary> sums = service.getStudentAllSubjectsSummary(student.getRollNo());
                    int totalHeld = sums.stream().mapToInt(AttendanceSummary::getTotalClasses).sum();
                    int totalAttended = sums.stream().mapToInt(AttendanceSummary::getPresentClasses).sum();
                    double pct = totalHeld > 0 ? ((double) totalAttended / totalHeld) * 100.0 : 0.0;
                    return new StudentDefaulterDTO(student.getRollNo(), student.getName(), totalHeld, totalAttended, pct);
                })
                .filter(dto -> dto.percentage < 75.0)
                .sorted(Comparator.comparingDouble(dto -> dto.percentage))
                .collect(Collectors.toList());

        TableFormatter tf = new TableFormatter("Roll No", "Student Name", "Held", "Attended", "Overall %", "Status", "Action");
        tf.setTitle("SHORTAGE REPORT: STUDENTS WITH ATTENDANCE BELOW 75%");

        for (StudentDefaulterDTO d : defaulters) {
            tf.addRow(
                    d.rollNo,
                    d.name,
                    String.valueOf(d.totalHeld),
                    String.valueOf(d.totalAttended),
                    RED + String.format("%.2f%%", d.percentage) + RESET,
                    RED + "SHORTAGE (< 75%)" + RESET,
                    YELLOW + "Issue Warning Letter" + RESET
            );
        }

        tf.print();

        DoubleSummaryStatistics stats = studentList.stream()
                .mapToDouble(s -> {
                    List<AttendanceSummary> sums = service.getStudentAllSubjectsSummary(s.getRollNo());
                    int held = sums.stream().mapToInt(AttendanceSummary::getTotalClasses).sum();
                    int att = sums.stream().mapToInt(AttendanceSummary::getPresentClasses).sum();
                    return held > 0 ? ((double) att / held) * 100.0 : 0.0;
                })
                .summaryStatistics();

        System.out.println("\n" + CYAN + BOLD + "Overall Class Attendance Metrics:" + RESET);
        System.out.printf("  • Total Enrolled Students : %d%n", stats.getCount());
        System.out.printf("  • Highest Attendance Pct  : %.2f%%%n", stats.getMax());
        System.out.printf("  • Lowest Attendance Pct   : %.2f%%%n", stats.getMin());
        System.out.printf("  • Average Class Turnout   : %.2f%%%n", stats.getAverage());
        System.out.printf("  • Defaulters Count (< 75%%): %s%d students%s%n", RED + BOLD, defaulters.size(), RESET);
    }

    private static class StudentDefaulterDTO {
        final String rollNo;
        final String name;
        final int totalHeld;
        final int totalAttended;
        final double percentage;

        StudentDefaulterDTO(String rollNo, String name, int totalHeld, int totalAttended, double percentage) {
            this.rollNo = rollNo;
            this.name = name;
            this.totalHeld = totalHeld;
            this.totalAttended = totalAttended;
            this.percentage = percentage;
        }
    }
}

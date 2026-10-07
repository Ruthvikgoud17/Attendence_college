package com.attendance.dao;

import com.attendance.model.Student;
import com.attendance.model.Subject;
import com.attendance.service.AttendanceService;

import static com.attendance.util.TableFormatter.*;

/**
 * Relational Database & SQL Persistence Simulator.
 * Demonstrates 4-Tier enterprise architecture, database driver connections,
 * parameterized PreparedStatement execution, and ResultSet mapping.
 * Course Faculty: Dr. Naga Padmaja
 */
public class DatabaseSimulator {

    public static void runSimulation(AttendanceService service) {
        System.out.println(CYAN + BOLD);
        System.out.println("==========================================================================");
        System.out.println("          RELATIONAL DATABASE & SQL PERSISTENCE SIMULATOR                 ");
        System.out.println("      Enterprise Tier Architecture • PreparedStatement • ResultSet Engine ");
        System.out.println("              Course Faculty: Dr. Naga Padmaja (Java Programming)         ");
        System.out.println("==========================================================================" + RESET);

        System.out.println("\n" + YELLOW + BOLD + "[1] 4-TIER ARCHITECTURE DATA FLOW" + RESET);
        System.out.println("  • Application Layer     : Attendance Management Service (com.attendance.service)");
        System.out.println("  • Database Access Layer : JDBC API (Connection, PreparedStatement, ResultSet)");
        System.out.println("  • Driver Manager Layer  : Type-4 Pure Java Thin Driver");
        System.out.println("  • Data Persistence Tier : Enterprise Relational Database / Storage");

        System.out.println("\n" + YELLOW + BOLD + "[2] SQL PREPARED STATEMENT EXECUTION" + RESET);
        String insertSql = "INSERT INTO attendance_records (roll_no, subject_code, lecture_date, status) VALUES (?, ?, ?, ?);";
        System.out.println("  SQL Template: " + CYAN + insertSql + RESET);

        Student sample = service.getStudent("101").orElse(null);
        Subject javaSub = service.getSubject("JAVA").orElse(null);

        if (sample != null && javaSub != null) {
            System.out.println("  Binding Query Parameters:");
            System.out.printf("    param[1] = \"%s\" (Student Roll)%n", sample.getRollNo());
            System.out.printf("    param[2] = \"%s\" (Subject Code - Faculty: %s)%n", javaSub.getSubjectCode(), javaSub.getFacultyName());
            System.out.println("    param[3] = 2026-10-07 (Lecture Date)");
            System.out.println("    param[4] = \"Present\" (Status)");
            System.out.println("  Executing: pstmt.executeUpdate()");
            System.out.println(GREEN + "  ✓ Result: Record committed to database successfully." + RESET);
        }

        System.out.println("\n" + YELLOW + BOLD + "[3] AGGREGATE SQL QUERY & RESULTSET MAPPING" + RESET);
        String querySql = "SELECT roll_no, COUNT(*) as attended FROM attendance_records WHERE status = 'Present' GROUP BY roll_no;";
        System.out.println("  SQL Query: " + CYAN + querySql + RESET);
        System.out.println("  Executing: ResultSet rs = pstmt.executeQuery()");
        System.out.println("  Streaming records via rs.next() with automatic try-with-resources resource cleanup.");
        System.out.println(GREEN + "  ✓ Transaction completed with zero database lock contention." + RESET);
    }
}

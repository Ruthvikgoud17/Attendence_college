package com.attendance;

import com.attendance.concurrent.ConcurrentAttendanceAuditor;
import com.attendance.dao.DatabaseSimulator;
import com.attendance.exception.AuthenticationException;
import com.attendance.exception.InvalidRollNumberException;
import com.attendance.gui.AttendanceRosterWindow;
import com.attendance.model.*;
import com.attendance.service.AttendanceAnalyticsEngine;
import com.attendance.service.AttendanceService;
import com.attendance.service.DataPersistenceService;
import com.attendance.util.TableFormatter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import static com.attendance.util.TableFormatter.*;

/**
 * College Attendance Management System.
 * Course Faculty: Dr. Naga Padmaja (Java Programming)
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static AttendanceService service;
    private static DataPersistenceService persistenceService;

    public static void main(String[] args) {
        persistenceService = new DataPersistenceService();
        service = new AttendanceService(persistenceService);

        // Preload 30 students, 5 subjects, and 10 days of attendance
        if (service.getAllStudents().isEmpty()) {
            persistenceService.seedInitialDemoData(service);
            service.loadData();
        }

        printBanner();

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = prompt("Enter choice [1-7]: ").trim();

            switch (choice) {
                case "1" -> handleTeacherLogin();
                case "2" -> handleStudentLogin();
                case "3" -> ConcurrentAttendanceAuditor.runAudit(service);
                case "4" -> AttendanceAnalyticsEngine.runAnalytics(service);
                case "5" -> AttendanceRosterWindow.openWindow(service);
                case "6" -> DatabaseSimulator.runSimulation(service);
                case "7" -> {
                    service.persistAll();
                    System.out.println("\n" + GREEN + "✓ All attendance records saved successfully. Goodbye!" + RESET);
                    running = false;
                }
                default -> System.out.println(RED + "Invalid choice! Please select 1 through 7." + RESET);
            }
        }
    }

    private static void printBanner() {
        System.out.println(CYAN + BOLD);
        System.out.println("==========================================================================");
        System.out.println("                    COLLEGE ATTENDANCE MANAGEMENT SYSTEM                  ");
        System.out.println("            Enterprise Attendance Tracking, Analytics & Reporting         ");
        System.out.println("              Course Lecturer: DR.J.Naga Padmaja (OOPS)                   ");
        System.out.println("==========================================================================" + RESET);
    }

    private static void printMainMenu() {
        System.out.println("\n" + BOLD + "=================== MAIN MENU ===================" + RESET);
        System.out.println("  [" + CYAN + "1" + RESET + "] Teacher Portal (Faculty Attendance - DR.J.Naga Padmaja)");
        System.out.println("  [" + CYAN + "2" + RESET + "] Student Portal (Roll Number + Password <roll>@123)");
        System.out.println("  [" + CYAN + "3" + RESET + "] Multi-Threaded Classroom Attendance Audit Engine");
        System.out.println("  [" + CYAN + "4" + RESET + "] High-Performance Analytics & Defaulters Report (< 75%)");
        System.out.println("  [" + CYAN + "5" + RESET + "] Interactive GUI Roster Window (Swing Event Model)");
        System.out.println("  [" + CYAN + "6" + RESET + "] Relational Database & SQL Persistence Simulator");
        System.out.println("  [" + CYAN + "7" + RESET + "] Save & Exit System");
        System.out.println("-------------------------------------------------");
    }

    // =========================================================================
    // 1. TEACHER LOGIN & SUBJECT-SPECIFIC DASHBOARD
    // =========================================================================

    private static void handleTeacherLogin() {
        System.out.println("\n" + CYAN + BOLD + "========== TEACHER LOGIN ==========" + RESET);
        System.out.println("5 Subjects & 5 Teachers available:");
        System.out.println("  - [OOPS]   Object Oriented Programming  -> Password: " + YELLOW + "oops" + RESET + "   (DR.J.Naga Padmaja)");
        System.out.println("  - [OSS]    Operating Systems            -> Password: " + YELLOW + "oss" + RESET + "    (Kavitha Chitrala)");
        System.out.println("  - [BEFA]   Business Economics & Fin     -> Password: " + YELLOW + "befa" + RESET + "   (Sallapalle Archana)");
        System.out.println("  - [DMS]    Discrete Mathematics         -> Password: " + YELLOW + "dms" + RESET + "    (DhanaLakshmi Dumpala)");
        System.out.println("  - [DBMS]   Database Management Systems  -> Password: " + YELLOW + "dbms" + RESET + "   (Prasada Reddy G S)");
        System.out.println("--------------------------------------------------------------------------");

        String passwordInput = prompt("Enter Subject Password: ").trim().toLowerCase();

        // Find subject corresponding to the password
        Subject loggedInSubject = null;
        for (Subject sub : service.getAllSubjects()) {
            if (sub.verifyPassword(passwordInput)) {
                loggedInSubject = sub;
                break;
            }
        }

        if (loggedInSubject == null) {
            System.out.println(RED + "Invalid password! Password must be the subject name (e.g. oops, oss, befa, dms, dbms)." + RESET);
            return;
        }

        System.out.println(GREEN + BOLD + "\n✓ Authentication Successful!" + RESET);
        System.out.printf(GREEN + "✓ Welcome, %s! Logged in as Teacher for: %s [%s]%n" + RESET,
                loggedInSubject.getFacultyName(), loggedInSubject.getSubjectName(), loggedInSubject.getSubjectCode());

        // Open dedicated Subject Teacher Dashboard
        runTeacherSubjectDashboard(loggedInSubject);
    }

    private static void runTeacherSubjectDashboard(Subject subject) {
        boolean session = true;
        while (session) {
            System.out.println("\n" + BOLD + "========== [" + CYAN + subject.getSubjectName().toUpperCase() + RESET + BOLD + "] TEACHER DASHBOARD ==========" + RESET);
            System.out.println("  Teacher In-Charge : " + CYAN + subject.getFacultyName() + RESET);
            System.out.println("  Subject Code      : " + CYAN + subject.getSubjectCode() + RESET);
            System.out.println("---------------------------------------------------------------");
            System.out.println("  [" + CYAN + "1" + RESET + "] Post Attendance (Select Roll Numbers for " + subject.getSubjectCode() + ")");
            System.out.println("  [" + CYAN + "2" + RESET + "] View Students Attendance Percentage (All 30 Students)");
            System.out.println("  [" + CYAN + "3" + RESET + "] View Subject Defaulters List (< 75% Shortage)");
            System.out.println("  [" + CYAN + "4" + RESET + "] View Daily Attendance Register for a Date");
            System.out.println("  [" + CYAN + "5" + RESET + "] Logout to Main Menu");
            System.out.println("---------------------------------------------------------------");

            String ch = prompt("Select Option [1-5]: ").trim();
            switch (ch) {
                case "1" -> postAttendanceForSubject(subject);
                case "2" -> viewStudentsAttendancePercentageForSubject(subject);
                case "3" -> viewDefaultersForSubject(subject);
                case "4" -> viewDailyRegisterForSubject(subject);
                case "5" -> {
                    System.out.println(YELLOW + "Logging out " + subject.getFacultyName() + "..." + RESET);
                    session = false;
                }
                default -> System.out.println(RED + "Invalid choice! Enter [1-5]." + RESET);
            }
        }
    }

    /**
     * Teacher posts attendance for their specific subject by selecting roll numbers.
     */
    private static void postAttendanceForSubject(Subject subject) {
        System.out.println("\n" + BOLD + "--- POST ATTENDANCE: " + subject.getSubjectName() + " ---" + RESET);
        LocalDate date = promptDate("Enter Lecture Date [yyyy-MM-dd] (press Enter for today " + LocalDate.now() + "): ", LocalDate.now());

        List<Student> students = service.getAllStudents();
        if (students.isEmpty()) {
            System.out.println(RED + "No students found in the database!" + RESET);
            return;
        }

        System.out.println("\nSelect Roll Number Entry Method:");
        System.out.println("  1. Select ABSENT Roll Numbers (All other students marked PRESENT)");
        System.out.println("  2. Select PRESENT Roll Numbers (All other students marked ABSENT)");
        System.out.println("  3. Sequential Roll-by-Roll Entry (P/A for each student)");
        String mode = prompt("Choose method [1, 2, or 3]: ").trim();

        int pCount = 0;
        int aCount = 0;

        if ("1".equals(mode)) {
            System.out.println("\nTotal Enrolled Students: 30 (Roll Nos 101 to 130)");
            System.out.println("Enter comma-separated Roll Numbers of ABSENT students (e.g. 103, 108, 126):");
            System.out.println("(Press Enter if all 30 students are present)");
            String input = prompt("Absent Roll Numbers: ").trim().toUpperCase();

            Set<String> absentRolls = new HashSet<>();
            for (String r : input.split(",")) {
                if (!r.trim().isEmpty()) {
                    absentRolls.add(r.trim());
                }
            }

            for (Student s : students) {
                String status = absentRolls.contains(s.getRollNo().toUpperCase()) ? "Absent" : "Present";
                service.markAttendance(s.getRollNo(), subject.getSubjectCode(), date, status);
                if ("Present".equals(status)) pCount++;
                else aCount++;
            }
        } else if ("2".equals(mode)) {
            System.out.println("\nEnter comma-separated Roll Numbers of PRESENT students (e.g. 101, 102, 105):");
            String input = prompt("Present Roll Numbers: ").trim().toUpperCase();

            Set<String> presentRolls = new HashSet<>();
            for (String r : input.split(",")) {
                if (!r.trim().isEmpty()) {
                    presentRolls.add(r.trim());
                }
            }

            for (Student s : students) {
                String status = presentRolls.contains(s.getRollNo().toUpperCase()) ? "Present" : "Absent";
                service.markAttendance(s.getRollNo(), subject.getSubjectCode(), date, status);
                if ("Present".equals(status)) pCount++;
                else aCount++;
            }
        } else {
            System.out.println("\nMarking attendance for " + students.size() + " students in " + subject.getSubjectName() + " on " + date + ":");
            for (Student s : students) {
                String inp = prompt(String.format("  Roll [%s] %-18s (P/A, Default P): ", s.getRollNo(), s.getName())).trim().toUpperCase();
                String status = inp.startsWith("A") ? "Absent" : "Present";
                service.markAttendance(s.getRollNo(), subject.getSubjectCode(), date, status);
                if ("Present".equals(status)) pCount++;
                else aCount++;
            }
        }

        System.out.println(GREEN + BOLD + "\n✓ Attendance successfully posted for " + subject.getSubjectName() + " on " + date + "!" + RESET);
        System.out.printf("  Summary: Total: %d | Present: %d | Absent: %d%n", students.size(), pCount, aCount);
    }

    /**
     * Shows all students' attendance percentage specifically in this teacher's subject.
     */
    private static void viewStudentsAttendancePercentageForSubject(Subject subject) {
        System.out.println("\n" + BOLD + "--- STUDENTS ATTENDANCE PERCENTAGE: " + subject.getSubjectName() + " ---" + RESET);
        List<Student> students = service.getAllStudents();

        TableFormatter tf = new TableFormatter("Roll No", "Student Name", "Held", "Attended", "Absent", "Attendance %", "Status");
        tf.setTitle("CLASS ATTENDANCE PERCENTAGE: " + subject.getSubjectName() + " (" + subject.getSubjectCode() + ")");

        int eligibleCount = 0;
        int shortageCount = 0;

        for (Student s : students) {
            AttendanceSummary sum = service.getStudentSubjectSummary(s.getRollNo(), subject.getSubjectCode());
            String pctColor = sum.isEligible() ? GREEN : RED;
            String statusColor = sum.isEligible() ? (GREEN + "Eligible" + RESET) : (RED + "Shortage" + RESET);

            if (sum.isEligible()) eligibleCount++;
            else shortageCount++;

            tf.addRow(
                    s.getRollNo(),
                    s.getName(),
                    String.valueOf(sum.getTotalClasses()),
                    String.valueOf(sum.getPresentClasses()),
                    String.valueOf(sum.getAbsentClasses()),
                    pctColor + String.format("%.1f%%", sum.getPercentage()) + RESET,
                    statusColor
            );
        }

        tf.print();
        System.out.printf("Summary for %s: Total Students: 30 | %sEligible (>= 75%%): %d%s | %sShortage (< 75%%): %d%s%n",
                subject.getSubjectName(), GREEN, eligibleCount, RESET, RED, shortageCount, RESET);
    }

    /**
     * Subject Defaulters (< 75% shortage).
     */
    private static void viewDefaultersForSubject(Subject subject) {
        System.out.println("\n" + BOLD + "--- SUBJECT DEFAULTERS LIST (< 75%): " + subject.getSubjectName() + " ---" + RESET);
        List<Map<String, Object>> defaulters = service.getSubjectDefaulters(subject.getSubjectCode(), 75.0);

        TableFormatter tf = new TableFormatter("Roll No", "Student Name", "Held", "Attended", "Absent", "Attendance %", "Deficit", "Classes to reach 75%");
        tf.setTitle("DEFAULTERS (< 75%): " + subject.getSubjectName());

        for (Map<String, Object> item : defaulters) {
            Student s = (Student) item.get("student");
            AttendanceSummary sum = (AttendanceSummary) item.get("summary");

            tf.addRow(
                    s.getRollNo(),
                    s.getName(),
                    String.valueOf(sum.getTotalClasses()),
                    String.valueOf(sum.getPresentClasses()),
                    String.valueOf(sum.getAbsentClasses()),
                    RED + String.format("%.1f%%", sum.getPercentage()) + RESET,
                    RED + String.format("-%.1f%%", 75.0 - sum.getPercentage()) + RESET,
                    RED + "+" + sum.getClassesNeededFor75() + " classes" + RESET
            );
        }

        tf.print();
        System.out.printf(BOLD + "Found %d student(s) with attendance below 75%% in %s.%n" + RESET,
                defaulters.size(), subject.getSubjectName());
    }

    /**
     * Daily attendance register for a specific date in this subject.
     */
    private static void viewDailyRegisterForSubject(Subject subject) {
        System.out.println("\n" + BOLD + "--- DAILY REGISTER: " + subject.getSubjectName() + " ---" + RESET);
        LocalDate date = promptDate("Enter Date to inspect [yyyy-MM-dd] (press Enter for today " + LocalDate.now() + "): ", LocalDate.now());

        List<AttendanceRecord> records = service.getAttendanceForDateAndSubject(subject.getSubjectCode(), date);

        TableFormatter tf = new TableFormatter("Roll No", "Student Name", "Subject", "Date", "Status");
        tf.setTitle("DAILY REGISTER: " + subject.getSubjectName() + " | " + date);

        int p = 0, a = 0;
        for (AttendanceRecord r : records) {
            String name = service.getStudent(r.getRollNo()).map(Student::getName).orElse("Unknown");
            String statusColored = r.isPresent() ? (GREEN + "Present" + RESET) : (RED + "Absent" + RESET);
            if (r.isPresent()) p++;
            else a++;
            tf.addRow(r.getRollNo(), name, subject.getSubjectCode(), r.getDate().toString(), statusColored);
        }

        tf.print();
        if (!records.isEmpty()) {
            double rate = ((double) p / records.size()) * 100.0;
            System.out.printf(CYAN + "Total Enrolled: %d | Present: %d | Absent: %d | Turnout: %.1f%%%n" + RESET,
                    records.size(), p, a, rate);
        }
    }

    // =========================================================================
    // 2. STUDENT LOGIN (VIEW ONLY)
    // =========================================================================

    private static void handleStudentLogin() {
        System.out.println("\n" + CYAN + BOLD + "========== STUDENT LOGIN ==========" + RESET);
        System.out.println("(Sample Roll Numbers available: 101 to 130 | Default password format: <roll>@123)");
        String roll = prompt("Enter Your Roll Number: ").trim().toUpperCase();

        Optional<Student> opt = service.getStudent(roll);
        if (opt.isEmpty()) {
            System.out.println(RED + "Invalid Roll Number! No student found with Roll No: " + roll + RESET);
            return;
        }

        Student student = opt.get();
        String password = prompt("Enter Student Password (" + student.getRollNo() + "@123): ").trim();
        String expectedPassword = student.getRollNo() + "@123";
        if (!password.equalsIgnoreCase(expectedPassword) && !password.equals("student123")) {
            System.out.println(RED + "Authentication failed! Incorrect password. Default is: " + expectedPassword + RESET);
            return;
        }

        System.out.println(GREEN + "✓ Welcome, " + student.getName() + "!" + RESET);

        boolean studentSession = true;
        while (studentSession) {
            System.out.println("\n" + BOLD + "========== STUDENT DASHBOARD (VIEW ONLY) ==========" + RESET);
            System.out.println("  Student Roll No : " + CYAN + student.getRollNo() + RESET);
            System.out.println("  Full Name       : " + CYAN + student.getName() + RESET);
            System.out.println("  Class           : " + CYAN + student.getDepartment() + " - " + student.getYearSection() + RESET);
            System.out.println("---------------------------------------------------");
            System.out.println("  [" + CYAN + "1" + RESET + "] View My 5-Subjects Attendance Statement");
            System.out.println("  [" + CYAN + "2" + RESET + "] View Date-wise Attendance History for a Subject");
            System.out.println("  [" + CYAN + "3" + RESET + "] Logout to Main Menu");
            System.out.println("---------------------------------------------------");

            String ch = prompt("Select Option [1-3]: ").trim();
            switch (ch) {
                case "1" -> displayStudent5SubjectsStatement(student);
                case "2" -> displayStudentSubjectDatewiseHistory(student);
                case "3" -> {
                    System.out.println(YELLOW + "Logging out of Student portal..." + RESET);
                    studentSession = false;
                }
                default -> System.out.println(RED + "Invalid option! Enter 1, 2, or 3." + RESET);
            }
        }
    }

    private static void displayStudent5SubjectsStatement(Student student) {
        List<AttendanceSummary> summaries = service.getStudentAllSubjectsSummary(student.getRollNo());

        TableFormatter tf = new TableFormatter("Subject Code", "Subject Name", "Total Held", "Attended", "Absent", "Attendance %", "Exam Eligibility", "Classes Needed for 75%");
        tf.setTitle("ATTENDANCE STATEMENT: " + student.getName() + " (Roll No: " + student.getRollNo() + ")");

        int totalLecturesAll = 0;
        int totalAttendedAll = 0;
        int totalAbsentAll = 0;

        for (AttendanceSummary s : summaries) {
            totalLecturesAll += s.getTotalClasses();
            totalAttendedAll += s.getPresentClasses();
            totalAbsentAll += s.getAbsentClasses();

            String pctColor = s.isEligible() ? GREEN : RED;
            String statusStr = s.isEligible()
                    ? (GREEN + "ELIGIBLE (>= 75%)" + RESET)
                    : (RED + "SHORTAGE (< 75%)" + RESET);

            String neededStr = s.getClassesNeededFor75() > 0
                    ? (RED + "+" + s.getClassesNeededFor75() + " classes" + RESET)
                    : (GREEN + "Goal Met" + RESET);

            tf.addRow(
                    s.getSubjectCode(),
                    s.getSubjectName(),
                    String.valueOf(s.getTotalClasses()),
                    String.valueOf(s.getPresentClasses()),
                    String.valueOf(s.getAbsentClasses()),
                    pctColor + String.format("%.1f%%", s.getPercentage()) + RESET,
                    statusStr,
                    neededStr
            );
        }

        tf.print();

        if (totalLecturesAll > 0) {
            double aggregatePct = ((double) totalAttendedAll / totalLecturesAll) * 100.0;
            System.out.println(BOLD + "\n--- OVERALL 5-SUBJECTS AGGREGATE ---" + RESET);
            System.out.printf("  Total Lectures Conducted Across 5 Subjects : %d%n", totalLecturesAll);
            System.out.printf("  Total Lectures Attended                    : %d%n", totalAttendedAll);
            System.out.printf("  Total Lectures Absent                      : %d%n", totalAbsentAll);
            System.out.printf("  Cumulative Attendance Percentage           : %s%.2f%%%s%n",
                    aggregatePct >= 75 ? GREEN : RED, aggregatePct, RESET);

            if (aggregatePct >= 75.0) {
                System.out.println(GREEN + BOLD + "  FINAL VERDICT: ELIGIBLE TO APPEAR IN SEMESTER END EXAMINATIONS." + RESET);
            } else {
                System.out.println(RED + BOLD + "  FINAL VERDICT: CAUTION! ATTENDANCE SHORTAGE (< 75%). HALL TICKET SUBJECT TO CONDONATION/APPROVAL." + RESET);
            }
        }
    }

    private static void displayStudentSubjectDatewiseHistory(Student student) {
        System.out.println("\nSelect Subject to view date-by-date history:");
        List<Subject> subjects = service.getAllSubjects();
        for (int i = 0; i < subjects.size(); i++) {
            Subject s = subjects.get(i);
            System.out.printf("  %d. [%s] %s%n", (i + 1), s.getSubjectCode(), s.getSubjectName());
        }

        String input = prompt("Select Subject [1-5 or Code like OOPS/OSS/BEFA/DMS/DBMS]: ").trim().toUpperCase();
        Subject subject = null;
        try {
            int num = Integer.parseInt(input);
            if (num >= 1 && num <= subjects.size()) {
                subject = subjects.get(num - 1);
            }
        } catch (NumberFormatException ignored) {}

        if (subject == null) {
            for (Subject s : subjects) {
                if (s.getSubjectCode().equalsIgnoreCase(input)) {
                    subject = s;
                    break;
                }
            }
        }

        if (subject == null) {
            System.out.println(RED + "Invalid subject selection!" + RESET);
            return;
        }

        List<AttendanceRecord> history = service.getStudentSubjectHistory(student.getRollNo(), subject.getSubjectCode());

        TableFormatter tf = new TableFormatter("Lecture Date", "Subject Code", "Subject Name", "Status");
        tf.setTitle("LECTURE LOG: " + student.getName() + " | " + subject.getSubjectName());

        int p = 0, a = 0;
        for (AttendanceRecord r : history) {
            String statusStr = r.isPresent() ? (GREEN + "Present" + RESET) : (RED + "Absent" + RESET);
            if (r.isPresent()) p++;
            else a++;
            tf.addRow(r.getDate().toString(), subject.getSubjectCode(), subject.getSubjectName(), statusStr);
        }

        tf.print();
        int total = history.size();
        double pct = (total > 0) ? ((double) p / total) * 100.0 : 0.0;
        System.out.printf(CYAN + "Total Lectures: %d | Attended: %d | Absent: %d | Percentage: %.1f%%%n" + RESET, total, p, a, pct);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private static String prompt(String message) {
        System.out.print(message);
        return scanner.nextLine();
    }

    private static LocalDate promptDate(String message, LocalDate defaultDate) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();
            if (input.isEmpty() && defaultDate != null) return defaultDate;
            try {
                return LocalDate.parse(input, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println(RED + "Invalid date format! Use YYYY-MM-DD (e.g., 2026-10-07)." + RESET);
            }
        }
    }
}

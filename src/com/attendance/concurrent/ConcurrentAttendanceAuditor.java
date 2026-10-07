package com.attendance.concurrent;

import com.attendance.exception.AuthenticationException;
import com.attendance.exception.InvalidRollNumberException;
import com.attendance.model.Student;
import com.attendance.model.Subject;
import com.attendance.service.AttendanceService;

import java.util.ArrayList;
import java.util.List;

import static com.attendance.util.TableFormatter.*;

/**
 * Concurrent Multi-Threaded Attendance Audit Engine.
 * Executes parallel classroom verification workers across all subjects,
 * manages thread lifecycles using join(), and handles validation exceptions.
 * Course Faculty: Dr. Naga Padmaja
 */
public class ConcurrentAttendanceAuditor {

    /**
     * Runnable task simulating parallel attendance audits across subjects.
     */
    public static class ClassroomAuditWorker implements Runnable {
        private final String subjectCode;
        private final String facultyName;
        private final AttendanceService service;
        private int processedRecords = 0;

        public ClassroomAuditWorker(String subjectCode, String facultyName, AttendanceService service) {
            this.subjectCode = subjectCode;
            this.facultyName = facultyName;
            this.service = service;
        }

        @Override
        public void run() {
            String threadName = Thread.currentThread().getName();
            System.out.printf("  [%s] Starting live audit for %s (Faculty: %s)%n", threadName, subjectCode, facultyName);
            try {
                Thread.sleep(70);
                List<Student> students = service.getAllStudents();
                for (Student s : students) {
                    service.getStudentSubjectSummary(s.getRollNo(), subjectCode);
                    processedRecords++;
                }
                System.out.printf("  [%s] ✓ Completed %d student verifications for %s%n", threadName, processedRecords, subjectCode);
            } catch (InterruptedException e) {
                System.out.println("  [" + threadName + "] Thread interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }

        public int getProcessedRecords() {
            return processedRecords;
        }
    }

    public static void runAudit(AttendanceService service) {
        System.out.println(CYAN + BOLD);
        System.out.println("==========================================================================");
        System.out.println("          MULTI-THREADED CONCURRENT ATTENDANCE AUDIT ENGINE               ");
        System.out.println("       Parallel Classroom Workers • Thread Synchronization & Verification ");
        System.out.println("              Course Faculty: Dr. Naga Padmaja (Java Programming)         ");
        System.out.println("==========================================================================" + RESET);

        // Section 1: Validation & Exception Handling Demonstration
        System.out.println("\n" + YELLOW + BOLD + "[1] ATTENDANCE CREDENTIAL & ROLL NUMBER VALIDATION" + RESET);
        testRollValidation("105");
        testRollValidation("INVALID_FORMAT");
        testStudentAuth("101", "101@123");
        testStudentAuth("101", "wrong_pwd");

        // Section 2: Multithreaded Execution
        System.out.println("\n" + YELLOW + BOLD + "[2] PARALLEL CLASSROOM WORKER EXECUTION" + RESET);
        System.out.println("Spawning concurrent worker threads across all 5 subjects...");

        List<Thread> threads = new ArrayList<>();
        List<ClassroomAuditWorker> workers = new ArrayList<>();

        for (Subject sub : service.getAllSubjects()) {
            ClassroomAuditWorker worker = new ClassroomAuditWorker(sub.getSubjectCode(), sub.getFacultyName(), service);
            Thread t = new Thread(worker, "AuditThread-" + sub.getSubjectCode());
            workers.add(worker);
            threads.add(t);
            t.start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                System.out.println(RED + "Main execution interrupted waiting for thread completion." + RESET);
                Thread.currentThread().interrupt();
            }
        }

        int totalAudited = workers.stream().mapToInt(ClassroomAuditWorker::getProcessedRecords).sum();
        System.out.printf(GREEN + BOLD + "\n✓ All worker threads completed via join(). Total records verified: %d.%n" + RESET, totalAudited);
    }

    private static void testRollValidation(String roll) {
        try {
            validateRoll(roll);
            System.out.println(GREEN + "  ✓ validateRoll('" + roll + "'): Accepted valid student format." + RESET);
        } catch (InvalidRollNumberException e) {
            System.out.println(RED + "  ✗ Caught InvalidRollNumberException: " + e.getMessage() + RESET);
        } finally {
            System.out.println("    [Validation cleanup routine executed for roll: '" + roll + "']");
        }
    }

    private static void validateRoll(String roll) throws InvalidRollNumberException {
        if (roll == null || !roll.matches("\\d{3}")) {
            throw new InvalidRollNumberException(roll);
        }
    }

    private static void testStudentAuth(String roll, String pass) {
        try {
            authenticate(roll, pass);
            System.out.println(GREEN + "  ✓ authenticateStudent('" + roll + "'): Credentials verified." + RESET);
        } catch (AuthenticationException e) {
            System.out.println(RED + "  ✗ Caught AuthenticationException: " + e.getMessage() + RESET);
        }
    }

    private static void authenticate(String roll, String pass) throws AuthenticationException {
        String expected = roll + "@123";
        if (!expected.equals(pass)) {
            throw new AuthenticationException(roll, "Incorrect student password provided.");
        }
    }
}

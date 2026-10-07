package com.attendance.service;

import com.attendance.model.AttendanceRecord;
import com.attendance.model.Student;
import com.attendance.model.Subject;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles persistent storage of Students, 5 Subjects with 5 Teachers, and Attendance records.
 */
public class DataPersistenceService {

    private static final String DATA_DIR = "data";
    private static final String STUDENTS_FILE = DATA_DIR + File.separator + "students.csv";
    private static final String SUBJECTS_FILE = DATA_DIR + File.separator + "subjects.csv";
    private static final String ATTENDANCE_FILE = DATA_DIR + File.separator + "attendance.csv";

    public DataPersistenceService() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public List<Student> loadStudents() {
        List<Student> students = new ArrayList<>();
        File file = new File(STUDENTS_FILE);
        if (!file.exists()) return students;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                Student s = Student.fromCsv(line);
                if (s != null) students.add(s);
            }
        } catch (IOException e) {
            System.err.println("Warning: Error loading students: " + e.getMessage());
        }
        return students;
    }

    public void saveStudents(List<Student> students) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(STUDENTS_FILE))) {
            for (Student s : students) {
                writer.write(s.toCsv());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving students: " + e.getMessage());
        }
    }

    public List<Subject> loadSubjects() {
        List<Subject> subjects = new ArrayList<>();
        File file = new File(SUBJECTS_FILE);
        if (!file.exists()) return subjects;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                Subject sub = Subject.fromCsv(line);
                if (sub != null) subjects.add(sub);
            }
        } catch (IOException e) {
            System.err.println("Warning: Error loading subjects: " + e.getMessage());
        }
        return subjects;
    }

    public void saveSubjects(List<Subject> subjects) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SUBJECTS_FILE))) {
            for (Subject s : subjects) {
                writer.write(s.toCsv());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving subjects: " + e.getMessage());
        }
    }

    public List<AttendanceRecord> loadAttendance() {
        List<AttendanceRecord> records = new ArrayList<>();
        File file = new File(ATTENDANCE_FILE);
        if (!file.exists()) return records;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                AttendanceRecord r = AttendanceRecord.fromCsv(line);
                if (r != null) records.add(r);
            }
        } catch (IOException e) {
            System.err.println("Warning: Error loading attendance: " + e.getMessage());
        }
        return records;
    }

    public void saveAttendance(List<AttendanceRecord> records) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ATTENDANCE_FILE))) {
            for (AttendanceRecord r : records) {
                writer.write(r.toCsv());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving attendance: " + e.getMessage());
        }
    }

    /**
     * Seeds 30 Students, 5 Core Subjects (with 5 distinct teachers and passwords),
     * and 10 days of attendance history.
     */
    public void seedInitialDemoData(AttendanceService service) {
        if (!service.getAllStudents().isEmpty()) {
            return;
        }

        // 1. Add 5 Subjects with 5 Different Teachers and Passwords (password = subject name)
        service.addSubject(new Subject("JAVA",   "Java Programming",         "Dr. Naga Padmaja", "java"));
        service.addSubject(new Subject("PYTHON", "Python Programming",       "Dr. Rajesh",   "python"));
        service.addSubject(new Subject("MATHS",  "Engineering Mathematics",  "Dr. Sharma",   "maths"));
        service.addSubject(new Subject("DSA",    "Data Structures & Algo",   "Prof. Priya",  "dsa"));
        service.addSubject(new Subject("WEB",    "Web Technologies",         "Prof. Arun",   "web"));

        // 2. Add 30 Realistic Students
        String[][] studentsData = {
                {"101", "Aarav Sharma"},
                {"102", "Bhavna Patel"},
                {"103", "Chirag Reddy"},
                {"104", "Divya Nair"},
                {"105", "Eshwar Rao"},
                {"106", "Farhan Khan"},
                {"107", "Gayatri Joshi"},
                {"108", "Harish Verma"},
                {"109", "Ishita Sen"},
                {"110", "Jatin Gupta"},
                {"111", "Kavya Iyer"},
                {"112", "Lokesh Yadav"},
                {"113", "Meera Das"},
                {"114", "Nikhil Kumar"},
                {"115", "Olivia Roy"},
                {"116", "Pranav Malhotra"},
                {"117", "Qadir Ali"},
                {"118", "Ritu Menon"},
                {"119", "Sanjay Pillai"},
                {"120", "Tanvi Deshmukh"},
                {"121", "Uday Chauhan"},
                {"122", "Vaishnavi Bhat"},
                {"123", "Wasim Akram"},
                {"124", "Xenia D'Souza"},
                {"125", "Yash Kulkarni"},
                {"126", "Zoya Siddiqui"},
                {"127", "Ananya Pandey"},
                {"128", "Bharat Subramanian"},
                {"129", "Chetan Bhagat"},
                {"130", "Deepa Mehta"}
        };

        for (String[] st : studentsData) {
            service.addStudent(new Student(st[0], st[1], "CSE", "3rd Year / A"));
        }

        // 3. Seed 10 Days of Attendance across all 5 subjects ending on today (present day)
        LocalDate today = LocalDate.now();
        String[] subjects = {"JAVA", "PYTHON", "MATHS", "DSA", "WEB"};

        for (int day = 9; day >= 0; day--) {
            LocalDate dt = today.minusDays(day);

            for (String sub : subjects) {
                for (int i = 0; i < studentsData.length; i++) {
                    String roll = studentsData[i][0];
                    int studentIndex = i + 1; // 1 to 30

                    boolean isAbsent = false;
                    if (day == 0) {
                        // Present day: all 5 subjects actively recorded!
                        if (studentIndex >= 26) {
                            isAbsent = ("MATHS".equals(sub) || "DSA".equals(sub));
                        } else if (studentIndex >= 21) {
                            isAbsent = ("DSA".equals(sub) && studentIndex % 2 == 0);
                        } else if (studentIndex == 9 && "MATHS".equals(sub)) {
                            isAbsent = true;
                        } else {
                            isAbsent = false; // Students 101-120 100% Present on today
                        }
                    } else {
                        if (studentIndex >= 26) {
                            // Shortage group (<65% attendance)
                            isAbsent = (day % 2 == 0) || (day == 1);
                        } else if (studentIndex >= 21) {
                            // Borderline group (~70% attendance)
                            isAbsent = (day == 2 || day == 5 || day == 8);
                        } else if (studentIndex >= 6) {
                            // Regular group (80-90% attendance)
                            isAbsent = (day == 4 && (studentIndex % 2 == 0));
                        } else {
                            // High group (~100% attendance)
                            isAbsent = (day == 3 && studentIndex == 4);
                        }
                    }

                    String status = isAbsent ? "Absent" : "Present";
                    service.markAttendance(roll, sub, dt, status);
                }
            }
        }

        saveStudents(service.getAllStudents());
        saveSubjects(service.getAllSubjects());
        saveAttendance(service.getAllAttendanceRecords());
    }
}

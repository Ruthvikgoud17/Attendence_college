package com.attendance.web;

import com.attendance.model.AttendanceRecord;
import com.attendance.model.AttendanceSummary;
import com.attendance.model.Student;
import com.attendance.model.Subject;
import com.attendance.service.AttendanceService;
import com.attendance.service.DataPersistenceService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Built-in Lightweight HTTP Web Server for College Attendance Management System.
 * Powered by Java SE com.sun.net.httpserver (Zero external dependencies required).
 * 
 * Provides:
 * - Static file serving for web UI (HTML, CSS, JS, Assets)
 * - REST API for Teacher Portal (Auth, Post Attendance, Defaulters, Register)
 * - REST API for Student Portal (5-Subject Statements, Date-wise History)
 * - REST API for College Analytics & Overview
 */
public class AttendanceWebServer {

    private static final int PORT = 8080;
    private static final String WEB_DIR = "web";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final AttendanceService service;
    private final DataPersistenceService persistenceService;
    private HttpServer server;

    public AttendanceWebServer() {
        this.persistenceService = new DataPersistenceService();
        this.service = new AttendanceService(persistenceService);

        // Preload demo data if data files were fresh or empty
        if (service.getAllStudents().isEmpty()) {
            persistenceService.seedInitialDemoData(service);
            service.loadData();
        }
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor() != null 
                ? Executors.newCachedThreadPool() : Executors.newFixedThreadPool(10));

        // API Contexts
        server.createContext("/api/status", new StatusHandler());
        server.createContext("/api/students", new StudentsHandler());
        server.createContext("/api/subjects", new SubjectsHandler());
        server.createContext("/api/teacher/login", new TeacherLoginHandler());
        server.createContext("/api/student/login", new StudentLoginHandler());
        server.createContext("/api/attendance/summary", new StudentSummaryHandler());
        server.createContext("/api/attendance/history", new StudentHistoryHandler());
        server.createContext("/api/attendance/subject-percentages", new SubjectPercentagesHandler());
        server.createContext("/api/attendance/defaulters", new DefaultersHandler());
        server.createContext("/api/attendance/daily-register", new DailyRegisterHandler());
        server.createContext("/api/attendance/mark", new MarkAttendanceHandler());
        server.createContext("/api/overview", new OverviewHandler());

        // Static Web Resource Context
        server.createContext("/", new StaticFileHandler());

        server.start();
        System.out.println("==================================================================");
        System.out.println("  COLLEGE ATTENDANCE MANAGEMENT SYSTEM - WEB SERVER RUNNING");
        System.out.println("==================================================================");
        System.out.println("  Web Interface URL: http://localhost:" + PORT);
        System.out.println("  API Endpoints    : http://localhost:" + PORT + "/api/status");
        System.out.println("  Press Ctrl+C in terminal to stop server.");
        System.out.println("==================================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(1);
        }
    }

    public static void main(String[] args) {
        try {
            AttendanceWebServer webServer = new AttendanceWebServer();
            webServer.start();

            // Try to open default browser automatically
            try {
                if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI("http://localhost:" + PORT));
                }
            } catch (Exception ignored) {
                // Ignore headless / browser launch errors
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nSaving attendance records and shutting down web server...");
                webServer.service.persistAll();
                webServer.stop();
                System.out.println("Web server stopped successfully.");
            }));

            // Keep server running continuously
            Thread.currentThread().join();

        } catch (InterruptedException ignored) {
            // Server thread interrupted on shutdown
        } catch (IOException e) {
            System.err.println("Failed to start Web Server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =========================================================================
    // HTTP Handlers
    // =========================================================================

    /**
     * Serves static frontend files from the web/ folder.
     */
    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCors(exchange);
                return;
            }

            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            // Clean path to prevent directory traversal
            path = path.replace("\\", "/");
            if (path.contains("..")) {
                sendResponse(exchange, 403, "text/plain", "Forbidden");
                return;
            }

            Path filePath = Paths.get(WEB_DIR, path.startsWith("/") ? path.substring(1) : path);
            File file = filePath.toFile();

            if (!file.exists() || file.isDirectory()) {
                // If not found, check index.html as fallback for SPA routing
                File index = Paths.get(WEB_DIR, "index.html").toFile();
                if (index.exists()) {
                    sendFile(exchange, 200, getContentType("index.html"), index);
                } else {
                    sendResponse(exchange, 404, "text/plain", "404 Not Found");
                }
                return;
            }

            String contentType = getContentType(file.getName());
            sendFile(exchange, 200, contentType, file);
        }
    }

    private class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            StringBuilder json = new StringBuilder();
            json.append("{")
                .append("\"status\":\"ONLINE\",")
                .append("\"system\":\"College Attendance Management System\",")
                .append("\"totalStudents\":").append(service.getAllStudents().size()).append(",")
                .append("\"totalSubjects\":").append(service.getAllSubjects().size()).append(",")
                .append("\"totalAttendanceRecords\":").append(service.getAllAttendanceRecords().size()).append(",")
                .append("\"serverTime\":\"").append(LocalDate.now()).append("\"")
                .append("}");
            sendResponse(exchange, 200, "application/json", json.toString());
        }
    }

    private class StudentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            List<Student> list = service.getAllStudents();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                Student s = list.get(i);
                if (i > 0) sb.append(",");
                sb.append(studentToJson(s));
            }
            sb.append("]");
            sendResponse(exchange, 200, "application/json", sb.toString());
        }
    }

    private class SubjectsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            List<Subject> list = service.getAllSubjects();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                Subject s = list.get(i);
                if (i > 0) sb.append(",");
                sb.append(subjectToJson(s));
            }
            sb.append("]");
            sendResponse(exchange, 200, "application/json", sb.toString());
        }
    }

    private class TeacherLoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> params = parseJsonSimple(body);
            String password = params.getOrDefault("password", "").trim().toLowerCase();
            String subjectCode = params.getOrDefault("subjectCode", "").trim().toUpperCase();

            Subject matchedSubject = null;

            // 1. If subjectCode specified, verify against that subject
            if (!subjectCode.isEmpty()) {
                Optional<Subject> opt = service.getSubject(subjectCode);
                if (opt.isPresent()) {
                    Subject sub = opt.get();
                    if (sub.verifyPassword(password) || password.equals("teach123") || password.equalsIgnoreCase(sub.getSubjectCode())) {
                        matchedSubject = sub;
                    }
                }
            }

            // 2. Otherwise search all subjects for matching password
            if (matchedSubject == null && !password.isEmpty()) {
                for (Subject sub : service.getAllSubjects()) {
                    if (sub.verifyPassword(password) || password.equalsIgnoreCase(sub.getSubjectCode())) {
                        matchedSubject = sub;
                        break;
                    }
                }
            }

            // 3. Fallback for master password teach123
            if (matchedSubject == null && password.equals("teach123")) {
                matchedSubject = service.getAllSubjects().isEmpty() ? null : service.getAllSubjects().get(0);
            }

            if (matchedSubject != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("{")
                  .append("\"success\":true,")
                  .append("\"subject\":").append(subjectToJson(matchedSubject)).append(",")
                  .append("\"message\":\"Authentication successful! Logged in as " + escapeJson(matchedSubject.getFacultyName()) + "\"")
                  .append("}");
                sendResponse(exchange, 200, "application/json", sb.toString());
            } else {
                sendResponse(exchange, 401, "application/json", 
                        "{\"success\":false,\"message\":\"Invalid password! Standard passwords are: java, python, maths, dsa, web (or teach123).\"}");
            }
        }
    }

    private class StudentLoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            String body = readRequestBody(exchange);
            Map<String, String> params = parseJsonSimple(body);
            String rollNo = params.getOrDefault("rollNo", "").trim().toUpperCase();
            String password = params.getOrDefault("password", "").trim();

            if (rollNo.isEmpty()) {
                sendResponse(exchange, 400, "application/json", "{\"success\":false,\"message\":\"Roll number is required.\"}");
                return;
            }

            Optional<Student> opt = service.getStudent(rollNo);
            if (opt.isEmpty()) {
                sendResponse(exchange, 404, "application/json", "{\"success\":false,\"message\":\"Invalid Roll Number! No student found with Roll No: " + escapeJson(rollNo) + "\"}");
                return;
            }

            Student student = opt.get();
            String expectedPassword = student.getRollNo() + "@123";

            if (password.equalsIgnoreCase(expectedPassword) || password.equals("student123")) {
                StringBuilder sb = new StringBuilder();
                sb.append("{")
                  .append("\"success\":true,")
                  .append("\"student\":").append(studentToJson(student)).append(",")
                  .append("\"message\":\"Authentication successful! Welcome, " + escapeJson(student.getName()) + "\"")
                  .append("}");
                sendResponse(exchange, 200, "application/json", sb.toString());
            } else {
                sendResponse(exchange, 401, "application/json", 
                        "{\"success\":false,\"message\":\"Incorrect student password! Default password format is " + escapeJson(expectedPassword) + "\"}");
            }
        }
    }

    private class StudentSummaryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
            String rollNo = query.get("rollNo");

            if (rollNo == null || rollNo.trim().isEmpty()) {
                sendResponse(exchange, 400, "application/json", "{\"error\":\"rollNo parameter required\"}");
                return;
            }

            Optional<Student> opt = service.getStudent(rollNo.trim());
            if (opt.isEmpty()) {
                sendResponse(exchange, 404, "application/json", "{\"error\":\"Student not found\"}");
                return;
            }

            Student student = opt.get();
            List<AttendanceSummary> summaries = service.getStudentAllSubjectsSummary(student.getRollNo());

            int totalClassesAll = 0;
            int totalPresentAll = 0;
            int totalAbsentAll = 0;

            StringBuilder listSb = new StringBuilder("[");
            for (int i = 0; i < summaries.size(); i++) {
                AttendanceSummary s = summaries.get(i);
                totalClassesAll += s.getTotalClasses();
                totalPresentAll += s.getPresentClasses();
                totalAbsentAll += s.getAbsentClasses();
                if (i > 0) listSb.append(",");
                listSb.append(summaryToJson(s));
            }
            listSb.append("]");

            double aggregatePct = totalClassesAll > 0 ? ((double) totalPresentAll / totalClassesAll) * 100.0 : 0.0;
            boolean aggregateEligible = aggregatePct >= 75.0;

            StringBuilder sb = new StringBuilder();
            sb.append("{")
              .append("\"student\":").append(studentToJson(student)).append(",")
              .append("\"summaries\":").append(listSb).append(",")
              .append("\"aggregate\":{")
              .append("\"totalHeld\":").append(totalClassesAll).append(",")
              .append("\"totalAttended\":").append(totalPresentAll).append(",")
              .append("\"totalAbsent\":").append(totalAbsentAll).append(",")
              .append("\"percentage\":").append(String.format(Locale.US, "%.2f", aggregatePct)).append(",")
              .append("\"isEligible\":").append(aggregateEligible).append(",")
              .append("\"verdict\":\"").append(aggregateEligible 
                      ? "ELIGIBLE TO APPEAR IN SEMESTER END EXAMINATIONS" 
                      : "ATTENDANCE SHORTAGE (< 75%) - CONDONATION / REVIEW REQUIRED").append("\"")
              .append("}}");

            sendResponse(exchange, 200, "application/json", sb.toString());
        }
    }

    private class StudentHistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
            String rollNo = query.get("rollNo");
            String subjectCode = query.get("subjectCode");

            if (rollNo == null || rollNo.trim().isEmpty()) {
                sendResponse(exchange, 400, "application/json", "{\"error\":\"rollNo parameter required\"}");
                return;
            }

            List<AttendanceRecord> records;
            if (subjectCode != null && !subjectCode.trim().isEmpty() && !subjectCode.equalsIgnoreCase("ALL")) {
                records = service.getStudentSubjectHistory(rollNo.trim(), subjectCode.trim());
            } else {
                List<String> subOrder = List.of("OOPS", "OSS", "BEFA", "DMS", "DBMS");
                records = service.getAllAttendanceRecords().stream()
                        .filter(r -> r.getRollNo().equalsIgnoreCase(rollNo.trim()))
                        .sorted(Comparator.comparing(AttendanceRecord::getDate).reversed()
                                .thenComparingInt(r -> {
                                    int idx = subOrder.indexOf(r.getSubjectCode().toUpperCase());
                                    return idx >= 0 ? idx : 99;
                                }))
                        .collect(Collectors.toList());
            }

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < records.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(recordToJson(records.get(i)));
            }
            sb.append("]");
            sendResponse(exchange, 200, "application/json", sb.toString());
        }
    }

    private class SubjectPercentagesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
            String subjectCode = query.get("subjectCode");

            if (subjectCode == null || subjectCode.trim().isEmpty()) {
                sendResponse(exchange, 400, "application/json", "{\"error\":\"subjectCode parameter required\"}");
                return;
            }

            Optional<Subject> optSub = service.getSubject(subjectCode);
            if (optSub.isEmpty()) {
                sendResponse(exchange, 404, "application/json", "{\"error\":\"Subject not found\"}");
                return;
            }

            Subject subject = optSub.get();
            List<Student> students = service.getAllStudents();

            int eligibleCount = 0;
            int shortageCount = 0;

            StringBuilder listSb = new StringBuilder("[");
            for (int i = 0; i < students.size(); i++) {
                Student s = students.get(i);
                AttendanceSummary sum = service.getStudentSubjectSummary(s.getRollNo(), subject.getSubjectCode());
                if (sum.isEligible()) eligibleCount++;
                else shortageCount++;

                if (i > 0) listSb.append(",");
                listSb.append("{")
                      .append("\"student\":").append(studentToJson(s)).append(",")
                      .append("\"summary\":").append(summaryToJson(sum))
                      .append("}");
            }
            listSb.append("]");

            StringBuilder sb = new StringBuilder();
            sb.append("{")
              .append("\"subject\":").append(subjectToJson(subject)).append(",")
              .append("\"totalStudents\":").append(students.size()).append(",")
              .append("\"eligibleCount\":").append(eligibleCount).append(",")
              .append("\"shortageCount\":").append(shortageCount).append(",")
              .append("\"students\":").append(listSb)
              .append("}");

            sendResponse(exchange, 200, "application/json", sb.toString());
        }
    }

    private class DefaultersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
            String subjectCode = query.get("subjectCode");
            double threshold = 75.0;
            if (query.get("threshold") != null) {
                try {
                    threshold = Double.parseDouble(query.get("threshold"));
                } catch (NumberFormatException ignored) {}
            }

            if (subjectCode == null || subjectCode.trim().isEmpty()) {
                sendResponse(exchange, 400, "application/json", "{\"error\":\"subjectCode parameter required\"}");
                return;
            }

            List<Map<String, Object>> defaulters = service.getSubjectDefaulters(subjectCode, threshold);
            Optional<Subject> optSub = service.getSubject(subjectCode);

            StringBuilder listSb = new StringBuilder("[");
            for (int i = 0; i < defaulters.size(); i++) {
                Map<String, Object> map = defaulters.get(i);
                Student s = (Student) map.get("student");
                AttendanceSummary sum = (AttendanceSummary) map.get("summary");

                if (i > 0) listSb.append(",");
                listSb.append("{")
                      .append("\"student\":").append(studentToJson(s)).append(",")
                      .append("\"summary\":").append(summaryToJson(sum)).append(",")
                      .append("\"deficit\":").append(String.format(Locale.US, "%.1f", Math.max(0, threshold - sum.getPercentage()))).append(",")
                      .append("\"classesNeeded\":").append(sum.getClassesNeededFor75())
                      .append("}");
            }
            listSb.append("]");

            StringBuilder sb = new StringBuilder();
            sb.append("{")
              .append("\"subject\":").append(optSub.map(AttendanceWebServer::subjectToJson).orElse("{}")).append(",")
              .append("\"threshold\":").append(threshold).append(",")
              .append("\"defaulterCount\":").append(defaulters.size()).append(",")
              .append("\"defaulters\":").append(listSb)
              .append("}");

            sendResponse(exchange, 200, "application/json", sb.toString());
        }
    }

    private class DailyRegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
            String subjectCode = query.get("subjectCode");
            String dateStr = query.get("date");

            if (subjectCode == null || dateStr == null) {
                sendResponse(exchange, 400, "application/json", "{\"error\":\"subjectCode and date parameters required\"}");
                return;
            }

            LocalDate date;
            try {
                date = LocalDate.parse(dateStr, DATE_FORMATTER);
            } catch (Exception e) {
                sendResponse(exchange, 400, "application/json", "{\"error\":\"Invalid date format (YYYY-MM-DD expected)\"}");
                return;
            }

            List<AttendanceRecord> records = service.getAttendanceForDateAndSubject(subjectCode, date);
            int presentCount = 0;
            int absentCount = 0;

            StringBuilder listSb = new StringBuilder("[");
            for (int i = 0; i < records.size(); i++) {
                AttendanceRecord r = records.get(i);
                if (r.isPresent()) presentCount++;
                else absentCount++;

                Optional<Student> sOpt = service.getStudent(r.getRollNo());
                String name = sOpt.map(Student::getName).orElse("Unknown");

                if (i > 0) listSb.append(",");
                listSb.append("{")
                      .append("\"rollNo\":\"").append(escapeJson(r.getRollNo())).append("\",")
                      .append("\"name\":\"").append(escapeJson(name)).append("\",")
                      .append("\"date\":\"").append(r.getDate()).append("\",")
                      .append("\"subjectCode\":\"").append(escapeJson(r.getSubjectCode())).append("\",")
                      .append("\"status\":\"").append(escapeJson(r.getStatus())).append("\",")
                      .append("\"isPresent\":").append(r.isPresent())
                      .append("}");
            }
            listSb.append("]");

            int total = records.size();
            double turnout = total > 0 ? ((double) presentCount / total) * 100.0 : 0.0;

            StringBuilder sb = new StringBuilder();
            sb.append("{")
              .append("\"subjectCode\":\"").append(escapeJson(subjectCode)).append("\",")
              .append("\"date\":\"").append(date).append("\",")
              .append("\"total\":").append(total).append(",")
              .append("\"presentCount\":").append(presentCount).append(",")
              .append("\"absentCount\":").append(absentCount).append(",")
              .append("\"turnoutRate\":").append(String.format(Locale.US, "%.1f", turnout)).append(",")
              .append("\"records\":").append(listSb)
              .append("}");

            sendResponse(exchange, 200, "application/json", sb.toString());
        }
    }

    private class MarkAttendanceHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            String body = readRequestBody(exchange);
            try {
                // Parse JSON payload:
                // { "subjectCode": "JAVA", "date": "2026-10-07", "records": [ {"rollNo":"101","status":"Present"} ] }
                String subjectCode = extractJsonStringField(body, "subjectCode");
                String dateStr = extractJsonStringField(body, "date");

                if (subjectCode == null || dateStr == null) {
                    sendResponse(exchange, 400, "application/json", "{\"error\":\"Missing subjectCode or date\"}");
                    return;
                }

                LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
                List<String[]> entries = extractRecordsList(body);

                int present = 0;
                int absent = 0;

                for (String[] entry : entries) {
                    String roll = entry[0];
                    String status = entry[1];
                    boolean ok = service.markAttendance(roll, subjectCode, date, status);
                    if (ok) {
                        if ("Present".equalsIgnoreCase(status)) present++;
                        else absent++;
                    }
                }

                service.persistAll();

                StringBuilder sb = new StringBuilder();
                sb.append("{")
                  .append("\"success\":true,")
                  .append("\"message\":\"Attendance recorded and saved successfully!\",")
                  .append("\"subjectCode\":\"").append(escapeJson(subjectCode)).append("\",")
                  .append("\"date\":\"").append(date).append("\",")
                  .append("\"totalProcessed\":").append(entries.size()).append(",")
                  .append("\"presentCount\":").append(present).append(",")
                  .append("\"absentCount\":").append(absent)
                  .append("}");

                sendResponse(exchange, 200, "application/json", sb.toString());

            } catch (Exception e) {
                sendResponse(exchange, 500, "application/json", "{\"error\":\"Failed to save attendance: " + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    private class OverviewHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;

            List<Student> students = service.getAllStudents();
            List<Subject> subjects = service.getAllSubjects();
            List<AttendanceRecord> records = service.getAllAttendanceRecords();

            long totalLecturesHeld = 0;
            long totalPresent = 0;

            StringBuilder subjectsStats = new StringBuilder("[");
            for (int i = 0; i < subjects.size(); i++) {
                Subject sub = subjects.get(i);
                List<AttendanceRecord> subRecords = records.stream()
                        .filter(r -> r.getSubjectCode().equalsIgnoreCase(sub.getSubjectCode()))
                        .collect(Collectors.toList());

                long subPresent = subRecords.stream().filter(AttendanceRecord::isPresent).count();
                double subPct = subRecords.size() > 0 ? ((double) subPresent / subRecords.size()) * 100.0 : 0.0;
                List<Map<String, Object>> defs = service.getSubjectDefaulters(sub.getSubjectCode(), 75.0);

                totalLecturesHeld += subRecords.size();
                totalPresent += subPresent;

                if (i > 0) subjectsStats.append(",");
                subjectsStats.append("{")
                             .append("\"subject\":").append(subjectToJson(sub)).append(",")
                             .append("\"totalRecords\":").append(subRecords.size()).append(",")
                             .append("\"presentCount\":").append(subPresent).append(",")
                             .append("\"attendanceRate\":").append(String.format(Locale.US, "%.1f", subPct)).append(",")
                             .append("\"defaulterCount\":").append(defs.size())
                             .append("}");
            }
            subjectsStats.append("]");

            double overallAttendance = totalLecturesHeld > 0 ? ((double) totalPresent / totalLecturesHeld) * 100.0 : 0.0;

            // Student overall standing counts
            int eligibleStudents = 0;
            int shortageStudents = 0;
            for (Student s : students) {
                List<AttendanceSummary> sums = service.getStudentAllSubjectsSummary(s.getRollNo());
                int h = 0, p = 0;
                for (AttendanceSummary as : sums) {
                    h += as.getTotalClasses();
                    p += as.getPresentClasses();
                }
                double pct = h > 0 ? ((double) p / h) * 100.0 : 0.0;
                if (pct >= 75.0) eligibleStudents++;
                else shortageStudents++;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("{")
              .append("\"totalStudents\":").append(students.size()).append(",")
              .append("\"totalSubjects\":").append(subjects.size()).append(",")
              .append("\"totalAttendanceRecords\":").append(records.size()).append(",")
              .append("\"overallAttendanceRate\":").append(String.format(Locale.US, "%.1f", overallAttendance)).append(",")
              .append("\"eligibleStudentsCount\":").append(eligibleStudents).append(",")
              .append("\"shortageStudentsCount\":").append(shortageStudents).append(",")
              .append("\"subjectsStats\":").append(subjectsStats)
              .append("}");

            sendResponse(exchange, 200, "application/json", sb.toString());
        }
    }

    // =========================================================================
    // JSON Serialization Helpers (Dependency-Free Standard Java)
    // =========================================================================

    public static String studentToJson(Student s) {
        return "{" +
                "\"rollNo\":\"" + escapeJson(s.getRollNo()) + "\"," +
                "\"name\":\"" + escapeJson(s.getName()) + "\"," +
                "\"department\":\"" + escapeJson(s.getDepartment()) + "\"," +
                "\"yearSection\":\"" + escapeJson(s.getYearSection()) + "\"," +
                "\"password\":\"" + escapeJson(s.getRollNo() + "@123") + "\"" +
                "}";
    }

    public static String subjectToJson(Subject sub) {
        return "{" +
                "\"subjectCode\":\"" + escapeJson(sub.getSubjectCode()) + "\"," +
                "\"subjectName\":\"" + escapeJson(sub.getSubjectName()) + "\"," +
                "\"facultyName\":\"" + escapeJson(sub.getFacultyName()) + "\"" +
                "}";
    }

    public static String summaryToJson(AttendanceSummary sum) {
        return "{" +
                "\"subjectCode\":\"" + escapeJson(sum.getSubjectCode()) + "\"," +
                "\"subjectName\":\"" + escapeJson(sum.getSubjectName()) + "\"," +
                "\"facultyName\":\"" + escapeJson(sum.getFacultyName()) + "\"," +
                "\"totalClasses\":" + sum.getTotalClasses() + "," +
                "\"presentClasses\":" + sum.getPresentClasses() + "," +
                "\"absentClasses\":" + sum.getAbsentClasses() + "," +
                "\"percentage\":" + String.format(Locale.US, "%.1f", sum.getPercentage()) + "," +
                "\"status\":\"" + escapeJson(sum.getStatus()) + "\"," +
                "\"classesNeededFor75\":" + sum.getClassesNeededFor75() + "," +
                "\"isEligible\":" + sum.isEligible() +
                "}";
    }

    public static String recordToJson(AttendanceRecord r) {
        return "{" +
                "\"recordId\":\"" + escapeJson(r.getRecordId()) + "\"," +
                "\"rollNo\":\"" + escapeJson(r.getRollNo()) + "\"," +
                "\"subjectCode\":\"" + escapeJson(r.getSubjectCode()) + "\"," +
                "\"date\":\"" + r.getDate() + "\"," +
                "\"status\":\"" + escapeJson(r.getStatus()) + "\"," +
                "\"isPresent\":" + r.isPresent() +
                "}";
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // =========================================================================
    // Simple Body & Query Parsers
    // =========================================================================

    private static boolean handleCors(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendCors(exchange);
            return true;
        }
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        return false;
    }

    private static void sendCors(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            return out.toString(StandardCharsets.UTF_8);
        }
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null || query.isEmpty()) return result;
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length > 0) {
                String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
                String val = pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
                result.put(key, val);
            }
        }
        return result;
    }

    private static Map<String, String> parseJsonSimple(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null) return map;
        String trimmed = json.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        String[] parts = trimmed.split(",");
        for (String part : parts) {
            String[] kv = part.split(":", 2);
            if (kv.length == 2) {
                String k = kv[0].trim().replaceAll("^\"|\"$", "");
                String v = kv[1].trim().replaceAll("^\"|\"$", "");
                map.put(k, v);
            }
        }
        return map;
    }

    private static String extractJsonStringField(String json, String field) {
        int idx = json.indexOf("\"" + field + "\"");
        if (idx == -1) return null;
        int colon = json.indexOf(":", idx);
        if (colon == -1) return null;
        int firstQuote = json.indexOf("\"", colon);
        if (firstQuote == -1) return null;
        int secondQuote = json.indexOf("\"", firstQuote + 1);
        if (secondQuote == -1) return null;
        return json.substring(firstQuote + 1, secondQuote);
    }

    private static List<String[]> extractRecordsList(String json) {
        List<String[]> list = new ArrayList<>();
        int recordsIdx = json.indexOf("\"records\"");
        if (recordsIdx == -1) return list;
        int startBracket = json.indexOf("[", recordsIdx);
        int endBracket = json.lastIndexOf("]");
        if (startBracket == -1 || endBracket == -1 || endBracket <= startBracket) return list;

        String arrayContent = json.substring(startBracket + 1, endBracket);
        // Extract each {...} object
        int cursor = 0;
        while ((cursor = arrayContent.indexOf("{", cursor)) != -1) {
            int endObj = arrayContent.indexOf("}", cursor);
            if (endObj == -1) break;
            String objStr = arrayContent.substring(cursor + 1, endObj);
            String roll = extractJsonStringField("{" + objStr + "}", "rollNo");
            String status = extractJsonStringField("{" + objStr + "}", "status");
            if (roll != null && status != null) {
                list.add(new String[]{roll, status});
            }
            cursor = endObj + 1;
        }
        return list;
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String contentType, String content) throws IOException {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
        exchange.close();
    }

    private static void sendFile(HttpExchange exchange, int statusCode, String contentType, File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
        exchange.close();
    }

    private static String getContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=UTF-8";
        if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".ico")) return "image/x-icon";
        return "application/octet-stream";
    }
}

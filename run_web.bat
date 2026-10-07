@echo off
title College Attendance Management System - Web Server
cls
echo ==========================================================================
echo       Compiling College Attendance Web Server...
echo ==========================================================================

if not exist bin mkdir bin

javac -d bin src/com/attendance/model/*.java src/com/attendance/exception/*.java src/com/attendance/concurrent/*.java src/com/attendance/gui/*.java src/com/attendance/dao/*.java src/com/attendance/service/*.java src/com/attendance/util/*.java src/com/attendance/web/*.java src/com/attendance/*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed. Please ensure JDK is installed and in PATH.
    pause
    exit /b %ERRORLEVEL%
)

echo [SUCCESS] Compilation successful!
echo ==========================================================================
echo       Launching Web Server on http://localhost:8080
echo ==========================================================================

:: Free port 8080 if previously occupied
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do taskkill /F /PID %%a >nul 2>&1

start http://localhost:8080
java -cp bin com.attendance.web.AttendanceWebServer
pause

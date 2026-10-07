@echo off
title College Attendance System - Web Server
cls
echo ===================================================
echo     Compiling College Attendance Web Server...
echo ===================================================

if not exist bin mkdir bin

javac -d bin src/com/attendance/model/*.java src/com/attendance/service/*.java src/com/attendance/util/*.java src/com/attendance/web/*.java src/com/attendance/*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed. Ensure JDK is installed and in PATH.
    pause
    exit /b %ERRORLEVEL%
)

echo [SUCCESS] Compilation successful!
echo ===================================================
echo     Launching Web Server on http://localhost:8080
echo ===================================================
start http://localhost:8080
java -cp bin com.attendance.web.AttendanceWebServer
pause

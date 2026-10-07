@echo off
title College Attendance Management System - Terminal Console
cls
echo ==========================================================================
echo       Compiling College Attendance Management System...
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
echo       Launching Terminal Application (Course Faculty: Dr. Naga Padmaja)
echo ==========================================================================
java -cp bin com.attendance.Main
pause

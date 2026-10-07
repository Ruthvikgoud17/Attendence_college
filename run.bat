@echo off
title College Attendance Management System
cls
echo ===================================================
echo     Compiling College Attendance System...
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
echo  Select Run Mode:
echo    [1] Modern Web Interface (http://localhost:8080)
echo    [2] Terminal Console CLI
echo ===================================================
set /p MODE="Enter choice [1 or 2, default 1]: "

if "%MODE%"=="2" (
    echo Launching Terminal Console Application...
    java -cp bin com.attendance.Main
) else (
    echo Launching Web Interface on http://localhost:8080...
    start http://localhost:8080
    java -cp bin com.attendance.web.AttendanceWebServer
)

pause

# College Attendance System - Web Server Launcher
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "    Compiling College Attendance Web Server...     " -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan

if (!(Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

javac -d bin src/com/attendance/model/*.java src/com/attendance/service/*.java src/com/attendance/util/*.java src/com/attendance/web/*.java src/com/attendance/*.java

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Compilation failed. Ensure JDK is installed and in PATH." -ForegroundColor Red
    pause
    exit $LASTEXITCODE
}

Write-Host "[SUCCESS] Compilation successful!" -ForegroundColor Green
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "    Launching Web Server on http://localhost:8080   " -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan

Start-Process "http://localhost:8080"
java -cp bin com.attendance.web.AttendanceWebServer

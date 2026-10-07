Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "    Compiling College Attendance System...        " -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

javac -d bin src/com/attendance/model/*.java src/com/attendance/service/*.java src/com/attendance/util/*.java src/com/attendance/*.java

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Compilation failed. Ensure JDK is installed and in PATH." -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "[SUCCESS] Compilation successful!`n" -ForegroundColor Green
Write-Host "Launching College Attendance System..." -ForegroundColor Yellow
java -cp bin com.attendance.Main

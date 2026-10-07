@echo off
title Push College Attendance System to GitHub & Vercel
cls
echo ===================================================================
echo     PUSH COLLEGE ATTENDANCE SYSTEM TO GITHUB & DEPLOY TO VERCEL
echo ===================================================================
echo.

git status >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [INFO] Initializing Git repository...
    git init
    git branch -M main
)

echo [1/3] Staging all files...
git add .

echo [2/3] Checking git commits...
git commit -m "Deploy: College Attendance Management System with Web Interface" >nul 2>&1

git remote get-url origin >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ===================================================================
    echo  No GitHub remote found!
    echo  1. Create a new empty repository on GitHub: https://github.com/new
    echo  2. Copy the repository URL (e.g., https://github.com/username/repo.git)
    echo ===================================================================
    echo.
    set /p REPO_URL="Enter your GitHub Repository URL: "
    if "%REPO_URL%"=="" (
        echo [ERROR] No URL entered. Exiting.
        pause
        exit /b 1
    )
    git remote add origin %REPO_URL%
    echo [INFO] Remote origin set to: %REPO_URL%
) else (
    echo [INFO] Existing remote origin found:
    git remote -v
)

echo.
echo [3/3] Pushing code to GitHub (branch: main)...
git branch -M main
git push -u origin main

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ===================================================================
    echo   [SUCCESS] CODE PUSHED TO GITHUB!
    echo ===================================================================
    echo.
    echo Next Step: Open on Vercel to access from anywhere:
    echo   1. Visit: https://vercel.com/new
    echo   2. Sign in with GitHub
    echo   3. Click "Import" next to your repository
    echo   4. Click "Deploy" (Zero configuration needed, vercel.json is ready!)
    echo.
    echo Your site will be live instantly with a public HTTPS link!
    echo ===================================================================
) else (
    echo.
    echo [ERROR] Git push failed. Please verify your repository URL and GitHub credentials.
)

pause

@echo off
REM ============================================================================
REM Corn COBOL-to-Java Compiler - Windows Build Script
REM Author: Sekacorn
REM Created: 2025-01-10
REM License: Corn Evaluation License — See LICENSE
REM Copyright (c) 2025-2026 Cornmeister LLC. All rights reserved.
REM ============================================================================

echo ===============================================
echo  Corn COBOL-to-Java Compiler - Build Script
echo ===============================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven from https://maven.apache.org/
    exit /b 1
)

REM Check Java version
echo Checking Java version...
powershell -NoProfile -Command "if ((& java -version 2>&1 | Out-String) -match '21\.') { exit 0 } else { exit 1 }"
if %ERRORLEVEL% NEQ 0 (
    echo WARNING: Java 21 is recommended
    echo Current Java version:
    java -version
    echo.
    echo Continue anyway? (Press Ctrl+C to cancel)
    pause
)

echo.
echo Starting Maven build...
echo.

REM Clean and compile
call mvn clean compile

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ===============================================
    echo  BUILD FAILED
    echo ===============================================
    exit /b 1
)

echo.
echo ===============================================
echo  BUILD SUCCESSFUL
echo ===============================================
echo.
echo To package the application, run:
echo   mvn package
echo.
echo To run tests:
echo   mvn test
echo.
echo To run the CLI:
echo   run.bat
echo.

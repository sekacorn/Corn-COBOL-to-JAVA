@echo off
REM ============================================================================
REM Corn COBOL-to-Java Compiler - Windows Package Script
REM Author: Sekacorn
REM Created: 2025-01-10
REM License: Corn Evaluation License — See LICENSE
REM Copyright (c) 2025-2026 Cornmeister LLC. All rights reserved.
REM ============================================================================

echo ===============================================
echo  Corn COBOL-to-Java Compiler - Package Script
echo ===============================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven is not installed or not in PATH
    exit /b 1
)

echo Building executable JAR...
echo.

REM Clean, compile, and package
call mvn clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ===============================================
    echo  PACKAGE FAILED
    echo ===============================================
    exit /b 1
)

echo.
echo ===============================================
echo  PACKAGE SUCCESSFUL
echo ===============================================
echo.
echo Executable JAR created at:
echo   modules\cli\target\corn-cobol-to-java.jar
echo.
echo To run the application:
echo   run.bat --help
echo.
echo To run with tests:
echo   mvn package
echo.

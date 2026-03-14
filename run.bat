@echo off
REM ============================================================================
REM Corn COBOL-to-Java Compiler - Windows Run Script
REM Author: Sekacorn
REM Created: 2025-01-10
REM License: Corn Evaluation License — See LICENSE
REM Copyright (c) 2025-2026 Cornmeister LLC. All rights reserved.
REM ============================================================================

setlocal

REM Check if JAR exists
set JAR_PATH=modules\cli\target\corn-cobol-to-java.jar

if not exist "%JAR_PATH%" (
    echo ERROR: corn-cobol-to-java.jar not found
    echo.
    echo Please build the project first:
    echo   build.bat
    echo   mvn package
    exit /b 1
)

REM Run the CLI with all arguments
echo Running Corn COBOL-to-Java Compiler...
echo.

java -jar "%JAR_PATH%" %*

endlocal

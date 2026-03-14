@echo off
REM ============================================================================
REM Corn COBOL-to-Java Compiler - Windows Setup Script
REM Author: Sekacorn
REM Created: 2026-03-14
REM License: Corn Evaluation License - See LICENSE
REM Copyright (c) 2025-2026 Cornmeister LLC. All rights reserved.
REM ============================================================================

set "SCRIPT_DIR=%~dp0"
if "%SCRIPT_DIR:~-1%"=="\" set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"

set "JAVA_HOME=C:\Program Files\OpenLogic\jdk-21.0.8.9-hotspot"
set "MAVEN_HOME=C:\Program Files\apache-maven-3.8.8"
set "MAVEN_REPO_LOCAL=%SCRIPT_DIR%\.m2\repository"
set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%"

if not exist "%JAVA_HOME%" (
    echo ERROR: JAVA_HOME does not exist: %JAVA_HOME%
    exit /b 1
)

if not exist "%MAVEN_HOME%" (
    echo ERROR: MAVEN_HOME does not exist: %MAVEN_HOME%
    exit /b 1
)

echo Environment configured for Corn COBOL-to-Java
echo JAVA_HOME=%JAVA_HOME%
echo MAVEN_HOME=%MAVEN_HOME%
echo MAVEN_REPO_LOCAL=%MAVEN_REPO_LOCAL%
echo.
call java -version
echo.
call mvn -version
echo.
echo This shell is now configured.
echo To keep these variables in your current cmd session, run:
echo   call setup.bat

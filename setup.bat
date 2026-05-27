@echo off
REM ============================================================================
REM Corn COBOL-to-Java Compiler - Windows Setup Check
REM ============================================================================

setlocal
set "PROJECT_ROOT=%~dp0"
if "%PROJECT_ROOT:~-1%"=="\" set "PROJECT_ROOT=%PROJECT_ROOT:~0,-1%"
cd /d "%PROJECT_ROOT%"

echo ===============================================
echo  Corn COBOL-to-Java Compiler - Setup Check
echo ===============================================
echo.

where java >nul 2>nul
if errorlevel 1 (
    echo ERROR: Java 21+ is required and java was not found in PATH.
    echo Install a JDK, then reopen this terminal.
    exit /b 1
)

where javac >nul 2>nul
if errorlevel 1 (
    echo ERROR: A full JDK is required and javac was not found in PATH.
    exit /b 1
)

where mvn >nul 2>nul
if errorlevel 1 (
    echo ERROR: Maven is required and mvn was not found in PATH.
    exit /b 1
)

echo Java runtime:
java -version 2>&1
echo.
echo Java compiler:
javac -version
echo.
echo Maven:
call mvn -version
echo.
echo Project root:
echo   %PROJECT_ROOT%
echo.
echo Setup looks ready.
echo.
echo Next steps:
echo   build.bat
echo   run.bat
echo.
exit /b 0

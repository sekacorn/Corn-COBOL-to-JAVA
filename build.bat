@echo off
REM ============================================================================
REM Corn COBOL-to-Java Compiler - Windows Build Script
REM ============================================================================

setlocal
set "PROJECT_ROOT=%~dp0"
if "%PROJECT_ROOT:~-1%"=="\" set "PROJECT_ROOT=%PROJECT_ROOT:~0,-1%"
cd /d "%PROJECT_ROOT%"

echo ===============================================
echo  Corn COBOL-to-Java Compiler - Build Script
echo ===============================================
echo.

call :check_tools
if errorlevel 1 exit /b 1

call :stop_demo_server

echo Starting Maven package build...
echo.
call mvn clean package -DskipTests
if errorlevel 1 goto build_failed

if not exist "modules\cli\target\corn-cobol-to-java.jar" goto missing_artifact
if not exist "modules\server\target\corn-demo-server.jar" goto missing_artifact

echo.
echo ===============================================
echo  BUILD SUCCESSFUL
echo ===============================================
echo.
echo Created:
echo   modules\cli\target\corn-cobol-to-java.jar
echo   modules\server\target\corn-demo-server.jar
echo.
echo Run the GUI:
echo   run.bat
echo.
echo Run the CLI:
echo   run.bat cli --help
echo.
exit /b 0

:check_tools
where java >nul 2>nul
if errorlevel 1 (
    echo ERROR: Java 21+ is required and java was not found in PATH.
    exit /b 1
)

where mvn >nul 2>nul
if errorlevel 1 (
    echo ERROR: Maven is required and mvn was not found in PATH.
    exit /b 1
)

echo Java:
java -version 2>&1
echo.
echo Maven:
call mvn -version
echo.
exit /b 0

:stop_demo_server
powershell -NoProfile -ExecutionPolicy Bypass -Command "$c = Get-NetTCPConnection -LocalPort 8085 -ErrorAction SilentlyContinue | Select-Object -First 1; if ($c) { $p = Get-Process -Id $c.OwningProcess -ErrorAction SilentlyContinue; if ($p -and $p.ProcessName -eq 'java') { Write-Host 'Stopping existing Corn demo server on port 8085...'; Stop-Process -Id $p.Id -Force; Start-Sleep -Seconds 1 } }"
exit /b 0

:missing_artifact
echo.
echo ===============================================
echo  BUILD FAILED
echo ===============================================
echo Expected executable jars were not created.
exit /b 1

:build_failed
echo.
echo ===============================================
echo  BUILD FAILED
echo ===============================================
exit /b 1

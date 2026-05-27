@echo off
REM ============================================================================
REM Corn COBOL-to-Java Compiler - Windows Package Script
REM ============================================================================

setlocal
set "PROJECT_ROOT=%~dp0"
if "%PROJECT_ROOT:~-1%"=="\" set "PROJECT_ROOT=%PROJECT_ROOT:~0,-1%"
cd /d "%PROJECT_ROOT%"

echo ===============================================
echo  Corn COBOL-to-Java Compiler - Package Script
echo ===============================================
echo.

where mvn >nul 2>nul
if errorlevel 1 (
    echo ERROR: Maven is required and mvn was not found in PATH.
    exit /b 1
)

echo Building executable CLI and demo server JARs...
echo.
powershell -NoProfile -ExecutionPolicy Bypass -Command "$c = Get-NetTCPConnection -LocalPort 8085 -ErrorAction SilentlyContinue | Select-Object -First 1; if ($c) { $p = Get-Process -Id $c.OwningProcess -ErrorAction SilentlyContinue; if ($p -and $p.ProcessName -eq 'java') { Write-Host 'Stopping existing Corn demo server on port 8085...'; Stop-Process -Id $p.Id -Force; Start-Sleep -Seconds 1 } }"
call mvn clean package -DskipTests
if errorlevel 1 goto package_failed

if not exist "modules\cli\target\corn-cobol-to-java.jar" goto missing_artifact
if not exist "modules\server\target\corn-demo-server.jar" goto missing_artifact

echo.
echo ===============================================
echo  PACKAGE SUCCESSFUL
echo ===============================================
echo.
echo Executable JARs created at:
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

:missing_artifact
echo.
echo ===============================================
echo  PACKAGE FAILED
echo ===============================================
echo Expected executable jars were not created.
exit /b 1

:package_failed
echo.
echo ===============================================
echo  PACKAGE FAILED
echo ===============================================
exit /b 1

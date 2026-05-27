@echo off
REM ============================================================================
REM Corn COBOL-to-Java Compiler - Windows Run Script
REM ============================================================================

setlocal
set "PROJECT_ROOT=%~dp0"
if "%PROJECT_ROOT:~-1%"=="\" set "PROJECT_ROOT=%PROJECT_ROOT:~0,-1%"
cd /d "%PROJECT_ROOT%"

set "CLI_JAR=modules\cli\target\corn-cobol-to-java.jar"
set "SERVER_JAR=modules\server\target\corn-demo-server.jar"
set "MODE=%~1"

if "%MODE%"=="" set "MODE=server"
if /i "%MODE%"=="gui" set "MODE=server"
if /i "%MODE%"=="web" set "MODE=server"

if /i "%MODE%"=="help" goto help
if /i "%MODE%"=="--help" goto help
if /i "%MODE%"=="-h" goto help
if /i "%MODE%"=="server" goto run_server
if /i "%MODE%"=="cli" goto run_cli

echo Unknown mode: %~1
echo.
goto help

:run_server
if not exist "%SERVER_JAR%" goto missing_server
where java >nul 2>nul
if errorlevel 1 (
    echo ERROR: Java 21+ is required and java was not found in PATH.
    exit /b 1
)

set "PORT=8085"
if not "%~2"=="" set "PORT=%~2"

echo Starting Corn Demo Server...
echo.
echo Open: http://localhost:%PORT%
echo Press Ctrl+C to stop the server.
echo.
java -jar "%SERVER_JAR%" --port "%PORT%" --static "%PROJECT_ROOT%\demo-ui"
exit /b %ERRORLEVEL%

:run_cli
if not exist "%CLI_JAR%" goto missing_cli
where java >nul 2>nul
if errorlevel 1 (
    echo ERROR: Java 21+ is required and java was not found in PATH.
    exit /b 1
)

shift
echo Running Corn COBOL-to-Java CLI...
echo.
java -jar "%CLI_JAR%" %*
exit /b %ERRORLEVEL%

:missing_server
echo ERROR: corn-demo-server.jar not found.
echo.
echo Build the project first:
echo   build.bat
echo.
exit /b 1

:missing_cli
echo ERROR: corn-cobol-to-java.jar not found.
echo.
echo Build the project first:
echo   build.bat
echo.
exit /b 1

:help
echo Usage:
echo   run.bat                 Start the web GUI on http://localhost:8085
echo   run.bat server [port]   Start the web GUI on a custom port
echo   run.bat gui [port]      Alias for server
echo   run.bat cli [args...]   Run the command-line compiler
echo.
echo Examples:
echo   run.bat
echo   run.bat server 8090
echo   run.bat cli --help
echo.
exit /b 0

#!/usr/bin/env bash
# ============================================================================
# Corn COBOL-to-Java Compiler - Unix Build Script
# ============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

JAVA_CMD="${JAVA_CMD:-java}"
MVN_CMD="${MVN_CMD:-mvn}"

MVN_ARGS=()
if [[ -n "${MAVEN_REPO_LOCAL:-}" ]]; then
    MVN_ARGS+=("-Dmaven.repo.local=$MAVEN_REPO_LOCAL")
fi

echo "==============================================="
echo " Corn COBOL-to-Java Compiler - Build Script"
echo "==============================================="
echo

check_tools() {
    if ! command -v "$JAVA_CMD" >/dev/null 2>&1 && [[ ! -x "$JAVA_CMD" && ! -f "$JAVA_CMD" ]]; then
        echo "ERROR: Java 21+ is required and java was not found in PATH."
        exit 1
    fi

    if ! command -v "$MVN_CMD" >/dev/null 2>&1 && [[ ! -x "$MVN_CMD" && ! -f "$MVN_CMD" ]]; then
        echo "ERROR: Maven is required and mvn was not found in PATH."
        exit 1
    fi

    echo "Java:"
    "$JAVA_CMD" -version 2>&1
    echo
    echo "Maven:"
    "$MVN_CMD" -version
    echo
}

stop_demo_server() {
    local port="${1:-8085}"

    if command -v lsof >/dev/null 2>&1; then
        local pid
        pid="$(lsof -ti tcp:"$port" -sTCP:LISTEN 2>/dev/null || true)"
        if [[ -n "$pid" ]]; then
            echo "Stopping existing Corn demo server on port $port..."
            kill "$pid" 2>/dev/null || true
            sleep 1
        fi
    elif command -v powershell.exe >/dev/null 2>&1; then
        powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "\$c = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue | Select-Object -First 1; if (\$c) { \$p = Get-Process -Id \$c.OwningProcess -ErrorAction SilentlyContinue; if (\$p -and \$p.ProcessName -eq 'java') { Write-Host 'Stopping existing Corn demo server on port $port...'; Stop-Process -Id \$p.Id -Force; Start-Sleep -Seconds 1 } }" || true
    fi
}

check_tools
stop_demo_server 8085

echo "Starting Maven package build..."
echo
"$MVN_CMD" clean package -DskipTests "${MVN_ARGS[@]}"

if [[ ! -f "modules/cli/target/corn-cobol-to-java.jar" ]]; then
    echo "ERROR: modules/cli/target/corn-cobol-to-java.jar was not created."
    exit 1
fi

if [[ ! -f "modules/server/target/corn-demo-server.jar" ]]; then
    echo "ERROR: modules/server/target/corn-demo-server.jar was not created."
    exit 1
fi

echo
echo "==============================================="
echo " BUILD SUCCESSFUL"
echo "==============================================="
echo
echo "Created:"
echo "  modules/cli/target/corn-cobol-to-java.jar"
echo "  modules/server/target/corn-demo-server.jar"
echo
echo "Run the GUI:"
echo "  ./run.sh"
echo
echo "Run the CLI:"
echo "  ./run.sh cli --help"
echo

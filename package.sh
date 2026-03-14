#!/bin/bash
# ============================================================================
# Corn COBOL-to-Java Compiler - Unix Package Script
# Author: Sekacorn
# Created: 2025-01-10
# License: Corn Evaluation License — See LICENSE
# Copyright (c) 2025-2026 Cornmeister LLC. All rights reserved.
# ============================================================================

set -e

MVN_CMD="${MVN_CMD:-mvn}"
MVN_CMD_WIN="${MVN_CMD_WIN:-}"
MVN_ARGS=()
if [ -n "${MAVEN_REPO_LOCAL:-}" ]; then
    MVN_ARGS+=("-Dmaven.repo.local=$MAVEN_REPO_LOCAL")
fi

run_maven() {
    if [ -n "$MVN_CMD_WIN" ]; then
        local command_line="& '$MVN_CMD_WIN'"
        local arg
        for arg in "$@"; do
            command_line="$command_line '$arg'"
        done
        powershell.exe -NoProfile -Command "$command_line"
    else
        "$MVN_CMD" "$@"
    fi
}

echo "==============================================="
echo " Corn COBOL-to-Java Compiler - Package Script"
echo "==============================================="
echo ""

# Check if Maven is installed
if [ -z "$MVN_CMD_WIN" ] && ! command -v "$MVN_CMD" >/dev/null 2>&1 && [ ! -x "$MVN_CMD" ] && [ ! -f "$MVN_CMD" ]; then
    echo "ERROR: Maven is not installed or not in PATH"
    exit 1
fi

echo "Building executable JAR..."
echo ""

# Clean, compile, and package
run_maven clean package -DskipTests "${MVN_ARGS[@]}"

echo ""
echo "==============================================="
echo " PACKAGE SUCCESSFUL"
echo "==============================================="
echo ""
echo "Executable JAR created at:"
echo "  modules/cli/target/corn-cobol-to-java.jar"
echo ""
echo "To run the application:"
echo "  ./run.sh --help"
echo ""
echo "To run with tests:"
echo "  \"$MVN_CMD\" package"
echo ""

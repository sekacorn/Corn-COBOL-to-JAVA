#!/bin/bash
# ============================================================================
# Corn COBOL-to-Java Compiler - Unix Build Script
# Author: Sekacorn
# Created: 2025-01-10
# License: Corn Evaluation License — See LICENSE
# Copyright (c) 2025-2026 Cornmeister LLC. All rights reserved.
# ============================================================================

set -e

JAVA_CMD="${JAVA_CMD:-java}"
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
echo " Corn COBOL-to-Java Compiler - Build Script"
echo "==============================================="
echo ""

# Check if Maven is installed
if [ -z "$MVN_CMD_WIN" ] && ! command -v "$MVN_CMD" >/dev/null 2>&1 && [ ! -x "$MVN_CMD" ] && [ ! -f "$MVN_CMD" ]; then
    echo "ERROR: Maven is not installed or not in PATH"
    echo "Please install Maven from https://maven.apache.org/"
    exit 1
fi

# Check Java version
echo "Checking Java version..."
java_version=$("$JAVA_CMD" -version 2>&1 | head -n 1 | awk -F '"' '{print $2}')
if [[ ! "$java_version" =~ ^21 ]]; then
    echo "WARNING: Java 21 is recommended"
    echo "Current Java version:"
    "$JAVA_CMD" -version
    echo ""
    read -p "Continue anyway? (y/n) " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

echo ""
echo "Starting Maven build..."
echo ""

# Clean and compile
run_maven clean compile "${MVN_ARGS[@]}"

echo ""
echo "==============================================="
echo " BUILD SUCCESSFUL"
echo "==============================================="
echo ""
echo "To package the application, run:"
echo "  \"$MVN_CMD\" package"
echo ""
echo "To run tests:"
echo "  \"$MVN_CMD\" test"
echo ""
echo "To run the CLI:"
echo "  ./run.sh"
echo ""

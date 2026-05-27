#!/usr/bin/env bash
# ============================================================================
# Corn COBOL-to-Java Compiler - Unix Setup Check
# ============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "==============================================="
echo " Corn COBOL-to-Java Compiler - Setup Check"
echo "==============================================="
echo

if ! command -v java >/dev/null 2>&1; then
    echo "ERROR: Java 21+ is required and java was not found in PATH."
    echo "Install a JDK, then reopen this terminal."
    exit 1
fi

if ! command -v javac >/dev/null 2>&1; then
    echo "ERROR: A full JDK is required and javac was not found in PATH."
    exit 1
fi

if ! command -v mvn >/dev/null 2>&1; then
    echo "ERROR: Maven is required and mvn was not found in PATH."
    exit 1
fi

echo "Java runtime:"
java -version 2>&1
echo
echo "Java compiler:"
javac -version
echo
echo "Maven:"
mvn -version
echo
echo "Project root:"
echo "  $SCRIPT_DIR"
echo
echo "Setup looks ready."
echo
echo "Next steps:"
echo "  ./build.sh"
echo "  ./run.sh"
echo

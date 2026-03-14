#!/bin/bash
# ============================================================================
# Corn COBOL-to-Java Compiler - Unix Setup Script
# Author: Sekacorn
# Created: 2026-03-14
# License: Corn Evaluation License - See LICENSE
# Copyright (c) 2025-2026 Cornmeister LLC. All rights reserved.
# ============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

JAVA_HOME_WIN='C:\Program Files\OpenLogic\jdk-21.0.8.9-hotspot'
MAVEN_HOME_WIN='C:\Program Files\apache-maven-3.8.8'

if command -v cygpath >/dev/null 2>&1; then
    JAVA_HOME="$(cygpath "$JAVA_HOME_WIN")"
    MAVEN_HOME="$(cygpath "$MAVEN_HOME_WIN")"
else
    JAVA_HOME='/c/Program Files/OpenLogic/jdk-21.0.8.9-hotspot'
    MAVEN_HOME='/c/Program Files/apache-maven-3.8.8'
fi

if [ ! -d "$JAVA_HOME" ]; then
    echo "ERROR: JAVA_HOME does not exist: $JAVA_HOME"
    return 1 2>/dev/null || exit 1
fi

if [ ! -d "$MAVEN_HOME" ]; then
    echo "ERROR: MAVEN_HOME does not exist: $MAVEN_HOME"
    return 1 2>/dev/null || exit 1
fi

export JAVA_HOME
export MAVEN_HOME
export MAVEN_REPO_LOCAL="$SCRIPT_DIR/.m2/repository"
export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH"
export JAVA_CMD="$JAVA_HOME/bin/java.exe"
export MVN_CMD="$MAVEN_HOME/bin/mvn.cmd"
export MVN_CMD_WIN='C:\Program Files\apache-maven-3.8.8\bin\mvn.cmd'

echo "Environment configured for Corn COBOL-to-Java"
echo "JAVA_HOME=$JAVA_HOME"
echo "MAVEN_HOME=$MAVEN_HOME"
echo "MAVEN_REPO_LOCAL=$MAVEN_REPO_LOCAL"
echo
"$JAVA_CMD" -version
echo
"$MVN_CMD" -version
echo
echo "Usage:"
echo "  source ./setup.sh"

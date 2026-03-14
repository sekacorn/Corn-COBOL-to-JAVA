#!/bin/bash
# ============================================================================
# Corn COBOL-to-Java Compiler - Unix Run Script
# Author: Sekacorn
# Created: 2025-01-10
# License: Corn Evaluation License — See LICENSE
# Copyright (c) 2025-2026 Cornmeister LLC. All rights reserved.
# ============================================================================

set -e

JAVA_CMD="${JAVA_CMD:-java}"

# Check if JAR exists
JAR_PATH="modules/cli/target/corn-cobol-to-java.jar"

if [ ! -f "$JAR_PATH" ]; then
    echo "ERROR: corn-cobol-to-java.jar not found"
    echo ""
    echo "Please build the project first:"
    echo "  ./build.sh"
    echo "  mvn package"
    exit 1
fi

# Run the CLI with all arguments
echo "Running Corn COBOL-to-Java Compiler..."
echo ""

"$JAVA_CMD" -jar "$JAR_PATH" "$@"

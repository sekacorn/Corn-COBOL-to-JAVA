#!/usr/bin/env bash
# ============================================================================
# Corn COBOL-to-Java Compiler - Unix Run Script
# ============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

JAVA_CMD="${JAVA_CMD:-java}"
CLI_JAR="modules/cli/target/corn-cobol-to-java.jar"
SERVER_JAR="modules/server/target/corn-demo-server.jar"

mode="${1:-server}"
case "$mode" in
    gui|web)
        mode="server"
        ;;
    help|--help|-h)
        mode="help"
        ;;
esac

run_server() {
    local port="${1:-8085}"

    if [[ ! -f "$SERVER_JAR" ]]; then
        echo "ERROR: corn-demo-server.jar not found."
        echo
        echo "Build the project first:"
        echo "  ./build.sh"
        exit 1
    fi

    if ! command -v "$JAVA_CMD" >/dev/null 2>&1 && [[ ! -x "$JAVA_CMD" && ! -f "$JAVA_CMD" ]]; then
        echo "ERROR: Java 21+ is required and java was not found in PATH."
        exit 1
    fi

    echo "Starting Corn Demo Server..."
    echo
    echo "Open: http://localhost:$port"
    echo "Press Ctrl+C to stop the server."
    echo
    "$JAVA_CMD" -jar "$SERVER_JAR" --port "$port" --static "$SCRIPT_DIR/demo-ui"
}

run_cli() {
    shift || true

    if [[ ! -f "$CLI_JAR" ]]; then
        echo "ERROR: corn-cobol-to-java.jar not found."
        echo
        echo "Build the project first:"
        echo "  ./build.sh"
        exit 1
    fi

    if ! command -v "$JAVA_CMD" >/dev/null 2>&1 && [[ ! -x "$JAVA_CMD" && ! -f "$JAVA_CMD" ]]; then
        echo "ERROR: Java 21+ is required and java was not found in PATH."
        exit 1
    fi

    echo "Running Corn COBOL-to-Java CLI..."
    echo
    "$JAVA_CMD" -jar "$CLI_JAR" "$@"
}

print_help() {
    cat <<'EOF'
Usage:
  ./run.sh                 Start the web GUI on http://localhost:8085
  ./run.sh server [port]   Start the web GUI on a custom port
  ./run.sh gui [port]      Alias for server
  ./run.sh cli [args...]   Run the command-line compiler

Examples:
  ./run.sh
  ./run.sh server 8090
  ./run.sh cli --help
EOF
}

case "$mode" in
    server)
        run_server "${2:-8085}"
        ;;
    cli)
        run_cli "$@"
        ;;
    help)
        print_help
        ;;
    *)
        echo "Unknown mode: $mode"
        echo
        print_help
        exit 1
        ;;
esac

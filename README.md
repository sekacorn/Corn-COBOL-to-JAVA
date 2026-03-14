# Corn COBOL-to-Java Compiler

COBOL-to-Java translation toolchain built with Java 21 and Maven.

[![License](https://img.shields.io/badge/License-Evaluation-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Build](https://img.shields.io/badge/Build-Maven-red.svg)](https://maven.apache.org/)

## Overview

Corn is a multi-module Java project for parsing COBOL, building an intermediate representation, generating Java, and providing a small runtime for COBOL semantics. The current repository is an evaluation-stage implementation focused on the core translation pipeline.

What is in this repository today:
- COBOL lexer/parser built with ANTLR4
- Intermediate representation for programs, expressions, and statements
- Java code generation for the current supported translation path
- Runtime helpers for numerics, strings, and file operations
- Picocli-based CLI commands for init, analyze, translate, validate, report, refactor, and gui
- Execution-based validation for the current supported subset using expected stdout fixtures

What is not in this repository yet:
- Separate `semantics`, `transforms`, `validator`, `server`, or `ui-desktop` modules
- A full multi-level code generation implementation
- Production-grade validation against GnuCOBOL execution outputs
- Complete COBOL dialect coverage

## Screenshots

### CLI Help

Current packaged CLI help from the shaded jar:

![Corn CLI help](./screenshot-cli-help.png)

### Translate and Validate Flow

Sample `translate` and `validate` execution against `samples/BANK-ACCOUNT.cbl`:

![Corn translate and validate flow](./screenshot-translate-validate.png)

### Workspace Explorer

The current `gui` command opens the workspace in the system file explorer:

![Corn workspace explorer](./screenshot-gui-explorer.png)

## Current Status

The codebase currently ships these Maven modules:

```text
modules/
  cli
  codegen-java
  ir
  lexer-parser
  runtime-java
```

Implemented pipeline:

```text
COBOL source
  -> lexer/parser
  -> IR
  -> Java code generation
  -> Java runtime support
```

The CLI advertises four code generation levels, but only level `2` is implemented at this time. The `translate` command now fails fast for levels `0`, `1`, and `3`.

## Features Available Now

### Parsing and IR
- ANTLR4 COBOL grammar
- Fixed-format and free-format parsing paths
- Data definitions, procedure statements, expressions, conditions, file control, and `INSPECT` support in the current parser/IR flow

### Java Code Generation
- Java source generation from parsed COBOL programs
- Paragraph/section translation into Java methods
- Working-storage and file-section field generation
- Generated file-status propagation into declared COBOL status fields

### Runtime Support
- COBOL-style numeric helpers
- COBOL string helpers including `INSPECT`
- Basic file runtime abstractions used by generated code

### CLI Commands
- `init`: creates a basic workspace config
- `analyze`: parses COBOL files and writes a JSON analysis report
- `translate`: parses and generates Java source
- `validate`: parses, generates Java, compiles generated Java, executes it, and can compare stdout against expected `.out` fixtures for the supported subset
- `report`: generates basic HTML, JSON, Markdown, or placeholder PDF reports
- `refactor`: performs baseline Java text cleanup; it is not yet a full LLM workflow
- `gui`: opens the workspace in the system file explorer; it is not a JavaFX desktop application

## MVP Boundary

This repository should currently be treated as an MVP for a bounded COBOL subset, not as a general COBOL modernization platform.

Supported MVP workflow:
- `translate --codegen-level 2` for the currently covered subset
- `validate` for parse -> generate -> compile -> execute validation
- optional expected-output fixtures named `<PROGRAM-ID>.out` or `<source-file>.out`

Supported MVP program shapes today are represented by the integration corpus in `modules/codegen-java/src/test/resources/cobol/`:
- hello-world style programs
- basic arithmetic
- control flow and `PERFORM`
- working-storage/data definitions
- current file-I/O compilation path
- current `INSPECT` support through runtime-backed code generation

## Quick Start

### Requirements

- Java 21
- Maven 3.8+

### Build

```bash
mvn clean install
```

### Run the CLI

```bash
java -jar modules/cli/target/corn-cobol-to-java.jar --help
```

### Example Commands

```bash
# Initialize a workspace
java -jar modules/cli/target/corn-cobol-to-java.jar init ^
  --source .\cobol ^
  --output .\output\java

# Analyze COBOL files
java -jar modules/cli/target/corn-cobol-to-java.jar analyze .\cobol

# Translate to Java
java -jar modules/cli/target/corn-cobol-to-java.jar translate .\cobol ^
  --output .\output\java ^
  --codegen-level 2

# Validate by generating and compiling Java
java -jar modules/cli/target/corn-cobol-to-java.jar validate .\cobol ^
  --output .\corn-validation

# Validate against expected stdout fixtures
java -jar modules/cli/target/corn-cobol-to-java.jar validate .\cobol ^
  --output .\corn-validation ^
  --test-data .\expected-output

# Generate a report
java -jar modules/cli/target/corn-cobol-to-java.jar report ^
  --workspace . ^
  --output .\corn-report.html ^
  --format HTML
```

## Repository Layout

```text
corn-cobol-to-java/
  docs/
    ARCHITECTURE.md
    PATENT_APPLICATION.md
    VALUE_PROPOSITION.md
  modules/
    cli/
    codegen-java/
    ir/
    lexer-parser/
    runtime-java/
  samples/
  README.md
  LICENSE
  pom.xml
```

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [Value Proposition](docs/VALUE_PROPOSITION.md)
- [Patent Application](docs/PATENT_APPLICATION.md)

## Limitations

- Only code generation level `2` is implemented.
- Several CLI commands are intentionally lightweight and should be treated as evaluation-stage utilities, not production workflows.
- The validation command currently checks parse/generate/compile/execute flow for generated Java and optional expected stdout fixtures, not full equivalence against executed GnuCOBOL outputs.
- The project still has planned modules referenced in design documents that are not present in this repository.
- The CLI version banner and some source comments still contain older commercial-license wording that should be aligned separately from this README.

## Licensing

This repository is distributed under the Corn Evaluation License for non-production evaluation use. See [LICENSE](LICENSE) for the full terms.

For production use or broader commercial licensing, contact `sekacorn@gmail.com`.

## Roadmap

Planned or partially implemented areas include:
- additional code generation levels
- richer semantic analysis and transform passes
- stronger validation workflows
- broader dialect coverage
- UI and service-facing tooling
- deeper file and data integration backends

## Contact

- Sekacorn
- Cornmeister LLC
- `sekacorn@gmail.com`

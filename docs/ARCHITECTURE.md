# Corn COBOL-to-Java Compiler - Current Architecture

This document describes the architecture that exists in this repository today. It is intentionally narrower than earlier vision material: several planned modules and deployment options are not implemented in this codebase yet.

## Current Modules

```text
modules/
  cli           Picocli entry points for init/analyze/translate/validate/report/refactor/gui
  codegen-java  Java source generation from IR
  ir            Core intermediate representation
  lexer-parser  ANTLR4 lexer/parser and IR builders
  runtime-java  Runtime helpers used by generated Java
```

## Current Pipeline

```text
COBOL source
  -> lexer/parser
  -> IR builders
  -> Program IR
  -> Java code generator
  -> generated Java source
  -> runtime-java support library
```

## High-Level Responsibilities

### `lexer-parser`
- Defines the COBOL ANTLR grammars
- Parses COBOL source into parse trees
- Builds the IR used by the rest of the pipeline
- Handles current parsing support for data definitions, procedure statements, conditions, file control, and `INSPECT`

### `ir`
- Defines immutable-ish domain objects for programs, divisions, statements, expressions, pictures, files, and source metadata
- Acts as the contract between parsing and code generation

### `codegen-java`
- Converts IR into Java classes
- Generates Java fields from working-storage and file-section items
- Generates Java methods for paragraphs and sections
- Emits runtime calls for COBOL-style operations such as numerics, strings, and file handling

### `runtime-java`
- Provides Java helpers that generated code depends on
- Includes numeric helpers, string helpers, and file abstractions
- Includes current `INSPECT` helper implementations used by generated code

### `cli`
- Exposes the repository through a shaded CLI jar
- `translate` is the main end-to-end path
- `validate` now supports parse -> generate -> compile -> execute validation for the current supported subset, with optional expected stdout fixtures
- `analyze`, `report`, `refactor`, and `gui` are still evaluation-stage utilities rather than full enterprise workflows

## Module Dependencies

```text
cli
  -> lexer-parser
  -> codegen-java
  -> runtime-java
  -> ir

lexer-parser
  -> ir

codegen-java
  -> ir
  -> runtime-java

runtime-java
  -> no internal module dependencies
```

## Translation Flow

### 1. Parse
- `CobolSourceParser` reads source files and selects the parsing path
- Parse errors are returned in `ParseResult`

### 2. Build IR
- Builder classes convert parse-tree nodes into IR statements, expressions, and program structure
- File control, condition names, and current statement coverage are represented here

### 3. Generate Java
- `JavaCodeGenerator` walks the IR and emits a Java class
- `JavaExpressionVisitor` and `JavaStatementVisitor` generate expression and statement code
- Generated code targets the current supported translation path, which corresponds to CLI codegen level `2`

### 4. Use Runtime Support
- Generated Java imports helpers from `runtime-java`
- Numeric operations, string operations, and file operations route through runtime types

### 5. Validate Generated Output
- `ValidateCommand` can compile generated Java into a temporary classes directory
- It can execute the generated `run()` entrypoint for the current supported subset
- It can compare stdout against `.out` fixtures named after `PROGRAM-ID` or source filename
- This is the current MVP validation path; it is not yet full COBOL-vs-GnuCOBOL equivalence validation

## Current Limitations

- Only code generation level `2` is implemented
- There is no separate `semantics`, `transforms`, `validator`, `server`, or `ui-desktop` module in this repository
- The validation command currently verifies parse/generate/compile/execute flow for generated Java and optional expected stdout fixtures; it does not yet run a full GnuCOBOL execution comparison pipeline
- Some design documents in this repo describe a broader target architecture than the code implements today

## MVP Scope

The current MVP should be understood as:
- bounded COBOL subset support
- `translate --codegen-level 2`
- execution-based validation of generated Java
- integration coverage centered on the sample corpus under `modules/codegen-java/src/test/resources/cobol/`

## Planned / Referenced Future Areas

These appear in roadmap or business documents but are not implemented here as modules:
- richer semantic analysis
- transform and optimization passes
- dedicated validation harness module
- service/server deployment
- desktop UI
- broader code generation level support

## Build Artifact

The CLI module produces:

```text
modules/cli/target/corn-cobol-to-java.jar
```

Run it with:

```bash
java -jar modules/cli/target/corn-cobol-to-java.jar --help
```

## Status

- Document version: 2.1
- Last updated: March 14, 2026
- Scope: current repository implementation

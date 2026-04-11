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
COBOL source (.cbl)
  -> CobolPreprocessor (fixed-form -> free-form normalization)
  -> ANTLR4 Lexer/Parser (CobolLexer.g4 / CobolParser.g4)
  -> CobolIRBuildingVisitor (parse tree -> immutable IR)
  -> JavaCodeGenerator (IR -> Java source via visitor pattern)
  -> Generated Java (imports com.sekacorn.corn.runtime.*)
```

## High-Level Responsibilities

### `lexer-parser`
- Defines the COBOL ANTLR grammars targeting the ANSI-85 subset
- `CobolPreprocessor` handles fixed-format normalization including string literal continuation across lines
- Parses COBOL source into parse trees with word-form operators (`EQUAL`, `GREATER`, `LESS`) and symbol operators
- Builds the IR used by the rest of the pipeline
- Handles current parsing support for data definitions (including level 88 condition names with `VALUES ARE`, `THRU`/`THROUGH` ranges, signed literals), procedure statements, conditions, file control, and `INSPECT`
- Commas and semicolons treated as optional separators (skipped by the lexer per COBOL standard)
- Arithmetic statements support Format 1 (in-place) and Format 2 (`GIVING`), multi-target operations, per-target `ROUNDED`, and standalone `NOT ON SIZE ERROR` clauses

### `ir`
- Defines immutable-ish domain objects for programs, divisions, statements, expressions, pictures, files, and source metadata
- Acts as the contract between parsing and code generation

### `codegen-java`
- Converts IR into Java classes via `JavaCodeGenerator`, `JavaStatementVisitor`, `JavaExpressionVisitor`
- Generates Java fields from working-storage and file-section items (`JavaFieldGenerator`)
- Generates Java methods for paragraphs and sections
- Emits runtime calls for COBOL-style operations (numerics, strings, file handling)
- Supports 30+ statement types and 8 expression types with full visitor coverage
- Proper `ON SIZE ERROR` and `NOT ON SIZE ERROR` handling with `CobolMath.Result.hasError()` checks
- Multi-target `MULTIPLY` and `DIVIDE` with per-target `ROUNDED` support
- `WRITE ADVANCING` and `ALTER` statement generation
- `CALL ON EXCEPTION` generates try-catch blocks
- `PERFORM VARYING` with `BY` clause generates correct for-loops

### `runtime-java`
- Provides Java helpers that generated code depends on (zero external dependencies)
- `CobolMath`: BigDecimal-based arithmetic with 8 rounding modes, size error detection, and ANSI-85-compliant truncation (default: `RoundingMode.DOWN` without `ROUNDED`)
- `CobolString`: `MOVE`, `INSPECT` (tallying/replacing/converting), `STRING`, `UNSTRING`, reference modification
- `CobolFile`, `SequentialFile`, `IndexedFile`: file I/O abstractions with status codes
- `ArithmeticContext`: captures target scale, precision, and rounding mode from PICTURE clauses

### `cli`
- Exposes the repository through a shaded CLI jar with branded ASCII banner
- `translate`: main end-to-end COBOL-to-Java path (codegen level 2)
- `validate`: parse -> generate -> compile -> execute validation with optional expected stdout fixtures
- `analyze`: parses COBOL files and outputs JSON analysis report
- `report`: generates HTML, JSON, or Markdown reports
- `init`, `refactor`, `gui`: evaluation-stage utilities

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

## Test Coverage

The integration test suite (`CodegenIntegrationIT`) validates the full pipeline across 18 test cases:
- Resource file tests: HELLO, ARITHMETIC, CONTROL, DATADEF, FILEIO, EVALUATE-COMPLEX, NESTED-IF, PERFORM-VARYING, STRING-OPS
- Inline COBOL tests: display, arithmetic, if-else, perform, perform-until, on-size-error, call

All tests verify exact output values, not just compilation success.

## Status

- Document version: 3.1
- Last updated: April 11, 2026
- Scope: current repository implementation

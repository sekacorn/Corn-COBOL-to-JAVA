# Corn COBOL-to-Java Compiler — Value Proposition

## The COBOL Modernization Challenge

- **200+ billion lines** of COBOL still running globally
- **43% of banking systems** depend on COBOL (2024 survey)
- **90% of COBOL developers** projected to retire by 2030
- Maintenance costs of **$3–5M annually** per major system
- Manual rewrite projects fail **70% of the time**

Financial institutions need a path from COBOL to modern Java that is accurate, auditable, and low-risk.

---

## What Corn Delivers

### Automated, Validated Translation
Corn parses COBOL source, builds an intermediate representation, and generates clean, maintainable Java — not a line-by-line transliteration. The generated code uses idiomatic Java patterns and a purpose-built runtime library for COBOL semantics.

### Correctness You Can Verify
The `validate` command runs the full pipeline — parse, generate, compile, execute — and can compare output against expected results. Every translation is verifiable before deployment.

### ANSI-85 Compliant Arithmetic
Financial calculations require exact numeric behavior. Corn uses `BigDecimal`-based arithmetic with 8 rounding modes, size error detection, and ANSI-85-compliant truncation to preserve COBOL's numeric guarantees.

### Clean Intellectual Property
Built from scratch with zero copied code from any proprietary, copyleft, or third-party COBOL implementation. All algorithms are original, based on published ANSI/ISO COBOL standards.

---

## Technology Stack

| Component | Technology |
|-----------|------------|
| **Language** | Java 21 |
| **Parser** | ANTLR4 (BSD-3) |
| **Build** | Maven |
| **Arithmetic** | BigDecimal with 8 rounding modes |
| **CLI** | Picocli |
| **Testing** | JUnit 5, in-memory compilation |

---

## Supported COBOL Subset

| Category | Statements |
|----------|-----------|
| **Arithmetic** | ADD, SUBTRACT, MULTIPLY, DIVIDE, COMPUTE (with ROUNDED and ON SIZE ERROR) |
| **Control Flow** | IF/ELSE, EVALUATE/WHEN, PERFORM (simple, UNTIL, VARYING, TIMES), GO TO, STOP RUN, EXIT, GOBACK |
| **Data Movement** | MOVE, INITIALIZE, SET |
| **I/O** | DISPLAY, ACCEPT, OPEN, CLOSE, READ, WRITE, REWRITE, DELETE, START |
| **String** | STRING, UNSTRING, INSPECT (TALLYING, REPLACING, CONVERTING) |
| **Program** | CALL (with ON EXCEPTION), SEARCH |

---

## Why Corn

1. **Accuracy over speed** — validated translation, not best-effort conversion
2. **Incremental adoption** — evaluate for free, pilot on real code, scale with confidence
3. **Modern output** — generated Java uses current language features and clean architecture
4. **No vendor lock-in** — you own the generated code under commercial license
5. **Financial-grade arithmetic** — BigDecimal precision with COBOL-compliant rounding

---

## Getting Started

```bash
# Build
mvn clean install

# Translate COBOL to Java
java -jar modules/cli/target/corn-cobol-to-java.jar translate ./cobol \
  --output ./output/java --codegen-level 2

# Validate the full pipeline
java -jar modules/cli/target/corn-cobol-to-java.jar validate ./cobol \
  --output ./corn-validation
```

---

## Licensing & Contact

The evaluation edition is available for non-production assessment. For commercial licensing, migration assessments, and enterprise inquiries:

**Cornmeister LLC**
Email: sekacorn@gmail.com

---

Copyright (c) 2025–2026 Cornmeister LLC (Maryland LLC). All rights reserved.

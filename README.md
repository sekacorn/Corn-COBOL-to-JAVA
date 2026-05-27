# Corn COBOL-to-Java Compiler

**Modernize millions of lines of COBOL — automatically, accurately, and on your terms.**

[![CI](https://github.com/sekacorn/corn-cobol-to-java/actions/workflows/ci.yml/badge.svg)](https://github.com/sekacorn/corn-cobol-to-java/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Evaluation-blue.svg)](LICENSE)
[![NIST Conformance](https://img.shields.io/badge/NIST%20CCVS85-90.1%25-brightgreen.svg)](#nist-ccvs85-conformance)

---

## The Problem

The world still runs on COBOL. 95% of ATM transactions, 80% of in-person transactions, and 43% of all banking systems depend on COBOL programs — many written 30-40 years ago. The developers who built them are retiring. The mainframe costs keep climbing. And the risk of doing nothing grows every quarter.

## The Solution

Corn is a **deterministic, rule-based COBOL-to-Java compiler** — not a black-box LLM that guesses. Every translation is reproducible, auditable, and traceable back to the original source. Built for the standards that financial institutions and government agencies demand.

---

## See It In Action

### Translate — COBOL In, Java Out

Paste any COBOL program. Get compilable Java in seconds. Side-by-side view with copy and download.

![Corn Translate — COBOL to Java side-by-side](./Screenshot-ui-frontend.png)

### Analyze — Know What You're Working With

Automatic complexity scoring, statement counts, division breakdown, feature detection, and parse diagnostics. Understand every program before you migrate it.

![Corn Analyze — diagnostics, metrics, and feature detection](./Screenshot-ui-frontend-analyze.png)

### Portfolio — Plan Your Migration Waves

See your entire COBOL portfolio at a glance. Programs are automatically grouped into migration waves by complexity and risk. Know exactly what moves first and what needs extra attention.

![Corn Portfolio — migration waves, risk assessment, effort breakdown](./Screenshot-ui-frontend-portfolio.png)

### Cost Estimator — Build the Business Case

Configure team rates and portfolio size. Get instant cost breakdowns by phase — automated translation, review, remediation, testing, and deployment. Built for the slide deck that gets the budget approved.

![Corn Cost Estimator — phase-by-phase cost and duration](./Screenshot-ui-frontend-cost.png)

### Execution Trace — Prove Equivalence

Step-through execution trace with variable state tracking. Side-by-side COBOL/Java diff visualization. The evidence your auditors and regulators need.

![Corn Execution Trace — step-through and diff visualization](./Screenshot-ui-frontend-trace.png)

---

## Why Corn

| | Traditional Rewrite | LLM Translation | **Corn** |
|---|---|---|---|
| **Deterministic** | Manual | No | **Yes** |
| **Auditable** | Depends on team | No | **Full trace** |
| **Reproducible** | No | No | **Every time** |
| **Cost** | $50-100/LOC | Unknown | **$3-5/LOC** |
| **Timeline** | Years | Months + rework | **Weeks to months** |
| **Regulatory ready** | Manual evidence | Not accepted | **Built-in compliance** |

---

## Platform Capabilities

### Implemented Today

- **Standards-oriented COBOL parser** targeting ANSI-85 with 90.1% NIST CCVS85 conformance (374/415 tests passing, 7 categories at 85%+, 5 at 100%)
- **Deterministic Java code generation** — same input always produces same output
- **Full pipeline**: parse, IR, generate, compile, execute, validate
- **31 COBOL statement types** including arithmetic, control flow, file I/O, string operations, INSPECT, SORT/MERGE, and inter-program communication
- **508-compliant demo UI** with real-time translation, analysis, portfolio planning, and cost estimation
- **REST API server** for integration into existing workflows
- **Execution-based validation** against expected output fixtures

### Supported COBOL Statements

| Category | Statements |
|----------|-----------|
| **Arithmetic** | `ADD`, `SUBTRACT`, `MULTIPLY`, `DIVIDE`, `COMPUTE` (with `ROUNDED`, `ON SIZE ERROR`, `GIVING`, `CORRESPONDING`, Format 1 & 2) |
| **Control Flow** | `IF`/`ELSE`, `EVALUATE`/`WHEN`, `PERFORM` (simple, `UNTIL`, `VARYING`, `TIMES`, `TEST BEFORE`/`AFTER`), `GO TO`, `STOP RUN`, `EXIT`, `GOBACK`, `NEXT SENTENCE`, `CONTINUE` |
| **Data Movement** | `MOVE` (including `CORRESPONDING`), `INITIALIZE`, `SET` |
| **I/O** | `DISPLAY`, `ACCEPT`, `OPEN`, `CLOSE` (with `LOCK`/`NO REWIND`), `READ`, `WRITE` (with `ADVANCING`, `AT END-OF-PAGE`), `REWRITE`, `DELETE`, `START` (with `NOT INVALID KEY`) |
| **String** | `STRING` (multi-source), `UNSTRING`, `INSPECT` (`TALLYING`, `REPLACING`, `CONVERTING`, combined) |
| **Program** | `CALL` (with `ON EXCEPTION`/`ON OVERFLOW`), `CANCEL`, `SEARCH`, `SORT`, `MERGE`, `RELEASE`, `RETURN`, `ALTER` |

---

## Screenshots — CLI

### CLI Help

![Corn CLI help](./screenshot-cli-help.png)

### Translate Flow

Translating 9 COBOL programs to Java with zero errors:

![Corn translate flow](./screenshot-translate-validate.png)

### Validation Pipeline

Parse, generate, compile, execute — fully automated:

![Corn validation](./screenshot-translate-validate%2002.png)

### Analyzer

JSON-based analysis report for every program:

![Corn analyzer](./screenshot-Analyzer.png)

---

## Sample Translation

**COBOL input:**
```cobol
       IDENTIFICATION DIVISION.
       PROGRAM-ID. ARITHMETIC.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  WS-A       PIC 9(5) VALUE 100.
       01  WS-B       PIC 9(5) VALUE 50.
       01  WS-RESULT  PIC 9(5) VALUE 0.
       PROCEDURE DIVISION.
           ADD WS-A TO WS-B GIVING WS-RESULT.
           DISPLAY "RESULT: " WS-RESULT.
           STOP RUN.
```

**Generated Java:**
```java
package com.generated.cobol;

public class Arithmetic {
    private Decimal wsA = Decimal.of("100");
    private Decimal wsB = Decimal.of("50");
    private Decimal wsResult = Decimal.zero();

    public void run() { mainPara(); }

    private void mainPara() {
        wsResult = wsA.add(wsB);
        System.out.println("RESULT: " + wsResult);
        return;
    }

    public static void main(String[] args) { new Arithmetic().run(); }
}
```

---

## Quick Start

### Run the Demo UI

```bash
corn-demo-server
# Open http://localhost:8085
```

### Run the CLI

```bash
corn-cobol-to-java --help

# Translate COBOL to Java
corn-cobol-to-java translate ./cobol \
  --output ./output/java --codegen-level 2

# Validate the full pipeline (parse → generate → compile → execute)
corn-cobol-to-java validate ./cobol \
  --output ./corn-validation

# Analyze COBOL source
corn-cobol-to-java analyze ./cobol
```

---

## Processing Model

```text
COBOL source (.cbl)
  → source normalization
  → deterministic COBOL analysis
  → internal program representation
  → Java source generation
  → validation-ready Java output
```

### Repository Structure

```text
corn-cobol-to-java/
  demo-ui/           Evaluation UI
  docs/              Product, compliance, and diligence documents
  modules/           Compiler, runtime, CLI, and server implementation
  samples/           Sample COBOL programs
```

---

## NIST CCVS85 Conformance

The parser is validated against the US government **NIST CCVS85** COBOL-85 compiler conformance test suite — 415 programs across 14 categories. This is the same test suite used to certify production COBOL compilers.

| Category | Pass | Total | Rate |
|----------|------|-------|------|
| IC (Inter-program Communication) | 47 | 47 | **100%** |
| IF (Intrinsic Functions) | 45 | 45 | **100%** |
| SM (Source Management) | 13 | 13 | **100%** |
| RL (Relative I/O) | 26 | 26 | **100%** |
| IX (Indexed I/O) | 29 | 29 | **100%** |
| SQ (Sequential I/O) | 78 | 84 | **92.9%** |
| ST (Sort/Merge) | 23 | 25 | **92.0%** |
| SG (Segmentation) | 12 | 13 | **92.3%** |
| NC (Nucleus) | 84 | 95 | **88.4%** |
| DB (Debug) | 13 | 15 | **86.7%** |
| OB (Obsolete) | 4 | 7 | 57.1% |
| RW (Report Writer) | 0 | 6 | 0.0% |
| CM (Communication) | 0 | 9 | 0.0% |
| EX (EXEC) | 0 | 1 | 0.0% |
| **Total** | **374** | **415** | **90.1%** |

> These results measure successful parse + Java code generation. Conformance rate is actively improving with each release.

---

## Compliance & Standards

- **Section 508 / WCAG 2.1 AA** — Demo UI is accessibility-compliant with ARIA labels, keyboard navigation, skip links, and sufficient color contrast
- **NIST CCVS85** — Parser validated against the US government COBOL-85 compiler conformance test suite
- **NIST SP 800-218 (SSDF)** — Secure software development practices followed throughout
- **Zero copyleft dependencies** — No GPL/LGPL/AGPL/SSPL/Commons Clause in production scope
- **Clean-room IP policy** — No copied code from proprietary or copyleft COBOL implementations
- **SBOM generation** — bill of materials generated on every build

---

## Roadmap

| Phase | Focus | Status |
|-------|-------|--------|
| **Core Pipeline** | Parse, IR, codegen, runtime, CLI | Shipped |
| **Demo Platform** | Web UI, REST API, portfolio/cost tools | Shipped |
| **NIST 75%+** | Grammar expansion, nested programs, EXTERNAL/GLOBAL | Achieved (77.1%) |
| **NIST 85%+** | Intrinsic functions, COPY/REPLACE preprocessing | Achieved (85.5%) |
| **NIST 90%+** | Abbreviated conditions, compound relational operators, qualified refs | Achieved (90.1%) |
| **Semantic Analysis** | Type checking, data flow, dead code detection | Private Repo |
| **Enterprise Features** | EXEC CICS, EXEC SQL, COMP-3 dialects, multi-program | Private Repo |
| **Production Platform** | Rust-based high-performance engine, cloud deployment | Private Repo |

---

## Repository Layout

```text
corn-cobol-to-java/
  demo-ui/           Web-based demo UI (HTML/CSS/JS)
  docs/              Product, compliance, and diligence documents
  modules/           Compiler, runtime, CLI, and server implementation
  samples/           Sample COBOL programs
  README.md
  LICENSE
  pom.xml
```

## Documentation

- [Value Proposition](docs/VALUE_PROPOSITION.md)
- [IP Provenance](docs/IP_PROVENANCE.md)
- [Dependency Policy](docs/DEPENDENCY_POLICY.md)
- [Third-Party Notices](THIRD_PARTY_NOTICES.md)

---

## Licensing

This repository is distributed under the **Corn Evaluation License** for non-production evaluation use. See [LICENSE](LICENSE) for the full terms.

For production licensing, enterprise agreements, or partnership inquiries:

**Cornmeister LLC** | `sekacorn@gmail.com`

---

*Built by Cornmeister LLC. Maryland, USA.*

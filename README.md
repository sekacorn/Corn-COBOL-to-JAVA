# Corn COBOL-to-Java Compiler

**Enterprise COBOL-to-Java modernization for organizations that cannot afford risky rewrites, vendor lock-in, or non-deterministic AI conversions.**

[![CI](https://github.com/sekacorn/corn-cobol-to-java/actions/workflows/ci.yml/badge.svg)](https://github.com/sekacorn/corn-cobol-to-java/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Evaluation-blue.svg)](LICENSE)
[![NIST Conformance](https://img.shields.io/badge/NIST%20CCVS85-90.1%25-brightgreen.svg)](#nist-ccvs85-translation-coverage)

---

## Mainframe modernization should not require a billion-dollar rewrite.

Banks, insurers, government agencies, and large enterprises still depend on COBOL systems that process critical financial, operational, and public-sector workloads.

The problem is not that COBOL failed.

The problem is that the business knowledge locked inside COBOL is too valuable to abandon, too risky to rewrite manually, and too important to hand to a black-box AI translator that cannot produce repeatable results.

**Corn** is a deterministic COBOL-to-Java compiler designed for organizations that need modernization with:

- repeatable translation
- auditability
- validation evidence
- traceability
- Java-based modernization paths
- enterprise licensing control
- reduced dependency on legacy mainframe vendors

Corn is not a prompt.

Corn is not a one-off script.

Corn is a compiler pipeline for serious COBOL modernization.

---

## What Corn Does

Corn translates COBOL into Java through a deterministic compiler pipeline.

```text
COBOL source
  → source normalization
  → COBOL parsing
  → internal representation
  → Java source generation
  → compile / execute / validate pipeline
```

The same COBOL input produces the same Java output every time.

That matters when the system being migrated handles money, claims, records, taxes, benefits, logistics, or regulated data.

---

## Why This Matters

Traditional COBOL modernization is expensive because enterprises are often forced into one of three bad options:

1. **Keep paying for legacy mainframe dependency**
2. **Fund a risky manual rewrite**
3. **Use AI-generated code with weak repeatability and limited audit evidence**

Corn introduces a fourth path:

> deterministic COBOL-to-Java modernization with validation, traceability, and enterprise control.

---

## See It In Action

### Translate — COBOL In, Java Out

Paste any COBOL program. Get Java output in seconds. Side-by-side view with copy and download.

![Corn Translate — COBOL to Java side-by-side](./Screenshot-ui-frontend.png)

### Analyze — Know What You're Working With

Automatic complexity scoring, statement counts, division breakdown, feature detection, and parse diagnostics. Understand every program before you migrate it.

![Corn Analyze — diagnostics, metrics, and feature detection](./Screenshot-ui-frontend-analyze.png)

### Portfolio — Plan Your Migration Waves

See your COBOL portfolio at a glance. Programs are grouped into migration waves by complexity and risk so teams can identify what moves first and what requires deeper review.

![Corn Portfolio — migration waves, risk assessment, effort breakdown](./Screenshot-ui-frontend-portfolio.png)

### Cost Estimator — Build the Business Case

Configure team rates and portfolio size. Estimate translation, review, remediation, testing, and deployment effort for executive planning and modernization budget discussions.

![Corn Cost Estimator — phase-by-phase cost and duration](./Screenshot-ui-frontend-cost.png)

### Execution Trace — Prove Equivalence

Step-through execution trace with variable state tracking. Side-by-side COBOL/Java diff visualization for review, remediation, and audit evidence.

![Corn Execution Trace — step-through and diff visualization](./Screenshot-ui-frontend-trace.png)

---

## Why Corn

| Capability | Traditional Rewrite | LLM Translation | Corn |
|---|---:|---:|---:|
| Deterministic output | No | No | **Yes** |
| Repeatable translation | Depends on team | No | **Yes** |
| Audit trail | Manual | Weak | **Built in** |
| COBOL-aware parsing | Human-dependent | Prompt-dependent | **Compiler-based** |
| Portfolio analysis | Manual | Limited | **Built in** |
| Validation pipeline | Separate effort | Weak | **Integrated** |
| Enterprise licensing control | Vendor-dependent | Unclear | **Yes** |
| Mainframe exit strategy | Slow | Risky | **Structured** |

---

## Built for Enterprise Modernization

Corn is designed for modernization teams that need to answer real executive questions:

- What COBOL programs do we have?
- Which programs are simple enough to translate first?
- Which programs are risky?
- What Java output is produced?
- Can the translation be repeated?
- Can the generated Java compile?
- Can execution behavior be validated?
- Can we create evidence for auditors, regulators, and internal review boards?
- Can we reduce long-term dependency on legacy mainframe vendors?

Corn helps turn COBOL migration from a vague consulting project into a measurable software engineering pipeline.

---

## Platform Capabilities

### Implemented Today

- **Standards-oriented COBOL parser** targeting ANSI-85 with 90.1% NIST CCVS85 translation coverage
- **374 / 415 NIST CCVS85 programs passing parse + Java generation**
- **Deterministic Java code generation**
- **Full pipeline**: parse, IR, generate, compile, execute, validate
- **31 COBOL statement types**
- **508-compliant demo UI**
- **REST API server**
- **CLI workflow**
- **Portfolio analysis**
- **Migration cost estimation**
- **Execution-based validation against expected output fixtures**
- **Clean-room IP policy**
- **Zero copyleft production-scope dependencies**
- **SBOM generation**

---

## Supported COBOL Statements

| Category | Statements |
|----------|-----------|
| **Arithmetic** | `ADD`, `SUBTRACT`, `MULTIPLY`, `DIVIDE`, `COMPUTE` with support for `ROUNDED`, `ON SIZE ERROR`, `GIVING`, `CORRESPONDING`, Format 1 and Format 2 |
| **Control Flow** | `IF` / `ELSE`, `EVALUATE` / `WHEN`, `PERFORM`, `PERFORM UNTIL`, `PERFORM VARYING`, `PERFORM TIMES`, `TEST BEFORE`, `TEST AFTER`, `GO TO`, `STOP RUN`, `EXIT`, `GOBACK`, `NEXT SENTENCE`, `CONTINUE` |
| **Data Movement** | `MOVE`, `MOVE CORRESPONDING`, `INITIALIZE`, `SET` |
| **I/O** | `DISPLAY`, `ACCEPT`, `OPEN`, `CLOSE`, `READ`, `WRITE`, `REWRITE`, `DELETE`, `START` |
| **String Processing** | `STRING`, `UNSTRING`, `INSPECT TALLYING`, `INSPECT REPLACING`, `INSPECT CONVERTING` |
| **Program Operations** | `CALL`, `CANCEL`, `SEARCH`, `SORT`, `MERGE`, `RELEASE`, `RETURN`, `ALTER` |

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

    public static void main(String[] args) {
        new Arithmetic().run();
    }
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

# Validate the full pipeline
corn-cobol-to-java validate ./cobol \
  --output ./corn-validation

# Analyze COBOL source
corn-cobol-to-java analyze ./cobol
```

---

## Repository Structure

```text
corn-cobol-to-java/
  demo-ui/           Evaluation UI
  docs/              Product, compliance, and diligence documents
  modules/           Compiler, runtime, CLI, and server implementation
  samples/           Sample COBOL programs
  README.md
  LICENSE
  pom.xml
```

---

## NIST CCVS85 Translation Coverage

Corn is validated against the US government NIST CCVS85 COBOL-85 compiler conformance test suite.

Current public evaluation coverage:

| Category | Pass | Total | Rate |
|----------|------|-------|------|
| IC — Inter-program Communication | 47 | 47 | **100%** |
| IF — Intrinsic Functions | 45 | 45 | **100%** |
| SM — Source Management | 13 | 13 | **100%** |
| RL — Relative I/O | 26 | 26 | **100%** |
| IX — Indexed I/O | 29 | 29 | **100%** |
| SQ — Sequential I/O | 78 | 84 | **92.9%** |
| ST — Sort / Merge | 23 | 25 | **92.0%** |
| SG — Segmentation | 12 | 13 | **92.3%** |
| NC — Nucleus | 84 | 95 | **88.4%** |
| DB — Debug | 13 | 15 | **86.7%** |
| OB — Obsolete | 4 | 7 | 57.1% |
| RW — Report Writer | 0 | 6 | 0.0% |
| CM — Communication | 0 | 9 | 0.0% |
| EX — EXEC | 0 | 1 | 0.0% |
| **Total** | **374** | **415** | **90.1%** |

> These results measure successful parse + Java code generation. Runtime semantic equivalence is validated separately where expected-output fixtures are available.

This distinction matters.

Corn does not claim that every COBOL program in the world can be blindly converted without review. Enterprise modernization still requires validation, testing, remediation, and production acceptance.

Corn’s value is that it gives modernization teams a deterministic compiler pipeline instead of starting from a blank rewrite.

---

## Compliance & Standards

- **NIST CCVS85** — COBOL-85 translation coverage measured against the government compiler conformance suite
- **Section 508 / WCAG 2.1 AA** — Demo UI includes accessibility support
- **NIST SP 800-218 SSDF alignment** — Secure software development practices
- **Zero copyleft dependencies in production scope**
- **Clean-room IP policy**
- **SBOM generation**
- **Dependency policy for enterprise diligence**
- **Third-party notices included**

---

## Current Limitations

This public repository is an evaluation edition.

Known limitations:

- Production use requires a separate commercial license.
- Public NIST results currently measure parse + Java generation coverage.
- Full enterprise semantic analysis is maintained in the private platform.
- Advanced enterprise features such as EXEC CICS, EXEC SQL, COMP-3 dialect expansion, JCL integration, and high-performance Rust engine work are maintained separately.
- Generated Java should be reviewed, tested, and validated before production use.
- This repository is not open-source software. It is distributed under an evaluation license.

---

## Roadmap

| Phase | Focus | Status |
|-------|-------|--------|
| **Core Pipeline** | Parse, IR, code generation, runtime, CLI | Shipped |
| **Demo Platform** | Web UI, REST API, portfolio tools, cost tools | Shipped |
| **NIST 75%+** | Grammar expansion, nested programs, EXTERNAL / GLOBAL | Achieved |
| **NIST 85%+** | Intrinsic functions, COPY / REPLACE preprocessing | Achieved |
| **NIST 90%+** | Abbreviated conditions, compound relational operators, qualified references | Achieved |
| **Semantic Analysis** | Type checking, data flow, dead code detection | Private platform |
| **Enterprise Features** | EXEC CICS, EXEC SQL, COMP-3 dialects, multi-program analysis | Private platform |
| **Production Platform** | Rust-based high-performance engine and cloud deployment | Private platform |

---

## Documentation

- [Value Proposition](docs/VALUE_PROPOSITION.md)
- [IP Provenance](docs/IP_PROVENANCE.md)
- [Dependency Policy](docs/DEPENDENCY_POLICY.md)
- [Third-Party Notices](THIRD_PARTY_NOTICES.md)

---

## Licensing

This repository is distributed under the **Corn Evaluation License** for non-production evaluation use.

See [LICENSE](LICENSE) for the full terms.

Production use, commercial deployment, generated Java use in production, enterprise evaluation, partnership discussions, or acquisition inquiries require a separate written agreement with Cornmeister LLC.

---

## Enterprise Licensing & Partnerships

Corn is designed for organizations evaluating large-scale COBOL modernization, mainframe exit planning, Java migration, portfolio assessment, and compiler-based modernization workflows.

For production licensing, enterprise agreements, pilots, partnerships, or acquisition inquiries:

**Cornmeister LLC**  
Maryland, USA  
`sekacorn@gmail.com`

---

## Strategic Position

Corn exists because critical systems deserve better than risky rewrites and non-repeatable AI-generated conversions.

The future of COBOL modernization should be:

- deterministic
- auditable
- testable
- explainable
- commercially controlled
- enterprise-ready

That is what Corn is being built to deliver.

---

*Built by Cornmeister LLC. Maryland, USA.*

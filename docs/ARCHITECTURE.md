# Corn COBOL-to-Java Compiler - Architecture Overview

This document intentionally avoids disclosing low-level implementation choices,
tooling, generated parser details, or proprietary translation techniques.

## Public Processing Model

```text
COBOL source
  -> source normalization
  -> deterministic COBOL analysis
  -> internal program representation
  -> maintainable Java source generation
  -> validation-ready output
```

## Product Responsibilities

- Parse supported COBOL source deterministically.
- Preserve source traceability for review and audit workflows.
- Represent program structure internally before Java generation.
- Generate maintainable Java for the supported COBOL subset.
- Provide runtime semantics needed by generated Java.
- Validate generated Java through compile and execution checks.
- Report diagnostics, feature usage, and migration-relevant metrics.

## Public Scope

The current evaluation edition supports a bounded COBOL subset, code generation
level `2`, generated Java execution validation, CLI workflows, and the demo UI.

COPY/REPLACE preprocessing, richer semantic analysis, advanced enterprise
dialects, embedded transaction processing, embedded SQL, and broader production
platform capabilities are roadmap or private-repository areas.

## Clean IP Boundary

Corn is proprietary clean-room IP. Do not publish internal grammar structure,
translation rules, parser generator details, code templates, or implementation
diagrams in public-facing materials. Use the IP provenance and dependency policy
documents for controlled diligence review.

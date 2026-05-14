# Dependency and License Policy

Corn is proprietary commercial software. Dependency choices must preserve the
ability to license the compiler, runtime, and generated Java commercially
without GPL-family or source-available contamination.

## Allowed by Default

- Apache-2.0
- BSD-2-Clause
- BSD-3-Clause
- MIT
- ISC
- Unicode-DFS-2016
- Public domain equivalents such as CC0-1.0 or Unlicense, after review
- Corn Evaluation License for internal Corn modules

## Requires Review

- EPL-1.0 or EPL-2.0
- MPL-2.0
- CDDL
- Any dependency with multiple license choices
- Any dependency used by `modules/runtime-java`
- Any dependency whose license is reported as a URL, custom text, or non-SPDX name

## Forbidden

- GPL
- LGPL
- AGPL
- SSPL
- Commons Clause
- BSL / Business Source License
- PolyForm noncommercial or other field-of-use-restricted licenses
- Unknown, missing, or ambiguous licenses

## Runtime Rule

`modules/runtime-java` must remain dependency-free unless there is a deliberate
architecture decision and written approval. Generated Java imports this runtime,
so its license posture is part of the product surface customers inherit.

## CI Enforcement

CI generates a CycloneDX SBOM and runs `scripts/check-license-sbom.py`. The scan
fails when a component has a forbidden license or no recognizable license.

The scan is a guardrail, not a legal opinion. Keep `THIRD_PARTY_NOTICES.md`
current and review new dependencies before merging.

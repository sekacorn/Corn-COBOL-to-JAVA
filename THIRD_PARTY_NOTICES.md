# Third-Party Notices

Corn COBOL-to-Java Compiler is proprietary software distributed under the
Corn Evaluation License. This file records the third-party packages currently
used by the repository and the licenses under which they are used.

Corn policy forbids GPL, LGPL, AGPL, SSPL, Commons Clause, unknown-license, and
other copyleft or source-available-restriction dependencies in production or
runtime scope.

## Production and Runtime Dependencies

| Component | Version | Scope | License | Notes |
|---|---:|---|---|---|
| ANTLR 4 Runtime (`org.antlr:antlr4-runtime`) | 4.13.1 | Parser runtime | BSD-3-Clause | Used to run the Corn-authored COBOL grammar. |
| Jackson Annotations (`com.fasterxml.jackson.core:jackson-annotations`) | 2.16.0 | IR/JSON | Apache-2.0 | JSON annotations. |
| Jackson Core (`com.fasterxml.jackson.core:jackson-core`) | 2.15.3 | IR/JSON | Apache-2.0 | Transitive Jackson JSON core. |
| Jackson Databind (`com.fasterxml.jackson.core:jackson-databind`) | 2.16.0 | IR/JSON/server/CLI | Apache-2.0 | JSON serialization and reports. |
| Jackson Datatype JDK8 (`com.fasterxml.jackson.datatype:jackson-datatype-jdk8`) | 2.16.0 | IR/JSON | Apache-2.0 | Java 8+ datatype support. |
| Jackson Datatype JSR310 (`com.fasterxml.jackson.datatype:jackson-datatype-jsr310`) | 2.16.0 | IR/JSON | Apache-2.0 | Java time datatype support. |
| Picocli (`info.picocli:picocli`) | 4.7.5 | CLI | Apache-2.0 | Command-line parser. |
| SLF4J API (`org.slf4j:slf4j-api`) | 2.0.9 | Logging facade | MIT | API only; no copyleft logging backend is included. |

## Test-Only Dependencies

| Component | Version | License | Notes |
|---|---:|---|---|
| AssertJ (`org.assertj:assertj-core`) | 3.24.2 | Apache-2.0 | Fluent test assertions. |
| Byte Buddy (`net.bytebuddy:*`) | Managed by Mockito | Apache-2.0 | Mockito transitive dependency. |
| JUnit Jupiter (`org.junit.jupiter:*`) | 5.10.1 | EPL-2.0 | Unit and integration tests. |
| JUnit Platform (`org.junit.platform:*`) | Managed by JUnit | EPL-2.0 | Test launcher/platform. |
| Mockito (`org.mockito:*`) | 5.7.0 | MIT | Test doubles. |
| Objenesis (`org.objenesis:objenesis`) | Mockito transitive | Apache-2.0 | Mockito transitive dependency. |

## Build Tooling

Maven plugins used during local builds and CI include Apache Maven plugins,
ANTLR Maven Plugin, JaCoCo, Failsafe/Surefire, Shade, Javadoc, Source, and
CycloneDX. Build tooling is not bundled into generated Java output.

## Runtime Dependency Boundary

`modules/runtime-java` is intentionally dependency-free. Generated Java imports
Corn runtime classes, so this module must remain free of copyleft dependencies
and preferably free of external dependencies altogether.

## Maintenance

When dependencies change:

1. Run `mvn -q -DskipTests package` to regenerate the CycloneDX SBOM.
2. Run `python scripts/check-license-sbom.py target/corn-cobol-to-java-bom.json`.
3. Update this notice file with any approved new dependency.
4. Reject any GPL, LGPL, AGPL, SSPL, Commons Clause, unknown, or ambiguous license.

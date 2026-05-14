# IP Provenance and Clean-Room Policy

Corn COBOL-to-Java Compiler is intended to be proprietary, clean-room
intellectual property owned by Cornmeister LLC.

## Clean-Room Statement

No code has been copied from IBM, Micro Focus, GnuCOBOL, OpenCOBOL, Koopa,
ProLeap, or any other COBOL compiler, parser, transpiler, modernization tool,
runtime, copybook processor, or related implementation.

The Corn compiler, runtime libraries, code generators, parser visitors,
translation rules, generated-code templates, test fixtures, and documentation
are intended to be original works, built from public language standards,
independent engineering, and Corn-authored source code.

## Competitive Positioning

Corn may be described as a COBOL modernization compiler for organizations that
need an alternative to established mainframe modernization vendors.

Do not state or imply that Corn is endorsed by, affiliated with, certified by,
or built from proprietary materials belonging to IBM, Micro Focus, Broadcom,
GnuCOBOL/OpenCOBOL, Koopa, ProLeap, or any other vendor or open-source project.

Product and vendor names may be used only for factual identification of dialects,
input environments, migration targets, or competitive landscape.

## Dependency Policy

Corn production dependencies must use permissive or commercially compatible
licenses. The project forbids production or runtime dependencies under GPL,
LGPL, AGPL, SSPL, Commons Clause, unknown licenses, or other licenses that impose
copyleft, network copyleft, field-of-use restrictions, or source-available
commercial restrictions.

The generated Java path is especially sensitive. Generated Java currently
depends on `modules/runtime-java`, so that module must remain dependency-free or
limited to explicitly approved permissive dependencies.

## Source Inputs

The project may use customer-owned or sample COBOL programs as parser/codegen
inputs only when the contributor has the right to provide them. Do not import
copyrighted vendor conformance suites, proprietary customer applications, or
third-party sample corpora unless the license and provenance are reviewed first.

## Contributor Rules

Contributors must not paste, translate, adapt, or mechanically port code from
third-party COBOL implementations or vendor documentation examples unless the
license expressly permits the intended proprietary use and the contribution is
reviewed.

Contributors should document any new dependency, generated artifact, grammar
source, test corpus, or algorithmic reference in the relevant pull request.

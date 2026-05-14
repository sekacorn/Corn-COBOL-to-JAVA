#!/usr/bin/env python3
"""Fail the build when the CycloneDX SBOM contains forbidden or unknown licenses."""

from __future__ import annotations

import json
import sys
from pathlib import Path


FORBIDDEN_MARKERS = (
    "GPL",
    "LGPL",
    "AGPL",
    "SSPL",
    "COMMONS CLAUSE",
    "BUSINESS SOURCE",
    "BUSL",
    "BSL",
    "POLYFORM",
    "NONCOMMERCIAL",
)

ALLOWED_LICENSES = {
    "Apache-2.0",
    "BSD-2-Clause",
    "BSD-3-Clause",
    "MIT",
    "ISC",
    "EPL-1.0",
    "EPL-2.0",
    "CDDL-1.0",
    "CDDL-1.1",
    "MPL-2.0",
    "CC0-1.0",
    "Unlicense",
    "Corn Evaluation License (90-day, non-production)",
}


def component_name(component: dict) -> str:
    group = component.get("group")
    name = component.get("name", "<unnamed>")
    version = component.get("version", "")
    prefix = f"{group}:" if group else ""
    suffix = f":{version}" if version else ""
    return f"{prefix}{name}{suffix}"


def license_names(component: dict) -> list[str]:
    result: list[str] = []
    for entry in component.get("licenses", []):
        lic = entry.get("license") or {}
        expression = entry.get("expression")
        name = lic.get("id") or lic.get("name") or expression
        if name:
            result.append(str(name).strip())
    return result


def main() -> int:
    if len(sys.argv) != 2:
        print("Usage: check-license-sbom.py <cyclonedx-bom.json>", file=sys.stderr)
        return 2

    sbom_path = Path(sys.argv[1])
    if not sbom_path.is_file():
        print(f"SBOM not found: {sbom_path}", file=sys.stderr)
        return 2

    data = json.loads(sbom_path.read_text(encoding="utf-8"))
    failures: list[str] = []

    for component in data.get("components", []):
        name = component_name(component)
        licenses = license_names(component)

        if not licenses:
            failures.append(f"{name}: missing license")
            continue

        for license_name in licenses:
            normalized = license_name.upper()
            if any(marker in normalized for marker in FORBIDDEN_MARKERS):
                failures.append(f"{name}: forbidden license '{license_name}'")
            elif license_name not in ALLOWED_LICENSES:
                failures.append(f"{name}: unknown or unapproved license '{license_name}'")

    if failures:
        print("License scan failed:")
        for failure in failures:
            print(f"  - {failure}")
        return 1

    print(f"License scan passed for {len(data.get('components', []))} SBOM components.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

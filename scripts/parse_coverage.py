"""Parse Kover XML reports and emit the line-coverage percentage.

Usage:
    python3 scripts/parse_coverage.py <glob-pattern>

Example:
    python3 scripts/parse_coverage.py "build/reports/kover/report.xml"

Output (stdout):
    42.3
"""

import glob
import sys
import xml.etree.ElementTree as ET


def parse_kover_xmls(pattern: str) -> tuple[int, int]:
    """Sum LINE counters from report-level <counter> in each Kover XML."""
    covered, missed = 0, 0
    paths = glob.glob(pattern, recursive=True)
    if not paths:
        raise FileNotFoundError(f"No coverage reports matched: {pattern}")
    for path in paths:
        root = ET.parse(path).getroot()
        # findall fetches only direct children of <report> — the report-level aggregate
        for counter in root.findall("counter"):
            if counter.get("type") == "LINE":
                covered += int(counter.get("covered", 0))
                missed += int(counter.get("missed", 0))
    return covered, missed


def main() -> None:
    if len(sys.argv) < 2:
        print("Usage: parse_coverage.py <glob-pattern>", file=sys.stderr)
        sys.exit(1)

    try:
        covered, missed = parse_kover_xmls(sys.argv[1])
    except FileNotFoundError as exc:
        print(str(exc), file=sys.stderr)
        sys.exit(2)
    total = covered + missed
    pct = round(covered / total * 100, 1) if total else 0.0
    print(pct)


if __name__ == "__main__":
    main()

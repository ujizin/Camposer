"""Parse JaCoCo XML reports and emit a shields.io endpoint JSON.

Usage:
    python3 scripts/parse_coverage.py <glob-pattern>

Example:
    python3 scripts/parse_coverage.py "**/reports/jacoco/jacocoHostTestReport/*.xml"

Output (stdout):
    {"schemaVersion":1,"label":"coverage","message":"42.3%","color":"yellow"}
"""

import glob
import json
import sys
import xml.etree.ElementTree as ET


def parse_jacoco_xmls(pattern: str) -> tuple[int, int]:
    """Sum LINE counters from report-level <counter> in each JaCoCo XML."""
    covered, missed = 0, 0
    paths = glob.glob(pattern, recursive=True)
    if not paths:
        return 0, 0
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

    covered, missed = parse_jacoco_xmls(sys.argv[1])
    total = covered + missed
    pct = round(covered / total * 100, 1) if total else 0.0

    color = "brightgreen" if pct >= 80 else "yellow" if pct >= 60 else "red"
    print(json.dumps({
        "schemaVersion": 1,
        "label": "coverage",
        "message": f"{pct}%",
        "color": color,
    }))


if __name__ == "__main__":
    main()

# Kover + KMP Coverage Design

**Date:** 2026-05-18
**Branch:** feat/kover
**Modules in scope:** `:camposer`, `:camposer-code-scanner`

---

## Goal

- Unified coverage badge in README (one number, live-updating)
- Non-blocking quality gate: CI warns when coverage drops below threshold but never blocks merge
- No external service — self-contained within GitHub ecosystem

---

## Architecture

```
coverage.yml  (macos-latest, single job)
│
├── Prerequisite: camposer has withHostTestBuilder {} (see Build Config)
│
├── ./gradlew koverXmlReport
│   → camposer/build/reports/kover/report*.xml
│   → camposer-code-scanner/build/reports/kover/report*.xml
│   (JVM host tests — commonTest compiled for JVM via withHostTestBuilder)
│   continue-on-error: true  ← warning gate, never blocks
│
├── ./gradlew iosSimulatorArm64Test
│   (Kotlin/Native, -coverage compiler flag on iosSimulatorArm64 test only)
│   → *.profraw  (LLVM format)
│
├── xcrun llvm-profdata merge *.profraw -o coverage.profdata
├── xcrun llvm-cov export <test.kexe>
│     --instr-profile=coverage.profdata
│     --format=lcov
│     --sources=iosMain   ← suffix match against debug-info absolute paths
│   → ios-coverage.lcov  (iosMain ONLY — no commonMain double-count)
│
├── python3 scripts/parse_coverage.py
│     <kover-xml-glob> <ios-coverage.lcov>
│   combined % = (kover_covered + ios_covered) / (kover_total + ios_total)
│
└── schneegans/dynamic-badges-action  (main push only)
    → GitHub Gist (camposer-coverage.json)
    → shields.io endpoint badge in README
```

### Coverage scope

| Source set | Tool | Measured |
|---|---|---|
| `commonMain` | Kover (JVM host test) | Yes |
| `androidMain` | Kover (JVM host test) | Yes (unit test path only) |
| `iosMain` | Kotlin/Native `-coverage` + llvm-cov | Yes |
| `androidDeviceTest` paths | — | No (on-device, no JVM agent possible) |

`androidDeviceTest` intentionally excluded. It tests CameraX hardware integration — `commonTest` via Kover already covers the state machine and business logic where unit coverage is meaningful.

---

## Build Configuration

### `gradle/libs.versions.toml`

```toml
[versions]
kover = "0.9.1"

[plugins]
kover = { id = "org.jetbrains.kotlinx.kover", version.ref = "kover" }
```

### `build.gradle.kts` (root)

Add alongside existing plugin declarations:

```kotlin
alias(libs.plugins.kover) apply false
```

### `camposer/build.gradle.kts`

**Prerequisite change** — add `withHostTestBuilder {}` so `commonTest` has a JVM compilation that Kover can instrument. Without this, `koverXmlReport` produces zero counters for `:camposer` (currently `commonTest` only compiles into `androidDeviceTest` and `iosSimulatorArm64Test`, neither of which Kover can instrument).

```kotlin
androidLibrary {
  // existing config ...
  withHostTestBuilder {}   // ← add this
}
```

Then apply Kover:

```kotlin
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
  alias(libs.plugins.kover)
}

kover {
  reports {
    filters {
      excludes {
        packages("androidx.*", "*.BuildConfig")
      }
    }
    verify {
      rule {
        bound {
          minValue = 60        // baseline — revisit after first run
          metric = CoverageUnit.LINE
        }
      }
    }
  }
}
```

### `camposer-code-scanner/build.gradle.kts`

Already has `withHostTestBuilder {}`. Add same Kover plugin + config block as above.

### iOS `-coverage` flag (both modules)

Scope to `iosSimulatorArm64` test only — avoids unnecessary instrumentation overhead on `iosArm64`/`iosX64` compilations that never run in CI:

```kotlin
kotlin {
  iosSimulatorArm64 {
    compilations.getByName("test") {
      compilerOptions.configure {
        freeCompilerArgs.add("-coverage")
      }
    }
  }
}
```

---

## Aggregation Script

**`scripts/parse_coverage.py`**

```python
import xml.etree.ElementTree as ET
import glob
import sys

def parse_kover_xmls(pattern):
    """Sum LINE counters from the report-level counter in each Kover XML."""
    covered, total = 0, 0
    for path in glob.glob(pattern, recursive=True):
        root = ET.parse(path).getroot()
        # Report-level counter is a direct child of <report> — last LINE entry
        for counter in root.findall("counter"):
            if counter.get("type") == "LINE":
                c = int(counter.get("covered", 0))
                m = int(counter.get("missed",  0))
                covered += c
                total   += c + m
    return covered, total

def parse_lcov(path):
    """Parse lcov DA: lines. Handles optional third field (LLVM block checksum)."""
    covered, total = 0, 0
    with open(path) as f:
        for line in f:
            if line.startswith("DA:"):
                parts = line.strip().split(",")
                hits  = parts[1]   # parts[2] may exist (LLVM checksum) — ignore
                total += 1
                if int(hits) > 0:
                    covered += 1
    return covered, total

kover_pattern = sys.argv[1]   # e.g. "**/kover/report*.xml"
lcov_file     = sys.argv[2]

a_cov, a_tot = parse_kover_xmls(kover_pattern)
b_cov, b_tot = parse_lcov(lcov_file)

total_lines   = a_tot + b_tot
if total_lines == 0:
    pct = 0.0
else:
    pct = round((a_cov + b_cov) / total_lines * 100, 1)

color = "brightgreen" if pct >= 80 else "yellow" if pct >= 60 else "red"
print(f'{{"schemaVersion":1,"label":"coverage","message":"{pct}%","color":"{color}"}}')
```

**Notes on the XML parsing**: `root.findall("counter")` fetches only direct children of `<report>`, which is the report-level aggregate LINE counter in Kover's JaCoCo-compatible XML — not method- or class-level counters. The glob pattern handles the actual output path (`reportDebug.xml` or `report.xml`) without hardcoding the variant name.

---

## CI Workflow

**`.github/workflows/coverage.yml`**

```yaml
name: Coverage

on:
  push:
    branches: [main]
    paths:
      - 'camposer/**'
      - 'camposer-code-scanner/**'
      - '.github/workflows/coverage.yml'
      - 'gradle/**'
      - 'gradle.properties'
      - '*.gradle.kts'
  pull_request:
    branches: ['*']
    paths:
      - 'camposer/**'
      - 'camposer-code-scanner/**'
      - '.github/workflows/coverage.yml'
      - 'gradle/**'
      - 'gradle.properties'
      - '*.gradle.kts'

concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true

jobs:
  coverage:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v6.0.1
      - uses: actions/setup-java@v5.1.0
        with:
          distribution: zulu
          java-version: 21

      - name: Make gradlew executable
        run: chmod +x ./gradlew

      - uses: gradle/actions/setup-gradle@v4

      - name: Kover XML (common + Android, JVM host tests)
        run: ./gradlew koverXmlReport
        continue-on-error: true

      - name: iOS tests with Kotlin/Native coverage
        run: ./gradlew iosSimulatorArm64Test

      - name: Export iOS lcov (iosMain only)
        run: |
          BINARY=$(find . -path "*/iosSimulatorArm64/debugTest/test.kexe" | head -1)

          # Guard: fail fast with clear diagnostic if no profraw produced
          PROFRAW_FILES=$(find . -name "*.profraw" | tr '\n' ' ')
          if [ -z "$PROFRAW_FILES" ]; then
            echo "ERROR: no .profraw files found — -coverage flag may not have applied"
            exit 1
          fi

          xcrun llvm-profdata merge -sparse $PROFRAW_FILES -o coverage.profdata

          # --sources uses a suffix matched against absolute paths in debug info.
          # "iosMain" matches paths like /home/runner/work/.../camposer/src/iosMain/...
          # Verify this against actual runner output on first run.
          xcrun llvm-cov export "$BINARY" \
            --instr-profile=coverage.profdata \
            --format=lcov \
            --sources=iosMain \
            > ios-coverage.lcov

      - name: Compute unified coverage
        id: coverage
        run: |
          JSON=$(python3 scripts/parse_coverage.py \
            "**/reports/kover/report*.xml" \
            ios-coverage.lcov)
          echo "json=$JSON" >> $GITHUB_OUTPUT

      - name: Update Gist badge
        if: github.ref == 'refs/heads/main'
        uses: schneegans/dynamic-badges-action@v1.7.0
        with:
          auth: ${{ secrets.GIST_TOKEN }}
          gistID: ${{ secrets.GIST_ID }}
          filename: camposer-coverage.json
          label: coverage
          message: ${{ fromJson(steps.coverage.outputs.json).message }}
          color: ${{ fromJson(steps.coverage.outputs.json).color }}
```

> Badge update only on `main` push — PRs compute coverage and show the job summary but don't overwrite the published badge.

> **`--sources=iosMain` note**: `llvm-cov` matches this suffix against the absolute source paths embedded in the binary's DWARF debug info. On first CI run, verify the filter is working (non-zero lcov output) using `xcrun llvm-cov report "$BINARY" --instr-profile=coverage.profdata` before filtering. Adjust the suffix if needed.

---

## README Badge

Add inside the existing `<p align="center">` badge block:

```html
<img src="https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/<user>/<GIST_ID>/raw/camposer-coverage.json">
```

Same visual style (flat, same height) as existing Maven Central and Platform badges.

---

## One-Time Manual Setup

> Not automated — done once by repo maintainer.

1. Create empty GitHub Gist at gist.github.com → copy the Gist ID
2. Create GitHub PAT with `gist` scope
3. Add two repo secrets:
   - `GIST_ID` — the Gist ID from step 1
   - `GIST_TOKEN` — the PAT from step 2
4. Replace `<user>` and `<GIST_ID>` in README badge URL

---

## Trade-offs

### Upsides

| What | Why it matters |
|---|---|
| No external service | No Codecov account, no vendor lock-in, no data leaving GitHub |
| True KMP coverage | `commonMain` via Kover (JVM host tests) + `iosMain` via Kotlin/Native `-coverage` |
| No double-counting | `--sources iosMain` suffix filter isolates iOS-specific files from lcov |
| Non-blocking gate | `continue-on-error: true` + Kover `verify` — visible warning, PR never killed |
| Badge auto-updates | Gist + shields.io endpoint — always reflects last `main` push |
| Dedicated workflow | `coverage.yml` separate from test workflows — failures don't pollute test status |
| Fits existing patterns | Plugin declared in root `apply false` + applied per-module = same as detekt |

### Downsides

| What | Mitigation |
|---|---|
| `withHostTestBuilder` prerequisite | Must be added to `:camposer` — without it Kover reports zero coverage |
| Kover misses `androidDeviceTest` | Acceptable — `commonMain` holds all testable logic |
| `-coverage` is Kotlin/Native experimental | Works today; may need adjustment on Kotlin bumps |
| Gist + PAT = one-time manual setup | Documented above; done once, never touched again |
| `macos-latest` = more expensive CI minutes | Scoped to `main` push + PRs only, path-filtered |
| No PR diff comment | Trade-off for zero external service dependency |
| `--sources` filter needs CI verification | First run: check llvm-cov produces non-zero lcov output |

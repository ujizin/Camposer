# Kover KMP Coverage Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add unified coverage badge + non-blocking quality gate to Camposer via Kover (Android/common) and Kotlin/Native `-coverage` (iOS), combined into one shields.io badge backed by a GitHub Gist.

**Architecture:** Kover instruments `commonTest` via a new JVM host test compilation in `:camposer`. iOS coverage uses Kotlin/Native's built-in `-coverage` flag, filtered to `iosMain` only to avoid double-counting. A Python script merges both XML+lcov reports into one percentage, stored in a GitHub Gist and displayed as a shields.io badge in README.

**Tech Stack:** Kover 0.9.1, Kotlin/Native LLVM coverage (`-coverage` flag), `xcrun llvm-profdata`/`llvm-cov`, Python 3 (stdlib only), GitHub Actions, GitHub Gist + `schneegans/dynamic-badges-action`, shields.io endpoint badge.

**Spec:** `docs/superpowers/specs/2026-05-18-kover-coverage-design.md`

---

## File Map

| File | Action | Responsibility |
|---|---|---|
| `gradle/libs.versions.toml` | Modify | Add `kover = "0.9.1"` version + plugin entry |
| `build.gradle.kts` | Modify | Declare Kover plugin `apply false` in root |
| `camposer/build.gradle.kts` | Modify | Add `withHostTestBuilder {}`, apply Kover, configure verify + filters, add iOS `-coverage` flag |
| `camposer-code-scanner/build.gradle.kts` | Modify | Apply Kover, configure verify + filters, add iOS `-coverage` flag |
| `scripts/parse_coverage.py` | Create | Merge Kover XML + lcov → unified % → JSON for shields.io |
| `.github/workflows/coverage.yml` | Create | CI: Kover → iOS tests → lcov export → parse → Gist badge |
| `README.md` | Modify | Add coverage badge in existing badge block |

---

## Chunk 1: Gradle Wiring

### Task 1: Add Kover to version catalog

**Files:**
- Modify: `gradle/libs.versions.toml`

- [ ] **Step 1: Add version + plugin entry**

Open `gradle/libs.versions.toml`. In the `[versions]` block, add after `detekt = "1.23.8"`:

```toml
kover = "0.9.1"
```

In the `[plugins]` block, add after `detekt = ...`:

```toml
kover = { id = "org.jetbrains.kotlinx.kover", version.ref = "kover" }
```

- [ ] **Step 2: Verify catalog parses**

```bash
./gradlew help --task tasks
```

Expected: no "unresolved reference" or TOML parse errors in output.

- [ ] **Step 3: Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "build(kover): add kover 0.9.1 to version catalog"
```

---

### Task 2: Declare Kover in root build

**Files:**
- Modify: `build.gradle.kts`

- [ ] **Step 1: Add root plugin declaration**

In `build.gradle.kts`, add inside the `plugins { }` block alongside the other `apply false` declarations:

```kotlin
alias(libs.plugins.kover) apply false
```

- [ ] **Step 2: Verify root sync**

```bash
./gradlew help
```

Expected: BUILD SUCCESSFUL — no unresolved reference errors.

- [ ] **Step 3: Commit**

```bash
git add build.gradle.kts
git commit -m "build(kover): declare kover plugin in root (apply false)"
```

---

### Task 3: Wire Kover into `:camposer`

**Files:**
- Modify: `camposer/build.gradle.kts`

This task has two parts: (a) add `withHostTestBuilder {}` so Kover has a JVM compilation to instrument, (b) apply and configure Kover.

**Why `withHostTestBuilder` is required**: without it, `commonTest` only compiles into `androidDeviceTest` (on-device, no JVM agent) and `iosSimulatorArm64Test` (Kotlin/Native). Kover's JVM agent cannot touch either. `withHostTestBuilder {}` creates a JVM-based host test compilation where `commonTest` code runs — this is what Kover instruments.

- [ ] **Step 1: Add `withHostTestBuilder {}` to `androidLibrary` block**

In `camposer/build.gradle.kts`, find the `androidLibrary { }` block. Add `withHostTestBuilder {}` as the first line inside the block, before `withDeviceTestBuilder`:

```kotlin
androidLibrary {
  namespace = "com.ujizin.camposer"
  compileSdk = Config.compileSdk
  minSdk = Config.minSdk

  withHostTestBuilder {}   // ← add this line

  withDeviceTestBuilder {
    sourceSetTreeName = "test"
    androidResources.enable = true
  }.configure { instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }

  compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}
```

- [ ] **Step 2: Add Kover import and plugin**

At the top of `camposer/build.gradle.kts`, add the import as the last import line (after `import ujizin.camposer.Config`):

```kotlin
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
```

> **Note:** The exact package path for `CoverageUnit` in Kover 0.9.1 should be verified after syncing. Run `./gradlew :camposer:dependencies` and inspect the Kover artifact to confirm. If the import fails, try `kotlinx.kover.gradle.plugin.dsl.metrics.CoverageUnit` as an alternative. The `metric` property inside `bound { }` may also be named `coverageUnits` in 0.9.x — verify via IDE autocomplete or the Kover 0.9.1 release notes before committing.

In the `plugins { }` block, add:

```kotlin
alias(libs.plugins.kover)
```

- [ ] **Step 3: Add Kover configuration block**

Append after the closing `}` of the `dokka { }` block (the last block in the file):

```kotlin
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
          minValue = 60
          metric = CoverageUnit.LINE
        }
      }
    }
  }
}
```

> **DSL note:** `kover { reports { } }` is the 0.8.x+ unified DSL. If Gradle sync fails with "unresolved reference: reports", the installed plugin version uses the older `koverReport { }` top-level extension instead — replace `kover { reports { ... } }` with `koverReport { ... }` (drop the `reports { }` wrapper). Similarly if `metric` is unresolved, try `coverageUnits`. Verify against the 0.9.1 changelog before assuming either form is correct.

- [ ] **Step 4: Add iOS `-coverage` flag**

Inside the existing `listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget -> ... }` block in `kotlin { }`, add the `-coverage` flag gated on target name. The block currently configures the framework — add the coverage flag after the framework configuration:

```kotlin
listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
  iosTarget.binaries.framework {
    baseName = "Camposer"
    isStatic = true
  }
  @Suppress("PropertyName")
  iosTarget.compilations.getByName("main") {
    val CMFormat by cinterops.creating
    val NSKeyValueObserving by cinterops.creating
  }
  // Add: instrument only the simulator test binary (runs in CI)
  if (iosTarget.name == "iosSimulatorArm64") {
    iosTarget.compilations.getByName("test").compilerOptions.configure {
      freeCompilerArgs.add("-coverage")
    }
  }
}
```

- [ ] **Step 5: Verify `:camposer` builds**

```bash
./gradlew :camposer:assemble
```

Expected: BUILD SUCCESSFUL. No unresolved reference to `CoverageUnit`.

- [ ] **Step 6: Verify Kover task exists**

```bash
./gradlew :camposer:tasks --group=verification | grep -i kover
```

Expected: `koverXmlReportDebug` appears in output (primary task name when AGP build variants are present). `koverXmlReport` may also appear as a lifecycle alias — either works.

- [ ] **Step 7: Run Kover report locally**

```bash
./gradlew :camposer:koverXmlReportDebug
```

Expected: BUILD SUCCESSFUL. If task not found, run `./gradlew :camposer:tasks --group=verification` to find the actual task name generated for this plugin combination.

Check report exists and has non-zero line counters:
```bash
grep 'type="LINE"' camposer/build/reports/kover/report*.xml | head -5
```

Expected: lines like `<counter type="LINE" missed="X" covered="Y"/>` with Y > 0.

- [ ] **Step 8: Apply spotless formatting**

```bash
./gradlew :camposer:spotlessApply
```

- [ ] **Step 9: Commit**

```bash
git add camposer/build.gradle.kts
git commit -m "build(kover): add withHostTestBuilder and Kover config to :camposer"
```

---

### Task 4: Wire Kover into `:camposer-code-scanner`

**Files:**
- Modify: `camposer-code-scanner/build.gradle.kts`

`:camposer-code-scanner` already has `withHostTestBuilder {}` — only the Kover plugin + iOS flag needed.

- [ ] **Step 1: Add Kover import and plugin**

At the top of `camposer-code-scanner/build.gradle.kts`, add as the last import line (after `import ujizin.camposer.Config`):

```kotlin
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
```

> Same DSL note as Task 3 Step 2: verify `CoverageUnit` package path after sync. Try `kotlinx.kover.gradle.plugin.dsl.metrics.CoverageUnit` if the primary import fails.

In the `plugins { }` block, add:

```kotlin
alias(libs.plugins.kover)
```

- [ ] **Step 2: Add Kover configuration block**

Append after the closing `}` of the `dokka { }` block (the last block in the file):

```kotlin
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
          minValue = 60
          metric = CoverageUnit.LINE
        }
      }
    }
  }
}
```

> Same DSL note as Task 3 Step 3: if `kover { reports { } }` fails, try `koverReport { }`.

- [ ] **Step 3: Add iOS `-coverage` flag**

Inside the existing `listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget -> ... }` block in `kotlin { }`, add the coverage flag gated on target name (same pattern as Task 3 Step 4):

```kotlin
listOf(
  iosX64(),
  iosArm64(),
  iosSimulatorArm64(),
).forEach { iosTarget ->
  iosTarget.binaries.framework {
    baseName = "CamposerCodeScannerKit"
    isStatic = true
  }
  // Add: instrument only the simulator test binary (runs in CI)
  if (iosTarget.name == "iosSimulatorArm64") {
    iosTarget.compilations.getByName("test").compilerOptions.configure {
      freeCompilerArgs.add("-coverage")
    }
  }
}
```

- [ ] **Step 4: Verify build**

```bash
./gradlew :camposer-code-scanner:assemble
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Run Kover report**

```bash
./gradlew :camposer-code-scanner:koverXmlReportDebug
```

Expected: BUILD SUCCESSFUL. (Report may have low coverage since no test files exist yet in this module — zero is valid.) If task not found, run `./gradlew :camposer-code-scanner:tasks --group=verification` to find the actual task name.

- [ ] **Step 6: Apply spotless**

```bash
./gradlew :camposer-code-scanner:spotlessApply
```

- [ ] **Step 7: Commit**

```bash
git add camposer-code-scanner/build.gradle.kts
git commit -m "build(kover): add Kover config and iOS -coverage flag to :camposer-code-scanner"
```

---

## Chunk 2: Script, CI Workflow, and README Badge

### Task 5: Write and verify `parse_coverage.py`

**Files:**
- Create: `scripts/parse_coverage.py`

- [ ] **Step 1: Create the script**

Create `scripts/parse_coverage.py`:

```python
import xml.etree.ElementTree as ET
import glob
import sys


def parse_kover_xmls(pattern):
    """Sum LINE counters from the report-level counter in each Kover XML.

    Uses root.findall("counter") — direct children of <report> only.
    These are the aggregate report-level LINE counters in Kover's
    JaCoCo-compatible XML, not method- or class-level counters.
    """
    covered, total = 0, 0
    for path in glob.glob(pattern, recursive=True):
        root = ET.parse(path).getroot()
        for counter in root.findall("counter"):
            if counter.get("type") == "LINE":
                c = int(counter.get("covered", 0))
                m = int(counter.get("missed", 0))
                covered += c
                total += c + m
    return covered, total


def parse_lcov(path):
    """Parse lcov DA: lines. Handles optional third field (LLVM block checksum)."""
    covered, total = 0, 0
    with open(path) as f:
        for line in f:
            if line.startswith("DA:"):
                parts = line.strip().split(",")
                hits = parts[1]  # parts[2] may exist (LLVM checksum) — ignore
                total += 1
                if int(hits) > 0:
                    covered += 1
    return covered, total


def coverage_json(pct):
    color = "brightgreen" if pct >= 80 else "yellow" if pct >= 60 else "red"
    return (
        f'{{"schemaVersion":1,"label":"coverage",'
        f'"message":"{pct}%","color":"{color}"}}'
    )


if __name__ == "__main__":
    kover_pattern = sys.argv[1]  # e.g. "**/reports/kover/report*.xml"
    lcov_file = sys.argv[2]

    a_cov, a_tot = parse_kover_xmls(kover_pattern)
    b_cov, b_tot = parse_lcov(lcov_file)

    total_lines = a_tot + b_tot
    if total_lines == 0:
        pct = 0.0
    else:
        pct = round((a_cov + b_cov) / total_lines * 100, 1)

    print(coverage_json(pct))
```

- [ ] **Step 2: Smoke-test with synthetic inputs**

Create a temporary test to validate the script before using it in CI:

```bash
# Synthetic Kover XML
cat > /tmp/test_kover.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<report name="test">
  <package name="com/ujizin">
    <class name="Foo">
      <counter type="LINE" missed="2" covered="8"/>
    </class>
    <counter type="LINE" missed="2" covered="8"/>
  </package>
  <counter type="LINE" missed="2" covered="8"/>
</report>
EOF

# Synthetic lcov (iosMain only: 3 covered, 1 missed)
cat > /tmp/test_ios.lcov << 'EOF'
SF:iosMain/Foo.kt
DA:1,1
DA:2,0
DA:3,1
DA:4,1
end_of_record
EOF

python3 scripts/parse_coverage.py "/tmp/test_kover.xml" /tmp/test_ios.lcov
```

Expected output (Kover: 8 covered of 10 total; lcov: 3 covered of 4 total → 11/14 = 78.6%):
```json
{"schemaVersion":1,"label":"coverage","message":"78.6%","color":"yellow"}
```

- [ ] **Step 3: Test zero-division guard**

```bash
cat > /tmp/empty.lcov << 'EOF'
EOF

python3 scripts/parse_coverage.py "/tmp/nonexistent_*.xml" /tmp/empty.lcov
```

Expected:
```json
{"schemaVersion":1,"label":"coverage","message":"0.0%","color":"red"}
```

- [ ] **Step 4: Test LLVM 3-field DA: format**

```bash
cat > /tmp/test_llvm.lcov << 'EOF'
SF:iosMain/Bar.kt
DA:1,5,abc123
DA:2,0,def456
end_of_record
EOF

python3 scripts/parse_coverage.py "/tmp/test_kover.xml" /tmp/test_llvm.lcov
```

Expected: valid JSON output (no `ValueError: too many values to unpack`).

- [ ] **Step 5: Test with actual Kover output**

Run from the **project root** (required — Python's `glob.glob` expands `**` relative to CWD):

```bash
python3 scripts/parse_coverage.py \
  "**/reports/kover/report*.xml" \
  /tmp/empty.lcov
```

Expected: valid JSON with a non-zero percentage (uses the Kover XML generated in Task 3 Step 7).

- [ ] **Step 6: Commit**

```bash
git add scripts/parse_coverage.py
git commit -m "feat(coverage): add parse_coverage.py to merge Kover XML + lcov"
```

---

### Task 6: Create `coverage.yml` CI workflow

**Files:**
- Create: `.github/workflows/coverage.yml`

- [ ] **Step 1: Create the workflow file**

Create `.github/workflows/coverage.yml`:

```yaml
name: Coverage

on:
  push:
    branches: [main]
    paths:
      - 'camposer/**'
      - 'camposer-code-scanner/**'
      - 'scripts/**'
      - '.github/workflows/coverage.yml'
      - 'gradle/**'
      - 'gradle.properties'
      - '*.gradle.kts'
  pull_request:
    branches: ['*']
    paths:
      - 'camposer/**'
      - 'camposer-code-scanner/**'
      - 'scripts/**'
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

      - name: Discover Kover tasks (diagnostic — helps on first run)
        run: ./gradlew tasks --group=verification | grep -i kover || true

      - name: Kover XML (common + Android, JVM host tests)
        # Task name is koverXmlReport (lifecycle alias) or koverXmlReportDebug.
        # continue-on-error so a task-not-found or verify failure is a warning, not a gate.
        # The diagnostic step above prints available tasks for debugging on first run.
        run: ./gradlew koverXmlReport
        continue-on-error: true

      - name: iOS tests with Kotlin/Native coverage
        run: ./gradlew iosSimulatorArm64Test

      - name: Export iOS lcov (iosMain only)
        run: |
          BINARY=$(find . -path "*/iosSimulatorArm64/debugTest/test.kexe" | head -1)

          PROFRAW_FILES=$(find . -name "*.profraw" | tr '\n' ' ')
          if [ -z "$PROFRAW_FILES" ]; then
            echo "ERROR: no .profraw files found — -coverage flag may not have applied"
            exit 1
          fi

          xcrun llvm-profdata merge -sparse $PROFRAW_FILES -o coverage.profdata

          # --sources=iosMain suffix-matches against absolute paths in DWARF debug info.
          # On first CI run, verify non-zero lcov output with:
          #   xcrun llvm-cov report "$BINARY" --instr-profile=coverage.profdata
          # Adjust the suffix if needed.
          xcrun llvm-cov export "$BINARY" \
            --instr-profile=coverage.profdata \
            --format=lcov \
            --sources=iosMain \
            > ios-coverage.lcov

          # Guard: warn if lcov is empty — means --sources filter matched nothing
          if [ ! -s ios-coverage.lcov ]; then
            echo "WARNING: ios-coverage.lcov is empty — --sources=iosMain may not match debug-info paths"
            echo "Run: xcrun llvm-cov report \$BINARY --instr-profile=coverage.profdata to inspect"
          fi

      - name: Compute unified coverage
        id: coverage
        run: |
          JSON=$(python3 scripts/parse_coverage.py \
            "**/reports/kover/report*.xml" \
            ios-coverage.lcov)
          echo "json=$JSON" >> $GITHUB_OUTPUT

      - name: Update Gist badge
        # Guard: only on main push AND only if coverage step produced output
        if: github.ref == 'refs/heads/main' && steps.coverage.outcome == 'success'
        uses: schneegans/dynamic-badges-action@v1.7.0
        with:
          auth: ${{ secrets.GIST_TOKEN }}
          gistID: ${{ secrets.GIST_ID }}
          filename: camposer-coverage.json
          label: coverage
          message: ${{ fromJson(steps.coverage.outputs.json).message }}
          color: ${{ fromJson(steps.coverage.outputs.json).color }}
```

- [ ] **Step 2: Validate YAML syntax**

```bash
python3 -c "import yaml; yaml.safe_load(open('.github/workflows/coverage.yml'))" && echo OK
```

Expected: `OK`

- [ ] **Step 3: Commit**

```bash
git add .github/workflows/coverage.yml
git commit -m "ci(coverage): add coverage.yml workflow with Kover + iOS lcov + Gist badge"
```

---

### Task 7: Add coverage badge to README

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Add badge placeholder**

In `README.md`, line 10 currently reads (everything on one line):

```
  <img alt="Static Badge" src="https://img.shields.io/badge/Platform-iOS-F5F5F7?logo=ios"> <br/>
```

Replace that single line with two lines — the coverage badge goes between the iOS badge and `<br/>`:

```
  <img alt="Static Badge" src="https://img.shields.io/badge/Platform-iOS-F5F5F7?logo=ios">
  <img alt="Coverage" src="https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/<user>/<GIST_ID>/raw/camposer-coverage.json"> <br/>
```

> **Note:** `<user>` is the GitHub username (e.g. `ujizin`) and `<GIST_ID>` is a placeholder from one-time manual setup. The badge shows "invalid" until the Gist is configured and `coverage.yml` runs once on `main`.

- [ ] **Step 2: Commit**

```bash
git add README.md
git commit -m "docs: add coverage badge placeholder to README"
```

---

### Task 8: Full local smoke-test

- [ ] **Step 1: Run full build to ensure nothing broken**

```bash
./gradlew build
```

Expected: BUILD SUCCESSFUL across all modules.

- [ ] **Step 2: Verify checkLegacyAbi (no accidental public API change)**

```bash
./gradlew checkLegacyAbi
```

Expected: BUILD SUCCESSFUL — `withHostTestBuilder` is internal infrastructure, not a public API change.

- [ ] **Step 3: Verify root Kover lifecycle task**

```bash
./gradlew koverXmlReport
```

Expected: BUILD SUCCESSFUL (this runs Kover across all modules). If task not found, run `./gradlew tasks --group=verification | grep -i kover` to find the correct root-level task. Note what it is — use that name in `coverage.yml` if `koverXmlReport` is unavailable.

Verify non-empty report output:
```bash
find . -path "**/reports/kover/report*.xml" | head -5
grep 'type="LINE"' camposer/build/reports/kover/report*.xml | head -3
```

Expected: at least one XML file, with covered line count > 0.

- [ ] **Step 4: Run iOS tests locally to confirm `-coverage` flag compiles**

```bash
./gradlew iosSimulatorArm64Test
```

Expected: BUILD SUCCESSFUL and tests pass. The `-coverage` flag only adds instrumentation — it must not break tests.

Verify `.profraw` files were generated:
```bash
find . -name "*.profraw" | head -5
```

Expected: at least one `.profraw` file under `build/`.

- [ ] **Step 5: End-to-end local dry-run of the full pipeline**

Run this from the **project root** to simulate exactly what CI does:

```bash
# 1. Merge profraw → profdata
BINARY=$(find . -path "*/iosSimulatorArm64/debugTest/test.kexe" | head -1)
PROFRAW_FILES=$(find . -name "*.profraw" | tr '\n' ' ')
xcrun llvm-profdata merge -sparse $PROFRAW_FILES -o /tmp/coverage.profdata

# 2. Export lcov (iosMain only)
xcrun llvm-cov export "$BINARY" \
  --instr-profile=/tmp/coverage.profdata \
  --format=lcov \
  --sources=iosMain \
  > /tmp/ios-coverage.lcov

# 3. Check lcov is non-empty
wc -l /tmp/ios-coverage.lcov
```

Expected: `wc -l` shows > 0 lines. If 0, run the diagnostic:
```bash
xcrun llvm-cov report "$BINARY" --instr-profile=/tmp/coverage.profdata | head -20
```
Inspect the source paths shown and adjust `--sources=` suffix to match them.

```bash
# 4. Run parse script end-to-end
python3 scripts/parse_coverage.py \
  "**/reports/kover/report*.xml" \
  /tmp/ios-coverage.lcov
```

Expected: valid JSON like `{"schemaVersion":1,"label":"coverage","message":"XX.X%","color":"..."}` with a non-zero percentage.

- [ ] **Step 6: Run spotless check across all modules**

```bash
./gradlew spotlessApply
```

Fix any formatting issues, then commit:

```bash
git add -p
git commit -m "style: apply spotless formatting"
```

- [ ] **Step 7: Run detekt**

```bash
./gradlew detekt :camposer:detektCommonMain
```

Expected: no new violations.

---

## One-Time Manual Setup Reminder

**Not part of this implementation — done by repo maintainer after merging:**

1. Go to gist.github.com → create a new public Gist (filename: `camposer-coverage.json`, content: `{}`)
2. Copy the Gist ID from the URL
3. Create a GitHub PAT (Settings → Developer settings → PATs) with `gist` scope
4. In repo Settings → Secrets → Actions, add:
   - `GIST_ID` = the Gist ID
   - `GIST_TOKEN` = the PAT
5. Edit `README.md` line with `<GIST_ID>` placeholder → replace with actual Gist ID
6. Push to `main` → `coverage.yml` runs → badge populates within ~5 minutes

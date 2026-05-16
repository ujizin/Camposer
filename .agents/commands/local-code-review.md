---
allowed-tools: Read, Glob, Grep, Bash, Write
---

Review code changes against Camposer's architectural invariants and save the result to a file.

The base branch argument is: $ARGUMENTS

If no argument was provided, use `main` as the base branch.

## Steps

1. **Get branch and diff:**
   ```bash
   git rev-parse --abbrev-ref HEAD
   git diff <base>...HEAD --name-only
   git diff <base>...HEAD
   ```

2. **Read all changed files** in full to understand context around the changes.

3. **Review** against the checklist below, focusing only on substantive issues.

4. **Save the review** to `.agents/reviews/review-YYYY-MM-DD.md` (today's date). If the file already exists, append a numeric suffix (e.g., `review-2026-05-16-2.md`).

5. Print the path to the saved review file.

---

## Do NOT Flag

- Naming preferences or style opinions
- Formatting or whitespace (handled by ktlint/Spotless)
- Missing comments or documentation
- "Could be more idiomatic" suggestions
- Anything already enforced by `spotlessCheck`

---

## Checklist

### 1. KMP Expect/Actual Completeness

For every change to an `expect` declaration or interface, verify **all three files** are updated:

| Modified | Must also update |
|----------|-----------------|
| `CameraEngine.kt` (interface) | `AndroidCameraEngine.kt` + `IOSCameraEngine.kt` |
| `CameraEngineImpl.kt` (expect) | `CameraEngineImpl.android.kt` + `CameraEngineImpl.ios.kt` |
| `FakeCameraEngine.kt` (expect) | `FakeCameraEngine.android.kt` + `FakeCameraEngine.ios.kt` |
| `FakeCameraTest.kt` (expect) | `FakeCameraTest.android.kt` + `FakeCameraTest.ios.kt` |
| `FakeCameraSession.kt` (expect) | `FakeCameraSession.android.kt` + `FakeCameraSession.ios.kt` |

🔴 Critical if any platform actual is missing — build fails with `expect has no actual`.

### 2. Architectural Invariants

| Invariant | Check |
|-----------|-------|
| No platform imports in `commonMain` | No `androidx.*`, `android.*`, `platform.*`, `AVFoundation` in any `commonMain` file |
| `CameraEngine` is internal | Not referenced in any `public` type or exposed via `CameraSession`/`CameraController` |
| State reaches hardware only through appliers | `CameraEngineImpl` does not call `cameraXController.*` or AVFoundation APIs directly |
| No global/singleton camera state | No `companion object`, `object`, or static field holding camera state |
| All public declarations marked `public` | Every new public symbol has explicit `public` keyword |

🔴 Critical for any violation.

### 3. Applier Ordering

For every `apply*` method added or modified in an applier:

1. Hardware write comes **first** (`cameraXController.x = value` or AVFoundation call)
2. State write comes **last** (`cameraState.updateX(value)`)

🟠 Warning if order is reversed — UI may show stale state.

### 4. Engine Impl Idempotency

For every `update*` method in `CameraEngineImpl` (Android or iOS actual), the guard must be present:

```kotlin
if (cameraState.yourProperty.value == yourProperty) return
```

🟠 Warning if guard is missing.

### 5. Public API Changes

If any `public` declaration was added, removed, or signature-changed:

- `./gradlew checkLegacyAbi` must be run before commit
- If intentional: `./gradlew updateLegacyAbi` and commit updated baseline
- If accidental: revert the change

🔴 Critical if public API changed without ABI check.

### 6. Test Coverage

| Change | Required |
|--------|----------|
| New camera property | New test class in `commonTest/.../session/` |
| New capability flag | Test for unsupported case returning `Result.failure` |
| `FakeCameraTest` changed | Both platform actuals updated |

🟠 Warning if new property has no test.

### 7. Platform Mapping Exhaustiveness

For enum values used in hardware writes, verify `when` expressions in platform mapping extensions are exhaustive — no `else` branch hiding missing cases.

🟡 Info if `else` used in `androidMain` or `iosMain` property mapping.

---

## Review Output Format

Save to `.agents/reviews/review-YYYY-MM-DD.md`:

```markdown
# Code Review: <branch-name>

**Date:** <YYYY-MM-DD>
**Base:** <base-branch>
**Files changed:** <count>

## Summary

<2-3 sentence summary of what the changes do>

## Findings

### 1. 🔴 <Short title>

**File:** `<path>:<line>`

<Clear description of the issue and why it matters>

```kotlin
// Suggested fix (if applicable)
```

### 2. 🟠 <Short title>

**File:** `<path>:<line>`

<Description>

---

## Checklist

- [x] KMP expect/actual completeness
- [x] Architectural invariants
- [x] Applier ordering
- [x] Engine idempotency guards
- [ ] Public API — run checkLegacyAbi before committing
- [x] Test coverage
- [x] Platform mapping exhaustiveness

## Verdict

<One-line assessment: "Ready to merge", "Needs fixes before merge", etc.>
```

If no findings, omit the Findings section. A clean review is a valid outcome.

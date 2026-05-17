# AGENTS.md — Camposer

Compose Multiplatform camera library. Android target uses CameraX; iOS target uses AVFoundation. Two published modules: `:camposer` (core) and `:camposer-code-scanner` (optional ML Kit / Vision barcode scanning).

## Architecture

KMP `expect/actual` is the primary abstraction. `commonMain` holds all interfaces, state, and composables — zero platform imports allowed there. Platform-specific code lives in `androidMain` (CameraX) and `iosMain` (AVFoundation).

State flows one direction: `CameraState` (MutableStateFlow) → `CameraEngine` → platform appliers → hardware. Appliers own the hardware write; state write always happens after.

See [ARCHITECTURE.md](ARCHITECTURE.md) for full codemap, data flow diagrams, and invariants.

## Essential Commands

```bash
make spotlessApply         # ./gradlew spotlessApply — fix formatting (required before commit)
make checkLegacyAbi        # ./gradlew checkLegacyAbi — verify no accidental public API breakage
make detekt                # ./gradlew detekt :camposer:detektCommonMain — KMP architecture rules
make build                 # ./gradlew build — full build, all platforms
make iosTest               # ./gradlew iosSimulatorArm64Test — fastest test run (~2-3 min, macOS)
make androidTest           # ./gradlew connectedAndroidTest — requires running emulator or device
make updateLegacyAbi       # ./gradlew updateLegacyAbi — only after intentional public API change
```

## Development Workflow

```bash
# 1. Make changes
# 2. Fix formatting and verify build
./gradlew spotlessApply && ./gradlew checkLegacyAbi && ./gradlew build
# 3. Run tests (macOS)
./gradlew iosSimulatorArm64Test
# 4. Run Android instrumented tests if Android-specific logic changed
./gradlew connectedAndroidTest
# 5. Update ABI baseline only if public API was intentionally changed
./gradlew updateLegacyAbi
```

## Do's and Don'ts

### Always

- Run `./gradlew spotlessApply` before committing
- Run `./gradlew checkLegacyAbi` when touching any public class or function
- Run `./gradlew :camposer:detektCommonMain` when touching any `commonMain` file
- Update all **3 files** when modifying `CameraEngine`, `FakeCameraEngine`, `FakeCameraTest`, or `FakeCameraSession` (expect + androidDeviceTest actual + iosTest actual)
- Mark every new public declaration with `public` — explicit API mode is enforced
- Delegate hardware writes through the applier that owns that concern
- Write state (`cameraState.update*()`) **after** the hardware write in appliers

### Never

- Add CameraX or AVFoundation imports to `commonMain`
- Expose `CameraEngine` through any public type — it is always internal
- Call platform APIs directly from `CameraEngineImpl` — delegate to an applier
- Skip the idempotency guard (`if (cameraState.x.value == x) return`) in `CameraEngineCore` (commonMain)
- Add platform-specific mapping extensions (`.mode`, `.avValue`) to `commonMain`
- Add or remove public API without running `checkLegacyAbi`

## Key Files

```text
camposer/src/
  commonMain/kotlin/com/ujizin/camposer/
    CameraPreview.kt                     Root @Composable
    session/CameraSession.kt             expect — public entry point
    state/CameraState.kt                 all properties as MutableStateFlow
    internal/core/
      CameraEngine.kt                    internal interface
      CameraEngineImpl.kt                expect — wires state to appliers
      applier/                           one interface per hardware concern

  androidMain/kotlin/com/ujizin/camposer/
    internal/core/
      CameraEngineImpl.android.kt        actual — delegates to AndroidCameraEngine
      AndroidCameraEngine.kt             CameraX hardware logic
      camerax/CameraXController.kt       interface over LifecycleCameraController

  iosMain/kotlin/com/ujizin/camposer/
    internal/core/
      CameraEngineImpl.ios.kt            actual — delegates to IOSCameraEngine
      IOSCameraEngine.kt                 AVFoundation hardware logic
```

## Key Configuration

- **Compile SDK:** 36, **Min SDK:** 23
- **Group ID:** `io.github.ujizin`
- **Version:** `buildSrc/src/main/kotlin/ujizin/camposer/Config.kt`
- **Formatting:** ktlint via Spotless (`build.gradle.kts` + `.editorconfig`)
- **Docs site:** MkDocs Material (`mkdocs.yml`) → `ujizin.github.io/Camposer`
- **Samples:** `samples/sample-android/` and `samples/sample-multiplatform/`

## Documentation

| Document | Path |
|----------|------|
| Architecture, codemap, invariants | [ARCHITECTURE.md](ARCHITECTURE.md) |
| Adding a camera property | [.agents/docs/camera-properties.md](.agents/docs/camera-properties.md) |
| Writing unit tests | [.agents/skills/unit-test-writer/SKILL.md](.agents/skills/unit-test-writer/SKILL.md) |

## Commands

Agent commands live in `.agents/commands/`. Claude Code auto-discovers them as `/` slash commands. Other agents: invoke by name (e.g. "use local-code-review").

| Command | Description |
|---------|-------------|
| `/local-code-review [base-branch]` | Review changes against Camposer's KMP invariants |
| `/pr-creator [base-branch]` | Create PR using the project template |
| `/release-notes <tag>` | Tag + publish GitHub release with formatted notes |

## Test Strategy

| Source set | Requires | Speed | When to run |
|------------|----------|-------|-------------|
| `commonTest` | compiles into each platform — no standalone JVM runner | — | always via platform tasks |
| `iosSimulatorArm64Test` | macOS + Xcode | ~2-3 min | fastest smoke test on macOS |
| `androidDeviceTest` | running emulator or device | ~5-10 min | when Android-specific logic changes |

**No standalone JVM test runner.** `commonTest` code is compiled into the platform test binary; you cannot run it directly with `./gradlew test`. The fastest smoke test on macOS is:

```bash
./gradlew iosSimulatorArm64Test
```

## Fake Infrastructure Pattern

**Missing any platform file = build failure.** Each fake is split across 3 files (see [ARCHITECTURE.md § Cross-Cutting Concerns](ARCHITECTURE.md)):

- `camposer/src/commonTest/.../fake/FakeCameraEngine.kt` (expect)
- `camposer/src/androidDeviceTest/.../fake/FakeCameraEngine.android.kt` (actual)
- `camposer/src/iosTest/.../fake/FakeCameraEngine.ios.kt` (actual)

Always create/update all 3 when adding or changing a fake.

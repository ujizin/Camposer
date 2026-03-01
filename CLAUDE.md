# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Camposer is a **Kotlin Multiplatform (KMP) camera library** built with Jetpack Compose. It targets Android (via CameraX) and iOS (via AVFoundation). There are two published modules:
- `:camposer` — core camera library
- `:camposer-code-scanner` — optional QR/barcode scanning extension (Android: ML Kit, iOS: Vision framework)

## Common Commands

```bash
# Fix code formatting before any PR
./gradlew spotlessApply

# Check formatting (used in CI)
./gradlew spotlessCheck

# Verify no breaking API changes
./gradlew checkLegacyAbi

# Update API baseline after intentional public API changes
./gradlew updateLegacyAbi

# Run Android instrumented tests (requires running emulator or device)
./gradlew connectedAndroidTest

# Run iOS tests (requires macOS)
./gradlew iosSimulatorArm64Test
```

## Architecture

### Expect/Actual Pattern

The library uses KMP's expect/actual pattern as its primary cross-platform abstraction. Platform-specific code lives in:
- `src/commonMain/` — shared interfaces, state, and composables
- `src/androidMain/` — CameraX implementation
- `src/iosMain/` — AVFoundation implementation

### Core Abstractions (commonMain)

| Class | Role |
|-------|------|
| `CameraPreview` | Root `@Composable` that renders the camera feed |
| `CameraSession` | `expect` class — state holder and main entry point; created via `rememberCameraSession()` |
| `CameraState` | `MutableStateFlow`-backed state for all camera properties (selector, flash, zoom, capture mode, etc.) |
| `CameraController` | Public API for triggering captures (`takePicture`, `startRecording`, `stopRecording`) |
| `CameraEngine` | Internal interface coordinating state changes across platforms |

### Layered Architecture

```
Public API:   CameraPreview (Composable) + CameraSession (state holder)
Controller:   CameraController → DefaultTakePictureCommand / DefaultRecordController
State:        CameraState (MutableStateFlow) + CameraInfo (capabilities)
Engine:       CameraEngine → CameraEngineImpl → AndroidCameraEngine / IOSCameraEngine
Platform:     Android (CameraX / PreviewView) | iOS (AVFoundation / UIViewController)
```

### Applier Pattern (Android / iOS)

State changes are applied to the camera through dedicated applier classes rather than monolithic update logic:
- `PreviewApplier`, `VideoApplier`, `ExposureZoomApplier`, `AnalyzerApplier`, `SessionTopologyApplier`

### Public API Rules

- **Explicit API mode is enforced** — all public declarations must be explicitly marked `public`. Never add new public API without considering ABI impact.
- Run `./gradlew checkLegacyAbi` before any PR touching public classes/functions. Run `./gradlew updateLegacyAbi` only after an intentional API change.

## Key Configuration

- **Compile SDK:** 36, **Min SDK:** 23
- **Group ID:** `io.github.ujizin`, **Version:** defined in `buildSrc/src/main/kotlin/ujizin/camposer/Config.kt`
- **Formatting:** ktlint via Spotless (configured in root `build.gradle.kts` + `.editorconfig`)
- **Docs site:** MkDocs Material at `mkdocs.yml`, deployed to `ujizin.github.io/Camposer`

## Sample Projects

- `samples/sample-android/` — Android-only Compose sample
- `samples/sample-multiplatform/` — KMP sample demonstrating full feature set

Both samples are the best reference for correct public API usage.

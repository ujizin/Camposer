# Architecture

Camposer solves the problem of camera access in Kotlin Multiplatform apps. Camera hardware is
entirely platform-specific (Android: CameraX, iOS: AVFoundation), but the user-facing API must be
shared. The library bridges this gap with a unified Compose API over platform-specific engines,
using KMP's `expect/actual` mechanism as the primary abstraction tool.

## High-Level Architecture

```text
┌─────────────────────────────────────────────────────────────┐
│  User Code                                                   │
└──────────────────────┬──────────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────────┐
│  Public API   CameraPreview (@Composable)                    │
│               CameraSession · CameraController               │
│               CameraState · CameraInfo                       │
└──────────────────────┬──────────────────────────────────────┘
                       │  state observation (StateFlow.collect)
┌──────────────────────▼──────────────────────────────────────┐
│  Engine       CameraEngine (interface, internal)             │
│               CameraEngineImpl (expect/actual)               │
└──────────────────────┬──────────────────────────────────────┘
                       │  delegates per-concern
┌──────────────────────▼──────────────────────────────────────┐
│  Appliers     PreviewApplier  ·  VideoApplier                │
│               ExposureZoomApplier  ·  AnalyzerApplier        │
│               SessionTopologyApplier                         │
└────────────┬──────────────────────────┬─────────────────────┘
             │                          │
┌────────────▼────────────┐  ┌──────────▼──────────────────────┐
│  AndroidCameraEngine    │  │  IOSCameraEngine                 │
│  CameraX                │  │  AVFoundation                    │
└─────────────────────────┘  └──────────────────────────────────┘
```

## Data Flow

**Setting change** (e.g. user switches flash mode):

```text
controller.setFlashMode(FlashMode.On)
  → CameraEngine.updateFlashMode(FlashMode.On)
    → CameraEngineImpl delegates to ExposureZoomApplier
      → applier calls platform API
        Android: CameraXController.enableTorch() / setFlashMode()
        iOS:     AVCaptureDevice.flashMode / torchMode
      → cameraState.updateFlashMode(FlashMode.On)  [state write — always last]
```

**Still capture** (`takePicture`):

```text
CameraController.takePicture(executor, callback)
  → DefaultTakePictureCommand.execute()
    → platform TakePictureCommand (actual)
        Android: ImageCapture.takePicture() via CameraX
        iOS:     AVCapturePhotoOutput.capturePhoto()
          → CaptureResult.Success(uri) or CaptureResult.Failure(e)
            → callback invoked on caller's executor/dispatcher
```

**Video recording** (`startRecording` / `stopRecording`):

```text
CameraController.startRecording(...)
  → DefaultRecordController.startRecording()
    → platform RecordController (actual)
        Android: VideoCapture.output.prepareRecording() → start()
        iOS:     IOSRecordController → AVAssetWriter pipeline
          → isRecording = true in CameraSession

CameraController.stopRecording()
  → RecordController.stopRecording()
    → recording finalized, CaptureResult delivered
      → isRecording = false in CameraSession
```

## Codemap

The entry points for library users:

- `CameraPreview` — root `@Composable`. Renders the camera feed and owns the UI lifecycle.
- `CameraSession` — `expect` class, the state holder. Created via `rememberCameraSession()`.
  Exposes `state`, `info`, `controller`, and streaming/initialization flags.

Public state and control surface:

- `CameraState` — all configurable camera properties as `MutableStateFlow` (selector, flash mode,
  zoom, capture mode, format, etc.). Mutating a flow triggers the engine.
- `CameraController` — user-facing capture API: `takePicture`, `startRecording`, `stopRecording`.
- `CameraInfo` / `CameraInfoState` — runtime capabilities reported by the hardware (supported
  features, zoom range, exposure compensation range).

Internal coordination (not part of the public API):

- `CameraEngine` — internal interface. Receives state-change commands and applies them to hardware.
- `CameraEngineImpl` — `expect` class wiring state observations to the applier layer.
  Each platform provides the `actual` implementation.
- `AndroidCameraEngine` / `IOSCameraEngine` — the platform `actual` impls. Touch hardware here only.

State application — the Applier layer:

Each applier owns one hardware concern. `CameraEngineImpl` delegates to them; it does not apply
state directly.

- `PreviewApplier` — preview stream configuration
- `VideoApplier` — video capture configuration
- `ExposureZoomApplier` — exposure compensation and zoom ratio
- `AnalyzerApplier` — image analysis pipeline
- `SessionTopologyApplier` — use-case binding (which CameraX use cases are active)

Code scanner extension (`:camposer-code-scanner`):

Optional module. Plugs into the `ImageAnalyzer` pipeline. Android uses ML Kit; iOS uses the Vision
framework. No dependency on this module from `:camposer`.

## Architectural Invariants

These are constraints that must hold. Violations will likely compile but break things subtly.

**`CameraEngine` is internal.** It is never exposed through `CameraSession` or any public type.
The public API surface is `CameraState` + `CameraController` + `CameraInfo` only.

**`commonMain` has zero platform imports.** No CameraX, no AVFoundation, no Android SDK types
anywhere in `commonMain`. KMP compilation enforces this, but be deliberate about it.

**State reaches hardware only through appliers.** `CameraEngineImpl` must not call platform APIs
directly — it delegates to the applier that owns that concern. This keeps each applier testable
and the engine impl readable.

**No global/singleton camera state.** Each `CameraSession` instance is independent. Multiple
sessions can coexist (though hardware may not support it). There is no static camera registry.

**Public API requires explicit `public` modifier.** `explicitApi()` is enforced in the Gradle
build. Every public declaration is intentional. ABI is validated by `checkLegacyAbi` in CI —
adding or removing public API without updating the baseline breaks the build.

## Boundaries

```text
commonMain          ──── shared: interfaces, state, composables, applier contracts
androidMain / iosMain ── platform: hardware access, actual impls, applier impls
commonTest          ──── unit tests via expect/actual fakes (no hardware)
androidSharedTest   ──── Android actual fakes (shared by androidHostTest JVM + androidDeviceTest)
androidDeviceTest   ──── instrumented tests (real or emulated Android hardware)
iosTest             ──── iOS unit tests (simulator)
```

The boundary between `commonMain` and platform source sets is enforced by KMP compilation.
The boundary between public and internal is enforced by explicit API mode + ABI validation.

## File Structure Reference

Key files only — not exhaustive. Paths relative to `camposer/src/`.

```text
commonMain/kotlin/com/ujizin/camposer/
  CameraPreview.kt                          Root composable, camera UI entry point
  session/
    CameraSession.kt                        expect — state holder, public entry point
    CameraSessionState.kt                   internal session lifecycle state machine
  state/
    CameraState.kt                          all configurable properties as MutableStateFlow
    properties/                             enums + value types (FlashMode, CaptureMode, etc.)
  info/
    CameraInfo.kt                           hardware capabilities (zoom range, features)
    CameraInfoState.kt                      mutable capabilities holder
  controller/
    camera/CameraController.kt              public capture API (takePicture, record, stop)
    record/RecordController.kt              internal recording contract
    takepicture/TakePictureCommand.kt       internal still capture contract
  manager/
    CameraDevicesManager.kt                 multi-camera device enumeration
  internal/core/
    CameraEngine.kt                         internal interface — state-change commands
    CameraEngineImpl.kt                     expect — wires state observations to appliers
    CameraEngineCore.kt                     shared base logic (non-platform)
    applier/
      PreviewApplier.kt                     preview stream configuration contract
      VideoApplier.kt                       video capture configuration contract
      ExposureZoomApplier.kt                exposure + zoom contract
      AnalyzerApplier.kt                    image analysis pipeline contract
      SessionTopologyApplier.kt             use-case binding contract

androidMain/kotlin/com/ujizin/camposer/
  session/CameraSession.android.kt          actual — wraps AndroidCameraEngine
  internal/core/
    CameraEngineImpl.android.kt             actual — delegates to AndroidCameraEngine
    AndroidCameraEngine.kt                  CameraX hardware logic (main Android impl)
    camerax/
      CameraXController.kt                  interface over CameraX LifecycleCameraController
      CameraXControllerWrapper.kt           concrete CameraX controller implementation
    applier/*.android.kt                    Android applier impls (one per concern)
  controller/camera/AndroidCameraController.kt  CameraX capture implementation
  internal/zoom/PinchToZoomController.kt    gesture-driven zoom via CameraX

iosMain/kotlin/com/ujizin/camposer/
  session/CameraSession.ios.kt              actual — wraps IOSCameraEngine
  internal/core/
    CameraEngineImpl.ios.kt                 actual — delegates to IOSCameraEngine
    IOSCameraEngine.kt                      AVFoundation hardware logic (main iOS impl)
    ios/IOSCameraController.kt              interface over AVCaptureSession
    applier/*.ios.kt                        iOS applier impls (one per concern)
  internal/view/
    CameraViewController.kt                UIViewController managing AVFoundation preview
    gesture/PinchToZoomGestureHandler.kt    pinch-to-zoom gesture recognizer
    gesture/TapToFocusGestureHandler.kt     tap-to-focus gesture recognizer
  internal/controller/IOSRecordController.kt  AVAssetWriter-based video recording
```

## Cross-Cutting Concerns

**KMP expect/actual** is used for `CameraSession`, `CameraEngineImpl`, and all test fakes
(`FakeCameraEngine`, `FakeCameraTest`, `FakeCameraSession`). Adding a method to any of these
requires updating three files: the common `expect` declaration, the Android actual in
`androidSharedTest`, and the iOS actual in `iosTest`.

**Coroutines** drive all async state. `CameraState` properties are `MutableStateFlow`. Platform
engines collect them on appropriate dispatchers. No callbacks cross the public API boundary.

**Compose lifecycle** manages camera startup and teardown. `CameraPreview` uses
`DisposableEffect` to bind/unbind the engine to the composable's lifecycle. There is no
imperative `start()`/`stop()` API.

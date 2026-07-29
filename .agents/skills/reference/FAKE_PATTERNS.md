# Fake Infrastructure Reference

Complete reference for Camposer's test fake classes. All fakes use KMP `expect/actual` — every change to a common `expect` declaration requires updating **three files**. Missing any one = build failure (`expect has no actual`).

---

## File Map

### FakeCameraTest

Holds hardware capability flags and records hardware calls for assertion.

| File | Role |
|------|------|
| `camposer/src/commonTest/kotlin/com/ujizin/camposer/fake/FakeCameraTest.kt` | `expect` — capability flags + assert methods |
| `camposer/src/androidSharedTest/kotlin/com/ujizin/camposer/fake/FakeCameraTest.android.kt` | `actual` — wraps `FakeCameraXController` |
| `camposer/src/iosTest/kotlin/com/ujizin/camposer/fake/FakeCameraTest.ios.kt` | `actual` — wraps `FakeIosCameraController` |

**Capability flags** (set BEFORE `initCameraSession()`):

```kotlin
cameraTest.isFlashSupported = false       // default: true
cameraTest.isExposureSupported = false    // default: true
cameraTest.isZSLSupported = false         // default: true
cameraTest.hasErrorInRecording = true     // default: false
```

**Assert methods** (call AFTER the controller action):

```kotlin
cameraTest.assertCamSelector(expected)
cameraTest.assertCaptureMode(expected)
cameraTest.assertZoomRatio(expected)
cameraTest.assertFlashMode(expected)
cameraTest.assertExposureCompensation(expected)
cameraTest.assertImageCaptureStrategy(expected)
cameraTest.assertImageAnalyzer(expected)
cameraTest.assertCamFormat(expected)
cameraTest.assertIsRecording(expected)
```

---

### FakeCameraEngine

Fake engine wired to `FakeCameraTest`. Android actual delegates to `CameraEngineImpl` via `by` — no method implementations needed when only adding to the interface.

| File | Role |
|------|------|
| `camposer/src/commonTest/kotlin/com/ujizin/camposer/fake/FakeCameraEngine.kt` | `expect` — mirrors `CameraEngine` interface |
| `camposer/src/androidSharedTest/kotlin/com/ujizin/camposer/fake/FakeCameraEngine.android.kt` | `actual` — `by CameraEngineImpl(...)` delegation |
| `camposer/src/iosTest/kotlin/com/ujizin/camposer/fake/FakeCameraEngine.ios.kt` | `actual` — explicit method implementations |

Android actual pattern (delegation — no changes needed for new methods):

```kotlin
internal actual class FakeCameraEngine actual constructor(
  cameraTest: FakeCameraTest,
  testDispatcher: CoroutineDispatcher,
) : AndroidCameraEngine by CameraEngineImpl(
    cameraXController = cameraTest.cameraXController,
    cameraController = cameraTest.cameraController,
    cameraInfo = cameraTest.cameraInfo,
    dispatcher = testDispatcher,
  )
```

iOS actual uses explicit overrides — must add new methods manually.

---

### FakeCameraSession / createCameraSession

Factory function that wires `FakeCameraTest` + `FakeCameraEngine` into a `CameraSession`.

| File | Role |
|------|------|
| `camposer/src/commonTest/kotlin/com/ujizin/camposer/fake/FakeCameraSession.kt` | `expect fun createCameraSession(...)` |
| `camposer/src/androidSharedTest/kotlin/com/ujizin/camposer/fake/FakeCameraSession.android.kt` | `actual` |
| `camposer/src/iosTest/kotlin/com/ujizin/camposer/fake/FakeCameraSession.ios.kt` | `actual` |

Usage in tests (via `CameraSessionTest` base class):

```kotlin
val cameraSession by lazy {
    createCameraSession(
        fakeCameraTest = cameraTest,
        testDispatcher = testDispatcher,
    )
}
```

---

## 3-File Rule Summary

| Touch this | Update these |
|-----------|--------------|
| `FakeCameraTest.kt` (expect) | `androidSharedTest/.../FakeCameraTest.android.kt` + `iosTest/.../FakeCameraTest.ios.kt` |
| `FakeCameraEngine.kt` (expect) | `androidSharedTest/.../FakeCameraEngine.android.kt` (Android: auto via `by`) + `iosTest/.../FakeCameraEngine.ios.kt` (explicit) |
| `FakeCameraSession.kt` (expect) | `androidSharedTest/.../FakeCameraSession.android.kt` + `iosTest/.../FakeCameraSession.ios.kt` |

---

## Adding a New Capability Flag

When a new property has a hardware capability check (e.g. `isXxxSupported`):

1. Add `var isXxxSupported: Boolean` to `FakeCameraTest.kt` (expect)
2. Add field + default in `androidSharedTest/.../FakeCameraTest.android.kt` (Android actual)
3. Add field + default in `iosTest/.../FakeCameraTest.ios.kt` (iOS actual)
4. Add `fun assertXxx(expected: XxxType)` to all three files

Platform actuals also wire the flag into the fake controller (`FakeCameraXController` / `FakeIosCameraController`) so the engine reads it during `initCameraSession()`.

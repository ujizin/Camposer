# Camera Properties

Camera properties are the configurable settings exposed through `CameraState` — things like flash
mode, zoom ratio, capture mode, or mirror mode. This document explains how they work internally,
how to classify a new one, and how to implement it correctly across all layers.

---

## How Properties Work

Every property follows the same three-layer model:

```text
[ Property Type (enum/value) ]
          │ defined in commonMain
          ▼
[ CameraState (MutableStateFlow) ]
          │ internal update function called by engine/applier
          ▼
[ CameraEngine → CameraEngineImpl (expect/actual) ]
          │ dispatches to platform
          ▼
[ Platform: CameraX (Android) | AVFoundation (iOS) ]
```

`CameraState` is the single source of truth. Hardware writes happen *before* the state update —
the state reflects confirmed hardware state, not an intent.

---

## Two Property Types

Properties fall into one of two categories based on whether they require an immediate hardware
write when changed.

### Type A — Simple

The value is stored in `CameraState` and read at session initialization. No hardware write occurs
when the value changes at runtime; the camera uses it on its next setup cycle.

**Examples:** `ScaleType`, `ImplementationMode`, `OrientationStrategy`

In the engine impl, Type A looks like:

```kotlin
actual override fun updateScaleType(scaleType: ScaleType) {
    if (cameraState.scaleType.value == scaleType) return
    cameraState.updateScaleType(scaleType)      // direct state write — no applier
}
```

### Type B — Hardware-Applied

The value must be pushed to hardware immediately when changed. An **applier** owns both the
hardware write and the state update. The state write always happens *after* the hardware write.

**Examples:** `FlashMode`, `ZoomRatio`, `MirrorMode`, `TorchEnabled`

In the engine impl, Type B looks like:

```kotlin
actual override fun updateFlashMode(flashMode: FlashMode) {
    if (cameraState.flashMode.value == flashMode) return
    exposureZoomApplier.applyFlashMode(flashMode)   // delegates to applier
}
```

And in the applier:

```kotlin
fun applyFlashMode(flashMode: FlashMode) {
    cameraXController.imageCaptureFlashMode = flashMode.mode   // hardware write first
    cameraState.updateFlashMode(flashMode)                     // state write always last
}
```

---

## Applier Ownership

Each applier owns a specific concern. When adding a Type B property, assign it to the applier
that best matches its hardware concern:

| Applier | Owns |
|---------|------|
| `ExposureZoomApplier` | Flash, torch, zoom ratio, exposure compensation |
| `PreviewApplier` | Mirror mode, focus-on-tap |
| `VideoApplier` | Frame rate, image capture strategy, video stabilization |
| `SessionTopologyApplier` | Capture mode, camera selector, format (requires use-case rebind) |

If no existing applier fits, create a new one implementing `CameraStateApplier`. Each applier is
responsible for a single hardware concern — do not add unrelated properties to an existing applier
just to avoid creating a new file.

---

## Fake Infrastructure

Tests use fake implementations of the engine layer. These fakes use the same `expect/actual`
pattern as the production code, which means **every interface change requires updating three
files**:

```text
commonTest/kotlin/.../fake/FakeCameraEngine.kt           ← expect declaration
androidSharedTest/kotlin/.../fake/FakeCameraEngine.android.kt  ← Android actual (shared JVM + device)
iosTest/kotlin/.../fake/FakeCameraEngine.ios.kt          ← iOS actual
```

The Android actual typically delegates to `CameraEngineImpl` via `by` delegation, so adding the
method to the interface and the Android engine impl is sufficient. The iOS actual may need a
manual implementation — check whether it uses delegation or explicit overrides.

The same three-file rule applies to `FakeCameraTest` when a property adds a hardware capability
check (`isFlashSupported`, etc.).

---

## Implementation Guide

### Type A — Simple Property

**1. Define the type** in `commonMain/kotlin/com/ujizin/camposer/state/properties/`:

```kotlin
public enum class YourProperty {
    OptionA,
    OptionB,
}
```

Mark every declaration `public` — explicit API mode is enforced.

**2. Add to `CameraState`** (`commonMain/.../state/CameraState.kt`):

```kotlin
private val _yourProperty = MutableStateFlow(YourProperty.OptionA)
public val yourProperty: StateFlow<YourProperty> = _yourProperty.asStateFlow()

internal fun updateYourProperty(yourProperty: YourProperty) {
    _yourProperty.update { yourProperty }
}
```

**3. Add to `CameraEngine` interface** (`commonMain/.../internal/core/CameraEngine.kt`):

```kotlin
fun updateYourProperty(yourProperty: YourProperty)
```

**4. Add to `CameraEngineImpl` expect** (`commonMain/.../internal/core/CameraEngineImpl.kt`):

```kotlin
override fun updateYourProperty(yourProperty: YourProperty)
```

**5. Implement in both actuals** — Android (`CameraEngineImpl.android.kt`) and iOS
(`CameraEngineImpl.ios.kt`):

```kotlin
actual override fun updateYourProperty(yourProperty: YourProperty) {
    if (cameraState.yourProperty.value == yourProperty) return
    cameraState.updateYourProperty(yourProperty)
}
```

**6. Add to `FakeCameraEngine` expect** (`commonTest/.../fake/FakeCameraEngine.kt`):

```kotlin
override fun updateYourProperty(yourProperty: YourProperty)
```

Check both platform actuals and update if they use explicit overrides (not by-delegation).

**7. Write tests** in `commonTest/.../session/`:

```kotlin
internal class CameraYourPropertyTest : CameraSessionTest() {

    @Test
    fun test_your_property_option_a() {
        initCameraSession()
        updateSession(/* yourProperty = YourProperty.OptionA */)
        assertEquals(YourProperty.OptionA, cameraSession.state.yourProperty.value)
    }
}
```

---

### Type B — Hardware-Applied Property

Type B includes all Type A steps plus the applier layer and (if needed) platform mapping files.

**Full call chain for reference** (FlashMode example):

```text
CameraEngine.updateFlashMode(FlashMode.On)
  → CameraEngineImpl.updateFlashMode()  [actual, Android]
    → ExposureZoomApplier.applyFlashMode(FlashMode.On)
      → cameraXController.imageCaptureFlashMode = flashMode.mode   [CameraX write]
      → cameraState.updateFlashMode(FlashMode.On)                  [state write — always last]
```

**1. Define the type** — same as Type A. If enum values need platform-specific constants, add
extension files:

`androidMain/.../state/properties/YourProperty.android.kt`:

```kotlin
internal val YourProperty.mode: Int
    get() = when (this) {
        YourProperty.OptionA -> SomeCameraXClass.CONSTANT_A
        YourProperty.OptionB -> SomeCameraXClass.CONSTANT_B
    }
```

`iosMain/.../state/properties/YourProperty.ios.kt`:

```kotlin
internal val YourProperty.avValue: AVCaptureSomething
    get() = when (this) {
        YourProperty.OptionA -> AVCaptureSomething.valueA
        YourProperty.OptionB -> AVCaptureSomething.valueB
    }
```

**2–4.** Add to `CameraState`, `CameraEngine`, and `CameraEngineImpl` expect — same as Type A.

**5. Implement in Android actual via applier** (`CameraEngineImpl.android.kt`):

```kotlin
actual override fun updateYourProperty(yourProperty: YourProperty) {
    if (cameraState.yourProperty.value == yourProperty) return
    someApplier.applyYourProperty(yourProperty)
}
```

**6. Add `applyYourProperty` to the chosen applier** (`androidMain/.../applier/`):

```kotlin
fun applyYourProperty(yourProperty: YourProperty) {
    cameraXController.someProperty = yourProperty.mode   // hardware write first
    cameraState.updateYourProperty(yourProperty)          // state write always last
}
```

**7. Implement in iOS actual via applier** (`CameraEngineImpl.ios.kt`):

```kotlin
actual override fun updateYourProperty(yourProperty: YourProperty) {
    if (cameraState.yourProperty.value == yourProperty) return
    someApplier.applyYourProperty(yourProperty)
}
```

Add `applyYourProperty` to the corresponding iOS applier (`iosMain/.../applier/`), calling the
AVFoundation API then updating state — same ordering rule as Android:

```kotlin
fun applyYourProperty(yourProperty: YourProperty) {
    avCaptureDevice.someProperty = yourProperty.avValue   // hardware write first
    cameraState.updateYourProperty(yourProperty)           // state write always last
}
```

**8. Add capability check** (if hardware support varies):

If the feature is not universally available, add a flag to `CameraInfo` / `CameraInfoState` and
validate before calling the engine:

```kotlin
override fun setYourProperty(yourProperty: YourProperty): Result<Unit> {
    if (!isYourPropertySupported) return Result.failure(CapabilityException(...))
    cameraEngine?.updateYourProperty(yourProperty)
    return Result.success(Unit)
}
```

If you add a capability flag, update `FakeCameraTest` across all three files (expect +
androidSharedTest actual + iosTest actual).

**9. Add to `FakeCameraEngine`** — same as Type A step 6.

**10. Write tests** — follow `CameraFlashModeTest` as the template. Cover:

- Happy path — property applied, state reflects the new value
- Unsupported capability — returns `Result.failure`, state unchanged
- All enum values cycle correctly

---

## Common Mistakes

| Mistake | Symptom | Fix |
|---------|---------|-----|
| Missing `FakeCameraEngine` platform actual | Build failure: `expect has no actual` | Update all 3 files: common expect + androidSharedTest actual + iosTest actual |
| Missing `FakeCameraTest` platform actual | Same | Same |
| State write before hardware write in applier | Potential stale UI / race | Always hardware → then state |
| Missing idempotency guard in engine impl | Property re-applied on every update cycle | Add `if (cameraState.x.value == x) return` |
| Public API added without ABI check | CI breaks on `checkLegacyAbi` | Run `./gradlew checkLegacyAbi` before committing |
| Platform mapping extension in `commonMain` | Compile error — no CameraX/AVFoundation in common | Put mapping extensions in `androidMain` / `iosMain` |

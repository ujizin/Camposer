# Pattern: Adding a Camera Property

This document is the step-by-step recipe for adding a new camera property to Camposer. Follow
it exactly — missing any step causes a build failure or silent misbehavior.

## Choose the right pattern first

| Pattern | When to use | Examples |
|---------|------------|---------|
| **Type A: Simple** | Value stored in `CameraState`, read at session start — no immediate hardware write | `ScaleType`, `ImplementationMode`, `OrientationStrategy` |
| **Type B: Hardware-applied** | Value must be written to hardware immediately when changed, optionally with platform mapping | `FlashMode`, `ZoomRatio`, `MirrorMode`, `TorchEnabled` |

---

## Type A: Simple Property (e.g. ScaleType)

Shortest path. The engine impl writes the value to `CameraState` directly — no applier needed.

### Step 1 — Define the property type

Create `camposer/src/commonMain/kotlin/com/ujizin/camposer/state/properties/YourProperty.kt`:

```kotlin
public enum class YourProperty {
  OptionA,
  OptionB,
}
```

Mark every declaration `public` (explicit API mode is enforced).

### Step 2 — Add to `CameraState`

In `camposer/src/commonMain/kotlin/com/ujizin/camposer/state/CameraState.kt`, add:

```kotlin
private val _yourProperty = MutableStateFlow(YourProperty.OptionA)
public val yourProperty: StateFlow<YourProperty> = _yourProperty.asStateFlow()

internal fun updateYourProperty(yourProperty: YourProperty) {
    _yourProperty.update { yourProperty }
}
```

Follow the existing pattern exactly — private mutable flow, public read-only view, internal update function.

### Step 3 — Add to `CameraEngine` interface

In `camposer/src/commonMain/kotlin/com/ujizin/camposer/internal/core/CameraEngine.kt`:

```kotlin
fun updateYourProperty(yourProperty: YourProperty)
```

### Step 4 — Add to `CameraEngineImpl` expect declaration

In `camposer/src/commonMain/kotlin/com/ujizin/camposer/internal/core/CameraEngineImpl.kt`:

```kotlin
override fun updateYourProperty(yourProperty: YourProperty)
```

### Step 5 — Implement in Android actual

In `camposer/src/androidMain/kotlin/com/ujizin/camposer/internal/core/CameraEngineImpl.android.kt`:

```kotlin
actual override fun updateYourProperty(yourProperty: YourProperty) {
    if (cameraState.yourProperty.value == yourProperty) return
    cameraState.updateYourProperty(yourProperty)
}
```

### Step 6 — Implement in iOS actual

In `camposer/src/iosMain/kotlin/com/ujizin/camposer/internal/core/CameraEngineImpl.ios.kt`:

```kotlin
actual override fun updateYourProperty(yourProperty: YourProperty) {
    if (cameraState.yourProperty.value == yourProperty) return
    cameraState.updateYourProperty(yourProperty)
}
```

### Step 7 — Add to `FakeCameraEngine` expect

In `camposer/src/commonTest/kotlin/com/ujizin/camposer/fake/FakeCameraEngine.kt`:

```kotlin
override fun updateYourProperty(yourProperty: YourProperty)
```

### Step 8 — Implement `FakeCameraEngine` actuals

The Android actual (`FakeCameraEngine.android.kt`) delegates to `CameraEngineImpl` via `by`
delegation — adding the method to the interface and `CameraEngineImpl.android.kt` is sufficient,
no change needed here.

Check `camposer/src/iosTest/kotlin/com/ujizin/camposer/fake/FakeCameraEngine.ios.kt` — if it
also uses by-delegation, same applies. If it has a manual implementation, add the method there.

### Step 9 — Write the test

Create `camposer/src/commonTest/kotlin/com/ujizin/camposer/session/CameraYourPropertyTest.kt`:

```kotlin
internal class CameraYourPropertyTest : CameraSessionTest() {

    @Test
    fun test_your_property_option_a() {
        initCameraSession()
        updateSession(/* yourProperty = YourProperty.OptionA */)
        assertEquals(YourProperty.OptionA, cameraSession.state.yourProperty.value)
    }

    @Test
    fun test_your_property_option_b() {
        initCameraSession()
        updateSession(/* yourProperty = YourProperty.OptionB */)
        assertEquals(YourProperty.OptionB, cameraSession.state.yourProperty.value)
    }
}
```

### Step 10 — Verify

```bash
./gradlew spotlessApply
./gradlew checkLegacyAbi   # run if you added public API
./gradlew build
./gradlew iosSimulatorArm64Test
```

---

## Type B: Hardware-Applied Property (e.g. FlashMode)

Longer path. The value must be pushed to hardware immediately when changed. An **applier** owns
the hardware write + state update. Platform mapping files convert the enum to platform constants.

The full FlashMode call chain for reference:

```
CameraController.setFlashMode(FlashMode.On)
  → CameraControllerContract validation (isFlashSupported check)
    → CameraEngine.updateFlashMode(FlashMode.On)
      → CameraEngineImpl.updateFlashMode()  [actual, Android]
        → ExposureZoomApplier.applyFlashMode(FlashMode.On)
          → cameraXController.imageCaptureFlashMode = flashMode.mode  [CameraX write]
          → cameraState.updateFlashMode(FlashMode.On)                 [state write — always last]
```

### Step 1 — Define the property type

Same as Type A Step 1.

If the enum values need to map to platform-specific constants, add extension files:

`camposer/src/androidMain/kotlin/com/ujizin/camposer/state/properties/YourProperty.android.kt`:

```kotlin
import androidx.camera.core.SomeCameraXClass

internal val YourProperty.mode: Int
    get() = when (this) {
        YourProperty.OptionA -> SomeCameraXClass.CONSTANT_A
        YourProperty.OptionB -> SomeCameraXClass.CONSTANT_B
    }
```

`camposer/src/iosMain/kotlin/com/ujizin/camposer/state/properties/YourProperty.ios.kt`:

```kotlin
import platform.AVFoundation.AVCaptureSomething

internal val YourProperty.avValue: AVCaptureSomething
    get() = when (this) {
        YourProperty.OptionA -> AVCaptureSomething.valueA
        YourProperty.OptionB -> AVCaptureSomething.valueB
    }
```

### Step 2 — Add to `CameraState`

Same as Type A Step 2.

### Step 3 — Add to `CameraEngine` interface

Same as Type A Step 3.

### Step 4 — Add to `CameraEngineImpl` expect

Same as Type A Step 4.

### Step 5 — Implement in Android actual (via applier)

In `camposer/src/androidMain/kotlin/com/ujizin/camposer/internal/core/CameraEngineImpl.android.kt`:

```kotlin
actual override fun updateYourProperty(yourProperty: YourProperty) {
    if (cameraState.yourProperty.value == yourProperty) return
    someApplier.applyYourProperty(yourProperty)
}
```

Pick the most appropriate existing applier:

| Applier | Owns |
|---------|------|
| `ExposureZoomApplier` | Flash, torch, zoom, exposure |
| `PreviewApplier` | Mirror mode, focus-on-tap |
| `VideoApplier` | Frame rate, image capture strategy, video stabilization |
| `SessionTopologyApplier` | Capture mode, camera selector, format (requires use-case rebind) |

If none fit, create a new applier implementing `CameraStateApplier`.

### Step 6 — Add `applyYourProperty` to the applier

In the chosen applier (e.g. `ExposureZoomApplier.android.kt`):

```kotlin
fun applyYourProperty(yourProperty: YourProperty) {
    cameraXController.someProperty = yourProperty.mode   // hardware write first
    cameraState.updateYourProperty(yourProperty)          // state write always last
}
```

State write comes **after** hardware write. The state is the confirmation, not the trigger.

### Step 7 — Mirror in iOS

In `camposer/src/iosMain/kotlin/com/ujizin/camposer/internal/core/CameraEngineImpl.ios.kt` and
the corresponding iOS applier — same structure as Steps 5-6 using AVFoundation APIs.

### Step 8 — Add capability check (if needed)

If the feature requires hardware capability (e.g. flash requires `isFlashSupported`), add the
flag to `CameraInfo` / `CameraInfoState`, then validate in the controller before calling the engine:

```kotlin
override fun setYourProperty(yourProperty: YourProperty): Result<Unit> {
    if (!isYourPropertySupported) return Result.failure(CaptureModeException(...))
    cameraEngine?.updateYourProperty(yourProperty)
    return Result.success(Unit)
}
```

### Step 9 — Update `FakeCameraTest` (if capability check added)

In `camposer/src/commonTest/kotlin/com/ujizin/camposer/fake/FakeCameraTest.kt` (expect):

```kotlin
var isYourPropertySupported: Boolean
fun assertYourProperty(expected: YourProperty)
```

Add `actual` implementations in **both**:
- `camposer/src/androidDeviceTest/kotlin/com/ujizin/camposer/fake/FakeCameraTest.android.kt`
- `camposer/src/iosTest/kotlin/com/ujizin/camposer/fake/FakeCameraTest.ios.kt`

**Missing either actual file = build failure.**

### Step 10 — Update `FakeCameraEngine` expect/actuals

Same as Type A Steps 7-8.

### Step 11 — Write tests

Follow `CameraFlashModeTest` as the template. Key test cases:

1. Happy path — property applied, state reflects the new value
2. Unsupported capability — returns `Result.failure`, state unchanged
3. All enum values cycle correctly

### Step 12 — Verify

```bash
./gradlew spotlessApply
./gradlew checkLegacyAbi
./gradlew build
./gradlew iosSimulatorArm64Test
./gradlew connectedAndroidTest   # required — hardware write must be validated on device
```

---

## Common Mistakes

| Mistake | Symptom | Fix |
|---------|---------|-----|
| Missing `FakeCameraEngine` platform actual | Build failure: `expect has no actual` | Add to all 3 files: common expect + android actual + ios actual |
| Missing `FakeCameraTest` platform actual | Same | Same |
| State write before hardware write in applier | Potential race / stale UI | Always hardware → then state |
| Missing idempotency guard in engine impl | Property re-applied on every update cycle | Add `if (cameraState.x.value == x) return` |
| Public API added without ABI check | CI breaks on `checkLegacyAbi` | Run `./gradlew checkLegacyAbi` before committing |

---
name: unit-tests
description: Use when writing or extending unit tests for Camposer — covers test infrastructure, fake classes, naming conventions, and patterns for both simple (Type A) and hardware-applied (Type B) camera properties.
---

# Unit Tests

Camposer tests run in `commonTest` against fake implementations of the camera engine. No mocks, no platform hardware — all tests are hermetic and cross-platform.

## Test Infrastructure

| Class | Role |
|-------|------|
| `CameraSessionTest` | Abstract base class — provides `cameraSession`, `cameraTest`, `controller`, `initCameraSession()`, `updateSession()` |
| `FakeCameraTest` | Records hardware calls and exposes `assert*()` helpers; configure `isXxxSupported` flags before `initCameraSession()` |
| `FakeCameraSession` | Fake session wired to `FakeCameraEngine` |

**Location:** `camposer/src/commonTest/kotlin/com/ujizin/camposer/session/`

**Imports needed:**

```kotlin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
// for async tests only:
import kotlinx.coroutines.test.runTest
```

## Naming Conventions

- File: `CameraXxxTest.kt`
- Class: `internal class CameraXxxTest : CameraSessionTest()`
- Methods: `fun test_preview_xxx_yyy()` — snake_case, describe the scenario

## Type A — Simple Property (e.g. ScaleType)

Property set via `updateSession()`. No capability check.

```kotlin
internal class CameraScaleTypeTest : CameraSessionTest() {

    @Test
    fun test_preview_scale_type() {
        initCameraSession()

        ScaleType.entries.forEach { scaleType ->
            updateSession(scaleType = scaleType)

            assertEquals(cameraSession.state.scaleType.value, scaleType)
        }
    }
}
```

**Pattern:** `initCameraSession()` → `updateSession(xxx = value)` → `assertEquals(state.xxx.value, value)`

## Type B — Hardware-Applied Property (e.g. FlashMode)

Property set via `controller.setXxx()`. May have a capability check (`isXxxSupported`).

```kotlin
internal class CameraFlashModeTest : CameraSessionTest() {

    @Test
    fun test_preview_all_flash_mode() {
        initCameraSession()

        FlashMode.entries.forEach { expected ->
            controller.setFlashMode(expected)

            assertFlashMode(expected)
            assertTrue(cameraSession.info.state.value.isFlashSupported)
        }
    }

    @Test
    fun test_preview_flash_mode_on_with_no_support() {
        cameraTest.isFlashSupported = false  // set BEFORE initCameraSession()

        initCameraSession()

        val result = controller.setFlashMode(FlashMode.On)

        assertTrue(result.isFailure)
        assertFalse(cameraSession.info.state.value.isFlashSupported)
        assertFlashMode(FlashMode.Off)  // state unchanged
    }

    private fun assertFlashMode(expected: FlashMode) {
        cameraTest.assertFlashMode(expected)                    // fake recorded it
        assertEquals(expected, cameraSession.state.flashMode.value)  // state matches
    }
}
```

**Pattern:** `initCameraSession()` → `controller.setXxx(value)` → `cameraTest.assertXxx(value)` + `assertEquals(state.xxx.value, value)`

**Capability-unsupported pattern:** set `cameraTest.isXxxSupported = false` → `initCameraSession()` → `controller.setXxx(...)` → `assertTrue(result.isFailure)` → assert state unchanged.

## Async Tests (e.g. Zoom)

Wrap in `runTest` when the property flows through coroutines:

```kotlin
@Test
fun test_preview_zoom_change() = runTest {
    initCameraSession()

    val expected = 4F
    cameraSession.controller.setZoomRatio(expected)

    cameraTest.assertZoomRatio(expected)
    assertEquals(cameraSession.state.zoomRatio.value, expected)
}
```

## Required Test Cases

For every new property, cover:

1. **Happy path** — each enum value (or representative values) applied correctly
2. **Unsupported capability** — `result.isFailure`, state unchanged _(Type B with capability check only)_
3. **Boundary values** — min/max for numeric properties (zoom, exposure)

## Common Mistakes

| Mistake | Fix |
|---------|-----|
| Setting `cameraTest.isXxxSupported` after `initCameraSession()` | Must be set BEFORE — fake reads flags at init |
| Asserting only `cameraSession.state` without `cameraTest.assertXxx()` | Always assert both — state and fake recording |
| Using `runTest` for sync property tests | Only needed for properties that emit through coroutine flows asynchronously |
| Checking state before calling `controller.setXxx()` | Call the setter first, then assert |

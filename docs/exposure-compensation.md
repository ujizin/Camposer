# Exposure Compensation

The exposureCompensation parameter allows you to manually adjust the camera’s exposure level, controlling how bright or dark the preview and captured images appear.

This value is represented as a Float, and its valid range depends on the active camera.

## Checking Supported Range

You can check the minimum and maximum supported exposure values for the current camera using:

```kotlin
import com.ujizin.camposer.lifecycle.compose.collectStateWithLifecycle

val cameraInfoState by cameraSession.info.collectStateWithLifecycle()
val minExposure = cameraInfoState.minExposure
val maxExposure = cameraInfoState.maxExposure
```

To set the desired exposure compensation, use the CameraController.

```Kotlin
cameraController.setExposureCompensation(1F)
```

## Usage Example

```kotlin
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ujizin.camposer.lifecycle.compose.collectStateWithLifecycle

val controller = remember { CameraController() }
val cameraSession = rememberCameraSession(controller)
val cameraInfoState by cameraSession.info.collectStateWithLifecycle()
val minExposure = cameraInfoState.minExposure
val maxExposure = cameraInfoState.maxExposure
val exposureCompensation by cameraSession.state.exposureCompensation.collectAsStateWithLifecycle()

CameraPreview(
    cameraSession = cameraSession,
) {
    Row {
        Button(onClick = {
            val exposure = (exposureCompensation - 1F).coerceAtLeast(minExposure)
            controller.setExposureCompensation(exposure)
        }) {
            Text("-")
        }

        Text("Exposure: $exposureCompensation")

        Button(onClick = {
            val exposure = (exposureCompensation + 1f).coerceAtMost(maxExposure)
            controller.setExposureCompensation(exposure)
        }) {
            Text("+")
        }
    }
}
```

!!! note "Desktop (JVM)"
    On desktop the value is an EV offset in stops, from `-3` to `+3`, relative to the automatic
    exposure the backend reported when the camera opened. `0` restores automatic exposure.
    Exposure control is available with the Windows (Media Foundation, DirectShow) and Linux
    (V4L2) backends. macOS (AVFoundation) exposes no exposure control through OpenCV, so
    `isExposureSupported` is `false` there.

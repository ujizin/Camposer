# Exposure Compensation

The exposureCompensation parameter allows you to manually adjust the camera’s exposure level, controlling how bright or dark the preview and captured images appear.

This value is represented as a Float, and its valid range depends on the active camera.

## Checking Supported Range

You can check the minimum and maximum supported exposure values for the current camera using:

```kotlin
val minExposure = cameraSession.info.minExposure
val maxExposure = cameraSession.info.maxExposure
```

To set the desired exposure compensation, use the CameraController.

```Kotlin
cameraController.setExposureCompensation(1F)
```

## Usage Example

```kotlin
val controller = remember { CameraController() }
val cameraSession = rememberCameraSession(controller)
val minExposure by rememberUpdateState(cameraSession.info.minExposure)
val maxExposure by rememberUpdateState(cameraSession.info.maxExposure)
val exposureCompensation by rememberUpdateState(cameraSession.state.exposureCompensation)

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
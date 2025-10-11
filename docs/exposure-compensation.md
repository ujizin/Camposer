# Exposure Compensation

The exposureCompensation parameter allows you to manually adjust the camera’s exposure level, controlling how bright or dark the preview and captured images appear.

This value is represented as a Float, and its valid range depends on the active camera.

## Checking Supported Range

You can check the minimum and maximum supported exposure values for the current camera using:

```kotlin
val minExposure = cameraState.info.minExposure
val maxExposure = cameraState.info.maxExposure
```

## Usage Example

```kotlin
val cameraState = rememberCameraState()
var exposureCompensation by remember { mutableStateOf(0f) }

CameraPreview(
    cameraState = cameraState,
    exposureCompensation = exposureCompensation,
) {
    val minExposure = cameraState.info.minExposure
    val maxExposure = cameraState.info.maxExposure

    Row {
        Button(onClick = {
            exposureCompensation = (exposureCompensation - 1f)
                .coerceAtLeast(minExposure)
        }) {
            Text("-")
        }

        Text("Exposure: $exposureCompensation")

        Button(onClick = {
            exposureCompensation = (exposureCompensation + 1f)
                .coerceAtMost(maxExposure)
        }) {
            Text("+")
        }
    }
}
```
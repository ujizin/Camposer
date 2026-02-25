# Capture Mode

The `captureMode` parameter defines the current operating mode of the camera, either photo capture or video recording.

Camposer provides two available modes:

- CaptureMode.Image (default) - Enables still image capture mode
- CaptureMode.Video - Enables video recording mode.

```kotlin
var captureMode by remember { mutableStateOf(CaptureMode.Image) }
val cameraSession = rememberCameraSession()

CameraPreview(
    cameraSession = cameraSession,
    captureMode = captureMode,
) {
    Button(
        onClick = {
            captureMode = when (captureMode) {
                CaptureMode.Image -> CaptureMode.Video
                CaptureMode.Video -> CaptureMode.Image
            }
        }
    ) {
        Text(
            "Switch to ${if (captureMode == CaptureMode.Image) "Video" else "Image"} Mode"
        )
    }
}
```

To capture photos or record videos, see the following sections for detailed usage examples and best practices.

## Notes

- When set to `CaptureMode.Image`, the controller enables still photo functionality.
- When set to `CaptureMode.Video`, recording related APIs become available.


# Mirror Mode

To enable mirror mode, configure it through the `CameraController`. The following options are available:

- **MirrorMode.On**: Always enabled
- **MirrorMode.OnlyInFront**: Enabled only when using the front camera
- **MirrorMode.Off**: Disabled

This mode applies when capturing a photo or recording a video.

### Usage example
```Kotlin
val cameraController = remember { CameraController() }
val cameraSession = rememberCameraSession(cameraController)
val mirrorMode by cameraSession.state.mirrorMode.collectAsStateWithLifecycle()

CameraPreview(
    cameraSession = cameraSession,
) {
    Button(
        onClick = { cameraController.setMirrorMode(
            when (mirrorMode) {
                MirrorMode.Off -> MirrorMode.On
                MirrorMode.On -> MirrorMode.OnlyInFront
                else -> MirrorMode.Off
            }
        ) },
    ) {
        Text("Mirror mode: $mirrorMode")
    }
}
```

# Flash Mode & Torch

## Introduction

To configure flash mode, you will need to set in `CameraController`. This will ensure stability and prevent bugs or crashes when some camera info configuration is changed.

It's also important to say that flash needs to be supported by camera, to check this info you can access:

```kotlin
// Check if flash mode is supported
val isFlashModeSupported by rememberUpdatedState(cameraSession.info.isFlashModeSupported)

// Check if torch is supported
val isTorchSupported by rememberUpdatedState(cameraSession.info.isTorchSupported)
```

## Usage Example

The following example demonstrates initializing flash mode and toggling it via a button. By default, the flash starts in the Off state:

```kotlin
val cameraController = remember { CameraController() }
val cameraSession = rememberCameraSession(cameraController)
val flashMode by rememberUpdatedState(cameraSession.state.flashMode)
val isTorchEnabled by rememberUpdatedState(cameraSession.state.isTorchEnabled)

CameraPreview(
    cameraSession = cameraSession,
    flashMode = flashMode
) {
    Button(
        onClick = { cameraController.setFlashMode(flashMode.reverse) } // Options: .Off, .On, .Auto (default: Off)
    ) {
        Text("Flash $flashMode")
    }

    Button(
        onClick = { cameraController.setTorchEnabled(flashMode.reverse) } // Options: true, false
    ) {
        Text("Torch $isTorchEnabled")
    }
}
```

# Flash Mode & Torch

## Introduction

To configure flash mode, you need to use the `CameraController`. This ensures stability and prevents bugs or crashes when camera configuration changes.

It's also important to note that flash needs to be supported by the camera. To check this, you can access:

```kotlin
// Check if flash is supported
val isFlashSupported by rememberUpdatedState(cameraSession.info.isFlashSupported)

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
        onClick = { cameraController.setTorchEnabled(!isTorchEnabled) } // Options: true, false
    ) {
        Text("Torch $isTorchEnabled")
    }
}
```

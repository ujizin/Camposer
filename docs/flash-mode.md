# Flash Mode & Torch

## Introduction

To configure flash mode, you need to use the `CameraController`. This ensures stability and prevents bugs or crashes when camera configuration changes.

It's also important to note that flash needs to be supported by the camera. To check this, you can access:

```kotlin
val cameraInfoState by cameraSession.info.collectStateWithLifecycle()

// Check if flash is supported
val isFlashSupported = cameraInfoState.isFlashSupported

// Check if torch is supported
val isTorchSupported = cameraInfoState.isTorchSupported
```

## Usage Example

The following example demonstrates initializing flash mode and toggling it via a button. By default, the flash starts in the Off state:

```kotlin
val cameraController = remember { CameraController() }
val cameraSession = rememberCameraSession(cameraController)
val flashMode by cameraSession.state.flashMode.collectAsStateWithLifecycle()
val isTorchEnabled by cameraSession.state.isTorchEnabled.collectAsStateWithLifecycle()

CameraPreview(
    cameraSession = cameraSession
) {
    Button(
        onClick = { cameraController.setFlashMode(flashMode.inverse) } // Options: .Off, .On, .Auto (default: Off)
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

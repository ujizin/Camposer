# Flash Mode

## Introduction

Configuring flash differs slightly from other camera settings. To ensure stability and prevent bugs or crashes, flash mode should be managed using CameraState’s remember functions.

## CameraState.rememberFlashMode

`CameraState.rememberFlashMode` is a composable helper that stores and manages the flash mode state for a camera preview. It ensures that flash configuration:

- Persists across recompositions and configuration changes (such as screen rotations).
- Automatically respects the capabilities of the currently selected camera, disabling flash when it is not available.

## Usage Example

The following example demonstrates initializing flash mode and toggling it via a button. By default, the flash starts in the Off state:

```kotlin
val cameraState = rememberCameraState()
var flashMode by cameraState.rememberFlashMode(
    initialFlashMode = FlashMode.Off, // Options: .Off, .On, .Auto (default: Off)
    useSaver = true // Automatically restore flash mode on configuration changes (default: true)
)

CameraPreview(
    cameraState = cameraState,
    flashMode = flashMode
) {
    Button(
        onClick = { flashMode = flashMode.reverse }
    ) {
        Text("Flash $flashMode")
    }
}
```
# Torch

## Introduction
The torch feature provides a continuous light source, unlike flash mode, which activates the light only when capturing a photo. When enabled, the torch remains on until explicitly turned off.

## CameraState.rememberTorch

`cameraState.rememberTorch`  is a composable helper that stores and manages the torch state for a camera preview. It ensures that flash configuration:

- Persists across recompositions and configuration changes (such as screen rotations).
- Automatically respects the capabilities of the currently selected camera, disabling flash when it is not available.

## Usage Example

```kotlin
val cameraState = rememberCameraState()
var torch by cameraState.rememberTorch(initialTorch = false)

CameraPreview(
    cameraState = cameraState,
    torch = torch
) {
    Button(onClick = { torch = !torch }) {
        Text("Torch $torch")
    }
}
```
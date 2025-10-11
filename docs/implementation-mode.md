# Implementation Mode

## Introduction

The implementationMode property affects **Android only**. It determines how the camera preview is rendered:

`ImplementationMode.Performance` (default) – Uses `SurfaceView`. High performance, minimal transformations (scaling/rotation) supported. Best for performance-critical apps.

`ImplementationMode.Compatible` – Uses `TextureView`. Slightly lower performance but supports transformations and visual effects. Useful for backward compatibility or when transformations are needed.

**Note:** this mode is particularly helpful when you need to overlay UI elements (buttons, Compose layouts, animations) directly on top of the camera preview, as SurfaceView may render below other UI layers.

## Usage example

```kotlin
var implementationMode by remember { mutableStateOf(ImplementationMode.Compatible) }

CameraPreview(
    implementationMode = implementationMode
) {
    Button(onClick = { implementationMode = implementationMode.reverse }) {
        Text("Switch Implementation Mode")
    }
}
```

# Zoom

## Zoom support

Zoom can be initialized and managed as state within Compose. The following example demonstrates how to set up and control zoom using `CameraController`:

```kotlin
val cameraSession = rememberCameraSession()
val zoomRatio by cameraSession.state.zoomRatio.collectAsStateWithLifecycle()
val cameraInfoState by cameraSession.info.collectStateWithLifecycle()
val minZoom = cameraInfoState.minZoom
val maxZoom = cameraInfoState.maxZoom

CameraPreview(
    cameraSession = cameraSession,
    isPinchToZoomEnabled = true // Default is already true
) {

    Button(
        onClick = { 
            val zoom = (zoomRatio + 1F).coerceIn(minZoom, maxZoom)
            cameraSession.controller.setZoomRatio(zoom)
        }
    ) {
        Text("Zoom ratio: $zoomRatio")
    }
}
```

**Note:**
The `isPinchToZoomEnabled` property is enabled by default. Pinch gestures automatically update the zoom ratio through the camera controller.

## Camera Zoom Properties

The `cameraSession` object exposes several properties that may be useful when implementing zoom functionality:

`cameraSession.info.state.value.minZoom` – Returns the minimum zoom level for the current [CamSelector](./camera-selector.md).

`cameraSession.info.state.value.maxZoom` – Returns the maximum zoom level for the current [CamSelector](./camera-selector.md).

`cameraSession.info.state.value.isZoomSupported` – Returns true if zoom is supported by the current [CamSelector](./camera-selector.md), false otherwise.

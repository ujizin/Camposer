# Zoom

## Zoom support

Zoom can be initialized and managed as a state within Compose. The following example demonstrates how to set up zoom for a camera preview:

```kotlin
val cameraState = rememberCameraState()
var zoomRatio by remember { mutableStateOf(cameraState.minZoom) }

CameraPreview(
    cameraState = cameraState,
    zoomRatio = zoomRatio,
    onZoomRatioChanged = { zoomRatio = it },
    isPinchToZoomEnabled = true // Default is already true
)
```

**Note:**
The `isPinchToZoomEnabled` property only functions when both zoomRatio and onZoomRatioChanged are set. Without these, CameraPreview remains stateless, and pinch-to-zoom gestures will not affect the zoom.

## Camera Zoom Properties

The `CameraState` object exposes several properties that may be useful when implementing zoom functionality:

`cameraState.info.minZoom` – Returns the minimum zoom level for the current [CamSelector](./camera-selector.md).

`cameraState.info.maxZoom` – Returns the maximum zoom level for the current [CamSelector](./camera-selector.md).

`cameraState.info.isZoomSupported` – Returns true if zoom is supported by the current [CamSelector](./camera-selector.md), false otherwise.
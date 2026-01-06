# Zoom

## Zoom support

Zoom can be initialized and managed as state within Compose. The following example demonstrates how to set up and control zoom using `CameraController`:

```kotlin
val cameraSession = rememberCameraSession()
val zoomRatio by rememberUpdatedState(cameraSession.state.zoomRatio)
val minZoom by rememberUpdatedState(cameraSession.info.minZoom)
val maxZoom by rememberUpdatedState(cameraSession.info.maxZoom)

CameraPreview(
    cameraSession = cameraSession,
    isPinchToZoomEnabled = true // Default is already true
) {

    Button(
        onClick = { 
            val zoom = (zoomRatio + 1F).coerceIn(minZoom, maxZoom)
            controller.setZoomRatio(zoom)
        }
    ) {
        Text("Zoom ratio: $zoomRatio")
    }
}
```

**Note:**
The `isPinchToZoomEnabled` property only functions when both zoomRatio and onZoomRatioChanged are set. Without these, CameraPreview remains stateless, and pinch-to-zoom gestures will not affect the zoom.

## Camera Zoom Properties

The `cameraSession` object exposes several properties that may be useful when implementing zoom functionality:

`cameraSession.info.minZoom` – Returns the minimum zoom level for the current [CamSelector](./camera-selector.md).

`cameraSession.info.maxZoom` – Returns the maximum zoom level for the current [CamSelector](./camera-selector.md).

`cameraSession.info.isZoomSupported` – Returns true if zoom is supported by the current [CamSelector](./camera-selector.md), false otherwise.
# Camera Session

## Introduction

`CameraSession` is the central component that manages the camera lifecycle and provides access to the camera's state, information, and controller. It serves as the main bridge between the UI and the camera hardware.

## Creating a Camera Session

Use `rememberCameraSession` to create and remember a camera session across recompositions:

```kotlin
@Composable
fun MyCameraScreen() {
    val cameraController = remember { CameraController() }
    val cameraSession = rememberCameraSession(cameraController)
    
    CameraPreview(cameraSession = cameraSession)
}
```

Alternatively, you can create a session without explicitly passing a controller:

```kotlin
val cameraSession = rememberCameraSession()
```

In this case, a controller is created internally and can be accessed via `cameraSession.controller`.

## Components

`CameraSession` provides access to three main components:

### 1. State (`cameraSession.state`)

The `state` holds all mutable camera configurations such as:

- `captureMode`: Image or Video mode
- `camSelector`: Front or Back camera
- `flashMode`: Flash settings (Off, On, Auto)
- `zoomRatio`: Current zoom level
- `exposureCompensation`: Exposure adjustment
- `isTorchEnabled`: Torch on/off state
- `mirrorMode`: Mirror mode configuration
- And many more camera settings

**Example:**
```kotlin
val cameraSession = rememberCameraSession()
val flashMode by cameraSession.state.flashMode.collectAsStateWithLifecycle()
val zoomRatio by cameraSession.state.zoomRatio.collectAsStateWithLifecycle()
```

### 2. Info (`cameraSession.info`)

The `info` provides read-only hardware capabilities of the current camera and limits through `CameraInfoState`:

- `minZoom` / `maxZoom`: Zoom range
- `minExposure` / `maxExposure`: Exposure range
- `isFlashSupported`: Flash availability
- `isTorchSupported`: Torch availability
- `isFocusSupported`: Focus capability
- `minFPS` / `maxFPS`: Frame rate limits
- `photoFormats` / `videoFormats`: Supported capture formats

**Example:**
```kotlin
val cameraSession = rememberCameraSession()
val cameraInfoState by cameraSession.info.collectAsStateWithLifecycle()
val isFlashSupported = cameraInfoState.isFlashSupported
val maxZoom = cameraInfoState.maxZoom

if (isFlashSupported) {
    // Show flash button
}
```

### 3. Controller (`cameraSession.controller`)

The `controller` provides methods to perform camera operations:

- `takePicture()`: Capture a photo
- `startRecording()` / `stopRecording()`: Record video
- `setZoomRatio()`: Adjust zoom
- `setFlashMode()`: Change flash mode
- `setExposureCompensation()`: Adjust exposure
- And more camera actions

**Example:**
```kotlin
val controller = remember { CameraController() }
val cameraSession = rememberCameraSession(controller)

Button(onClick = {
    controller.takePicture { result ->
        when(result) {
            is CaptureResult.Success -> { /* Handle success */ }
            is CaptureResult.Error -> { /* Handle error */ }
        }
    }
}) {
    Text("Take Picture")
}
```

## Session Status Properties

### isStreaming

Indicates whether the camera is currently streaming (preview is active).

```kotlin
val cameraSession = rememberCameraSession()
val isStreaming by rememberUpdatedState(cameraSession.isStreaming)

if (isStreaming) {
    // Camera preview is active
}
```

### isInitialized

Indicates whether the camera session has been fully initialized.

```kotlin
val cameraSession = rememberCameraSession()
val isInitialized by rememberUpdatedState(cameraSession.isInitialized)

if (isInitialized) {
    // Camera is ready to use
}
```

### hasInitializationError

Indicates whether the camera initialization failed. This is useful for showing error UI or implementing retry logic.

```kotlin
val cameraSession = rememberCameraSession()
val hasError by rememberUpdatedState(cameraSession.hasInitializationError)

if (hasError) {
    // Show error message and retry button
    Column {
        Text("Failed to initialize camera")
        Button(onClick = {
            if (cameraSession.retryInitialization()) {
                // Retry successful, camera initialized
            } else {
                // Retry failed, still has error
            }
        }) {
            Text("Retry")
        }
    }
}
```

**Retry Initialization**: Use `cameraSession.retryInitialization()` to attempt initialization again after a failure. The method returns `true` if successful, `false` if it still fails.
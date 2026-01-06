# Orientation Strategy

The Output Orientation strategy determines how captured media (images or videos) is rotated. 

It defines whether rotation is applied via metadata (e.g., EXIF tags) or by physically rotating the pixels, based on different sources of truth.

!!! info 
    On Android, images often use EXIF orientation tags. To display images or videos correctly, use libraries like Coil, Glide, or ExoPlayer that support EXIF rotation.

## Orientation Options

- **Preview**: Matches the camera preview’s UI orientation. Provides a “what you see is what you get” result, regardless of device rotation.
    
    !!! warning
        Unfortunately, Preview mode is currently supported only on iOS.

- **Device**: Matches the device’s physical orientation. The output reflects how the device is held, even if the UI is locked.

## Usage example

```kotlin
val cameraController = remember { CameraController() }
val cameraSession = rememberCameraSession(cameraController)
val orientationStrategy by rememberUpdatedState(cameraSession.state.orientationStrategy)
CameraPreview(
    cameraSession = cameraSession,
) {

    Button(
        onClick = { 
            cameraController.setOrientationStrategy(
                when(orientationStrategy) {
                    OrientationStrategy.Preview -> OrientationStrategy.Device
                    else -> OrientationStrategy.Preview
                    
                }
            )
        },
    ) {
        Text("Orientation strategy: $orientationStrategy")
    }
}
```
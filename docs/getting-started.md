# Getting Started

## Permissions

Before using Camposer, you must request Camera permission, and Audio/Microphone permission if recording video.

Camposer does not provide an API for requesting permissions. You can handle this manually or use a third-party library such as [moko-permissions](https://github.com/icerockdev/moko-permissions).

Ensure permissions are granted before creating or displaying the `CameraPreview` composable to avoid crashes or undefined behavior.

## Adding the Dependency

To include Camposer in your project, add the dependency to the `commonMain.dependencies` block in your `build.gradle.kts` file:

```kotlin
implementation("io.github.ujizin:camposer:<version>")
```

You can find the latest version at the top-right corner of this documentation or on the [Camposer GitHub page](https://github.com/ujizin/camposer).
## Using CameraPreview Compose

Camposer provides the CameraPreview composable, which displays a live camera feed directly within your Compose UI. It serves as the main entry point for integrating camera functionality into your app.

```kotlin
@Composable
fun MyScreen() {
    val cameraController = remember { CameraController() }
    val cameraSession = remembercameraSession(cameraController)
    var camSelector by rememberCamSelector(CamSelector.Back)
    CameraPreview(
        cameraSession = cameraSession,
        camSelector = camSelector,
    ) {
        // Camera Preview UI
    }
}

```

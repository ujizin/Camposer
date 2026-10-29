# Getting Started

## Permissions

Before using Camposer, you must request Camera permission, and Audio/Microphone permission if recording video.

You can use the optional [camposer-permissions](permissions.md) module to request camera and microphone permissions in Compose. Alternatively, handle permissions manually or use a third-party library such as [moko-permissions](https://github.com/icerockdev/moko-permissions).

Ensure permissions are granted before creating or displaying the `CameraPreview` composable to avoid crashes or undefined behavior.

## Adding the Dependency

To include Camposer in your project, add the dependency to the `commonMain.dependencies` block in your `build.gradle.kts` file:

```kotlin
implementation("io.github.ujizin:camposer:<version>")
```

You can find the latest version at the top-right corner of this documentation or on the [Camposer GitHub page](https://github.com/ujizin/camposer).

### Desktop (JVM)

The same dependency works for Compose Desktop. The desktop target captures video with OpenCV and
encodes recordings with FFmpeg through [JavaCV](https://github.com/bytedeco/javacv). The native
binaries for macOS (Apple silicon and Intel), Windows x64 and Linux x64 ship with the library, so
no extra setup is needed. The published artifact is large because of them.

!!! info "macOS camera prompt"
    macOS asks for camera and microphone access the first time a device opens. When you package
    the application with `nativeDistributions`, declare the usage descriptions in the `Info.plist`:

    ```kotlin
    compose.desktop {
        application {
            nativeDistributions {
                macOS {
                    infoPlist {
                        extraKeysRawXml = """
                            <key>NSCameraUsageDescription</key>
                            <string>Required to show the camera preview.</string>
                            <key>NSMicrophoneUsageDescription</key>
                            <string>Required to record audio with video.</string>
                        """.trimIndent()
                    }
                }
            }
        }
    }
    ```

## Using CameraPreview Compose

Camposer provides the CameraPreview composable, which displays a live camera feed directly within your Compose UI. It serves as the main entry point for integrating camera functionality into your app.

```kotlin
@Composable
fun MyScreen() {
    val cameraController = remember { CameraController() }
    val cameraSession = rememberCameraSession(cameraController)
    var camSelector by rememberCamSelector(CamSelector.Back)
    CameraPreview(
        cameraSession = cameraSession,
        camSelector = camSelector,
    ) {
        // Camera Preview UI
    }
}

```

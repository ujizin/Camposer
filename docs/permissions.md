# Permissions

## Camera and microphone permissions

Camposer provides an optional add-on library for handling camera and microphone permissions. It is not a general-purpose permissions library — it covers only the two permissions required for camera and audio capture.

### Installation

Add the library to your project:

```kotlin
implementation("io.github.ujizin:camposer-permissions:<version>")
```

!!! info
    The latest version can be found at the top-right corner of this documentation or on the [Camposer GitHub page](https://github.com/ujizin/camposer).

### Setup

Before using the library, declare the required permissions in your platform configuration.

**Android** — add to `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

**iOS** — add to `Info.plist`:

```xml
<key>NSCameraUsageDescription</key>
<string>Required to capture photos and video.</string>
<key>NSMicrophoneUsageDescription</key>
<string>Required to record audio with video.</string>
```

### Usage example

```kotlin
val cameraPermission = rememberCameraPermissionState()
val audioPermission = rememberAudioPermissionState()

when (val status = cameraPermission.status) {
    PermissionStatus.Granted -> CameraContent()
    is PermissionStatus.Denied -> Button(
        onClick = {
            if (status.canRequestAgain) {
                cameraPermission.launchPermissionRequest()
            } else {
                cameraPermission.openAppSettings()
            }
        },
    ) { Text("Grant camera permission") }
}
```

Both `rememberCameraPermissionState` and `rememberAudioPermissionState` accept an optional `onPermissionResult: (Boolean) -> Unit` callback that is invoked only when the user responds to an explicit permission request. The `status` property refreshes automatically when the app returns to the foreground.

### Combining permissions

There is no combined camera-and-audio state. Check both states in your own code:

```kotlin
val hasAll = cameraPermission.status.isGranted && audioPermission.status.isGranted
```

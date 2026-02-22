# Camposer

<p align="center">
 <img src="docs/assets/ic_launcher_round.png" width="200"/>
</p>
<p align="center">
  <a href="https://search.maven.org/search?q=g:%22io.github.ujizin%22%20AND%20a:%22camposer%22">
  <img src="https://img.shields.io/maven-metadata/v.svg?color=dark-green&label=Maven%20Central&metadataUrl=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Fio%2Fgithub%2Fujizin%2Fcamposer%2Fmaven-metadata.xml"/></a>
  <img alt="Static Badge" src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android">
  <img alt="Static Badge" src="https://img.shields.io/badge/Platform-iOS-F5F5F7?logo=ios"> <br/>
  <a href="https://androidweekly.net/issues/issue-546"><img src="https://androidweekly.net/issues/issue-546/badge"/></a>
</p>

<p align="center">A multiplatform camera library built with Jetpack Compose that supports taking photos, recording videos, flash modes, zoom ratios, and more.</p>
<br>
<p align="center">
<img src="https://user-images.githubusercontent.com/51065868/201734193-053dd4f5-c9cb-4a62-9692-1a62264911a5.gif" width="200"/> <img src="https://user-images.githubusercontent.com/51065868/201736304-f1f1b5fa-3f3d-4c12-9d40-a790e0d4d82b.gif" width="200"/>
</p>

<p align="center"><small>Check out the <a href="https://github.com/ujizin/Camposer/tree/main/sample">Sample project</a></small></p>

## Quickstart

Add dependencies to your module `build.gradle.kts` and sync your project:

```kotlin
// Android
dependencies {
  implementation("io.github.ujizin:camposer:0.5.0")
}

// Kotlin Multiplatform
sourceSets {
  commonMain.dependencies {
    implementation("io.github.ujizin:camposer:<version>")
    // Required when you want to use code analysis features (e.g., QR code scanning).
    implementation("io.github.ujizin:camposer-code-scanner:<version>")
  }
}

```

### How to use

To add the `CameraPreview` composable, use the example below:

```Kotlin
val controller = remember { CameraController() }
val cameraSession = rememberCameraSession(controller)
var camSelector by remember { mutableStateOf(CamSelector.Back) }
CameraPreview(
  cameraSession = cameraSession,
  camSelector = camSelector,
) {
  // Camera Preview UI
}
```

## Documentation

Visit the documentation to learn more: https://ujizin.github.io/Camposer

## Usage examples

### Taking pictures

To take a picture, use `CameraSession` and call its `takePicture` method.

```Kotlin
// Using temporary byte array
cameraSession.takePicture { result ->
  /* ... */
}

// Using files
cameraSession.takePicture(fileName) { result -> /* ... */ }
```

### Recording videos

Set `captureMode = CaptureMode.Video` on `CameraPreview`, then call `toggleRecording` twice to
stop recording, or use `startRecording` and `stopRecording` separately.

```Kotlin
// Using content values
cameraSession.startRecording(fileName) { result ->
  /* ... */
}
cameraSession.stopRecording()
```

### Switch cameras

To switch cameras, pass `camSelector` to your `CameraPreview` composable, as shown above, then
update its state.

```Kotlin
// Use front camera
camSelector = CamSelector.Front

// Use back camera
camSelector = CamSelector.Back

// Inverse camera selector
camSelector = camSelector.inverse
```

### More

To learn about and use additional features, check the [Documentation](https://ujizin.github.io/Camposer).

Have a `fun code()`!

## License

```
Copyright 2022 ujizin (Lucas Yuji) 

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

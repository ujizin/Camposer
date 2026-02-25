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

<p align="center">📸 A multiplatform camera library built with Jetpack Compose for taking photos, recording videos, controlling flash/torch, zooming, and more.</p>
<br>
<p align="center">
<img src="https://user-images.githubusercontent.com/51065868/201734193-053dd4f5-c9cb-4a62-9692-1a62264911a5.gif" width="175"/> <img src="https://user-images.githubusercontent.com/51065868/201736304-f1f1b5fa-3f3d-4c12-9d40-a790e0d4d82b.gif" width="175"/>
</p>

<p align="center"><small>✨ Check out the <a href="https://github.com/ujizin/Camposer/tree/main/samples">sample projects</a></small></p>

## 🚀 Quick Start

Add the dependencies to your module `build.gradle.kts`, then sync your project:

```kotlin
// Android
dependencies {
  implementation("io.github.ujizin:camposer:<version>")
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

### 🧩 Basic Usage

To show the `CameraPreview` composable, use the example below:

```kotlin
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

## ✨ Features

- 📸 Take pictures
- 🎥 Record videos
- 🔍 Zoom
- 🎯 Focus support (tap to focus)
- ⚡ Flash mode
- 🔦 Torch
- 🎛️ Exposure compensation
- 🖼️ Image/video quality controls
- 🔭 Multi-camera lens support (Ultra-wide, Wide & Telephoto)
- ⏱️ 30/60 FPS video recording
- 🎬 Video stabilization (iOS only for now)
- 🧠 Image analyzer (code scanner)

## 📚 Documentation

Visit the docs to learn more: [ujizin.github.io/Camposer](https://ujizin.github.io/Camposer)

## 🛠️ Usage Examples

### 📸 Taking Pictures

Use `CameraSession` and call `takePicture`:

```kotlin
// Capture into a temporary byte array
cameraSession.takePicture { result ->
  /* ... */
}

// Capture into a file
cameraSession.takePicture(fileName) { result -> /* ... */ }
```

### 🎥 Recording Videos

Set `captureMode = CaptureMode.Video` in `CameraPreview`, then call `startRecording` and `stopRecording`.

```kotlin
cameraSession.startRecording(fileName) { result ->
  /* ... */
}
cameraSession.stopRecording()
```

### 🔄 Switching Cameras

To switch cameras, pass `camSelector` to `CameraPreview` (as shown above) and update its state.

```kotlin
// Front camera
camSelector = CamSelector.Front

// Back camera
camSelector = CamSelector.Back

// Toggle camera selector
camSelector = camSelector.inverse
```

### ➕ More

To explore additional features, check the [documentation](https://ujizin.github.io/Camposer).

Have `fun code()`! 👨‍💻

## ✨ Inspiration

Camposer includes features inspired by [react-native-vision-camera](https://github.com/mrousavy/react-native-vision-camera).

## 📄 License

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

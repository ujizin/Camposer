# Camposer

<p align="center">
 <img src="sample/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png" />
</p>
<p align="center">
  <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
  <img src="https://github.com/ujizin/Camposer/actions/workflows/android_test.yml/badge.svg?branch=main"/>
  <img src="https://github.com/ujizin/Camposer/actions/workflows/build.yml/badge.svg?branch=main"/>
  <a href="https://android-arsenal.com/api?level=23"><img alt="API" src="https://img.shields.io/badge/API-23%2B-brightgreen.svg?style=flat"/></a>
  <a href="https://github.com/ujizin"><img alt="Profile" src="https://badgen.net/badge/ujizin/Github/orange?icon=github"/></a>
  <a href="https://ujizin.github.io/Camposer/"><img alt="Dokka" src="https://badgen.net/badge/Dokka/Camposer/purple?icon=libraries"/></a>
</p>

<p align="center">Tired to use a camera in Jetpack Compose with interoperability? Then Camposer was made for you. <br> A camera library totally in Jetpack Compose which supports taking photos, recording videos, flash modes, zoom ratio, and among others!</p>
<br>
<p align="center">
<img src="https://user-images.githubusercontent.com/51065868/201734193-053dd4f5-c9cb-4a62-9692-1a62264911a5.gif" width="250"/> <img src="https://user-images.githubusercontent.com/51065868/201736304-f1f1b5fa-3f3d-4c12-9d40-a790e0d4d82b.gif" width="250"/>
</p>

<p align="center"><small>Check out the <a href="https://github.com/ujizin/Camposer/tree/main/sample">Sample project</a></small></p>

## How it works

To add CameraPreview composable, just use the example below:

```Kotlin
val cameraState = rememberCameraState()
var camSelector = rememberCamSelector(CamSelector.Back)
CameraPreview(
  cameraState = cameraState,
  camSelector = camSelector,
) {
  // Camera Preview UI
}
```

### Take picture

```Kotlin
// Using content values
cameraState.takePicture(contentValues, mediaURI) { result ->
  /* ... */
}

// Using files
cameraState.takePicture(file) { result -> /* ... */ }
```

### Start recording

```Kotlin
// Using content values
cameraState.startRecording()
cameraState.stopRecording(contentValues, mediaURI) { result ->
  /* ... */
}

// Using file
cameraState.startRecording()
cameraState.stopRecording(file) { result -> /* ... */ }

// Using content values + toggle
cameraState.toggleRecording(contentValues, mediaURI) { result ->
  /* ... */
}

// Using files + toggle
cameraState.toggleRecording(file) { result -> /* ... */ }
```

### Switch cameras

To switch cameras, you have to add `camSelector` to your `CameraPreview` composable, as shown previously, after implementation, just need to change its state.

```Kotlin
// Use front camera
camSelector = CamSelector.Front

// Use back camera
camSelector = CamSelector.Back

// Reverse camera selector
camSelector = camSelector.reverse
```

###  Other configurations

If you want to use other configurations, you can see our [wiki](https://github.com/DevLucasYuji/Camposer/wiki).

## Setup

1. Add dependency to your `build.gradle`

```
implementation 'io.github.ujizin:camposer:0.1.0'
```
2. Sync your project
3. Have a `fun code()`!

##  License

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

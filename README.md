# Camposer

<img src="sample/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png" />

Tired to use CameraX with Jetpack Compose Interoperability? So Camposer was made for you, a camera using 100% Jetpack Compose which supports photos & videos.

## How it works

Add CameraPreview composable

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

And that's it! For more information, check the [wiki](https://github.com/DevLucasYuji/Camposer/wiki).

### Take picture

```Kotlin
// Using content values
cameraState.takePicture(contentValues,mediaURI) { result ->
  /* ... */
}

// Using files
cameraState.takePicture(file) { result -> /* ... */ }
```

### Start recording

```Kotlin
// Using content values
cameraState.startRecording()
cameraState.stopRecording(contentValues,mediaURI) { result ->
  /* ... */
}

// Using file
cameraState.startRecording()
cameraState.stopRecording(file) { result -> /* ... */ }

// Using content values + toggle
cameraState.toggleRecording(contentValues,mediaURI) { result ->
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

1. Add Jitpack to your `settings.gradle`
```
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // add Jitpack here
        maven { url 'https://jitpack.io' }
       ...
    }
}
```
2. Add dependency to your `build.gradle`

```
implementation 'com.ujizin.camposer:0.1.0'
```
3. Sync your project
4. Have a happy `fun code()`!

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

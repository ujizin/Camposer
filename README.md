# Camposer

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

And that's it! For more information, check the `wiki`.

### Take picture

```Kotlin
// Using content values
cameraState.takePicture(contentValues,mediaURI) { result -> 
	// result.savedUri
}

// Using files
cameraState.takePicture(file) { result -> /* result.savedUri */}
```

### Start recording

```Kotlin
// Using content values
cameraState.startRecording()
cameraState.stopRecording(contentValues,mediaURI) { result -> 
	// result.savedUri 
}

// Using file
cameraState.startRecording()
cameraState.stopRecording(file) { result -> /* result.savedUri */ }

// Using content values + toggle
cameraState.toggleRecording(contentValues,mediaURI) { result -> 
	// result.savedUri
}

// Using files + toggle
cameraState.toggleRecording(file) { result -> /* result.savedUri */}
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

If you want to use other configurations, you can see our `wiki`.

## Setup

1. Add itpack to your `settings.gradle`
```
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // add jitpack here 👇🏽
        maven { url 'https://jitpack.io' }
       ...
    }
}
```
2. Add dependency to your `build.gradle`

```
implementation 'br.com.devlucasyuji.camposer:0.1.0'
```
3. Sync your project
4. Have a happy `fun code()`!

##  License

```
Copyright (c) 2022 Lucas Yuji

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
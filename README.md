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
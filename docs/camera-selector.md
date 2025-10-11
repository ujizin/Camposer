# Camera Selector

## Introduction

The Camera Selector module provides a simple interface to switch between available cameras. Two predefined selectors are available:

`CamSelector.Back` – the device’s rear camera.

`CamSelector.Front` – the device’s front camera.

The selected camera can be managed as a Compose state, allowing seamless integration into composable functions.

## Usage Example

The following example demonstrates how to display a camera preview and switch between cameras:
```Kotlin
@Composable
fun Camera() {
  var camSelector by remember { mutableStateOf(CamSelector.Back) } // Or CamSelector.Front
  CameraPreview(camSelector = camSelector) {
    Button(onClick = {
      camSelector = camSelector.reverse // Switch Camera
    }) { 
       Text("Switch") 
    }
  }
}
```

## Camera Switch Callback

To handle events triggered by camera changes, the `onPreviewStreamChanged` callback is available:

`onPreviewStreamChanged`: Invoked whenever the camera preview stream changes

`onSwitchCameraContent: @Composable (ImageBitmap) -> Unit`: Composable invoked whenever the camera preview stream changes, providing the current frame as a Bitmap.


This can be used for tasks such as processing the camera feed or updating UI elements in response to camera switching.

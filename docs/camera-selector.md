# Camera Selector

## Introduction

The Camera Selector module provides a simple interface to switch between available cameras. Two predefined selectors are available:

`CamSelector.Back` – the device’s default rear camera.

`CamSelector.Front` – the device’s default front camera.

The selected camera can be managed as a Compose state, allowing seamless integration into composable functions.

### Usage Example

The following example demonstrates how to display a camera preview and switch between cameras:
```Kotlin
@Composable
fun Camera() {
  var camSelector by remember { mutableStateOf(CamSelector.Back) } // Or CamSelector.Front
  CameraPreview(camSelector = camSelector) {
    Button(onClick = {
      camSelector = camSelector.inverse // Switch Camera
    }) { 
       Text("Switch") 
    }
  }
}
```

## Custom Camera Selector

You can customize the camera selection by using `CamSelector`.
It allows you to specify the camera position and one or more lens types such as Wide, UltraWide, or Telephoto. <br/>

These options can be combined to define exactly which cameras are eligible for use.

### Usage example

```kotlin
@Composable
fun CameraPreviewScreen() {
  val controller = remember { CameraController() }
  val cameraSession = rememberCameraSession(controller)
  val camSelector = remember {
    CamSelector(
        camPosition = CamPosition.Back,
        camLensTypes = listOf(
          CamLensType.UltraWide,
          CamLensType.Wide,
          CamLensType.Telephoto,
        )
    )
  }

  CameraPreview(
    cameraSession = cameraSession,
    camSelector = camSelector,
  )
}
```

If the specified camera selector is not supported, the system will fall back to the closest available match.

> **Note:** multi-camera lens support on Android depends on CameraX. Having multiple lenses on a device does not necessarily mean they are supported.

## Custom own Camera Selector (Advanced)
For advanced use cases, you can also retrieve the list of available camera devices and explicitly select the desired one.

### Usage example

```Kotlin
@Composable
fun CameraPreviewScreen() {
  val controller = remember { CameraController() }
  val cameraSession = rememberCameraSession(controller)
  val cameraDevicesState = rememberCameraDeviceState()
  var camSelector by remember { mutableStateOf(CamSelector.Back) }

  LaunchedEffect(cameraDevicesState) {
    if (cameraDevicesState is CameraDeviceState.Devices) {
      val cameraDevices = cameraDevicesState.cameraDevices
      val selectedDevice = cameraDevices.find { /* your logic */ }
      if (selectedDevice != null) {
        camSelector = CamSelector(selectedDevice)
      }
    }
  }

  CameraPreview(
    cameraSession = cameraSession,
    camSelector = camSelector,
  )
}
```

## External Camera (Experimental)

Camposer also supports external cameras, such as [Continuity Camera Devices](https://support.apple.com/en-us/102546) on iOS, UVC cameras (mostly supported on iPad), or cameras supported by CameraX on Android.

## Camera Switch Callback (Android only)

To handle events triggered by camera changes, the `onPreviewStreamChanged` callback is available:

`onPreviewStreamChanged`: Invoked whenever the camera preview stream changes

`onSwitchCameraContent: @Composable (ImageBitmap) -> Unit`: Composable invoked whenever the camera preview stream changes, providing the current frame as a Bitmap.


This can be used for tasks such as processing the camera feed or updating UI elements in response to camera switching.

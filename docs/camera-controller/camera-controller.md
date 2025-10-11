# Camera Controller

## Introduction

The Camera Controller provides functionality for taking pictures and recording videos, separating these actions from UI state management. By using a controller, you can manage camera operations independently of the CameraPreview composable.

## Usage example

```kotlin
val controller = remember { CameraController() }
val cameraState = rememberCameraState(controller)

CameraPreview(
    cameraState = cameraState,
)
```

## Why Use a CameraController?

In earlier versions of Camposer, actions such as taking pictures or recording videos were handled directly in `CameraState`. This approach limited camera operations to the UI layer, making it difficult to separate logic from presentation.

With Camposer 1.0, the `CameraController` was introduced to decouple camera operations from UI logic, following clean architecture principles.

For example, you can instantiate the controller in a ViewModel and handle all camera actions there, keeping the UI layer focused solely on presentation.

### ViewModel Example
```kotlin
class MyViewModel : ViewModel() {

    val cameraController = CameraController()

    fun takePicture() {
        cameraController.takePicture(
            // Provide configuration and callbacks here
        )
    }

    fun startRecording() {
        cameraController.startRecording(
            // Provide configuration and callbacks here
        )
    }

    fun stopRecording() {
        cameraController.stopRecording()
    }
}
```

### View Example
```kotlin
@Composable
fun CameraScreen(viewModel: MyViewModel) {
    // You can also store the CameraController in your UI state, 
    // allowing it to persist across recompositions while keeping it 
    // separate from the composable UI.
    val cameraState = rememberCameraState(viewModel.cameraController)

    CameraPreview(cameraState = cameraState)

    Row {
        Button(onClick = viewModel::takePicture) {
            Text("Take Picture")
        }

        Button(onClick = viewModel::startRecording) {
            Text("Start Recording")
        }

        Button(onClick = viewModel::stopRecording) {
            Text("Stop Recording")
        }
    }
}
```

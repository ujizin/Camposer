# Taking Picture

The CameraController provides functionality for taking pictures. There are two types of results you can retrieve:

## ByteArray

Use this option when you do not want to save the picture to a file, but just retrieve its byte data.

### Usage Example

```kotlin
cameraController.takePicture { result -> 
    when(result) {
        CaptureResult.Success -> {
            uiState.update { it.copy(imageBitmap = result.data.decodeToImageBitmap()) }
        }
        CaptureResult.Error -> // ...
    }
}
```


## Path (from `kotlinx.io`)

Use this option when you want to save the picture to a specific file path and retrieve the saved file.

### Usage Example

```kotlin
val path = Path("$SystemTemporaryDirectory/photo.jpg")
cameraController.takePicture(path) { result -> 
    when(result) {
        CaptureResult.Success -> {
            uiState.update { it.copy(filePath = "${result.path}") }
        }
        CaptureResult.Error -> // ...
    }
}
```

## Android-Specific API

On Android, `CameraController.takePicture` supports additional options to provide more control over file handling:

- ContentValues - for saving to MediaStore.
- File - for saving to a specific location.
- OutputFileOptions - for advanced camera APIs.

These options allow you to implement custom expect/actual methods for platform-specific handling, ensuring flexibility across different use cases.
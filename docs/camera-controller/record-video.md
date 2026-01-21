# Recording video

The CameraController provides full functionality for recording videos, saving them to a specified file path (String).

## File Path

To record a video, you must provide a destination file path as a String:

```kotlin
val path = "/path/to/save/video.mp4"
```

## Start recording

Start video recording with startRecording. The callback provides the recording result:

```kotlin
cameraController.startRecording(path) { result ->
    when(result) {
        is CaptureResult.Success -> {
            uiState.update { it.copy(filePath = result.data) }
        }
        is CaptureResult.Error -> // ...
    }
}
```

- result.data contains the saved video file path on success.
- CaptureResult.Error indicates a failure during recording.

## Stop Recording

Stop recording using:

```kotlin
cameraController.stopRecording()
```

- Calling stopRecording finalizes the video and triggers the startRecording callback.
- To temporarily halt recording without finalizing the file, use `cameraController.pauseRecording`.

## Pause Recording

Pause an ongoing recording:

```kotlin
cameraController.pauseRecording()
```

## Resume Recording

Resume a paused recording:

```kotlin
cameraController.resumeRecording()
```

- `resumeRecording` continues the same recording session without creating a new file.

## Android-Specific API

On Android, `CameraController.startRecording` supports additional options to provide more control over file handling:

- ContentValues - for saving to MediaStore.
- File - for saving to a specific location.
- OutputFileOptions - for advanced camera APIs.

These options allow you to implement custom expect/actual methods for platform-specific handling, ensuring flexibility across different use cases.
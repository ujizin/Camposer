# Image Capture Strategy

The imageCaptureStrategy parameter defines how the camera optimizes the photo capture process, balancing between speed and image quality.

## Available Strategies

| Strategy | Android | iOS |
|-----------|----------------|--------------|
| **`ImageCaptureStrategy.MinLatency`** | `ImageCapture.CAPTURE_MODE_ZERO_SHUTTER_LAG` | `AVCapturePhotoQualityPrioritizationSpeed` |
| **`ImageCaptureStrategy.MaxQuality`** | `ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY` | `AVCapturePhotoQualityPrioritizationQuality` *(High Resolution enabled)* |
| **`ImageCaptureStrategy.Balanced`** | `ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY` | `AVCapturePhotoQualityPrioritizationBalanced` |

### Strategy Descriptions

- **MinLatency**: Prioritizes **speed**, minimizing delay between shutter press and capture. Best for burst or quick captures.  
  On Android devices without Zero Shutter Lag (ZSL) support, this automatically falls back to **Balanced**.

- **MaxQuality**: Prioritizes **maximum image quality**, applying extra processing and (on iOS) enabling high-resolution capture.  
  May increase capture latency slightly.

- **Balanced**: Provides a **middle ground** between latency and quality.  
  Recommended as the **default** option for consistent results across devices.

## Usage example

```kotlin
CameraPreview(
    // ...
    imageCaptureStrategy = ImageCaptureStrategy.Balanced // or MinLatency, MaxQuality
)
```
# Camera Format

## Introduction

Camera Format defines the output configuration for resolution, aspect ratio, frame rate (FPS), and video stabilization.

In most cases, the predefined resolution presets are sufficient:

- CamFormat.UltraHigh: 4K resolution (3840 × 2160)
- CamFormat.High: Full HD / 2K resolution (1920 × 1080)
- CamFormat.Medium: HD resolution (1280 × 720)
- CamFormat.Low: SD resolution (720 × 480)

The camera format automatically selects the best available match based on the provided values.

### Usage example

```Kotlin
// ...
val camFormat = CamFormat.High

CameraPreview(
    cameraSession = cameraSession,
    camFormat = camFormat,
)
```

### Custom Camera Format

For advanced configurations, such as higher frame rates or specific video stabilization modes, you can define a custom camera format by passing configuration options to the constructor. 

The order of these configurations matters, as Camposer prioritizes them sequentially.

!!! warning
    Some configurations are not yet supported on Android. For instance, `VideoStabilizationConfig` is currently limited by the CameraX controller and not possible to be set. 

    Additionally, `FrameRateConfig` may not work for values other than 24, 30, or 60 FPS on Android.

```Kotlin
// ...
val camFormat = remember {
    CamFormat(
        ResolutionConfig(3840, 2160),
        AspectRatio(16F / 9F),
        FrameRateConfig(60),
        VideoStabilizationConfig(VideoStabilizationMode.Standard)
    )
}

CameraPreview(
    cameraSession = cameraSession,
    camFormat = camFormat,
)
```

### Configuration Priority

The camera format uses a scoring and fallback mechanism. Based on the example above, it attempts to match configurations in the following order:

1. ResolutionConfig + AspectRatio + FrameRateConfig + VideoStabilizationConfig
2. ResolutionConfig + AspectRatio + FrameRateConfig
3. ResolutionConfig + AspectRatio + VideoStabilizationConfig
4. ResolutionConfig + AspectRatio
5. ResolutionConfig + FrameRateConfig
6. ResolutionConfig + VideoStabilizationConfig
7. ResolutionConfig

!!! info
    Support for some configurations on Android depends on CameraX. Having these features available in the device’s native camera does not necessarily mean they are supported by CameraX.

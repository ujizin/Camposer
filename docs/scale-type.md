# Scale Type

!!! platform "Supported on Android, iOS and Desktop platforms"

## Introduction

Camposer supports customizing how the camera preview content is scaled within its container using the scaleType property.

## Available Options

The following table shows the corresponding values for each platform when using Camposer’s preview scale types:

| Type | Android | iOS |
| -------- | ------- | ------- |
| FitStart | FIT_START | AVLayerVideoGravityResizeAspect | 
| FitCenter | FIT_CENTER | AVLayerVideoGravityResizeAspect |
| FitEnd | FIT_END | AVLayerVideoGravityResizeAspect | 
| FillStart | FILL_START | AVLayerVideoGravityResizeAspectFill |
| FillCenter | FILL_CENTER | AVLayerVideoGravityResizeAspectFill |
| FillEnd | FILL_END | AVLayerVideoGravityResizeAspectFill |

## Usage Example

```kotlin
CameraPreview(
  scaleType = ScaleType.FitStart // default is ScaleType.FillCenter
)
```

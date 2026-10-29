# Scale Type

## Introduction

Camposer supports customizing how the camera preview content is scaled within its container using the scaleType property.

## Available Options

The following table shows the corresponding values for each platform when using Camposer’s preview scale types:

| Type | Android | iOS | Desktop (JVM) |
| -------- | ------- | ------- | ------- |
| FitStart | FIT_START | AVLayerVideoGravityResizeAspect | Fit, aligned top-left |
| FitCenter | FIT_CENTER | AVLayerVideoGravityResizeAspect | Fit, centered |
| FitEnd | FIT_END | AVLayerVideoGravityResizeAspect | Fit, aligned bottom-right |
| FillStart | FILL_START | AVLayerVideoGravityResizeAspectFill | Fill, aligned top-left |
| FillCenter | FILL_CENTER | AVLayerVideoGravityResizeAspectFill | Fill, centered |
| FillEnd | FILL_END | AVLayerVideoGravityResizeAspectFill | Fill, aligned bottom-right |

## Usage Example

```kotlin
CameraPreview(
  scaleType = ScaleType.FitStart // default is ScaleType.FillCenter
)
```

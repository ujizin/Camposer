# Focus on Tap

## Introduction

Focus on Tap is enabled by default in Camposer on supported devices. Users can tap the camera preview to focus at a specific point.

## Check Focus-on-Tap Support

To determine whether the device supports focus on tap functionality, use the `isFocusOnTapSupported` property from cameraSession:

```kotlin
val isFocusOnTapSupported = cameraSession.info.isFocusOnTapSupported
```

## Disable Focus on Tap

```kotlin
val isFocusOnTapEnabled by remember { mutableStateOf(false) }

CameraPreview(
    // ...
    isFocusOnTapEnabled = isFocusOnTapEnabled
)
```

## Custom Focus Content

You can provide a custom composable for the focus indicator using focusTapContent:
```kotlin
CameraPreview(
    // ...
    focusTapContent = { 
        AwesomeFocusTapContent() 
    }
)
```

## Custom Focus UI Duration

The `CameraPreview.onFocus` parameter provides complete control over the focus indicator behavior. It is a suspendable callback that receives an onComplete function, which should be called when the focus layout should be removed.

**Default Behavior:** the focus layout disappears after 1 second.

```kotlin
CameraPreview(
    // ...
    onFocus = { onComplete ->
        delay(10_000L) // Keep focus UI visible for 10 seconds
        onComplete()
    }
)
```

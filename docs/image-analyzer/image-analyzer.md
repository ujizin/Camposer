# Image Analyzer (Custom)

## Introduction
If you need to implement a custom image analyzer, for example, using TensorFlow or other ML frameworks, you can extend Camposer’s built-in analyzer. 

This allows you to process camera frames directly while leveraging CameraPreview and cameraSession features.

## Defining a Custom Analyzer (Example)

### Common
```kotlin
fun interface OCRListener {
    fun onAnalyzed(result: String)
}

expect class OCRImageAnalyzer(
    listener: OCRListener
)
```

### Android
```kotlin
actual class OCRImageAnalyzer(
    private val listener: OCRListener
): ImageAnalyzer.Analyzer {
    override fun analyze(imageProxy: ImageProxy) {
        // code to analyze ...
        listener.onAnalyzed(result)
    }
}
```

### iOS
```kotlin
actual class OCRImageAnalyzer(
    private val listener: OCRListener
): NSObject(), , AVCaptureVideoDataOutputSampleBufferDelegate {
     override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection,
    ) {
        // code to analyze ...
        listener.onAnalyzed(result)
    }
}
```

## Using remember to Attach the Analyzer (Example)

### Common
```kotlin
@Composable
public expect fun cameraSession.rememberOCRAnalyzer(
    ocrAnalyzer: OCRAnalyzer,
): ImageAnalyzer
```

### Android

```kotlin
@Composable
public actual fun cameraSession.rememberOCRAnalyzer(
    ocrAnalyzer: OCRAnalyzer,
) = remember(ocrAnalyzer) {
    ImageAnalyzer(
        controller = controller,
        analyzer = OCRImageAnalyzer(ocrAnalyzer),
    )
}
```

### iOS

```kotlin
@Composable
public actual fun cameraSession.rememberOCRAnalyzer(
    ocrAnalyzer: OCRAnalyzer,
) = remember(ocrAnalyzer) {
    val queue = dispatch_queue_create("OCRAnalyzer_queue", null)
    val analyzer = OCRImageAnalyzer(ocrAnalyzer)
    ImageAnalyzer(
        iosCameraSession = iosCameraSession,
        analyzer = ImageAnalyzer.Analyzer(
            output = AVCaptureMetadataOutput(), // Or output needed to your case
            onOutputAttached =  { output ->
                output.setMetadataObjectsDelegate(analyzer, queue)
            }
        )
    )
}
```

### Attaching the Analyzer in Compose (Example)

```kotlin
val cameraSession = rememberCameraSession()
val ocrImageAnalyzer = cameraSession.rememberOCRAnalyzer {
    // Result here!
}

CameraPreview(
    cameraSession = cameraSession,
    imageAnalyzer = ocrImageAnalyzer,
)
```
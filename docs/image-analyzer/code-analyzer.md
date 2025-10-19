# Code Analyzer (Code Scan)

## Code scan

Camposer supports barcode and QR code scanning via an add-on library.

### Installation

Add the library to your project:

```kotlin
implementation("io.github.ujizin:camposer-code-scanner:<version>")
```

> The latest version can be found at the top-right corner of this documentation or on the [Camposer GitHub page](https://github.com/ujizin/camposer).

### Usage example

```kotlin
val cameraSession = remembercameraSession()
val codeImageAnalyzer = cameraSession.rememberCodeImageAnalyzer(
    codeTypes = listOf(CodeType.QRCode),
    onError = {},
) { result ->
    // result.type - code type scanned
    // result.text - result text
}

CameraPreview(
    cameraSession = cameraSession,
    imageAnalyzer = codeImageAnalyzer,
)
```


### Supported Code Types

The code scanner supports the following formats:

- QRCode
- Barcode39
- Barcode93
- Barcode128
- BarcodeEAN8
- BarcodeEAN13
- CodaBar
- ITF
- Aztec
- DataMatrix
- PDF417
- UPCE
- UPCA
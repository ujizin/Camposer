package com.ujizin.camposer.codescanner

import androidx.compose.runtime.Composable
import com.ujizin.camposer.session.CameraSession
import com.ujizin.camposer.state.properties.ImageAnalyzer

/**
 * Creates and remembers an [ImageAnalyzer] for scanning barcodes and QR codes.
 *
 * This Composable function initializes a platform-specific analyzer capable of detecting various code formats.
 * It is designed to be used within a [CameraSession] context.
 *
 * @param codeTypes The list of code formats to detect (e.g., [CodeType.QRCode], [CodeType.Ean13]). Defaults to [CodeType.QRCode].
 * @param onError A callback invoked when an error occurs during image analysis.
 * @param codeAnalyzerListener A listener that receives callbacks when codes are successfully detected.
 * @return An [ImageAnalyzer] instance configured for code scanning, ready to be passed to the camera implementation.
 */
@Composable
public expect fun CameraSession.rememberCodeImageAnalyzer(
  codeTypes: List<CodeType> = listOf(CodeType.QRCode),
  onError: (Throwable) -> Unit = {},
  codeAnalyzerListener: CodeAnalyzerListener,
): ImageAnalyzer

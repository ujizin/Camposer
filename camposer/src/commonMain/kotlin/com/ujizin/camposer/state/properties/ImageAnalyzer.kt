package com.ujizin.camposer.state.properties

import androidx.compose.runtime.Stable

/**
 * Image Analyzer used to analyze camera frames.
 *
 * A class wrapper for `ImageAnalysis` on Android and `AVCaptureOutput` (e.g `AVCaptureMetadataOutput`) on iOS,
 * allowing real-time processing for tasks like QR code scanning, face detection, or ML.
 */
@Stable
public expect class ImageAnalyzer

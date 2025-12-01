package com.ujizin.camposer.state.properties

/**
 * Image Analyzer used to analyze camera frames.
 *
 * A class wrapper for `ImageAnalysis` on Android and `AVCaptureOutput` (e.g `AVCaptureMetadataOutput`) on iOS,
 * allowing real-time processing for tasks like QR code scanning, face detection, or ML.
 */
public expect class ImageAnalyzer

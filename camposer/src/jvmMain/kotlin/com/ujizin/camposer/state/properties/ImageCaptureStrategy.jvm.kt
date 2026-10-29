package com.ujizin.camposer.state.properties

private const val MIN_LATENCY_JPEG_QUALITY = 75
private const val BALANCED_JPEG_QUALITY = 90
private const val MAX_QUALITY_JPEG_QUALITY = 100

/**
 * JPEG quality (0-100) used to encode still pictures on desktop.
 */
internal val ImageCaptureStrategy.jpegQuality: Int
  get() = when (this) {
    ImageCaptureStrategy.MinLatency -> MIN_LATENCY_JPEG_QUALITY
    ImageCaptureStrategy.Balanced -> BALANCED_JPEG_QUALITY
    ImageCaptureStrategy.MaxQuality -> MAX_QUALITY_JPEG_QUALITY
  }

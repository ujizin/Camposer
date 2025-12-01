package com.ujizin.camposer.state.properties

/**
 * Defines the strategy for capturing images.
 *
 * @property MinLatency Captures the image as fast as possible, prioritizing speed over quality.
 * On Android, this uses Zero Shutter Lag mode if available; otherwise, it defaults to
 * minimizing latency.
 *
 * @property MaxQuality Captures the best possible image, prioritizing quality over speed. On iOS,
 * this also enables high resolution capture if supported.
 *
 * @property Balanced A compromise between capture speed and image quality. On Android, this uses
 * the Minimize Latency mode.
 */
public expect enum class ImageCaptureStrategy {
  MinLatency,
  MaxQuality,
  Balanced,
}

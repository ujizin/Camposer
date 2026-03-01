package com.ujizin.camposer.internal.capture

import org.bytedeco.opencv.opencv_core.Mat

/**
 * Abstraction over OpenCV's VideoCapture to enable testability.
 *
 * Wraps the native VideoCapture API so that tests can inject a fake
 * implementation without requiring real camera hardware.
 */
internal interface JvmCameraCapture {
  /** Opens the camera at [deviceIndex]. Returns true on success. */
  fun open(deviceIndex: Int): Boolean

  /** Returns true if the capture is open and ready. */
  val isOpen: Boolean

  /**
   * Reads the next frame into [mat].
   * Returns true if a frame was successfully read.
   */
  fun read(mat: Mat): Boolean

  /** Sets a VideoCapture property (CAP_PROP_* constants). */
  fun set(propId: Int, value: Double): Boolean

  /** Gets a VideoCapture property value. */
  fun get(propId: Int): Double

  /** Releases the camera resource. */
  fun release()
}

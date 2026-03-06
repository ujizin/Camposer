package com.ujizin.camposer.internal.capture

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.flow.StateFlow
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
  fun set(
    propId: Int,
    value: Double,
  ): Boolean

  /** Gets a VideoCapture property value. */
  fun get(propId: Int): Double

  /** Releases the camera resource and stops streaming. */
  fun release()

  /** Starts the internal frame read loop. */
  fun startStreaming()

  /** Stops the internal frame read loop. */
  suspend fun stopStreaming()

  /** The latest converted frame, null until the first frame is read. */
  val currentFrame: StateFlow<ImageBitmap?>

  /** Whether the frame loop is actively streaming. */
  val isStreaming: StateFlow<Boolean>

  /** The latest raw frame (used by take picture / record). */
  var currentMat: Mat?

  /** Adds a listener invoked for each captured frame. */
  fun addFrameListener(listener: (Mat) -> Unit)

  /** Removes a previously added frame listener. */
  fun removeFrameListener(listener: (Mat) -> Unit)
}

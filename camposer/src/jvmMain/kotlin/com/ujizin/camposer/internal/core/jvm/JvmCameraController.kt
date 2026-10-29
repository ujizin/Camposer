package com.ujizin.camposer.internal.core.jvm

import androidx.compose.ui.graphics.ImageBitmap
import com.ujizin.camposer.internal.preview.PreviewGeometry
import com.ujizin.camposer.state.properties.CameraData
import com.ujizin.camposer.state.properties.ImageAnalyzer
import kotlinx.coroutines.flow.StateFlow

/**
 * Desktop camera hardware contract. One implementation drives the real webcam, tests inject a
 * fake. Every hardware write in the JVM engine goes through this interface.
 */
internal interface JvmCameraController {
  val capabilities: JvmCameraCapabilities

  /** Resolution and frame rate the backend currently delivers, null before the device opens. */
  val currentFormat: CameraData?

  /** Device index of the open camera, null before the device opens. */
  val deviceIndex: Int?

  val isStreaming: StateFlow<Boolean>

  val currentFrame: StateFlow<ImageBitmap?>

  val isRecording: StateFlow<Boolean>

  val isMuted: StateFlow<Boolean>

  /** Opens [deviceIndex]. Replaces a previously opened device. Completes on a background thread. */
  fun open(
    deviceIndex: Int,
    onResult: (Result<Unit>) -> Unit,
  )

  fun startStreaming()

  fun stopStreaming()

  fun setFormat(
    width: Int,
    height: Int,
  )

  fun setFrameRate(frameRate: Int)

  fun setExposureCompensation(exposureCompensation: Float)

  fun setPictureQuality(quality: Int)

  fun setFrameAnalyzer(analyzer: ImageAnalyzer.Analyzer?)

  fun updatePreviewGeometry(geometry: PreviewGeometry?)

  fun takePicture(
    isMirrored: Boolean,
    onPictureCaptured: (Result<ByteArray>) -> Unit,
  )

  fun takePicture(
    filename: String,
    isMirrored: Boolean,
    onPictureCaptured: (Result<String>) -> Unit,
  )

  fun startRecording(
    filename: String,
    isMirrored: Boolean,
    onVideoCaptured: (Result<String>) -> Unit,
  )

  fun pauseRecording(): Result<Boolean>

  fun resumeRecording(): Result<Boolean>

  fun stopRecording(): Result<Boolean>

  fun muteRecording(isMuted: Boolean): Result<Boolean>

  fun release()
}

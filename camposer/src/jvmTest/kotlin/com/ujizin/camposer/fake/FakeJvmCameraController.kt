package com.ujizin.camposer.fake

import androidx.compose.ui.graphics.ImageBitmap
import com.ujizin.camposer.internal.core.jvm.JvmCameraCapabilities
import com.ujizin.camposer.internal.core.jvm.JvmCameraController
import com.ujizin.camposer.internal.preview.PreviewGeometry
import com.ujizin.camposer.state.properties.CameraData
import com.ujizin.camposer.state.properties.CameraFrame
import com.ujizin.camposer.state.properties.ImageAnalyzer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.awt.image.BufferedImage

/**
 * In-memory [JvmCameraController]. Records every hardware write so tests can assert on it.
 * Capabilities are configurable so the shared common tests can exercise flash, torch, zoom
 * and stabilization paths that real desktop hardware never reports.
 */
internal class FakeJvmCameraController : JvmCameraController {
  var fakeIsFlashSupported = true
  var fakeIsExposureSupported = true
  var fakeIsVideoStabilizationSupported = true
  var fakeErrorInRecording = false
  var fakeOpenFails = false

  var fakeFormat: CameraData = CameraData(width = 1920, height = 1080, minFps = 1, maxFps = 30)
    private set

  var fakeFrameRate: Int = -1
    private set

  var fakeExposureCompensation: Float = 0F
    private set

  var fakePictureQuality: Int = -1
    private set

  var fakeFrameAnalyzer: ImageAnalyzer.Analyzer? = null
    private set

  var fakePreviewGeometry: PreviewGeometry? = null
    private set

  var fakeLastPictureMirrored: Boolean? = null
    private set

  var isFakeReleased = false
    private set

  val openedIndexes = mutableListOf<Int>()

  override val capabilities: JvmCameraCapabilities
    get() = JvmCameraCapabilities(
      isExposureSupported = fakeIsExposureSupported,
      minExposure = if (fakeIsExposureSupported) -8F else 0F,
      maxExposure = if (fakeIsExposureSupported) 8F else 0F,
      isZoomSupported = true,
      minZoom = 1F,
      maxZoom = 30F,
      isFlashSupported = fakeIsFlashSupported,
      isTorchSupported = fakeIsFlashSupported,
      isVideoStabilizationSupported = fakeIsVideoStabilizationSupported,
      isFocusSupported = false,
      minFrameRate = 1,
      maxFrameRate = 60,
    )

  override val currentFormat: CameraData
    get() = fakeFormat

  override var deviceIndex: Int? = null
    private set

  private val _isStreaming = MutableStateFlow(false)
  override val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

  private val _currentFrame = MutableStateFlow<ImageBitmap?>(null)
  override val currentFrame: StateFlow<ImageBitmap?> = _currentFrame.asStateFlow()

  private val _isRecording = MutableStateFlow(false)
  override val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

  private val _isMuted = MutableStateFlow(false)
  override val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

  override fun open(
    deviceIndex: Int,
    onResult: (Result<Unit>) -> Unit,
  ) {
    if (fakeOpenFails) {
      onResult(Result.failure(IllegalStateException("fake open failure")))
      return
    }
    this.deviceIndex = deviceIndex
    openedIndexes += deviceIndex
    onResult(Result.success(Unit))
  }

  override fun startStreaming() {
    _isStreaming.update { true }
  }

  override fun stopStreaming() {
    _isStreaming.update { false }
  }

  override fun setFormat(
    width: Int,
    height: Int,
  ) {
    fakeFormat = CameraData(
      width = width,
      height = height,
      minFps = fakeFormat.minFps,
      maxFps = fakeFormat.maxFps,
    )
  }

  override fun setFrameRate(frameRate: Int) {
    fakeFrameRate = frameRate
    fakeFormat = CameraData(
      width = fakeFormat.width,
      height = fakeFormat.height,
      minFps = fakeFormat.minFps,
      maxFps = frameRate,
    )
  }

  override fun setExposureCompensation(exposureCompensation: Float) {
    fakeExposureCompensation = exposureCompensation
  }

  override fun setPictureQuality(quality: Int) {
    fakePictureQuality = quality
  }

  override fun setFrameAnalyzer(analyzer: ImageAnalyzer.Analyzer?) {
    fakeFrameAnalyzer = analyzer
    analyzer?.analyze(createFakeFrame())
  }

  override fun updatePreviewGeometry(geometry: PreviewGeometry?) {
    fakePreviewGeometry = geometry
  }

  override fun takePicture(
    isMirrored: Boolean,
    onPictureCaptured: (Result<ByteArray>) -> Unit,
  ) {
    fakeLastPictureMirrored = isMirrored
    onPictureCaptured(Result.success(byteArrayOf()))
  }

  override fun takePicture(
    filename: String,
    isMirrored: Boolean,
    onPictureCaptured: (Result<String>) -> Unit,
  ) {
    fakeLastPictureMirrored = isMirrored
    onPictureCaptured(Result.success(filename))
  }

  override fun startRecording(
    filename: String,
    isMirrored: Boolean,
    onVideoCaptured: (Result<String>) -> Unit,
  ) {
    _isRecording.update { true }

    if (fakeErrorInRecording) {
      onVideoCaptured(Result.failure(Exception("Fake error")))
      return
    }

    onVideoCaptured(Result.success(filename))
  }

  override fun pauseRecording(): Result<Boolean> = Result.success(true)

  override fun resumeRecording(): Result<Boolean> = Result.success(true)

  override fun stopRecording(): Result<Boolean> {
    _isRecording.update { false }
    _isMuted.update { false }
    return Result.success(true)
  }

  override fun muteRecording(isMuted: Boolean): Result<Boolean> {
    _isMuted.update { isMuted }
    return Result.success(true)
  }

  override fun release() {
    isFakeReleased = true
    _isStreaming.update { false }
  }

  fun emitFrame(
    width: Int = 2,
    height: Int = 2,
  ) {
    _currentFrame.update { ImageBitmap(width, height) }
  }

  private fun createFakeFrame(): CameraFrame =
    CameraFrame(
      image = BufferedImage(2, 2, BufferedImage.TYPE_3BYTE_BGR),
      previewGeometry = fakePreviewGeometry,
    )
}

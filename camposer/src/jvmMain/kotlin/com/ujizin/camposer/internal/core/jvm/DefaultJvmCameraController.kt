package com.ujizin.camposer.internal.core.jvm

import androidx.compose.ui.graphics.ImageBitmap
import com.ujizin.camposer.internal.controller.JvmRecordController
import com.ujizin.camposer.internal.error.CameraDeviceNotFoundException
import com.ujizin.camposer.internal.error.CameraNotStreamingException
import com.ujizin.camposer.internal.preview.PreviewGeometry
import com.ujizin.camposer.internal.record.FFmpegVideoRecorder
import com.ujizin.camposer.internal.record.JavaSoundAudioCapture
import com.ujizin.camposer.internal.record.JvmAudioCapture
import com.ujizin.camposer.internal.record.JvmVideoRecorder
import com.ujizin.camposer.internal.utils.Logger
import com.ujizin.camposer.state.properties.CameraData
import com.ujizin.camposer.state.properties.CameraFrame
import com.ujizin.camposer.state.properties.ImageAnalyzer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.roundToInt

private const val READ_RETRY_DELAY_MS = 16L
private const val DEFAULT_FRAME_RATE = 30
private const val MIN_FRAME_RATE = 1
private const val DEFAULT_JPEG_QUALITY = 90
private const val UNSUPPORTED_PROPERTY = -1.0

/**
 * Desktop camera hardware driver.
 *
 * Owns the capture thread that reads frames from a [JvmVideoSource] and fans them out to the
 * preview, the recorder, the image analyzer and pending picture requests. Every access to the
 * source is serialized by [sourceLock].
 */
internal class DefaultJvmCameraController(
  private val videoSourceFactory: JvmVideoSource.Factory = OpenCvVideoSource,
  recorderFactory: JvmVideoRecorder.Factory = FFmpegVideoRecorder,
  audioCaptureFactory: JvmAudioCapture.Factory = JavaSoundAudioCapture,
  private val captureDispatcher: CoroutineDispatcher =
    newSingleThreadDispatcher("Camposer-Capture"),
  private val analysisDispatcher: CoroutineDispatcher =
    newSingleThreadDispatcher("Camposer-Analysis"),
) : JvmCameraController {
  private val captureScope = CoroutineScope(SupervisorJob() + captureDispatcher)
  private val analysisScope = CoroutineScope(SupervisorJob() + analysisDispatcher)

  private val sourceLock = ReentrantLock()
  private var videoSource: JvmVideoSource? = null
  private var frameLoopJob: Job? = null
  private val isStreamingRequested = AtomicBoolean(false)

  private val recordController = JvmRecordController(recorderFactory, audioCaptureFactory)
  private val pendingPictureRequests = ConcurrentLinkedQueue<(JvmFrame) -> Unit>()
  private val isAnalysisBusy = AtomicBoolean(false)

  @Volatile
  private var frameAnalyzer: ImageAnalyzer.Analyzer? = null

  @Volatile
  private var previewGeometry: PreviewGeometry? = null

  @Volatile
  private var pictureQuality: Int = DEFAULT_JPEG_QUALITY

  @Volatile
  private var autoExposureValue: Double = 0.0

  @Volatile
  private var exposureBackend: ExposureBackend? = null

  @Volatile
  override var capabilities: JvmCameraCapabilities = JvmCameraCapabilities()
    private set

  @Volatile
  override var currentFormat: CameraData? = null
    private set

  @Volatile
  override var deviceIndex: Int? = null
    private set

  private val _isStreaming = MutableStateFlow(false)
  override val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

  private val _currentFrame = MutableStateFlow<ImageBitmap?>(null)
  override val currentFrame: StateFlow<ImageBitmap?> = _currentFrame.asStateFlow()

  override val isRecording: StateFlow<Boolean>
    get() = recordController.isRecording

  override val isMuted: StateFlow<Boolean>
    get() = recordController.isMuted

  override fun open(
    deviceIndex: Int,
    onResult: (Result<Unit>) -> Unit,
  ) {
    frameLoopJob?.cancel()
    captureScope.launch {
      val result = runCatching { openSource(deviceIndex) }
      onResult(result)
      if (result.isSuccess && isStreamingRequested.get()) startFrameLoop()
    }
  }

  private fun openSource(deviceIndex: Int) =
    sourceLock.withLock {
      videoSource?.release()
      videoSource = null

      val source = videoSourceFactory.create()
      if (!source.open(deviceIndex)) {
        source.release()
        throw CameraDeviceNotFoundException(deviceIndex)
      }

      videoSource = source
      this.deviceIndex = deviceIndex
      refreshCapabilities(source)
    }

  override fun startStreaming() {
    isStreamingRequested.set(true)
    captureScope.launch { startFrameLoop() }
  }

  override fun stopStreaming() {
    isStreamingRequested.set(false)
    frameLoopJob?.cancel()
  }

  /** Must run on the capture thread. */
  private fun startFrameLoop() {
    if (frameLoopJob?.isActive == true) return
    if (sourceLock.withLock { videoSource } == null) return
    frameLoopJob = captureScope.launch { runFrameLoop() }
  }

  private suspend fun CoroutineScope.runFrameLoop() {
    _isStreaming.value = true
    try {
      while (isActive) {
        val frame = sourceLock.withLock { videoSource?.read() }
        if (frame == null) {
          delay(READ_RETRY_DELAY_MS)
          continue
        }
        dispatchFrame(frame)
        yield()
      }
    } finally {
      _isStreaming.value = false
    }
  }

  private fun dispatchFrame(frame: JvmFrame) {
    _currentFrame.value = frame.toImageBitmap()
    recordController.onFrame(frame)
    dispatchAnalysis(frame)
    while (true) {
      val request = pendingPictureRequests.poll() ?: break
      request(frame)
    }
  }

  private fun dispatchAnalysis(frame: JvmFrame) {
    val analyzer = frameAnalyzer ?: return
    if (!isAnalysisBusy.compareAndSet(false, true)) return

    val cameraFrame = CameraFrame(frame.toBufferedImage(), previewGeometry)
    analysisScope.launch {
      try {
        analyzer.analyze(cameraFrame)
      } catch (
        @Suppress("TooGenericExceptionCaught") error: Exception,
      ) {
        // The analyzer runs user code; a failure must not stop the capture loop.
        Logger.error("Image analyzer failed", error)
      } finally {
        isAnalysisBusy.set(false)
      }
    }
  }

  override fun setFormat(
    width: Int,
    height: Int,
  ) {
    sourceLock.withLock {
      val source = videoSource ?: return
      source.set(JvmVideoProperty.FrameWidth, width.toDouble())
      source.set(JvmVideoProperty.FrameHeight, height.toDouble())
      refreshFormat(source)
    }
  }

  override fun setFrameRate(frameRate: Int) {
    sourceLock.withLock {
      val source = videoSource ?: return
      source.set(JvmVideoProperty.FrameRate, frameRate.toDouble())
      refreshFormat(source)
    }
  }

  override fun setExposureCompensation(exposureCompensation: Float) {
    sourceLock.withLock {
      val source = videoSource ?: return
      val backend = exposureBackend ?: return

      if (exposureCompensation == 0F) {
        source.set(JvmVideoProperty.AutoExposure, backend.autoValue)
        return
      }

      source.set(JvmVideoProperty.AutoExposure, backend.manualValue)
      source.set(
        JvmVideoProperty.Exposure,
        backend.toRawExposure(autoExposureValue, exposureCompensation),
      )
    }
  }

  override fun setPictureQuality(quality: Int) {
    pictureQuality = quality
  }

  override fun setFrameAnalyzer(analyzer: ImageAnalyzer.Analyzer?) {
    frameAnalyzer = analyzer
  }

  override fun updatePreviewGeometry(geometry: PreviewGeometry?) {
    previewGeometry = geometry
  }

  override fun takePicture(
    isMirrored: Boolean,
    onPictureCaptured: (Result<ByteArray>) -> Unit,
  ) = requestPicture(isMirrored, onPictureCaptured)

  override fun takePicture(
    filename: String,
    isMirrored: Boolean,
    onPictureCaptured: (Result<String>) -> Unit,
  ) = requestPicture(isMirrored) { result ->
    onPictureCaptured(
      result.mapCatching { bytes ->
        File(filename).apply { parentFile?.mkdirs() }.writeBytes(bytes)
        filename
      },
    )
  }

  private fun requestPicture(
    isMirrored: Boolean,
    onPictureCaptured: (Result<ByteArray>) -> Unit,
  ) {
    if (!_isStreaming.value) {
      onPictureCaptured(Result.failure(CameraNotStreamingException()))
      return
    }

    pendingPictureRequests.add { frame ->
      onPictureCaptured(runCatching { frame.encodeJpeg(pictureQuality, isMirrored) })
    }
  }

  override fun startRecording(
    filename: String,
    isMirrored: Boolean,
    onVideoCaptured: (Result<String>) -> Unit,
  ) {
    val format = currentFormat
    if (!_isStreaming.value || format == null) {
      onVideoCaptured(Result.failure(CameraNotStreamingException()))
      return
    }

    recordController.start(
      filename = filename,
      width = format.width,
      height = format.height,
      frameRate = (format.maxFps ?: DEFAULT_FRAME_RATE).toDouble(),
      isMirrored = isMirrored,
      onVideoCaptured = onVideoCaptured,
    )
  }

  override fun pauseRecording(): Result<Boolean> = recordController.pause()

  override fun resumeRecording(): Result<Boolean> = recordController.resume()

  override fun stopRecording(): Result<Boolean> = recordController.stop()

  override fun muteRecording(isMuted: Boolean): Result<Boolean> = recordController.mute(isMuted)

  override fun release() {
    isStreamingRequested.set(false)
    frameLoopJob?.cancel()
    frameAnalyzer = null
    pendingPictureRequests.clear()
    recordController.dispose()
    analysisScope.cancel()
    (analysisDispatcher as? ExecutorCoroutineDispatcher)?.close()

    captureScope.launch {
      sourceLock.withLock {
        videoSource?.release()
        videoSource = null
      }
      _currentFrame.value = null
      captureScope.cancel()
      (captureDispatcher as? ExecutorCoroutineDispatcher)?.close()
    }
  }

  private fun refreshCapabilities(source: JvmVideoSource) {
    val backend = ExposureBackend.fromBackendName(source.backendName)
    val exposure = source.get(JvmVideoProperty.Exposure)
    val isExposureSupported =
      backend != null && exposure != 0.0 && exposure != UNSUPPORTED_PROPERTY

    exposureBackend = backend.takeIf { isExposureSupported }
    autoExposureValue = exposure
    capabilities = JvmCameraCapabilities(
      isExposureSupported = isExposureSupported,
      minExposure = if (isExposureSupported) -ExposureBackend.COMPENSATION_RANGE else 0F,
      maxExposure = if (isExposureSupported) ExposureBackend.COMPENSATION_RANGE else 0F,
      minFrameRate = MIN_FRAME_RATE,
    )
    refreshFormat(source)
  }

  private fun refreshFormat(source: JvmVideoSource) {
    val width = source.get(JvmVideoProperty.FrameWidth).roundToInt()
    val height = source.get(JvmVideoProperty.FrameHeight).roundToInt()
    val frameRate = source
      .get(JvmVideoProperty.FrameRate)
      .takeIf { it > 0.0 }
      ?.roundToInt()
      ?: DEFAULT_FRAME_RATE

    currentFormat = CameraData(
      width = width,
      height = height,
      isFocusSupported = false,
      minFps = MIN_FRAME_RATE,
      maxFps = frameRate,
    )
  }

  private companion object {
    fun newSingleThreadDispatcher(name: String): CoroutineDispatcher =
      Executors
        .newSingleThreadExecutor { runnable -> Thread(runnable, name).apply { isDaemon = true } }
        .asCoroutineDispatcher()
  }
}

package com.ujizin.camposer.controller.camera

import com.ujizin.camposer.CaptureResult
import com.ujizin.camposer.controller.record.DefaultRecordController
import com.ujizin.camposer.controller.record.RecordController
import com.ujizin.camposer.controller.takepicture.TakePictureCommand
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.info.FakeJvmCameraInfo
import com.ujizin.camposer.internal.capture.FakeJvmCameraCapture
import com.ujizin.camposer.internal.core.CameraEngineImpl
import com.ujizin.camposer.internal.record.AudioDeviceSelector
import com.ujizin.camposer.internal.record.FrameRecorderBridge
import com.ujizin.camposer.internal.record.JvmAudioCapture
import com.ujizin.camposer.internal.record.JvmVideoRecorder
import com.ujizin.camposer.internal.record.MatFrameConverter
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.bytedeco.javacv.Frame
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_EXPOSURE
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FPS
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_ZOOM
import org.bytedeco.opencv.opencv_core.Mat
import java.nio.ShortBuffer
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.Control
import javax.sound.sampled.Line
import javax.sound.sampled.LineListener
import javax.sound.sampled.TargetDataLine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class CommonCameraControllerTest {
  private data class Harness(
    val controller: CameraController,
    val engine: CameraEngineImpl,
    val capture: FakeJvmCameraCapture,
    val cameraInfo: CameraInfo,
    val fakeCameraInfo: FakeJvmCameraInfo,
  )

  private fun buildHarness(): Harness {
    val dispatcher = UnconfinedTestDispatcher()
    val controller = CameraController(dispatcher)
    val capture = FakeJvmCameraCapture()
    val fakeCameraInfo = FakeJvmCameraInfo()
    val info = CameraInfo(fakeCameraInfo)
    val engine = CameraEngineImpl(
      cameraController = controller,
      cameraInfo = info,
      capture = capture,
      dispatcher = dispatcher,
    )

    controller.initialize(
      recordController = FakeRecordController(),
      takePictureCommand = FakeTakePictureCommand(),
      cameraEngine = engine,
    )

    return Harness(
      controller = controller,
      engine = engine,
      capture = capture,
      cameraInfo = info,
      fakeCameraInfo = fakeCameraInfo,
    )
  }

  /**
   * Builds a harness wired with a real [DefaultRecordController] that uses no-op
   * [JvmVideoRecorder] and [JvmAudioCapture] factories — no FFmpeg or audio hardware required.
   */
  private fun buildHarnessWithRealRecordController(): Harness {
    val dispatcher = UnconfinedTestDispatcher()
    val controller = CameraController(dispatcher)
    val capture = FakeJvmCameraCapture()
    val fakeCameraInfo = FakeJvmCameraInfo()
    val info = CameraInfo(fakeCameraInfo)
    val engine = CameraEngineImpl(
      cameraController = controller,
      cameraInfo = info,
      capture = capture,
      dispatcher = dispatcher,
    )

    val recordController = DefaultRecordController(
      cameraEngine = engine,
      videoRecorderFactory = { filename, cap ->
        JvmVideoRecorder(
          filename = filename,
          capture = cap,
          recorderFactory = { _, _, _, _ -> NoOpFrameRecorderBridge() },
          converterFactory = { NoOpMatFrameConverter() },
        )
      },
      audioFactory = {
        JvmAudioCapture(audioDeviceSelector = NoOpAudioDeviceSelector())
      },
    )

    controller.initialize(
      recordController = recordController,
      takePictureCommand = FakeTakePictureCommand(),
      cameraEngine = engine,
    )

    return Harness(
      controller = controller,
      engine = engine,
      capture = capture,
      cameraInfo = info,
      fakeCameraInfo = fakeCameraInfo,
    )
  }

  // ---------------------------------------------------------------------------
  // Existing tests (using FakeRecordController)
  // ---------------------------------------------------------------------------

  @Test
  fun `given pending zoom and exposure when session starts then pending values are applied once`() {
    // Given
    val (controller, engine, capture) = buildHarness()

    controller.setZoomRatio(4f)
    controller.setExposureCompensation(2f)

    // And: nothing applied to capture yet (values are pending)
    assertEquals(0, capture.setCallCount(CAP_PROP_ZOOM))
    assertEquals(0, capture.setCallCount(CAP_PROP_EXPOSURE))

    // When
    controller.onSessionStarted()

    // Then
    assertEquals(4f, engine.cameraState.zoomRatio.value)
    assertEquals(2f, engine.cameraState.exposureCompensation.value)
    assertEquals(1, capture.setCallCount(CAP_PROP_ZOOM))
    assertEquals(1, capture.setCallCount(CAP_PROP_EXPOSURE))

    // When
    controller.onSessionStarted()

    // Then
    assertEquals(1, capture.setCallCount(CAP_PROP_ZOOM))
    assertEquals(1, capture.setCallCount(CAP_PROP_EXPOSURE))
  }

  @Test
  fun `given out-of-range frame rate when setting video frame rate then result is failure`() {
    // Given
    val (controller, engine, capture) = buildHarness()
    controller.onSessionStarted()

    // When
    val result = controller.setVideoFrameRate(100)

    // Then
    assertTrue(result.isFailure)
    assertEquals(-1, engine.cameraState.frameRate.value)
    assertEquals(0, capture.setCallCount(CAP_PROP_FPS))
  }

  @Test
  fun `given unsupported stabilization when setting video stabilization then result is failure`() {
    // Given
    val (controller, engine, _) = buildHarness()
    controller.onSessionStarted()

    // When
    val result = controller.setVideoStabilizationEnabled(VideoStabilizationMode.Standard)

    // Then
    assertTrue(result.isFailure)
    assertEquals(VideoStabilizationMode.Off, engine.cameraState.videoStabilizationMode.value)
  }

  @Test
  fun `given pending flash on when flash becomes unsupported before session then flash is off`() {
    // Given
    val (controller, engine, _, cameraInfo, fakeCameraInfo) = buildHarness()

    val pendingResult = controller.setFlashMode(FlashMode.On)
    assertTrue(pendingResult.isSuccess) // flash was stored as pending
    fakeCameraInfo.isFlashSupported = false
    cameraInfo.rebind()

    // When
    controller.onSessionStarted()

    // Then
    assertEquals(FlashMode.Off, engine.cameraState.flashMode.value)
  }

  // ---------------------------------------------------------------------------
  // New tests: real DefaultRecordController with no-op factories
  // ---------------------------------------------------------------------------

  @Test
  fun `given recording started when stop called then frame listener removed and callback fired`() {
    val (controller, _, capture) = buildHarnessWithRealRecordController()
    controller.onSessionStarted()

    var result: CaptureResult<String>? = null
    controller.startRecording("test.mp4") { result = it }

    // frame listener was added after successful startRecording
    assertEquals(1, capture.frameListenerCount())

    controller.stopRecording()

    // frame listener was removed
    assertEquals(0, capture.frameListenerCount())
    // callback was invoked (either Success or Error depending on stop() outcome)
    assertNotNull(result)
  }

  @Test
  fun `given stopRecording called without start then result is failure`() {
    val (controller) = buildHarnessWithRealRecordController()
    controller.onSessionStarted()

    val result = controller.stopRecording()
    assertTrue(result.isFailure)
  }

  @Test
  fun `given recording active when pauseRecording called then result is failure`() {
    val (controller) = buildHarnessWithRealRecordController()
    controller.onSessionStarted()
    controller.startRecording("test.mp4") { }

    val result = controller.pauseRecording()
    assertTrue(result.isFailure)
  }

  @Test
  fun `given recording active when muteRecording called then isMuted state updates`() {
    val (controller) = buildHarnessWithRealRecordController()
    controller.onSessionStarted()
    controller.startRecording("test.mp4") { }

    controller.muteRecording(true)
    assertTrue(controller.isMuted.value)

    controller.muteRecording(false)
    assertFalse(controller.isMuted.value)
  }

  // ---------------------------------------------------------------------------
  // No-op fakes for DefaultRecordController factory injection
  // ---------------------------------------------------------------------------

  private class NoOpFrameRecorderBridge : FrameRecorderBridge {
    override var videoCodec: Int = 0
    override var audioCodec: Int = 0
    override var frameRate: Double = 30.0
    override var sampleRate: Int = 44100
    override var audioChannels: Int = 1
    override var videoBitrate: Int = 0
    override var audioBitrate: Int = 0

    override fun start() = Unit

    override fun record(frame: Frame) = Unit

    override fun recordSamples(
      sampleRate: Int,
      audioChannels: Int,
      samples: ShortBuffer,
    ): Boolean = true

    override fun stop() = Unit

    override fun release() = Unit
  }

  private class NoOpMatFrameConverter : MatFrameConverter {
    override fun convert(mat: Mat): Frame? = null

    override fun close() = Unit
  }

  private class NoOpTargetDataLine : TargetDataLine {
    override fun open(format: AudioFormat) = Unit

    override fun open() = Unit

    override fun open(
      format: AudioFormat,
      bufferSize: Int,
    ) = Unit

    override fun start() = Unit

    override fun stop() = Unit

    override fun close() = Unit

    override fun drain() = Unit

    override fun flush() = Unit

    override fun isRunning(): Boolean = false

    override fun isActive(): Boolean = false

    override fun getFormat(): AudioFormat = AudioFormat(44100f, 16, 1, true, false)

    override fun getBufferSize(): Int = 1024

    override fun available(): Int = 0

    override fun getFramePosition(): Int = 0

    override fun getLongFramePosition(): Long = 0L

    override fun getMicrosecondPosition(): Long = 0L

    override fun getLevel(): Float = 0f

    override fun isOpen(): Boolean = false

    override fun getControls(): Array<Control> = emptyArray()

    override fun isControlSupported(control: Control.Type): Boolean = false

    override fun getControl(control: Control.Type): Control = throw UnsupportedOperationException()

    override fun addLineListener(listener: LineListener) = Unit

    override fun removeLineListener(listener: LineListener) = Unit

    override fun getLineInfo(): Line.Info = Line.Info(TargetDataLine::class.java)

    override fun read(
      b: ByteArray,
      off: Int,
      len: Int,
    ): Int = 0
  }

  private class NoOpAudioDeviceSelector : AudioDeviceSelector {
    override fun openLine(format: AudioFormat): TargetDataLine = NoOpTargetDataLine()
  }

  // ---------------------------------------------------------------------------
  // Existing fakes
  // ---------------------------------------------------------------------------

  private class FakeRecordController : RecordController {
    private val _isMuted = MutableStateFlow(false)
    override val isMuted: StateFlow<Boolean> = _isMuted

    private val _isRecording = MutableStateFlow(false)
    override val isRecording: StateFlow<Boolean> = _isRecording

    override fun startRecording(
      filename: String,
      onVideoCaptured: (CaptureResult<String>) -> Unit,
    ) = Unit

    override fun resumeRecording(): Result<Boolean> = Result.success(true)

    override fun pauseRecording(): Result<Boolean> = Result.success(true)

    override fun stopRecording(): Result<Boolean> = Result.success(true)

    override fun muteRecording(isMuted: Boolean): Result<Boolean> {
      _isMuted.value = isMuted
      return Result.success(true)
    }
  }

  private class FakeTakePictureCommand : TakePictureCommand {
    override fun takePicture(onImageCaptured: (CaptureResult<ByteArray>) -> Unit) = Unit

    override fun takePicture(
      filename: String,
      onImageCaptured: (CaptureResult<String>) -> Unit,
    ) = Unit
  }
}

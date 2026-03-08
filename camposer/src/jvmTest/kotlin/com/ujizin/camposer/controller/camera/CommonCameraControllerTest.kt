package com.ujizin.camposer.controller.camera

import com.ujizin.camposer.CaptureResult
import com.ujizin.camposer.controller.record.RecordController
import com.ujizin.camposer.controller.takepicture.TakePictureCommand
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.info.FakeJvmCameraInfo
import com.ujizin.camposer.internal.capture.FakeJvmCameraCapture
import com.ujizin.camposer.internal.core.CameraEngineImpl
import com.ujizin.camposer.state.properties.FlashMode
import com.ujizin.camposer.state.properties.VideoStabilizationMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_EXPOSURE
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FPS
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_ZOOM
import kotlin.test.Test
import kotlin.test.assertEquals
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

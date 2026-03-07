package com.ujizin.camposer.controller.camera

import com.ujizin.camposer.CaptureResult
import com.ujizin.camposer.controller.record.RecordController
import com.ujizin.camposer.controller.takepicture.TakePictureCommand
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.info.FakeJvmCameraInfo
import com.ujizin.camposer.internal.capture.FakeJvmCameraCapture
import com.ujizin.camposer.internal.core.CameraEngineImpl
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
  )

  private fun buildHarness(): Harness {
    val dispatcher = UnconfinedTestDispatcher()
    val controller = CameraController(dispatcher)
    val capture = FakeJvmCameraCapture()
    val info = CameraInfo(FakeJvmCameraInfo())
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
    )
  }

  @Test
  fun `pending zoom and exposure are applied after onSessionStarted`() {
    val (controller, engine, capture) = buildHarness()

    controller.setZoomRatio(4f)
    controller.setExposureCompensation(2f)

    assertEquals(0, capture.setCallCount(CAP_PROP_ZOOM))
    assertEquals(0, capture.setCallCount(CAP_PROP_EXPOSURE))

    controller.onSessionStarted()

    assertEquals(4f, engine.cameraState.zoomRatio.value)
    assertEquals(2f, engine.cameraState.exposureCompensation.value)
    assertEquals(1, capture.setCallCount(CAP_PROP_ZOOM))
    assertEquals(1, capture.setCallCount(CAP_PROP_EXPOSURE))

    controller.onSessionStarted()
    assertEquals(1, capture.setCallCount(CAP_PROP_ZOOM))
    assertEquals(1, capture.setCallCount(CAP_PROP_EXPOSURE))
  }

  @Test
  fun `setVideoFrameRate fails when frame rate is outside camera range`() {
    val (controller, engine, capture) = buildHarness()
    controller.onSessionStarted()

    val result = controller.setVideoFrameRate(100)

    assertTrue(result.isFailure)
    assertEquals(-1, engine.cameraState.frameRate.value)
    assertEquals(0, capture.setCallCount(CAP_PROP_FPS))
  }

  @Test
  fun `setVideoStabilizationEnabled fails when unsupported`() {
    val (controller, engine, _) = buildHarness()
    controller.onSessionStarted()

    val result = controller.setVideoStabilizationEnabled(VideoStabilizationMode.Standard)

    assertTrue(result.isFailure)
    assertEquals(VideoStabilizationMode.Off, engine.cameraState.videoStabilizationMode.value)
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

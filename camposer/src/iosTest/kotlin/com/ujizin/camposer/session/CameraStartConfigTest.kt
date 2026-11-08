package com.ujizin.camposer.session

import com.ujizin.camposer.fake.FakeCaptureDeviceFormat
import com.ujizin.camposer.fake.createCameraSession
import com.ujizin.camposer.state.properties.ImageCaptureStrategy
import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.format.config.AspectRatioConfig
import com.ujizin.camposer.state.properties.format.config.ResolutionConfig
import com.ujizin.camposer.state.update
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

@OptIn(ExperimentalForeignApi::class)
internal class CameraStartConfigTest : CameraSessionTest() {
  private val format16By9 = FakeCaptureDeviceFormat(width = 1920, height = 1080)
  private val format4By3 = FakeCaptureDeviceFormat(width = 1920, height = 1440)

  private val fakeController
    get() = cameraTest.fakeIosCameraController

  @Test
  fun test_image_capture_strategy_set_before_start_is_applied_on_start() {
    val session = createSessionNotStarted()

    session.updateConfig(imageCaptureStrategy = ImageCaptureStrategy.MaxQuality)
    session.startCamera()

    cameraTest.assertImageCaptureStrategy(ImageCaptureStrategy.MaxQuality)
  }

  // Issue #105: a camFormat passed at composition was written before the session started, and
  // starting the session replaced it with the preset format.
  @Test
  fun test_cam_format_set_before_start_is_applied_on_start() {
    val camFormat = CamFormat(AspectRatioConfig(4F / 3F), ResolutionConfig(1920, 1440))
    val session = createSessionNotStarted()

    session.updateConfig(camFormat = camFormat)
    session.startCamera()

    assertSame(format4By3, fakeController.fakeDeviceFormat)
    assertEquals(camFormat, session.state.camFormat.value)
  }

  @Test
  fun test_cam_format_set_after_start_is_applied() {
    val camFormat = CamFormat(AspectRatioConfig(4F / 3F), ResolutionConfig(1920, 1440))
    val session = createSessionNotStarted()

    session.startCamera()
    session.updateConfig(camFormat = camFormat)

    assertSame(format4By3, fakeController.fakeDeviceFormat)
    assertEquals(camFormat, session.state.camFormat.value)
  }

  private fun createSessionNotStarted(): CameraSession {
    // Formats are read when the session is created, so they have to be set first.
    fakeController.fakeFormats = listOf(format16By9, format4By3)
    return createCameraSession(
      fakeCameraTest = cameraTest,
      testDispatcher = testDispatcher,
      autoStart = false,
    )
  }

  private fun CameraSession.updateConfig(
    imageCaptureStrategy: ImageCaptureStrategy = state.imageCaptureStrategy.value,
    camFormat: CamFormat = state.camFormat.value,
  ) = with(state) {
    update(
      camSelector = camSelector.value,
      captureMode = captureMode.value,
      scaleType = scaleType.value,
      isImageAnalysisEnabled = isImageAnalyzerEnabled.value,
      imageAnalyzer = imageAnalyzer.value,
      implementationMode = implementationMode.value,
      isFocusOnTapEnabled = isFocusOnTapEnabled.value,
      imageCaptureStrategy = imageCaptureStrategy,
      camFormat = camFormat,
      isPinchToZoomEnabled = isPinchToZoomEnabled.value,
    )
  }
}

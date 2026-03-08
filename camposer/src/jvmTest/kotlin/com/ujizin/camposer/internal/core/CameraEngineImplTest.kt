package com.ujizin.camposer.internal.core

import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.info.FakeJvmCameraInfo
import com.ujizin.camposer.internal.capture.FakeJvmCameraCapture
import com.ujizin.camposer.state.properties.CaptureMode
import com.ujizin.camposer.state.properties.ImageAnalyzer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_EXPOSURE
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_ZOOM
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class CameraEngineImplTest {
  private data class Harness(
    val engine: CameraEngineImpl,
    val capture: FakeJvmCameraCapture,
  )

  private fun buildEngine(): Harness {
    val dispatcher = UnconfinedTestDispatcher()
    val capture = FakeJvmCameraCapture()
    val controller = CameraController(dispatcher)
    val info = CameraInfo(FakeJvmCameraInfo())
    return Harness(
      engine = CameraEngineImpl(
        cameraController = controller,
        cameraInfo = info,
        capture = capture,
        dispatcher = dispatcher,
      ),
      capture = capture,
    )
  }

  @Test
  fun `given capture mode change when updating capture mode then camera state is updated`() {
    // Given
    val engine = buildEngine().engine

    // When
    engine.updateCaptureMode(CaptureMode.Video)

    // Then
    assertEquals(CaptureMode.Video, engine.cameraState.captureMode.value)
  }

  @Test
  fun `given out-of-range zoom values when updating zoom ratio then value is clamped to camera bounds`() {
    // Given
    val (engine, capture) = buildEngine()

    // When
    engine.updateZoomRatio(100f)

    // Then
    assertEquals(10f, engine.cameraState.zoomRatio.value)
    assertEquals(10.0, capture.lastSetValue(CAP_PROP_ZOOM))

    // When
    engine.updateZoomRatio(-100f)

    // Then
    assertEquals(1f, engine.cameraState.zoomRatio.value)
    assertEquals(1.0, capture.lastSetValue(CAP_PROP_ZOOM))
  }

  @Test
  fun `given out-of-range exposure values when updating exposure compensation then value is clamped to camera bounds`() {
    // Given
    val (engine, capture) = buildEngine()

    // When
    engine.updateExposureCompensation(100f)

    // Then
    assertEquals(5f, engine.cameraState.exposureCompensation.value)
    assertEquals(5.0, capture.lastSetValue(CAP_PROP_EXPOSURE))

    // When
    engine.updateExposureCompensation(-100f)

    // Then
    assertEquals(-5f, engine.cameraState.exposureCompensation.value)
    assertEquals(-5.0, capture.lastSetValue(CAP_PROP_EXPOSURE))
  }

  @Test
  fun `given same zoom value when updating zoom ratio then no additional capture set call is made`() {
    // Given
    val (engine, capture) = buildEngine()

    // When
    engine.updateZoomRatio(2f)

    // Then
    assertEquals(1, capture.setCallCount(CAP_PROP_ZOOM))

    // When
    engine.updateZoomRatio(2f)

    // Then
    assertEquals(1, capture.setCallCount(CAP_PROP_ZOOM))
  }

  @Test
  fun `given analyzer set when toggling analyzer enabled then listener attaches once and is removed when disabled`() {
    // Given
    val (engine, capture) = buildEngine()
    engine.updateImageAnalyzer(ImageAnalyzer { })

    // When
    engine.updateImageAnalyzerEnabled(true)

    // Then
    assertEquals(1, capture.addFrameListenerCalls)
    assertEquals(1, capture.frameListenerCount())

    // When
    engine.updateImageAnalyzerEnabled(false)

    // Then
    assertEquals(1, capture.removeFrameListenerCalls)
    assertEquals(0, capture.frameListenerCount())
  }
}

package com.ujizin.camposer.internal.core

import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.info.FakeJvmCameraInfo
import com.ujizin.camposer.internal.capture.FakeJvmCameraCapture
import com.ujizin.camposer.state.properties.CaptureMode
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_EXPOSURE
import org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_ZOOM
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CameraEngineImplTest {
  private data class Harness(
    val engine: CameraEngineImpl,
    val capture: FakeJvmCameraCapture,
  )

  private fun buildEngine(): Harness {
    val capture = FakeJvmCameraCapture()
    val controller = CameraController()
    val info = CameraInfo(FakeJvmCameraInfo())
    return Harness(
      engine = CameraEngineImpl(
        cameraController = controller,
        cameraInfo = info,
        capture = capture,
      ),
      capture = capture,
    )
  }

  @Test
  fun `updateCaptureMode updates cameraState`() {
    val engine = buildEngine().engine
    engine.updateCaptureMode(CaptureMode.Video)
    assertEquals(CaptureMode.Video, engine.cameraState.captureMode.value)
  }

  @Test
  fun `updateZoomRatio clamps to camera bounds`() {
    val (engine, capture) = buildEngine()

    engine.updateZoomRatio(100f)
    assertEquals(10f, engine.cameraState.zoomRatio.value)
    assertEquals(10.0, capture.lastSetValue(CAP_PROP_ZOOM))

    engine.updateZoomRatio(-100f)
    assertEquals(1f, engine.cameraState.zoomRatio.value)
    assertEquals(1.0, capture.lastSetValue(CAP_PROP_ZOOM))
  }

  @Test
  fun `updateExposureCompensation clamps to camera bounds`() {
    val (engine, capture) = buildEngine()

    engine.updateExposureCompensation(100f)
    assertEquals(5f, engine.cameraState.exposureCompensation.value)
    assertEquals(5.0, capture.lastSetValue(CAP_PROP_EXPOSURE))

    engine.updateExposureCompensation(-100f)
    assertEquals(-5f, engine.cameraState.exposureCompensation.value)
    assertEquals(-5.0, capture.lastSetValue(CAP_PROP_EXPOSURE))
  }

  @Test
  fun `updateZoomRatio is no-op when value is unchanged`() {
    val (engine, capture) = buildEngine()

    engine.updateZoomRatio(2f)
    assertEquals(1, capture.setCallCount(CAP_PROP_ZOOM))

    engine.updateZoomRatio(2f)
    assertEquals(1, capture.setCallCount(CAP_PROP_ZOOM))
  }
}

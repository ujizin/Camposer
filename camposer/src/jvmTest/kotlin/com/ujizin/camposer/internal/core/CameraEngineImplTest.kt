package com.ujizin.camposer.internal.core

import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.info.CameraInfo
import com.ujizin.camposer.info.JvmCameraInfo
import com.ujizin.camposer.internal.capture.FakeJvmCameraCapture
import com.ujizin.camposer.state.properties.CaptureMode
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CameraEngineImplTest {

  private fun buildEngine(): CameraEngineImpl {
    val capture = FakeJvmCameraCapture()
    val controller = CameraController()
    val info = CameraInfo(JvmCameraInfo(capture))
    return CameraEngineImpl(
      cameraController = controller,
      cameraInfo = info,
      capture = capture,
    )
  }

  @Test
  fun `updateCaptureMode updates cameraState`() {
    val engine = buildEngine()
    engine.updateCaptureMode(CaptureMode.Video)
    assertEquals(CaptureMode.Video, engine.cameraState.captureMode.value)
  }

  @Test
  fun `updateZoomRatio applies to capture and state`() {
    val engine = buildEngine()
    engine.updateZoomRatio(2f)
    assertEquals(2f, engine.cameraState.zoomRatio.value)
  }
}

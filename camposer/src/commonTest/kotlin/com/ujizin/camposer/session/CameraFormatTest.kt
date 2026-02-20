package com.ujizin.camposer.session

import com.ujizin.camposer.state.properties.format.CamFormat
import com.ujizin.camposer.state.properties.format.config.AspectRatioConfig
import com.ujizin.camposer.state.properties.format.config.ResolutionConfig
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CameraFormatTest : CameraSessionTest() {
  @Test
  fun test_preview_cam_format_4k() {
    val camFormat = CamFormat(
      ResolutionConfig(3860, 2080),
    )

    updateSession(camFormat = camFormat)

    cameraTest.assertCamFormat(camFormat)
    assertEquals(cameraSession.state.camFormat.value, camFormat)
  }

  @Test
  fun test_preview_cam_format_16_9() {
    val camFormat = CamFormat(
      AspectRatioConfig(16F / 9F),
    )

    updateSession(camFormat = camFormat)

    cameraTest.assertCamFormat(camFormat)
    assertEquals(cameraSession.state.camFormat.value, camFormat)
  }

  @Test
  fun test_preview_cam_format_4_3() {
    val camFormat = CamFormat(
      AspectRatioConfig(4F / 3F),
    )

    updateSession(camFormat = camFormat)

    cameraTest.assertCamFormat(camFormat)
    assertEquals(cameraSession.state.camFormat.value, camFormat)
  }
}

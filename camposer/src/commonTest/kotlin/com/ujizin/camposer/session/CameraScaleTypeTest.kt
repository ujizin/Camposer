package com.ujizin.camposer.session

import com.ujizin.camposer.state.properties.ScaleType
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CameraScaleTypeTest : CameraSessionTest() {
  @Test
  fun test_preview_scale_type() {
    initCameraSession()

    ScaleType.entries.forEach { scaleType ->
      updateSession(scaleType = scaleType)

      assertEquals(cameraSession.state.scaleType.value, scaleType)
    }
  }
}

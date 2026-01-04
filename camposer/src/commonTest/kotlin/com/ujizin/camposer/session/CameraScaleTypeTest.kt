package com.ujizin.camposer.session

import androidx.compose.ui.test.ExperimentalTestApi
import com.ujizin.camposer.state.properties.ScaleType
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
internal class CameraScaleTypeTest : CameraSessionTest() {
  @Test
  fun test_preview_scale_type() {
    initCameraSession()

    ScaleType.entries.forEach { scaleType ->
      updateSession(scaleType = scaleType)

      assertEquals(cameraSession.state.scaleType, scaleType)
    }
  }
}

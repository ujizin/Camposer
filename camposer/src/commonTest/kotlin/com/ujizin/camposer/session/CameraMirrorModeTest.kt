package com.ujizin.camposer.session

import com.ujizin.camposer.state.properties.MirrorMode
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CameraMirrorModeTest : CameraSessionTest() {
  @Test
  fun test_preview_mirror_mode() {
    initCameraSession()

    MirrorMode.entries.forEach { mode ->
      controller.setMirrorMode(mode)

      assertEquals(mode, cameraSession.state.mirrorMode.value)
    }
  }
}

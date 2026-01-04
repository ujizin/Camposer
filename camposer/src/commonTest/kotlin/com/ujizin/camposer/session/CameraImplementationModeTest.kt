package com.ujizin.camposer.session

import androidx.compose.ui.test.ExperimentalTestApi
import com.ujizin.camposer.state.properties.ImplementationMode
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
internal class CameraImplementationModeTest : CameraSessionTest() {
  @Test
  fun test_preview_implementation_mode() {
    initCameraSession()

    ImplementationMode.entries.forEach { mode ->
      updateSession(implementationMode = mode)

      assertEquals(cameraSession.state.implementationMode, mode)
    }
  }
}

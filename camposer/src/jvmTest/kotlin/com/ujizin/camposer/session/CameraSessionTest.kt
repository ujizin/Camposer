package com.ujizin.camposer.session

import com.ujizin.camposer.controller.camera.CameraController
import com.ujizin.camposer.internal.capture.FakeJvmCameraCapture
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CameraSessionTest {

  @Test
  fun `session initializes successfully with a working capture`() {
    val controller = CameraController()
    val session = CameraSession(controller, FakeJvmCameraCapture())
    assertTrue(session.isInitialized)
    assertFalse(session.hasInitializationError)
    session.dispose()
  }

  @Test
  fun `dispose stops the frame loop`() {
    val controller = CameraController()
    val session = CameraSession(controller, FakeJvmCameraCapture())
    session.dispose()
    assertFalse(session.isStreaming)
  }
}

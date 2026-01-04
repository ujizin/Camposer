package com.ujizin.camposer.session

import androidx.compose.ui.test.ExperimentalTestApi
import com.ujizin.camposer.state.properties.CaptureMode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
internal class CameraCaptureModeTest : CameraSessionTest() {
  @Test
  fun test_capture_mode_image() =
    runTest {
      val expected = CaptureMode.Image
      updateSession(captureMode = expected)

      assertCaptureMode(expected)
    }

  @Test
  fun test_capture_mode_video() =
    runTest {
      val expected = CaptureMode.Video
      updateSession(
        captureMode = expected,
      )

      assertCaptureMode(expected)
    }

  private fun assertCaptureMode(expected: CaptureMode) {
    cameraTest.assertCaptureMode(expected)
    assertEquals(expected, cameraSession.state.captureMode)
  }
}

package com.ujizin.camposer.session

import androidx.compose.ui.test.ExperimentalTestApi
import com.ujizin.camposer.CaptureResult
import com.ujizin.camposer.state.properties.CaptureMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
internal class CameraRecordVideoTest : CameraSessionTest() {
  @Test
  fun test_record_video() {
    val expectedFilename = "/video/video.mp4"
    var actualFilename = ""

    updateSession(captureMode = CaptureMode.Video)

    controller.startRecording(expectedFilename) { result ->
      result as CaptureResult.Success
      actualFilename = result.data
    }

    assertTrue(controller.isRecording)

    controller.stopRecording()

    assertEquals(expectedFilename, actualFilename)
    assertFalse(controller.isRecording)
  }

  @Test
  fun test_record_video_error() {
    cameraTest.hasErrorInRecording = true

    val expectedFilename = "/video/video.mp4"
    var hasError = false

    updateSession(captureMode = CaptureMode.Video)

    controller.startRecording(expectedFilename) { result ->
      hasError = result is CaptureResult.Error
    }

    assertTrue(controller.isRecording)

    controller.stopRecording()

    assertTrue(hasError)
    assertFalse(controller.isRecording)
  }
}

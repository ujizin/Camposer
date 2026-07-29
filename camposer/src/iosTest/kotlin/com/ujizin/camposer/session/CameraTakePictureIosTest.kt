package com.ujizin.camposer.session

import com.ujizin.camposer.CaptureResult
import com.ujizin.camposer.error.CaptureModeException
import com.ujizin.camposer.state.properties.CaptureMode
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class CameraTakePictureIosTest : CameraSessionTest() {
  @Test
  fun test_take_picture_bytes_fails_when_capture_mode_is_video() {
    updateSession(captureMode = CaptureMode.Video)

    var result: CaptureResult<ByteArray>? = null
    controller.takePicture { result = it }

    val r = assertNotNull(result)
    assertTrue(r is CaptureResult.Error, "expected Error but got $r")
    assertTrue(
      r.throwable is CaptureModeException,
      "expected CaptureModeException but got ${r.throwable}",
    )
  }

  @Test
  fun test_take_picture_filename_fails_when_capture_mode_is_video() {
    updateSession(captureMode = CaptureMode.Video)

    var result: CaptureResult<String>? = null
    controller.takePicture(filename = "test.jpg") { result = it }

    val r = assertNotNull(result)
    assertTrue(r is CaptureResult.Error, "expected Error but got $r")
    assertTrue(
      r.throwable is CaptureModeException,
      "expected CaptureModeException but got ${r.throwable}",
    )
  }

  @Test
  fun test_take_picture_succeeds_when_capture_mode_is_image() {
    updateSession(captureMode = CaptureMode.Image)

    var result: CaptureResult<ByteArray>? = null
    controller.takePicture { result = it }

    val r = assertNotNull(result)
    assertTrue(r is CaptureResult.Success, "expected Success but got $r")
  }
}

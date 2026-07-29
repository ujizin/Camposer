package com.ujizin.camposer.state.properties

import androidx.camera.view.CameraController.IMAGE_CAPTURE
import androidx.camera.view.CameraController.VIDEO_CAPTURE
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CaptureModeAndroidTest {
  @Test
  fun test_image_capture_mode_maps_to_image_capture() {
    assertEquals(IMAGE_CAPTURE, CaptureMode.Image.value)
  }

  @Test
  fun test_video_capture_mode_maps_to_video_capture() {
    assertEquals(VIDEO_CAPTURE, CaptureMode.Video.value)
  }

  @Test
  fun test_all_capture_modes_covered() {
    CaptureMode.entries.forEach { mode ->
      mode.value // must not throw
    }
  }
}

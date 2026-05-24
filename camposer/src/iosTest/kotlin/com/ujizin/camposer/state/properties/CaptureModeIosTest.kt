package com.ujizin.camposer.state.properties

import platform.AVFoundation.AVCaptureMovieFileOutput
import platform.AVFoundation.AVCapturePhotoOutput
import kotlin.test.Test
import kotlin.test.assertTrue

internal class CaptureModeIosTest {
  @Test
  fun test_image_capture_mode_output_is_photo_output() {
    assertTrue(CaptureMode.Image.output is AVCapturePhotoOutput)
  }

  @Test
  fun test_video_capture_mode_output_is_movie_file_output() {
    assertTrue(CaptureMode.Video.output is AVCaptureMovieFileOutput)
  }
}

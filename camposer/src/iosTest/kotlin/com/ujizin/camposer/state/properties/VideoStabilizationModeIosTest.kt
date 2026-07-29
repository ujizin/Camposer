package com.ujizin.camposer.state.properties

import platform.AVFoundation.AVCaptureVideoStabilizationModeCinematic
import platform.AVFoundation.AVCaptureVideoStabilizationModeCinematicExtended
import platform.AVFoundation.AVCaptureVideoStabilizationModeCinematicExtendedEnhanced
import platform.AVFoundation.AVCaptureVideoStabilizationModeOff
import platform.AVFoundation.AVCaptureVideoStabilizationModeStandard
import kotlin.test.Test
import kotlin.test.assertEquals

internal class VideoStabilizationModeIosTest {
  @Test
  fun test_off_maps_to_av_off() {
    assertEquals(AVCaptureVideoStabilizationModeOff, VideoStabilizationMode.Off.value)
  }

  @Test
  fun test_standard_maps_to_av_standard() {
    assertEquals(AVCaptureVideoStabilizationModeStandard, VideoStabilizationMode.Standard.value)
  }

  @Test
  fun test_cinematic_maps_to_av_cinematic() {
    assertEquals(AVCaptureVideoStabilizationModeCinematic, VideoStabilizationMode.Cinematic.value)
  }

  @Test
  fun test_cinematic_extended_maps_to_av_cinematic_extended() {
    assertEquals(
      AVCaptureVideoStabilizationModeCinematicExtended,
      VideoStabilizationMode.CinematicExtended.value,
    )
  }

  @Test
  fun test_cinematic_extended_enhanced_maps_to_av_cinematic_extended_enhanced() {
    assertEquals(
      AVCaptureVideoStabilizationModeCinematicExtendedEnhanced,
      VideoStabilizationMode.CinematicExtendedEnhanced.value,
    )
  }

  @Test
  fun test_all_modes_covered() {
    VideoStabilizationMode.entries.forEach { mode ->
      mode.value // must not throw
    }
  }
}

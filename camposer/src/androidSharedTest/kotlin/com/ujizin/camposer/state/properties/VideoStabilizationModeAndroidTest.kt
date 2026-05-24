package com.ujizin.camposer.state.properties

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class VideoStabilizationModeAndroidTest {
  @Test
  fun test_off_disables_both_video_and_preview_stabilization() {
    val (video, preview) = VideoStabilizationMode.Off.toAndroidStabilizationFlags()
    assertFalse(video)
    assertFalse(preview)
  }

  @Test
  fun test_standard_enables_video_only() {
    val (video, preview) = VideoStabilizationMode.Standard.toAndroidStabilizationFlags()
    assertTrue(video)
    assertFalse(preview)
  }

  @Test
  fun test_cinematic_enables_both_video_and_preview() {
    val (video, preview) = VideoStabilizationMode.Cinematic.toAndroidStabilizationFlags()
    assertTrue(video)
    assertTrue(preview)
  }

  @Test
  fun test_cinematic_extended_enables_both_video_and_preview() {
    val (video, preview) = VideoStabilizationMode.CinematicExtended.toAndroidStabilizationFlags()
    assertTrue(video)
    assertTrue(preview)
  }

  @Test
  fun test_cinematic_extended_enhanced_enables_both_video_and_preview() {
    val (video, preview) = VideoStabilizationMode.CinematicExtendedEnhanced
      .toAndroidStabilizationFlags()
    assertTrue(video)
    assertTrue(preview)
  }

  @Test
  fun test_all_modes_covered() {
    VideoStabilizationMode.entries.forEach { mode ->
      val (video, preview) = mode.toAndroidStabilizationFlags()
      if (mode == VideoStabilizationMode.Off) {
        assertFalse(video)
      } else {
        assertTrue(video)
      }
      assertEquals(
        mode in setOf(
          VideoStabilizationMode.Cinematic,
          VideoStabilizationMode.CinematicExtended,
          VideoStabilizationMode.CinematicExtendedEnhanced,
        ),
        preview,
      )
    }
  }
}

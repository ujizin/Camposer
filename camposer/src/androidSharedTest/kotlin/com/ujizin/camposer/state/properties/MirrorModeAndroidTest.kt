package com.ujizin.camposer.state.properties

import androidx.camera.core.MirrorMode.MIRROR_MODE_OFF
import androidx.camera.core.MirrorMode.MIRROR_MODE_ON
import androidx.camera.core.MirrorMode.MIRROR_MODE_ON_FRONT_ONLY
import kotlin.test.Test
import kotlin.test.assertEquals

internal class MirrorModeAndroidTest {
  @Test
  fun test_mirror_on_maps_to_mirror_mode_on() {
    assertEquals(MIRROR_MODE_ON, MirrorMode.On.mode)
  }

  @Test
  fun test_mirror_off_maps_to_mirror_mode_off() {
    assertEquals(MIRROR_MODE_OFF, MirrorMode.Off.mode)
  }

  @Test
  fun test_mirror_only_in_front_maps_to_mirror_mode_on_front_only() {
    assertEquals(MIRROR_MODE_ON_FRONT_ONLY, MirrorMode.OnlyInFront.mode)
  }

  @Test
  fun test_all_mirror_modes_covered() {
    MirrorMode.entries.forEach { mode ->
      mode.mode // must not throw
    }
  }
}

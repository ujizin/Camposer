package com.ujizin.camposer.state.properties

import com.ujizin.camposer.state.properties.selector.CamPosition
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class MirrorModeJvmTest {
  @Test
  fun test_on_mirrors_every_position() {
    CamPosition.entries.forEach { position ->
      assertTrue(MirrorMode.On.isMirrorEnabled(position))
    }
  }

  @Test
  fun test_off_never_mirrors() {
    CamPosition.entries.forEach { position ->
      assertFalse(MirrorMode.Off.isMirrorEnabled(position))
    }
  }

  @Test
  fun test_only_in_front_mirrors_front_only() {
    assertTrue(MirrorMode.OnlyInFront.isMirrorEnabled(CamPosition.Front))
    assertFalse(MirrorMode.OnlyInFront.isMirrorEnabled(CamPosition.Back))
    assertFalse(MirrorMode.OnlyInFront.isMirrorEnabled(CamPosition.External))
    assertFalse(MirrorMode.OnlyInFront.isMirrorEnabled(CamPosition.Unknown))
  }
}

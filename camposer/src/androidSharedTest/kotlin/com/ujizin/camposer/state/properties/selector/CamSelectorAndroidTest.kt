package com.ujizin.camposer.state.properties.selector

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class CamSelectorAndroidTest {
  @Test
  fun test_selectors_with_same_position_and_lens_are_equal() {
    val a = CamSelector(CamPosition.Back, listOf(CamLensType.Wide))
    val b = CamSelector(CamPosition.Back, listOf(CamLensType.Wide))
    assertEquals(a, b)
  }

  @Test
  fun test_selectors_with_different_positions_not_equal() {
    assertNotEquals(CamSelector.Front, CamSelector.Back)
  }

  @Test
  fun test_selectors_with_different_lens_types_not_equal() {
    val wide = CamSelector(CamPosition.Back, listOf(CamLensType.Wide))
    val telephoto = CamSelector(CamPosition.Back, listOf(CamLensType.Telephoto))
    assertNotEquals(wide, telephoto)
  }

  @Test
  fun test_front_companion_equals_cam_selector_front() {
    assertEquals(CamSelector.Front, CamSelector(CamPosition.Front))
  }

  @Test
  fun test_back_companion_equals_cam_selector_back() {
    assertEquals(CamSelector.Back, CamSelector(CamPosition.Back))
  }

  @Test
  fun test_empty_lens_types_defaults_to_wide() {
    val withEmpty = CamSelector(CamPosition.Back, emptyList())
    val withWide = CamSelector(CamPosition.Back, listOf(CamLensType.Wide))
    assertEquals(withWide, withEmpty)
  }

  @Test
  fun test_hashcode_equal_for_equal_selectors() {
    val a = CamSelector(CamPosition.Back, listOf(CamLensType.Wide))
    val b = CamSelector(CamPosition.Back, listOf(CamLensType.Wide))
    assertEquals(a.hashCode(), b.hashCode())
  }

  @Test
  fun test_to_string_contains_position_and_lens() {
    val selector = CamSelector(CamPosition.Back, listOf(CamLensType.Wide))
    val str = selector.toString()
    assert(str.contains("Back"))
    assert(str.contains("Wide"))
  }
}

package com.ujizin.camposer.codescanner

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class FrameRectTest {
  @Test
  fun width_is_right_minus_left() {
    val rect = FrameRect(left = 10, top = 0, right = 50, bottom = 0)
    assertEquals(40, rect.width)
  }

  @Test
  fun height_is_bottom_minus_top() {
    val rect = FrameRect(left = 0, top = 20, right = 0, bottom = 80)
    assertEquals(60, rect.height)
  }

  @Test
  fun width_zero_when_left_equals_right() {
    val rect = FrameRect(left = 30, top = 0, right = 30, bottom = 100)
    assertEquals(0, rect.width)
  }

  @Test
  fun height_zero_when_top_equals_bottom() {
    val rect = FrameRect(left = 0, top = 50, right = 100, bottom = 50)
    assertEquals(0, rect.height)
  }

  @Test
  fun equals_true_for_same_coordinates() {
    val a = FrameRect(1, 2, 3, 4)
    val b = FrameRect(1, 2, 3, 4)
    assertEquals(a, b)
  }

  @Test
  fun equals_false_when_left_differs() {
    assertNotEquals(FrameRect(9, 2, 3, 4), FrameRect(1, 2, 3, 4))
  }

  @Test
  fun equals_false_when_top_differs() {
    assertNotEquals(FrameRect(1, 9, 3, 4), FrameRect(1, 2, 3, 4))
  }

  @Test
  fun equals_false_when_right_differs() {
    assertNotEquals(FrameRect(1, 2, 9, 4), FrameRect(1, 2, 3, 4))
  }

  @Test
  fun equals_false_when_bottom_differs() {
    assertNotEquals(FrameRect(1, 2, 3, 9), FrameRect(1, 2, 3, 4))
  }

  @Test
  fun hashCode_equal_for_equal_objects() {
    val a = FrameRect(1, 2, 3, 4)
    val b = FrameRect(1, 2, 3, 4)
    assertEquals(a.hashCode(), b.hashCode())
  }

  @Test
  fun toString_contains_all_coordinate_values() {
    val str = FrameRect(left = 11, top = 22, right = 33, bottom = 44).toString()
    assertTrue(str.contains("11"))
    assertTrue(str.contains("22"))
    assertTrue(str.contains("33"))
    assertTrue(str.contains("44"))
  }
}

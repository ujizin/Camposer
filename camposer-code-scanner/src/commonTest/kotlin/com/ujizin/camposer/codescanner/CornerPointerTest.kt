package com.ujizin.camposer.codescanner

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class CornerPointerTest {
  @Test
  fun equals_true_for_same_xy() {
    assertEquals(CornerPointer(10, 20), CornerPointer(10, 20))
  }

  @Test
  fun equals_false_when_x_differs() {
    assertNotEquals(CornerPointer(99, 20), CornerPointer(10, 20))
  }

  @Test
  fun equals_false_when_y_differs() {
    assertNotEquals(CornerPointer(10, 99), CornerPointer(10, 20))
  }

  @Test
  fun hashCode_equal_for_equal_objects() {
    assertEquals(CornerPointer(10, 20).hashCode(), CornerPointer(10, 20).hashCode())
  }

  @Test
  fun toString_contains_x_and_y_values() {
    val str = CornerPointer(x = 5, y = 15).toString()
    assertTrue(str.contains("5"))
    assertTrue(str.contains("15"))
  }
}

package com.ujizin.camposer.codescanner

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class CodeResultTest {
  private val rect = FrameRect(0, 0, 100, 100)
  private val corners = listOf(CornerPointer(0, 0), CornerPointer(100, 0))

  @Test
  fun equals_true_for_same_values() {
    val a = CodeResult(CodeType.QRCode, "hello", rect, corners)
    val b = CodeResult(CodeType.QRCode, "hello", rect, corners)
    assertEquals(a, b)
  }

  @Test
  fun equals_false_when_type_differs() {
    assertNotEquals(
      CodeResult(CodeType.QRCode, "hello", rect, corners),
      CodeResult(CodeType.Aztec, "hello", rect, corners),
    )
  }

  @Test
  fun equals_false_when_text_differs() {
    assertNotEquals(
      CodeResult(CodeType.QRCode, "hello", rect, corners),
      CodeResult(CodeType.QRCode, "world", rect, corners),
    )
  }

  @Test
  fun equals_false_when_frameRect_differs() {
    assertNotEquals(
      CodeResult(CodeType.QRCode, "hello", rect, corners),
      CodeResult(CodeType.QRCode, "hello", FrameRect(1, 2, 3, 4), corners),
    )
  }

  @Test
  fun equals_false_when_corners_differ() {
    assertNotEquals(
      CodeResult(CodeType.QRCode, "hello", rect, corners),
      CodeResult(CodeType.QRCode, "hello", rect, emptyList()),
    )
  }

  @Test
  fun hashCode_equal_for_equal_objects() {
    val a = CodeResult(CodeType.QRCode, "hello", rect, corners)
    val b = CodeResult(CodeType.QRCode, "hello", rect, corners)
    assertEquals(a.hashCode(), b.hashCode())
  }

  @Test
  fun toString_contains_type_and_text() {
    val str = CodeResult(CodeType.QRCode, "abc123", rect, corners).toString()
    assertTrue(str.contains("QRCode"))
    assertTrue(str.contains("abc123"))
  }
}

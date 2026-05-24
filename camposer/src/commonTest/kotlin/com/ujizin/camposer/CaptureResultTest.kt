package com.ujizin.camposer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class CaptureResultTest {
  @Test
  fun test_success_holds_data() {
    val result = CaptureResult.Success("payload")
    assertEquals("payload", result.data)
  }

  @Test
  fun test_success_equality_same_data() {
    assertEquals(CaptureResult.Success("a"), CaptureResult.Success("a"))
  }

  @Test
  fun test_success_not_equal_different_data() {
    assertNotEquals(CaptureResult.Success("a"), CaptureResult.Success("b"))
  }

  @Test
  fun test_error_holds_throwable() {
    val cause = RuntimeException("fail")
    val result = CaptureResult.Error(cause)
    assertSame(cause, result.throwable)
  }

  @Test
  fun test_error_equality_same_throwable() {
    val cause = RuntimeException("fail")
    assertEquals(CaptureResult.Error(cause), CaptureResult.Error(cause))
  }

  @Test
  fun test_error_not_equal_different_throwable() {
    assertNotEquals(
      CaptureResult.Error(RuntimeException("a")),
      CaptureResult.Error(RuntimeException("b")),
    )
  }

  @Test
  fun test_success_is_not_error() {
    val result: CaptureResult<String> = CaptureResult.Success("x")
    assertFalse(result is CaptureResult.Error)
  }

  @Test
  fun test_error_is_not_success() {
    val result: CaptureResult<Nothing> = CaptureResult.Error(RuntimeException())
    assertFalse(result is CaptureResult.Success)
  }

  @Test
  fun test_success_with_null_data() {
    val result = CaptureResult.Success<String?>(null)
    assertTrue(result is CaptureResult.Success)
  }
}

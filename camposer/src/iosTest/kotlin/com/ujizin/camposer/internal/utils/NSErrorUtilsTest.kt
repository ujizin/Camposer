package com.ujizin.camposer.internal.utils

import com.ujizin.camposer.internal.error.NSErrorException
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.pointed
import kotlinx.cinterop.value
import platform.Foundation.NSError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal class NSErrorUtilsTest {
  @Test
  fun test_returns_value_when_no_error() {
    val result = executeWithErrorHandling<String> { _ -> "success" }
    assertEquals("success", result)
  }

  @Test
  fun test_returns_int_value_when_no_error() {
    val result = executeWithErrorHandling<Int> { _ -> 42 }
    assertEquals(42, result)
  }

  @Test
  fun test_throws_ns_error_exception_when_error_is_set() {
    assertFailsWith<NSErrorException> {
      executeWithErrorHandling<Unit> { errorPtr ->
        errorPtr.pointed.value =
          NSError.errorWithDomain(
            domain = "com.ujizin.camposer.test",
            code = 1L,
            userInfo = null,
          )
      }
    }
  }

  @Test
  fun test_exception_message_contains_error_description() {
    val ex =
      assertFailsWith<NSErrorException> {
        executeWithErrorHandling<Unit> { errorPtr ->
          errorPtr.pointed.value =
            NSError.errorWithDomain(
              domain = "com.ujizin.camposer.test",
              code = 99L,
              userInfo = null,
            )
        }
      }
    val msg = assertNotNull(ex.message, "exception message must not be null")
    assertTrue(msg.isNotEmpty(), "exception message must not be empty")
  }
}

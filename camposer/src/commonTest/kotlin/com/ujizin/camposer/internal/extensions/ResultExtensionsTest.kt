package com.ujizin.camposer.internal.extensions

import com.ujizin.camposer.CaptureResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class ResultExtensionsTest {
  @Test
  fun test_success_result_maps_to_capture_result_success() {
    val result = Result.success("hello").toCaptureResult()
    assertTrue(result is CaptureResult.Success)
    assertEquals("hello", (result as CaptureResult.Success).data)
  }

  @Test
  fun test_failure_result_maps_to_capture_result_error() {
    val cause = RuntimeException("oops")
    val result = Result.failure<String>(cause).toCaptureResult()
    assertTrue(result is CaptureResult.Error)
    assertSame(cause, (result as CaptureResult.Error).throwable)
  }

  @Test
  fun test_success_with_null_data_maps_to_capture_result_success() {
    val result = Result.success<String?>(null).toCaptureResult()
    assertTrue(result is CaptureResult.Success)
  }
}

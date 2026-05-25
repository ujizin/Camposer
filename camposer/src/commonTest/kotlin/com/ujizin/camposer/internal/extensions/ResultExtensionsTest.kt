package com.ujizin.camposer.internal.extensions

import com.ujizin.camposer.CaptureResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

internal class ResultExtensionsTest {
  @Test
  fun test_success_result_maps_to_capture_result_success() {
    val result = Result.success("hello").toCaptureResult()
    assertEquals("hello", assertIs<CaptureResult.Success<String>>(result).data)
  }

  @Test
  fun test_failure_result_maps_to_capture_result_error() {
    val cause = RuntimeException("oops")
    val result = Result.failure<String>(cause).toCaptureResult()
    assertSame(cause, assertIs<CaptureResult.Error>(result).throwable)
  }

  @Test
  fun test_success_with_null_data_maps_to_capture_result_success() {
    val result = Result.success<String?>(null).toCaptureResult()
    assertIs<CaptureResult.Success<String?>>(result)
  }
}

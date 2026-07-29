package com.ujizin.camposer.error

import com.ujizin.camposer.state.properties.CaptureMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class ExceptionsTest {
  // ── CaptureModeException ─────────────────────────────────────────────────────

  @Test
  fun test_capture_mode_exception_message_contains_mode_name() {
    val ex = CaptureModeException(CaptureMode.Image)
    assertTrue(ex.message!!.contains("Image"), "message='${ex.message}'")
  }

  @Test
  fun test_capture_mode_exception_message_contains_video_mode_name() {
    val ex = CaptureModeException(CaptureMode.Video)
    assertTrue(ex.message!!.contains("Video"), "message='${ex.message}'")
  }

  @Test
  fun test_capture_mode_exception_uses_custom_message_when_provided() {
    val ex = CaptureModeException(CaptureMode.Image, "custom message")
    assertEquals("custom message", ex.message)
  }

  @Test
  fun test_capture_mode_exception_is_exception() {
    val ex: Any = CaptureModeException(CaptureMode.Image)
    assertIs<Exception>(ex)
  }

  // ── RecordNotInitializedException ────────────────────────────────────────────

  @Test
  fun test_record_not_initialized_exception_has_message() {
    val ex = RecordNotInitializedException()
    assertTrue(ex.message!!.isNotEmpty())
  }

  @Test
  fun test_record_not_initialized_exception_is_exception() {
    val ex: Any = RecordNotInitializedException()
    assertIs<Exception>(ex)
  }
}

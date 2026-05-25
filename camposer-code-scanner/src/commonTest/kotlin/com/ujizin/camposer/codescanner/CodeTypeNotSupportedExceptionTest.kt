package com.ujizin.camposer.codescanner

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class CodeTypeNotSupportedExceptionTest {
  @Test
  fun codeType_property_preserved() {
    val ex = CodeTypeNotSupportedException(CodeType.Aztec)
    assertEquals(CodeType.Aztec, ex.codeType)
  }

  @Test
  fun default_message_contains_code_type_name() {
    val ex = CodeTypeNotSupportedException(CodeType.QRCode)
    assertNotNull(ex.message)
    assertTrue(ex.message!!.contains("QRCode"))
  }

  @Test
  fun custom_message_overrides_default() {
    val ex = CodeTypeNotSupportedException(CodeType.QRCode, "custom error")
    assertEquals("custom error", ex.message)
  }

  @Test
  fun is_exception_subtype() {
    val ex: Any = CodeTypeNotSupportedException(CodeType.QRCode)
    assertIs<Exception>(ex)
  }

  @Test
  fun all_code_types_produce_non_null_message() {
    CodeType.entries.forEach { type ->
      val ex = CodeTypeNotSupportedException(type)
      assertNotNull(ex.message)
    }
  }
}

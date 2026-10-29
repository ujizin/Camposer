package com.ujizin.camposer.codescanner

import com.google.zxing.BarcodeFormat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class CodeTypeJvmTest {
  @Test
  fun test_every_code_type_maps_to_a_distinct_format() {
    val formats = CodeType.entries.map { it.format }

    assertEquals(formats.size, formats.toSet().size)
  }

  @Test
  fun test_from_format_round_trips() {
    CodeType.entries.forEach { type ->
      assertEquals(type, CodeType.fromFormat(type.format))
    }
  }

  @Test
  fun test_from_format_returns_null_for_unsupported_format() {
    assertNull(CodeType.fromFormat(BarcodeFormat.MAXICODE))
  }
}

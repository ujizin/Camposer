package com.ujizin.camposer.state.properties.format

import com.ujizin.camposer.state.properties.format.config.ResolutionConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class CamFormatTest {

  @Test
  fun `default constructor stores empty configs`() {
    val format = CamFormat()
    assertEquals(CamFormat.Default.configs, format.configs)
  }

  @Test
  fun `vararg constructor stores provided configs`() {
    val config = ResolutionConfig.High
    val format = CamFormat(config)
    assertEquals(listOf(config), format.configs)
  }

  @Test
  fun `equality is based on configs`() {
    val a = CamFormat(ResolutionConfig.High)
    val b = CamFormat(ResolutionConfig.High)
    val c = CamFormat(ResolutionConfig.Medium)
    assertEquals(a, b)
    assertNotEquals(a, c)
  }

  @Test
  fun `equal instances have equal hashCodes`() {
    val a = CamFormat(ResolutionConfig.High)
    val b = CamFormat(ResolutionConfig.High)
    assertEquals(a.hashCode(), b.hashCode())
  }
}

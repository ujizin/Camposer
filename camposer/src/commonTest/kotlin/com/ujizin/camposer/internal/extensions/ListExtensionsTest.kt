package com.ujizin.camposer.internal.extensions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ListExtensionsTest {
  @Test
  fun test_returns_first_element_of_matching_type() {
    val list: List<Any> = listOf(1, "hello", 2, "world")
    assertEquals("hello", list.firstIsInstanceOrNull<String>())
  }

  @Test
  fun test_returns_null_when_no_element_matches_type() {
    val list: List<Any> = listOf(1, 2, 3)
    assertNull(list.firstIsInstanceOrNull<String>())
  }

  @Test
  fun test_returns_null_for_empty_list() {
    assertNull(emptyList<Any>().firstIsInstanceOrNull<String>())
  }

  @Test
  fun test_returns_first_not_second_matching_element() {
    val first = "first"
    val second = "second"
    val list: List<Any> = listOf(first, second)
    assertEquals(first, list.firstIsInstanceOrNull<String>())
  }

  @Test
  fun test_returns_null_when_type_does_not_match() {
    val list: List<Any> = listOf("text")
    assertNull(list.firstIsInstanceOrNull<Int>())
  }
}

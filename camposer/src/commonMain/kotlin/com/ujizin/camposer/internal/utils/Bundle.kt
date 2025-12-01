package com.ujizin.camposer.internal.utils

internal class Bundle {
  private val map = mutableMapOf<String, Any>()

  @Suppress("UNCHECKED_CAST")
  operator fun <T> get(key: String): T? = map[key] as? T

  operator fun <T : Any> set(
    key: String,
    value: T,
  ) {
    map[key] = value
  }

  fun clear() {
    map.clear()
  }
}

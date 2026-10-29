package com.ujizin.camposer.internal.utils

/**
 * Desktop (JVM) implementation of Camposer logger.
 * Writes to the standard error stream with a "Camposer" prefix.
 * Debug messages are printed only when the `camposer.debug` system property is `true`.
 */
internal actual object Logger {
  private const val TAG = "Camposer"
  private const val DEBUG_PROPERTY = "camposer.debug"

  private val isDebugEnabled: Boolean
    get() = System.getProperty(DEBUG_PROPERTY)?.toBoolean() == true

  actual fun d(message: String) {
    if (!isDebugEnabled) return
    System.err.println("[$TAG] DEBUG: $message")
  }

  actual fun error(
    message: String,
    throwable: Throwable?,
  ) {
    System.err.println("[$TAG] ERROR: $message")
    throwable?.printStackTrace()
  }
}

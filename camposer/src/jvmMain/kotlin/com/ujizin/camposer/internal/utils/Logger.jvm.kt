package com.ujizin.camposer.internal.utils

/**
 * JVM implementation of Camposer logger.
 * Writes debug output to stdout and errors to stderr.
 * Debug logging is gated by the `camposer.debug` system property.
 */
internal actual object Logger {
  private const val TAG = "Camposer"

  private val isDebugEnabled: Boolean by lazy {
    System.getProperty("camposer.debug")?.toBooleanStrictOrNull() ?: false
  }

  actual fun d(message: String) {
    if (!isDebugEnabled) return
    println("[$TAG] DEBUG: $message")
  }

  actual fun error(
    message: String,
    throwable: Throwable?,
  ) {
    System.err.println("[$TAG] ERROR: $message")
    throwable?.printStackTrace(System.err)
  }
}

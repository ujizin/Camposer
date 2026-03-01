package com.ujizin.camposer.internal.utils

/**
 * JVM implementation of Camposer logger.
 * Writes debug output to stdout and errors to stderr.
 * Note: debug logging is unconditional on JVM desktop (no release-build stripping equivalent).
 */
internal actual object Logger {
  private const val TAG = "Camposer"

  actual fun d(message: String) {
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

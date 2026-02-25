package com.ujizin.camposer.internal.utils

/**
 * Internal logger for Camposer library.
 * Logs only in debug builds to prevent log pollution in production.
 */
internal expect object Logger {
  /**
   * Log a debug message.
   *
   * @param message The debug message to log
   */
  fun d(message: String)

  /**
   * Log an error message with optional throwable.
   *
   * @param message The error message to log
   * @param throwable Optional throwable for stack trace
   */
  fun error(
    message: String,
    throwable: Throwable? = null,
  )
}

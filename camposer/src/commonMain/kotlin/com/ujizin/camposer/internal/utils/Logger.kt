package com.ujizin.camposer.internal.utils

/**
 * Internal logger for Camposer library.
 * Logs only in debug builds to prevent log pollution in production.
 */
internal expect object Logger {
  /**
   * Check if the current build is a debug build.
   */
  val isDebugBuild: Boolean

  /**
   * Log a debug message.
   * Only logs if [isDebugBuild] is true.
   *
   * @param message The debug message to log
   */
  fun debug(message: String)

  /**
   * Log an error message with optional throwable.
   * Only logs if [isDebugBuild] is true.
   *
   * @param message The error message to log
   * @param throwable Optional throwable for stack trace
   */
  fun error(message: String, throwable: Throwable? = null)
}

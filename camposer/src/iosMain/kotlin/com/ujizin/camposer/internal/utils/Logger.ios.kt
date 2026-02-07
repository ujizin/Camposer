package com.ujizin.camposer.internal.utils

import platform.Foundation.NSLog
import kotlin.experimental.ExperimentalNativeApi

/**
 * iOS implementation of Camposer logger.
 * Uses NSLog for logging with "Camposer" prefix.
 */
internal actual object Logger {
  private const val TAG = "Camposer"

  @OptIn(ExperimentalNativeApi::class)
  actual fun debug(message: String) {
    if (!Platform.isDebugBinary) return
    NSLog("[$TAG] DEBUG: $message")
  }

  actual fun error(
    message: String,
    throwable: Throwable?,
  ) {
    if (throwable == null) {
      NSLog("[$TAG] ERROR: $message")
      return
    }

    NSLog("[$TAG] ERROR: $message - ${throwable.message}")
    NSLog("[$TAG] Stack trace: ${throwable.stackTraceToString()}")
  }
}

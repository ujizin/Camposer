package com.ujizin.camposer.internal.utils

import platform.Foundation.NSLog

/**
 * iOS implementation of Camposer logger.
 * Uses NSLog for logging with "Camposer" prefix.
 */
internal actual object Logger {
  private const val TAG = "Camposer"

  // iOS doesn't have a built-in debug flag, so we check for DEBUG preprocessor
  // In practice, this will be optimized away in release builds
  actual val isDebugBuild: Boolean
    get() = isDebugMode()

  actual fun debug(message: String) {
    if (!isDebugBuild) return
    NSLog("[$TAG] DEBUG: $message")
  }

  actual fun error(message: String, throwable: Throwable?) {
    if (!isDebugBuild) return
    
    if (throwable != null) {
      NSLog("[$TAG] ERROR: $message - ${throwable.message}")
      NSLog("[$TAG] Stack trace: ${throwable.stackTraceToString()}")
    } else {
      NSLog("[$TAG] ERROR: $message")
    }
  }

  // Helper to detect debug mode on iOS
  // This checks if the app is built with optimization disabled
  private fun isDebugMode(): Boolean {
    // In Kotlin/Native, we can check the optimization level
    // Release builds are optimized, debug builds are not
    return kotlinx.cinterop.Platform.isDebugBinary
  }
}

package com.ujizin.camposer.internal.utils

import android.util.Log
import com.ujizin.camposer.BuildConfig

/**
 * Android implementation of Camposer logger.
 * Uses Android's Log system with "Camposer" tag.
 */
internal actual object Logger {
  private const val TAG = "Camposer"

  actual val isDebugBuild: Boolean
    get() = BuildConfig.DEBUG

  actual fun debug(message: String) {
    if (!isDebugBuild) return
    Log.d(TAG, message)
  }

  actual fun error(message: String, throwable: Throwable?) {
    if (!isDebugBuild) return
    
    if (throwable != null) {
      Log.e(TAG, message, throwable)
    } else {
      Log.e(TAG, message)
    }
  }
}

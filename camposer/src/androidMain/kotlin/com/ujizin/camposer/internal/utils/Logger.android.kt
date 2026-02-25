package com.ujizin.camposer.internal.utils

import android.util.Log

/**
 * Android implementation of Camposer logger.
 * Uses Android's Log system with "Camposer" tag.
 */
internal actual object Logger {
  private const val TAG = "Camposer"

  actual fun d(message: String) {
    Log.d(TAG, message)
  }

  actual fun error(
    message: String,
    throwable: Throwable?,
  ) {
    Log.e(TAG, message, throwable)
  }
}

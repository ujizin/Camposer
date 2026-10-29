package com.ujizin.camposer.internal.record

import java.nio.ShortBuffer

/**
 * Microphone input delivered as signed 16-bit PCM samples.
 */
internal interface JvmAudioCapture {
  val sampleRate: Int

  val channels: Int

  /**
   * Opens the microphone and delivers samples on a background thread.
   *
   * Throws when no microphone line is available.
   */
  fun start(onSamples: (ShortBuffer) -> Unit)

  fun stop()

  fun interface Factory {
    fun create(): JvmAudioCapture
  }
}

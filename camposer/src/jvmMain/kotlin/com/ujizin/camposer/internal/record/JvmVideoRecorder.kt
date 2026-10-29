package com.ujizin.camposer.internal.record

import com.ujizin.camposer.internal.core.jvm.JvmFrame
import java.nio.ShortBuffer

/**
 * Output settings of one recording.
 *
 * @property audioChannels `0` records video only.
 */
internal class RecordingConfig(
  val filename: String,
  val width: Int,
  val height: Int,
  val frameRate: Double,
  val audioChannels: Int,
  val sampleRate: Int,
)

/**
 * Encodes frames and audio samples into a video file.
 *
 * Calls are not thread-safe. The owner serializes access.
 */
internal interface JvmVideoRecorder {
  fun start()

  fun recordFrame(
    frame: JvmFrame,
    isMirrored: Boolean,
    timestampMicros: Long,
  )

  fun recordSamples(samples: ShortBuffer)

  /** Finalizes the file and releases native resources. */
  fun stop()

  fun interface Factory {
    fun create(config: RecordingConfig): JvmVideoRecorder
  }
}

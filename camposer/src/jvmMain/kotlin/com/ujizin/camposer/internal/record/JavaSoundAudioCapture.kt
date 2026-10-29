package com.ujizin.camposer.internal.record

import com.ujizin.camposer.internal.utils.Logger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.TargetDataLine

private const val DEFAULT_SAMPLE_RATE = 44_100
private const val DEFAULT_CHANNELS = 1
private const val SAMPLE_SIZE_BITS = 16
private const val BYTES_PER_SAMPLE = 2
private const val BUFFER_DIVISOR = 5
private const val JOIN_TIMEOUT_MS = 1_000L

/**
 * [JvmAudioCapture] backed by `javax.sound.sampled`. Reads from the default microphone.
 */
internal class JavaSoundAudioCapture(
  override val sampleRate: Int = DEFAULT_SAMPLE_RATE,
  override val channels: Int = DEFAULT_CHANNELS,
) : JvmAudioCapture {
  private var line: TargetDataLine? = null
  private var thread: Thread? = null

  @Volatile
  private var isRunning = false

  override fun start(onSamples: (ShortBuffer) -> Unit) {
    val format = AudioFormat(sampleRate.toFloat(), SAMPLE_SIZE_BITS, channels, true, false)
    val openedLine = AudioSystem.getTargetDataLine(format)
    openedLine.open(format)
    openedLine.start()
    line = openedLine
    isRunning = true

    thread = Thread({ readLoop(openedLine, onSamples) }, "Camposer-Audio").apply {
      isDaemon = true
      start()
    }
  }

  private fun readLoop(
    openedLine: TargetDataLine,
    onSamples: (ShortBuffer) -> Unit,
  ) {
    val bufferSize = (openedLine.bufferSize / BUFFER_DIVISOR).coerceAtLeast(BYTES_PER_SAMPLE)
    val buffer = ByteArray(bufferSize - bufferSize % BYTES_PER_SAMPLE)
    while (isRunning) {
      val read = openedLine.read(buffer, 0, buffer.size)
      if (read < BYTES_PER_SAMPLE) continue

      val sampleCount = read / BYTES_PER_SAMPLE
      val samples = ShortArray(sampleCount)
      ByteBuffer
        .wrap(buffer, 0, sampleCount * BYTES_PER_SAMPLE)
        .order(ByteOrder.LITTLE_ENDIAN)
        .asShortBuffer()
        .get(samples)
      onSamples(ShortBuffer.wrap(samples))
    }
  }

  override fun stop() {
    isRunning = false
    line?.let { openedLine ->
      openedLine.stop()
      openedLine.close()
    }
    line = null
    thread?.let { captureThread ->
      runCatching { captureThread.join(JOIN_TIMEOUT_MS) }
        .onFailure { Logger.error("Audio capture thread did not stop in time", it) }
    }
    thread = null
  }

  internal companion object Factory : JvmAudioCapture.Factory {
    override fun create(): JvmAudioCapture = JavaSoundAudioCapture()
  }
}

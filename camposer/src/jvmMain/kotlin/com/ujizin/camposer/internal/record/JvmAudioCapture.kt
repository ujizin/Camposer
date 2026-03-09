package com.ujizin.camposer.internal.record

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.nio.ShortBuffer
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.TargetDataLine

internal class JvmAudioCapture(
  private val sampleRate: Int = 44100,
  private val channels: Int = 1,
  private val audioDeviceSelector: AudioDeviceSelector = DefaultAudioDeviceSelector,
) {
  private var line: TargetDataLine? = null
  private var captureJob: Job? = null
  private val _isMuted = MutableStateFlow(false)
  val isMuted: StateFlow<Boolean> = _isMuted

  // Throws if the audio device cannot be opened.
  internal fun start(
    scope: CoroutineScope,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    onSamples: (ShortBuffer) -> Unit,
  ) {
    val format = AudioFormat(sampleRate.toFloat(), 16, channels, true, false)
    val openedLine = audioDeviceSelector.openLine(format)
    openedLine.open(format)
    openedLine.start()
    line = openedLine

    captureJob = scope.launch(dispatcher) {
      val buffer = ByteArray(openedLine.bufferSize / 5)
      while (isActive) {
        val read = openedLine.read(buffer, 0, buffer.size)
        if (read <= 0 || _isMuted.value) {
          yield() // Suspend to allow cancellation and cooperative scheduling.
          continue
        }
        // Mask off the low bit to guarantee an even byte count before halving (16-bit PCM alignment).
        val sampleCount = (read and 1.inv()) / 2
        val shorts = ShortArray(sampleCount) { i ->
          ((buffer[i * 2 + 1].toInt() shl 8) or (buffer[i * 2].toInt() and 0xFF)).toShort()
        }
        onSamples(ShortBuffer.wrap(shorts))
      }
    }
  }

  internal fun stop() {
    captureJob?.cancel()
    captureJob = null
    line?.stop()
    line?.close()
    line = null
  }

  internal fun mute(muted: Boolean) {
    _isMuted.value = muted
  }
}

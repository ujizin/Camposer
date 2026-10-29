package com.ujizin.camposer.fake

import com.ujizin.camposer.internal.record.JvmAudioCapture
import java.nio.ShortBuffer

internal class FakeJvmAudioCapture(
  override val sampleRate: Int = 44_100,
  override val channels: Int = 1,
  private val failOnStart: Boolean = false,
) : JvmAudioCapture {
  private var onSamples: ((ShortBuffer) -> Unit)? = null

  var isStarted = false
    private set

  var isStopped = false
    private set

  override fun start(onSamples: (ShortBuffer) -> Unit) {
    if (failOnStart) throw IllegalStateException("fake microphone unavailable")
    this.onSamples = onSamples
    isStarted = true
  }

  override fun stop() {
    isStopped = true
    onSamples = null
  }

  fun emit(samples: ShortArray) {
    onSamples?.invoke(ShortBuffer.wrap(samples))
  }

  class Factory(
    private val failOnStart: Boolean = false,
  ) : JvmAudioCapture.Factory {
    val created = mutableListOf<FakeJvmAudioCapture>()

    val last: FakeJvmAudioCapture
      get() = created.last()

    override fun create(): JvmAudioCapture =
      FakeJvmAudioCapture(failOnStart = failOnStart).also { created += it }
  }
}

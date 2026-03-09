package com.ujizin.camposer.fake

import com.ujizin.camposer.internal.record.AudioDeviceSelector
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.Control
import javax.sound.sampled.Line
import javax.sound.sampled.LineListener
import javax.sound.sampled.TargetDataLine

/**
 * A no-op [TargetDataLine] for use in tests that need an audio line but do not exercise
 * real audio I/O. All methods are silent stubs; [read] always returns 0.
 */
internal class NoOpTargetDataLine : TargetDataLine {
  override fun open(format: AudioFormat) = Unit

  override fun open() = Unit

  override fun open(
    format: AudioFormat,
    bufferSize: Int,
  ) = Unit

  override fun start() = Unit

  override fun stop() = Unit

  override fun close() = Unit

  override fun drain() = Unit

  override fun flush() = Unit

  override fun isRunning(): Boolean = false

  override fun isActive(): Boolean = false

  override fun getFormat(): AudioFormat = AudioFormat(44100f, 16, 1, true, false)

  override fun getBufferSize(): Int = 1024

  override fun available(): Int = 0

  override fun getFramePosition(): Int = 0

  override fun getLongFramePosition(): Long = 0L

  override fun getMicrosecondPosition(): Long = 0L

  override fun getLevel(): Float = 0f

  override fun isOpen(): Boolean = false

  override fun getControls(): Array<Control> = emptyArray()

  override fun isControlSupported(control: Control.Type): Boolean = false

  override fun getControl(control: Control.Type): Control = throw UnsupportedOperationException()

  override fun addLineListener(listener: LineListener) = Unit

  override fun removeLineListener(listener: LineListener) = Unit

  override fun getLineInfo(): Line.Info = Line.Info(TargetDataLine::class.java)

  override fun read(
    b: ByteArray,
    off: Int,
    len: Int,
  ): Int = 0
}

/** An [AudioDeviceSelector] that always returns a [NoOpTargetDataLine]. */
internal class NoOpAudioDeviceSelector : AudioDeviceSelector {
  override fun openLine(format: AudioFormat): TargetDataLine = NoOpTargetDataLine()
}

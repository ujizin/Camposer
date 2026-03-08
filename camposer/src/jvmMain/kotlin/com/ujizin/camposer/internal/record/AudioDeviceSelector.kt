package com.ujizin.camposer.internal.record

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.TargetDataLine

internal fun interface AudioDeviceSelector {
  fun openLine(format: AudioFormat): TargetDataLine
}

internal object DefaultAudioDeviceSelector : AudioDeviceSelector {
  override fun openLine(format: AudioFormat): TargetDataLine {
    val info = DataLine.Info(TargetDataLine::class.java, format)
    return AudioSystem.getLine(info) as TargetDataLine
  }
}

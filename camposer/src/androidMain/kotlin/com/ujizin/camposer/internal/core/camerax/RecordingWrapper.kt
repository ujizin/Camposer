package com.ujizin.camposer.internal.core.camerax

import androidx.camera.video.Recording

internal interface RecordingWrapper {
  fun resume()

  fun pause()

  fun stop()

  fun mute(isMuted: Boolean)
}

internal class RecordingWrapperImpl(
  private val recording: Recording,
) : RecordingWrapper {
  override fun resume() = recording.resume()

  override fun pause() = recording.pause()

  override fun stop() = recording.stop()

  override fun mute(isMuted: Boolean) = recording.mute(isMuted)
}

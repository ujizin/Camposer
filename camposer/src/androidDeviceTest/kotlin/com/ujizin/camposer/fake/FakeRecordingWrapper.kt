package com.ujizin.camposer.fake

import com.ujizin.camposer.internal.core.camerax.RecordingWrapper

class FakeRecordingWrapper(
  private val onRecord: () -> Unit,
) : RecordingWrapper {
  var isRecording = false
    private set

  var isRunning = false
    private set

  var isMuted = false
    private set

  init {
    isRecording = true
  }

  override fun resume() {
    isRunning = isRecording && true
  }

  override fun pause() {
    isRunning = false
  }

  override fun stop() {
    isRecording = false
    onRecord()
  }

  override fun mute(isMuted: Boolean) {
    this.isMuted = isMuted
  }
}

package com.ujizin.camposer.fake

import com.ujizin.camposer.internal.core.jvm.JvmFrame
import com.ujizin.camposer.internal.record.JvmVideoRecorder
import com.ujizin.camposer.internal.record.RecordingConfig
import java.nio.ShortBuffer

internal class FakeJvmVideoRecorder(
  val config: RecordingConfig,
  private val failOnStart: Boolean = false,
  private val failOnStop: Boolean = false,
  private val failOnFrame: Boolean = false,
) : JvmVideoRecorder {
  class RecordedFrame(
    val frame: JvmFrame,
    val isMirrored: Boolean,
    val timestampMicros: Long,
  )

  var isStarted = false
    private set

  var isStopped = false
    private set

  val frames = mutableListOf<RecordedFrame>()
  val samples = mutableListOf<ShortArray>()

  override fun start() {
    if (failOnStart) throw IllegalStateException("fake start failure")
    isStarted = true
  }

  override fun recordFrame(
    frame: JvmFrame,
    isMirrored: Boolean,
    timestampMicros: Long,
  ) {
    if (failOnFrame) throw IllegalStateException("fake frame failure")
    frames += RecordedFrame(frame, isMirrored, timestampMicros)
  }

  override fun recordSamples(samples: ShortBuffer) {
    val copy = ShortArray(samples.remaining())
    samples.duplicate().get(copy)
    this.samples += copy
  }

  override fun stop() {
    isStopped = true
    if (failOnStop) throw IllegalStateException("fake stop failure")
  }

  class Factory(
    private val failOnStart: Boolean = false,
    private val failOnStop: Boolean = false,
    private val failOnFrame: Boolean = false,
  ) : JvmVideoRecorder.Factory {
    val created = mutableListOf<FakeJvmVideoRecorder>()

    val last: FakeJvmVideoRecorder
      get() = created.last()

    override fun create(config: RecordingConfig): JvmVideoRecorder =
      FakeJvmVideoRecorder(
        config = config,
        failOnStart = failOnStart,
        failOnStop = failOnStop,
        failOnFrame = failOnFrame,
      ).also { created += it }
  }
}

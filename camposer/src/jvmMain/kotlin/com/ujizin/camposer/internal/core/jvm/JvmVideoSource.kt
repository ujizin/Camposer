package com.ujizin.camposer.internal.core.jvm

/**
 * Properties of a [JvmVideoSource] that the engine reads or writes.
 */
internal enum class JvmVideoProperty {
  FrameWidth,
  FrameHeight,
  FrameRate,
  Exposure,
  AutoExposure,
}

/**
 * Abstraction over the native video capture so the controller can be tested without hardware.
 *
 * Calls are not thread-safe. The owner serializes access.
 */
internal interface JvmVideoSource {
  val isOpen: Boolean

  /** Name of the capture backend (for example `AVFOUNDATION`, `MSMF`, `DSHOW`, `V4L2`). */
  val backendName: String

  fun open(deviceIndex: Int): Boolean

  /** Reads the next frame. Returns null when no frame is available. */
  fun read(): JvmFrame?

  fun get(property: JvmVideoProperty): Double

  fun set(
    property: JvmVideoProperty,
    value: Double,
  ): Boolean

  fun release()

  fun interface Factory {
    fun create(): JvmVideoSource
  }
}

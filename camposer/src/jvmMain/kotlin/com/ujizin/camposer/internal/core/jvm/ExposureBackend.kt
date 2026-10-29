package com.ujizin.camposer.internal.core.jvm

import kotlin.math.pow

private const val V4L2_AUTO = 0.75
private const val V4L2_MANUAL = 0.25
private const val WINDOWS_AUTO = 1.0
private const val WINDOWS_MANUAL = 0.0

/**
 * How each capture backend represents exposure.
 *
 * Exposure compensation is expressed in EV stops relative to the automatic exposure that the
 * backend reported when the device was opened. Backends that store exposure as `log2(seconds)`
 * add the stops directly; linear backends multiply by `2^stops`.
 *
 * @property autoValue value written to the auto-exposure property to enable automatic exposure.
 * @property manualValue value written to the auto-exposure property to enable manual exposure.
 * @property isLogScale whether the exposure property is `log2(seconds)`.
 */
internal enum class ExposureBackend(
  val autoValue: Double,
  val manualValue: Double,
  val isLogScale: Boolean,
) {
  V4L2(autoValue = V4L2_AUTO, manualValue = V4L2_MANUAL, isLogScale = false),
  MSMF(autoValue = WINDOWS_AUTO, manualValue = WINDOWS_MANUAL, isLogScale = true),
  DSHOW(autoValue = WINDOWS_AUTO, manualValue = WINDOWS_MANUAL, isLogScale = true),
  ;

  fun toRawExposure(
    baseExposure: Double,
    compensation: Float,
  ): Double =
    when {
      isLogScale -> baseExposure + compensation
      else -> baseExposure * 2.0.pow(compensation.toDouble())
    }

  internal companion object {
    /** EV stops accepted in each direction. */
    const val COMPENSATION_RANGE = 3F

    fun fromBackendName(backendName: String): ExposureBackend? {
      val name = backendName.uppercase()
      return entries.firstOrNull { backend -> name.contains(backend.name) }
    }
  }
}

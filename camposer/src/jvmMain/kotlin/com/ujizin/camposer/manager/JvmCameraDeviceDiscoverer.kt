package com.ujizin.camposer.manager

import com.ujizin.camposer.internal.core.jvm.JvmVideoProperty
import com.ujizin.camposer.internal.core.jvm.JvmVideoSource
import com.ujizin.camposer.internal.core.jvm.OpenCvVideoSource
import com.ujizin.camposer.state.properties.CameraData
import com.ujizin.camposer.state.properties.selector.CamLensType
import com.ujizin.camposer.state.properties.selector.CamPosition
import com.ujizin.camposer.state.properties.selector.CameraId
import kotlin.math.roundToInt

private const val MAX_PROBED_DEVICES = 10
private const val MAX_CONSECUTIVE_FAILURES = 2
private const val MIN_FRAME_RATE = 1
private const val DEFAULT_FRAME_RATE = 30

internal fun interface JvmCameraDeviceDiscoverer {
  fun discoverDevices(): List<CameraDevice>
}

/**
 * Finds webcams by opening device indexes in order.
 *
 * Desktop backends cannot list devices without opening them, so every probe briefly opens the
 * camera. Probing stops after [MAX_CONSECUTIVE_FAILURES] misses or [MAX_PROBED_DEVICES] indexes.
 */
internal class ProbingCameraDeviceDiscoverer(
  private val videoSourceFactory: JvmVideoSource.Factory = OpenCvVideoSource,
) : JvmCameraDeviceDiscoverer {
  override fun discoverDevices(): List<CameraDevice> {
    val devices = mutableListOf<CameraDevice>()
    var consecutiveFailures = 0
    var index = 0

    while (index < MAX_PROBED_DEVICES && consecutiveFailures < MAX_CONSECUTIVE_FAILURES) {
      val device = probe(index)
      if (device == null) {
        consecutiveFailures++
      } else {
        consecutiveFailures = 0
        devices += device
      }
      index++
    }

    return devices
  }

  private fun probe(index: Int): CameraDevice? {
    val source = videoSourceFactory.create()
    try {
      if (!source.open(index)) return null

      val frameRate = source
        .get(JvmVideoProperty.FrameRate)
        .takeIf { it > 0.0 }
        ?.roundToInt()
        ?: DEFAULT_FRAME_RATE
      val format = CameraData(
        width = source.get(JvmVideoProperty.FrameWidth).roundToInt(),
        height = source.get(JvmVideoProperty.FrameHeight).roundToInt(),
        isFocusSupported = false,
        minFps = MIN_FRAME_RATE,
        maxFps = frameRate,
      )

      return CameraDevice(
        cameraId = CameraId(index),
        name = "Camera $index",
        position = CamPosition.Unknown,
        fov = 0F,
        lensType = listOf(CamLensType.Wide),
        photoData = listOf(format),
        videoData = listOf(format),
      )
    } finally {
      source.release()
    }
  }
}

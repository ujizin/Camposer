package com.ujizin.camposer.state.properties.selector

public actual class CameraId internal constructor(
  public val deviceId: String,
) {
  actual override operator fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || other !is CameraId) return false
    return deviceId == other.deviceId
  }

  actual override fun hashCode(): Int = deviceId.hashCode()

  actual override fun toString(): String = "CameraId(deviceId=$deviceId)"
}

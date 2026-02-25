package com.ujizin.camposer.state.properties.selector

public actual class CameraId internal constructor(
  public val uniqueId: String,
) {
  actual override operator fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || other !is CameraId) return false

    return uniqueId == other.uniqueId
  }

  actual override fun hashCode(): Int = uniqueId.hashCode()

  actual override fun toString(): String = "CameraId(uniqueId=$uniqueId)"
}

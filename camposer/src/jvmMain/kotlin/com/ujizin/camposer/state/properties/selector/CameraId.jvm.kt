package com.ujizin.camposer.state.properties.selector

/**
 * Desktop camera identifier. Wraps the numeric device index used by the capture backend.
 *
 * @property index zero-based device index, `0` is the default webcam.
 */
public actual class CameraId internal constructor(
  public val index: Int,
) {
  actual override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is CameraId) return false

    return index == other.index
  }

  actual override fun hashCode(): Int = index

  actual override fun toString(): String = "CameraId(index=$index)"
}

package com.ujizin.camposer.state.properties.selector

/**
 * Represents a unique identifier for a camera device.
 *
 * This class serves as a platform-agnostic wrapper for the underlying camera ID
 * (e.g. CameraIdentifier on Android and uniqueId on iOS).
 */
public expect class CameraId {
  override fun equals(other: Any?): Boolean

  override fun hashCode(): Int

  override fun toString(): String
}

package com.ujizin.camposer.manager

import com.ujizin.camposer.state.properties.CameraData
import com.ujizin.camposer.state.properties.selector.CamLensType
import com.ujizin.camposer.state.properties.selector.CamPosition
import com.ujizin.camposer.state.properties.selector.CameraId

/**
 * Represents a physical (or logical in Android) camera device on the system.
 *
 * This class encapsulates essential characteristics and capabilities of a specific camera,
 * including its unique identifier, physical position (front/back), lens types (wide, telephoto, etc.),
 * and supported configurations for both photo and video capture.
 *
 * @param cameraId The unique identifier for the camera hardware.
 * @param name The name for the camera hardware
 * @param position The physical position of the camera (e.g., [CamPosition.Front] or [CamPosition.Back]).
 * @param fov The field of view (FOV) of the camera in degrees.
 * @param lensType A list of lens types supported by this camera device.
 * @param photoData A list of supported resolutions and configurations for taking photos.
 * @param videoData A list of supported resolutions and configurations for recording videos.
 */
public class CameraDevice internal constructor(
  public val cameraId: CameraId,
  public val name: String,
  public val position: CamPosition,
  public val fov: Float,
  public val lensType: List<CamLensType>,
  public val photoData: List<CameraData>,
  public val videoData: List<CameraData>,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is CameraDevice) return false

    if (name != other.name) return false
    if (cameraId != other.cameraId) return false
    if (position != other.position) return false
    if (lensType != other.lensType) return false
    if (photoData != other.photoData) return false
    if (fov != other.fov) return false
    return videoData == other.videoData
  }

  override fun hashCode(): Int {
    var result = cameraId.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + position.hashCode()
    result = 31 * result + lensType.hashCode()
    result = 31 * result + photoData.hashCode()
    result = 31 * result + videoData.hashCode()
    result = 31 * result + fov.hashCode()
    return result
  }

  override fun toString(): String =
    "CameraDevice(cameraId=$cameraId, name=$name, position=$position, lensType=$lensType, fov=$fov, videoData=$videoData)"
}

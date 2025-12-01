package com.ujizin.camposer.manager

import com.ujizin.camposer.state.properties.CameraData
import com.ujizin.camposer.state.properties.selector.CamLensType
import com.ujizin.camposer.state.properties.selector.CamPosition
import com.ujizin.camposer.state.properties.selector.CameraId

public class CameraDevice internal constructor(
  public val cameraId: CameraId,
  public val position: CamPosition,
  public val lensType: List<CamLensType>,
  public val photoData: List<CameraData>,
  public val videoData: List<CameraData>,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is CameraDevice) return false

    if (cameraId != other.cameraId) return false
    if (position != other.position) return false
    if (lensType != other.lensType) return false
    if (photoData != other.photoData) return false
    return videoData == other.videoData
  }

  override fun hashCode(): Int {
    var result = cameraId.hashCode()
    result = 31 * result + position.hashCode()
    result = 31 * result + lensType.hashCode()
    result = 31 * result + photoData.hashCode()
    result = 31 * result + videoData.hashCode()
    return result
  }

  override fun toString(): String =
    "CameraDevice(cameraId=$cameraId, position=$position, lensType=$lensType"
}

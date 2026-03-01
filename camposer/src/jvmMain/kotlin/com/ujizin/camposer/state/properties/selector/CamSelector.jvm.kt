package com.ujizin.camposer.state.properties.selector

import androidx.compose.runtime.Stable
import com.ujizin.camposer.manager.CameraDevice

@Stable
public actual class CamSelector {
  public actual val camPosition: CamPosition
  public actual val camLensTypes: List<CamLensType>

  internal var cameraDevice: CameraDevice? = null

  /**
   * JVM device index used to select a physical desktop camera by its numeric index.
   *
   * Desktop cameras have no physical "front" or "back" distinction — both
   * [CamPosition.Back] and [CamPosition.Front] default to device index 0 (the
   * primary webcam). When a [CamSelector] is constructed via [CamSelector(cameraDevice)],
   * the index is derived from [CameraDevice.cameraId.deviceId] parsed as an integer.
   * If parsing fails, it falls back to 0.
   */
  internal val deviceIndex: Int
    get() = cameraDevice?.cameraId?.deviceId?.toIntOrNull() ?: 0

  public actual constructor(camPosition: CamPosition, camLensTypes: List<CamLensType>) {
    this.camPosition = camPosition
    this.camLensTypes = camLensTypes.ifEmpty { listOf(CamLensType.Wide) }
  }

  public actual constructor(cameraDevice: CameraDevice) : this(
    camPosition = cameraDevice.position,
    camLensTypes = cameraDevice.lensType,
  ) {
    this.cameraDevice = cameraDevice
  }

  actual override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || other !is CamSelector) return false
    if (camPosition != other.camPosition) return false
    return camLensTypes == other.camLensTypes
  }

  actual override fun hashCode(): Int {
    var result = camPosition.hashCode()
    result = 31 * result + camLensTypes.hashCode()
    return result
  }

  actual override fun toString(): String =
    "CamSelector(camPosition=$camPosition, camLensType=$camLensTypes)"

  public actual companion object {
    public actual val Front: CamSelector = CamSelector(CamPosition.Front)
    public actual val Back: CamSelector = CamSelector(CamPosition.Back)
  }
}

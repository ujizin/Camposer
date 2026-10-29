package com.ujizin.camposer.state.properties.selector

import androidx.compose.runtime.Stable
import com.ujizin.camposer.manager.CameraDevice

private const val DEFAULT_DEVICE_INDEX = 0

/**
 * Desktop camera selector.
 *
 * Desktop webcams have no physical position. [CamPosition.Front] and [CamPosition.Back] both
 * open the default device (index `0`). To pick a specific webcam, build the selector from a
 * [CameraDevice] returned by [com.ujizin.camposer.manager.rememberCameraDeviceState].
 */
@Stable
public actual class CamSelector {
  public actual val camPosition: CamPosition
  public actual val camLensTypes: List<CamLensType>

  internal var cameraDevice: CameraDevice? = null
    private set

  /** Device index opened by the capture backend. */
  internal val deviceIndex: Int
    get() = cameraDevice?.cameraId?.index ?: DEFAULT_DEVICE_INDEX

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
    if (other !is CamSelector) return false

    if (camPosition != other.camPosition) return false
    if (camLensTypes != other.camLensTypes) return false
    return deviceIndex == other.deviceIndex
  }

  actual override fun hashCode(): Int {
    var result = camPosition.hashCode()
    result = 31 * result + camLensTypes.hashCode()
    result = 31 * result + deviceIndex
    return result
  }

  actual override fun toString(): String =
    "CamSelector(camPosition=$camPosition, camLensType=$camLensTypes, deviceIndex=$deviceIndex)"

  public actual companion object {
    public actual val Front: CamSelector = CamSelector(CamPosition.Front)
    public actual val Back: CamSelector = CamSelector(CamPosition.Back)
  }
}

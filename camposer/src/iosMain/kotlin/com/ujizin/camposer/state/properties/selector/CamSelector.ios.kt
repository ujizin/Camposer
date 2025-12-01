package com.ujizin.camposer.state.properties.selector

import com.ujizin.camposer.manager.CameraDevice
import com.ujizin.camposer.state.properties.selector.CamLensType.Telephoto
import com.ujizin.camposer.state.properties.selector.CamLensType.UltraWide
import com.ujizin.camposer.state.properties.selector.CamLensType.Wide
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceDiscoverySession
import platform.AVFoundation.AVCaptureDeviceType
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInDualCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInDualWideCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInTripleCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.position

public actual class CamSelector {
  public actual val camPosition: CamPosition
  public actual val camLensTypes: List<CamLensType>

  internal var cameraDevice: CameraDevice? = null

  internal val captureDevice: AVCaptureDevice
    get() = AVCaptureDeviceDiscoverySession
      .discoverySessionWithDeviceTypes(
        getDeviceTypes(),
        AVMediaTypeVideo,
        camPosition.value,
      ).devices
      .firstOrNull {
        val device = it as? AVCaptureDevice
        when {
          cameraDevice != null -> cameraDevice?.cameraId?.uniqueId == device?.uniqueID
          else -> device?.position == camPosition.value
        }
      } as? AVCaptureDevice
      ?: error("No camera found to position $camPosition with $camLensTypes")

  public actual constructor(camPosition: CamPosition, camLensTypes: List<CamLensType>) {
    this.camPosition = camPosition
    this.camLensTypes = camLensTypes.ifEmpty { listOf(Wide) }
  }

  public actual constructor(cameraDevice: CameraDevice) : this(
    camPosition = cameraDevice.position,
    camLensTypes = cameraDevice.lensType,
  ) {
    this.cameraDevice = cameraDevice
  }

  internal fun getDeviceTypes(): List<AVCaptureDeviceType> =
    buildList {
      if (camLensTypes.containsAll(listOf(Wide, UltraWide, Telephoto))) {
        add(AVCaptureDeviceTypeBuiltInTripleCamera)
      }

      if (camLensTypes.containsAll(listOf(Wide, UltraWide))) {
        add(AVCaptureDeviceTypeBuiltInDualWideCamera)
      }

      if (camLensTypes.containsAll(listOf(Wide, Telephoto))) {
        add(AVCaptureDeviceTypeBuiltInDualCamera)
      }

      addAll(camLensTypes.map { it.type })

      add(AVCaptureDeviceTypeBuiltInWideAngleCamera)
    }.distinct()

  actual override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is CamSelector) return false

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

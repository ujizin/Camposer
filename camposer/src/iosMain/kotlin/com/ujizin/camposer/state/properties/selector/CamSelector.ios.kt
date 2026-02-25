package com.ujizin.camposer.state.properties.selector

import androidx.compose.runtime.Stable
import com.ujizin.camposer.internal.core.ios.IOSCameraController
import com.ujizin.camposer.manager.CameraDevice
import com.ujizin.camposer.state.properties.selector.CamLensType.Telephoto
import com.ujizin.camposer.state.properties.selector.CamLensType.UltraWide
import com.ujizin.camposer.state.properties.selector.CamLensType.Wide
import platform.AVFoundation.AVCaptureDeviceType
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInDualCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInDualWideCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInTripleCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureDeviceTypeExternal

@Stable
public actual class CamSelector {
  public actual val camPosition: CamPosition
  public actual val camLensTypes: List<CamLensType>

  internal var cameraDevice: CameraDevice? = null

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

      if (camPosition == CamPosition.External) {
        add(AVCaptureDeviceTypeExternal)
      }

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

internal fun IOSCameraController.getCaptureDevice(camSelector: CamSelector) =
  getCaptureDevice(
    camSelector.getDeviceTypes(),
    camSelector.camPosition.value,
    camSelector.cameraDevice?.cameraId?.uniqueId,
  ) ?: error("No camera found to position ${camSelector.camPosition}")

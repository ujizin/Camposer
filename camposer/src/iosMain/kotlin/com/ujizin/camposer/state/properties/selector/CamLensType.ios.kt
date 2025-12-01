package com.ujizin.camposer.state.properties.selector

import platform.AVFoundation.AVCaptureDeviceType
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInDualCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInDualWideCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInTelephotoCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInTripleCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInUltraWideCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera

public actual enum class CamLensType(
  internal val type: AVCaptureDeviceType,
) {
  Wide(AVCaptureDeviceTypeBuiltInWideAngleCamera),
  UltraWide(AVCaptureDeviceTypeBuiltInUltraWideCamera),
  Telephoto(AVCaptureDeviceTypeBuiltInTelephotoCamera),
  ;

  internal companion object {
    fun getPhysicalLensByVirtual(position: AVCaptureDeviceType): List<CamLensType> =
      when (position) {
        AVCaptureDeviceTypeBuiltInWideAngleCamera -> listOf(Wide)
        AVCaptureDeviceTypeBuiltInUltraWideCamera -> listOf(UltraWide)
        AVCaptureDeviceTypeBuiltInTelephotoCamera -> listOf(Telephoto)
        AVCaptureDeviceTypeBuiltInDualWideCamera -> listOf(Wide, UltraWide)
        AVCaptureDeviceTypeBuiltInDualCamera -> listOf(Wide, Telephoto)
        AVCaptureDeviceTypeBuiltInTripleCamera -> listOf(Wide, UltraWide, Telephoto)
        else -> emptyList()
      }
  }
}

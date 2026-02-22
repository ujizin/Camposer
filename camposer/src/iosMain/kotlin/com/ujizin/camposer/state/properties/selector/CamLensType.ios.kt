package com.ujizin.camposer.state.properties.selector

import platform.AVFoundation.AVCaptureDeviceType
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInDualCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInDualWideCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInTelephotoCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInTripleCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInUltraWideCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera

internal val CamLensType.type: AVCaptureDeviceType
  get() = when (this) {
    CamLensType.Wide -> AVCaptureDeviceTypeBuiltInWideAngleCamera
    CamLensType.UltraWide -> AVCaptureDeviceTypeBuiltInUltraWideCamera
    CamLensType.Telephoto -> AVCaptureDeviceTypeBuiltInTelephotoCamera
  }

internal fun getPhysicalLensByVirtual(position: AVCaptureDeviceType): List<CamLensType> =
  when (position) {
    AVCaptureDeviceTypeBuiltInWideAngleCamera -> listOf(CamLensType.Wide)

    AVCaptureDeviceTypeBuiltInUltraWideCamera -> listOf(CamLensType.UltraWide)

    AVCaptureDeviceTypeBuiltInTelephotoCamera -> listOf(CamLensType.Telephoto)

    AVCaptureDeviceTypeBuiltInDualWideCamera -> listOf(CamLensType.Wide, CamLensType.UltraWide)

    AVCaptureDeviceTypeBuiltInDualCamera -> listOf(CamLensType.Wide, CamLensType.Telephoto)

    AVCaptureDeviceTypeBuiltInTripleCamera -> listOf(
      CamLensType.Wide,
      CamLensType.UltraWide,
      CamLensType.Telephoto,
    )

    else -> emptyList()
  }

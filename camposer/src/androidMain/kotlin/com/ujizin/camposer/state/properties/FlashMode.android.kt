package com.ujizin.camposer.state.properties

import androidx.camera.core.ImageCapture

internal val FlashMode.mode: Int
  get() = when (this) {
    FlashMode.On -> ImageCapture.FLASH_MODE_ON
    FlashMode.Auto -> ImageCapture.FLASH_MODE_AUTO
    FlashMode.Off -> ImageCapture.FLASH_MODE_OFF
  }

internal fun Int.toFlashMode(): FlashMode =
  when (this) {
    ImageCapture.FLASH_MODE_ON -> FlashMode.On
    ImageCapture.FLASH_MODE_AUTO -> FlashMode.Auto
    else -> FlashMode.Off
  }

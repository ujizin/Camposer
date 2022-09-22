package br.com.devlucasyuji.camposer

import androidx.camera.core.ImageCapture

/**
 * Flash mode from the camera.
 * */
enum class FlashMode(internal val mode: Int) {
    On(ImageCapture.FLASH_MODE_ON),
    Auto(ImageCapture.FLASH_MODE_AUTO),
    Off(ImageCapture.FLASH_MODE_OFF)
}

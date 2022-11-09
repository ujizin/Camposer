package com.ujizin.camposer.state

import androidx.camera.core.ImageCapture

/**
 * Camera Flash mode.
 *
 * @param mode internal flash mode from cameraX
 * @see ImageCapture.FlashMode
 * */
enum class FlashMode(internal val mode: Int) {
    On(ImageCapture.FLASH_MODE_ON),
    Auto(ImageCapture.FLASH_MODE_AUTO),
    Off(ImageCapture.FLASH_MODE_OFF);

    val inverse get() = when(this) {
        On -> Off
        else -> On
    }

    companion object {
        internal fun find(mode: Int) = values().firstOrNull { it.mode == mode } ?: Off
    }
}

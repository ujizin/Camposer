package br.com.devlucasyuji.camposer

import androidx.camera.core.CameraSelector

fun customBackCamSelector(): CamSelector = customCamSelector {
    CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
        .build()
}

fun customFrontCamSelector(): CamSelector = customCamSelector {
    CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
        .build()
}

val CamSelector.customReverse
    get() = when (lensFacing) {
        CamSelector.LensFacing.Back -> customFrontCamSelector()
        CamSelector.LensFacing.Front -> customBackCamSelector()
    }

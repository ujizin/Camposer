package com.ujizin.camposer.extensions

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import com.ujizin.camposer.helper.CameraHelper

internal fun CameraManager.isImageAnalysisSupported(lensFacing: Int?): Boolean {
    val cameraId = cameraIdList.firstOrNull {
        getCameraCharacteristics(it).get(CameraCharacteristics.LENS_FACING) == lensFacing
    } ?: return false

    val level = getCameraCharacteristics(cameraId)
        .get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL) ?: 0

    return level >= CameraHelper.compatHardwareLevel3
}

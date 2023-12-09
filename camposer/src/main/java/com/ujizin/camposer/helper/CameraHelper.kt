package com.ujizin.camposer.helper

import android.hardware.camera2.CameraCharacteristics
import android.os.Build

internal object CameraHelper {
    
    private const val COMPAT_HARDWARE_LEVEL_3 = 3 

    internal val compatHardwareLevel3: Int = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3
        else -> COMPAT_HARDWARE_LEVEL_3
    }
}

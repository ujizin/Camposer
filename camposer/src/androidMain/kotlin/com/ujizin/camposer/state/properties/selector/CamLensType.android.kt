package com.ujizin.camposer.state.properties.selector

import android.annotation.SuppressLint
import android.hardware.camera2.CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS
import android.hardware.camera2.CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE
import android.util.SizeF
import androidx.camera.camera2.internal.Camera2CameraInfoImpl
import kotlin.math.atan2
import kotlin.math.hypot

public actual enum class CamLensType {
    Wide,
    UltraWide,
    Telephoto;

    internal companion object {

        private const val CAMERA_ULTRA_WIDE_FOV = 94F
        private const val CAMERA_WIDE_FOV = 61F

        @SuppressLint("RestrictedApi")
        internal fun findType(
            info: Camera2CameraInfoImpl,
        ): List<CamLensType> = info.cameraCharacteristicsMap.map { (_, characteristics) ->
            val sensorSize = characteristics.get(SENSOR_INFO_PHYSICAL_SIZE) ?: return@map Wide
            val focalLengths = characteristics.get(LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                ?: return@map Wide

            val fov = getFOV(focalLengths, sensorSize)
            when {
                fov > CAMERA_ULTRA_WIDE_FOV -> UltraWide
                fov > CAMERA_WIDE_FOV -> Wide
                else -> Telephoto
            }
        }

        private fun getFOV(focalLengths: FloatArray, sensorSize: SizeF): Double {
            val focalLength = focalLengths.minOrNull() ?: return 0.0

            if ((sensorSize.width == 0f) || (sensorSize.height == 0f)) {
                return 0.0
            }

            val sensorDiagonal = hypot(sensorSize.width.toDouble(), sensorSize.height.toDouble())
            return Math.toDegrees(2.0 * atan2(sensorDiagonal, 2.0 * focalLength))
        }
    }
}
package com.ujizin.camposer.state.properties.selector

import android.annotation.SuppressLint
import androidx.camera.camera2.internal.Camera2CameraInfoImpl
import androidx.camera.core.CameraSelector

/**
 * Camera Selector.
 *
 * @param selector internal camera selector from CameraX
 * @see CameraSelector
 * */
public actual class CamSelector {

    public actual val camPosition: CamPosition

    public actual val camLensTypes: List<CamLensType>

    public val camLensType: CamLensType by lazy { camLensTypes.firstOrNull() ?: CamLensType.Wide }

    internal val selector: CameraSelector by lazy { createCameraSelector() }

    public actual constructor(camPosition: CamPosition, camLensTypes: List<CamLensType>) {
        this.camPosition = camPosition
        this.camLensTypes = camLensTypes.ifEmpty { listOf(CamLensType.Wide) }
    }

    @SuppressLint("RestrictedApi")
    private fun createCameraSelector(): CameraSelector = CameraSelector.Builder()
        .addCameraFilter { cameraInfos ->
            val camera2Infos = cameraInfos.filterIsInstance<Camera2CameraInfoImpl>()
            val cameras = camera2Infos.filter {
                val isCamLensSupported = CamLensType.findType(it).contains(camLensType)

                camPosition.value == it.lensFacing && isCamLensSupported
            }
            cameras.ifEmpty { cameraInfos }
        }
        .requireLensFacing(camPosition.value)
        .build()

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

    actual override fun toString(): String {
        return "CamSelector(camPosition=$camPosition, camLensType=$camLensTypes)"
    }

    public actual companion object {
        public actual val Front: CamSelector = CamSelector(CamPosition.Front)
        public actual val Back: CamSelector = CamSelector(CamPosition.Back)
    }
}

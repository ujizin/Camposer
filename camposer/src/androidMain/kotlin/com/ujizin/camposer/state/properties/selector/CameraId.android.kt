package com.ujizin.camposer.state.properties.selector

import android.annotation.SuppressLint
import androidx.camera.core.CameraIdentifier
import androidx.camera.core.CameraInfo

@SuppressLint("RestrictedApi")
public actual class CameraId internal constructor(
    internal val identifier: CameraIdentifier?,
    internal val physicalCameraInfos: Set<CameraInfo?>,
    public val ids: List<String> = identifier?.cameraIds.orEmpty(),
) {

    public actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CameraId) return false

        return identifier == other.identifier
    }

    public actual override fun hashCode(): Int {
        return identifier.hashCode()
    }

    public actual override fun toString(): String {
        return "CameraId(ids=${ids})"
    }
}
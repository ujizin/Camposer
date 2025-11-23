package com.ujizin.camposer.state.properties.selector

import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraSelector.LENS_FACING_BACK
import androidx.camera.core.CameraSelector.LENS_FACING_EXTERNAL
import androidx.camera.core.CameraSelector.LENS_FACING_FRONT
import androidx.camera.core.CameraSelector.LENS_FACING_UNKNOWN
import androidx.camera.core.ExperimentalLensFacing


public actual enum class CamPosition(
    @field:CameraSelector.LensFacing
    internal val value: Int,
) {
    Back(LENS_FACING_BACK),
    Front(LENS_FACING_FRONT),

    @ExperimentalLensFacing
    External(LENS_FACING_EXTERNAL),
    Unknown(LENS_FACING_UNKNOWN);

    internal companion object {
        internal fun findByLens(lensFacing: Int): CamPosition = entries.firstOrNull {
            lensFacing == it.value
        } ?: Unknown
    }
}
package com.ujizin.camposer.state.properties.selector

import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraSelector.LENS_FACING_BACK
import androidx.camera.core.CameraSelector.LENS_FACING_FRONT

public actual enum class CamPosition(
    @field:CameraSelector.LensFacing
    internal val value: Int,
) {
    Back(LENS_FACING_BACK),
    Front(LENS_FACING_FRONT),
}
package com.ujizin.camposer.state.properties.selector

import android.annotation.SuppressLint
import android.hardware.camera2.CameraMetadata.LENS_FACING_FRONT
import androidx.camera.core.CameraSelector

/**
 * Camera Selector.
 *
 * @param selector internal camera selector from CameraX
 * @see CameraSelector
 * */
public actual class CamSelector {

    public val selector: CameraSelector

    internal constructor(selector: CameraSelector) {
        this.selector = selector
    }

    public actual val isFront: Boolean
        @SuppressLint("RestrictedApi")
        get() = selector.lensFacing == LENS_FACING_FRONT

    /**
     * Inverse camera selector. Works only with default Front & Back Selector.
     * */
    public val inverse: CamSelector
        @SuppressLint("RestrictedApi")
        get() = when (this.selector.lensFacing) {
            LENS_FACING_FRONT -> Back
            else -> Front
        }

    public actual companion object {
        public actual val Front: CamSelector
            get() = CamSelector(CameraSelector.DEFAULT_FRONT_CAMERA)
        public actual val Back: CamSelector
            get() = CamSelector(CameraSelector.DEFAULT_BACK_CAMERA)
    }
}

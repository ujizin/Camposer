package com.ujizin.camposer.config.properties

import androidx.camera.core.CameraSelector

/**
 * Camera Selector.
 *
 * @param selector internal camera selector from CameraX
 * @see CameraSelector
 * */
public actual enum class CamSelector(
    internal val selector: CameraSelector
) {
    Front(CameraSelector.DEFAULT_FRONT_CAMERA),
    Back(CameraSelector.DEFAULT_BACK_CAMERA);

    /**
     * Inverse camera selector. Works only with default Front & Back Selector.
     * */
    public val inverse: CamSelector
        get() = when (this) {
            Front -> Back
            Back -> Front
        }
}

package br.com.devlucasyuji.camposer.state

import android.annotation.SuppressLint
import androidx.camera.core.CameraSelector
import androidx.compose.runtime.Immutable

/**
 * Selector from Camera.
 *
 * @param selector Camera selector by CameraX
 * */
sealed class CamSelector(
    internal var selector: CameraSelector
) {
    /**
     * Default front camera from CameraX.
     * */
    object Front : CamSelector(CameraSelector.DEFAULT_FRONT_CAMERA)

    /**
     * Default back camera from cameraX.
     * */
    object Back : CamSelector(CameraSelector.DEFAULT_BACK_CAMERA)

    @Immutable
    internal data class CustomSelector(
        private val cameraSelector: CameraSelector
    ) : CamSelector(cameraSelector) {

        override fun toString() = "Custom ${lensFacing.name}"
    }

    enum class LensFacing { Back, Front }

    /**
     * Return lens facing front or back.
     * */
    val lensFacing: LensFacing
        @SuppressLint("RestrictedApi")
        get() = when (selector.lensFacing) {
            CameraSelector.LENS_FACING_BACK -> LensFacing.Back
            else -> LensFacing.Front
        }

    /**
     * Reverse camera selector. Works only with default Front & Back Selector.
     * */
    val reverse: CamSelector
        get() = when (this) {
            Front -> Back
            Back -> Front
            else -> this
        }
}

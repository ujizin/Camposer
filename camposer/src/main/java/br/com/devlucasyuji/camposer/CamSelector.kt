package br.com.devlucasyuji.camposer

import android.annotation.SuppressLint
import androidx.camera.core.CameraSelector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

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

@Composable
fun rememberCameraSelector(
    selector: CamSelector = CamSelector.Back
): MutableState<CamSelector> = remember {
    mutableStateOf(selector)
}

@Composable
fun rememberCameraSelector(
    block: () -> CameraSelector
): MutableState<CamSelector> = remember {
    mutableStateOf(customCamSelector(block))
}

fun customCamSelector(
    block: () -> CameraSelector
): CamSelector = CamSelector.CustomSelector(block())
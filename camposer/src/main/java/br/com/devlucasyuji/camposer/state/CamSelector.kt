package br.com.devlucasyuji.camposer.state

import android.annotation.SuppressLint
import androidx.camera.core.CameraSelector
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver

/**
 * Selector from Camera.
 *
 * @param selector Camera selector by CameraX
 * */
sealed class CamSelector(
    internal val selector: CameraSelector
) {

    /**
     * Default front camera from CameraX.
     * */
    object Front : CamSelector(CameraSelector.DEFAULT_FRONT_CAMERA)

    /**
     * Default back camera from cameraX.
     * */
    object Back : CamSelector(CameraSelector.DEFAULT_BACK_CAMERA)

    enum class LensFacing(val value: Int) {
        Back(CameraSelector.LENS_FACING_BACK),
        Front(CameraSelector.LENS_FACING_FRONT)
    }

    /**
     * Return lens facing front or back.
     * */
    @SuppressLint("RestrictedApi")
    val lensFacing: LensFacing = when (selector.lensFacing) {
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
        }

    companion object {
        internal val Saver: Saver<MutableState<CamSelector>, *> = listSaver(
            save = { listOf(it.value.lensFacing) },
            restore = {
                mutableStateOf(
                    when (it[0]) {
                        LensFacing.Front -> Front
                        LensFacing.Back -> Back
                    }
                )
            }
        )
    }
}

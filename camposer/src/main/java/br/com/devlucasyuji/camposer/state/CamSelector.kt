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
enum class CamSelector(
    internal val selector: CameraSelector
) {
    Front(CameraSelector.DEFAULT_FRONT_CAMERA),
    Back(CameraSelector.DEFAULT_BACK_CAMERA);

    /**
     * Reverse camera selector. Works only with default Front & Back Selector.
     * */
    val reverse: CamSelector
        get() = when (this) {
            Front -> Back
            Back -> Front
        }

    companion object {

        @SuppressLint("RestrictedApi")
        internal val Saver: Saver<MutableState<CamSelector>, *> = listSaver(
            save = { listOf(it.value.selector.lensFacing) },
            restore = {
                mutableStateOf(
                    when (it[0]) {
                        CameraSelector.LENS_FACING_FRONT -> Front
                        else -> Back
                    }
                )
            }
        )
    }
}

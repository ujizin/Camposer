package br.com.devlucasyuji.camposer

import androidx.camera.core.CameraSelector as CameraXSelector

/**
 * Side from Camera.
 *
 * @param selector Camera selector by CameraX
 * */
enum class CameraSelector(
    internal var selector: CameraXSelector
) {
    Front(CameraXSelector.DEFAULT_FRONT_CAMERA),
    Back(CameraXSelector.DEFAULT_BACK_CAMERA);

    val reverse: CameraSelector
        get() = when (this) {
            Front -> Back
            Back -> Front
        }
}

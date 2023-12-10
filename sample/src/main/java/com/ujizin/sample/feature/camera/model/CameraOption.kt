package com.ujizin.sample.feature.camera.model

import android.os.Build
import androidx.annotation.StringRes
import com.ujizin.camposer.state.CaptureMode
import com.ujizin.sample.R

enum class CameraOption(@StringRes val titleRes: Int) {
    Photo(R.string.photo),
    Video(R.string.video),
    QRCode(R.string.qr_code);

    fun toCaptureMode(): CaptureMode = when(this) {
        QRCode, Photo -> CaptureMode.Image
        Video -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CaptureMode.Video
        } else {
            throw IllegalStateException("Camera state not support video capture mode")
        }
    }
}

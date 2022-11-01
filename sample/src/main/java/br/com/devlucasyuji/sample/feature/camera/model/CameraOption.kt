package br.com.devlucasyuji.sample.feature.camera.model

import androidx.annotation.StringRes
import br.com.devlucasyuji.camposer.state.CaptureMode
import br.com.devlucasyuji.sample.R

enum class CameraOption(@StringRes val titleRes: Int) {
    Photo(R.string.photo),
    Video(R.string.video),
    QRCode(R.string.qr_code);

    fun toCaptureMode(): CaptureMode = when(this) {
        QRCode, Photo -> CaptureMode.Image
        Video -> CaptureMode.Video
    }
}

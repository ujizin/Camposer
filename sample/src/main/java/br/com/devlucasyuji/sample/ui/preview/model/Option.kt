package br.com.devlucasyuji.sample.ui.preview.model

import androidx.annotation.StringRes
import br.com.devlucasyuji.camposer.state.CaptureMode
import br.com.devlucasyuji.sample.R

enum class Option(@StringRes val titleRes: Int) {
    Photo(R.string.photo), Video(R.string.video);

    fun toCaptureMode(): CaptureMode = when(this) {
        Photo -> CaptureMode.Image
        Video -> CaptureMode.Video
    }
}

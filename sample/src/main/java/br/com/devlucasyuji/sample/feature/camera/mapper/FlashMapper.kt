package br.com.devlucasyuji.sample.feature.camera.mapper

import br.com.devlucasyuji.camposer.state.FlashMode
import br.com.devlucasyuji.sample.feature.camera.model.Flash

fun Flash.toFlashMode() = when (this) {
    Flash.Auto -> FlashMode.Auto
    Flash.On -> FlashMode.On
    Flash.Off, Flash.Always -> FlashMode.Off
}

fun FlashMode.toFlash(isTorchEnabled: Boolean) = when (this) {
    FlashMode.On -> Flash.On
    FlashMode.Auto -> Flash.Auto
    FlashMode.Off -> Flash.Off
}.takeIf { !isTorchEnabled } ?: Flash.Always

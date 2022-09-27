package br.com.devlucasyuji.camposer.state

import androidx.camera.view.PreviewView

enum class ImplementationMode(internal val value: PreviewView.ImplementationMode) {
    Compatible(PreviewView.ImplementationMode.COMPATIBLE),
    Performance(PreviewView.ImplementationMode.PERFORMANCE);

    val inverse: ImplementationMode
        get() = when (this) {
            Compatible -> Performance
            else -> Compatible
        }
}
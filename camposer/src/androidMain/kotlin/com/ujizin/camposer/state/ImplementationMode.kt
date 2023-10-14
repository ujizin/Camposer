package com.ujizin.camposer.state

import androidx.camera.view.PreviewView

/**
 * Camera implementation mode.
 *
 * @param value internal implementation mode from cameraX
 * @see PreviewView.ImplementationMode
 * */
public actual enum class ImplementationMode(internal val value: PreviewView.ImplementationMode) {
    Compatible(PreviewView.ImplementationMode.COMPATIBLE),
    Performance(PreviewView.ImplementationMode.PERFORMANCE),
}
package com.ujizin.camposer.state

import androidx.camera.view.PreviewView

/**
 * Camera implementation mode.
 *
 * @param value internal implementation mode from cameraX
 * @see PreviewView.ImplementationMode
 * */
public enum class ImplementationMode(internal val value: PreviewView.ImplementationMode) {
    Compatible(PreviewView.ImplementationMode.COMPATIBLE),
    Performance(PreviewView.ImplementationMode.PERFORMANCE);

    /**
     * Inverse currently implementation mode.
     * */
    public val inverse: ImplementationMode
        get() = when (this) {
            Compatible -> Performance
            else -> Compatible
        }
}
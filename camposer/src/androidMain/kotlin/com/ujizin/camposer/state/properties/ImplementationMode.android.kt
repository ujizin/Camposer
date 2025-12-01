package com.ujizin.camposer.state.properties

import androidx.camera.view.PreviewView

/**
 * Camera implementation mode.
 *
 * @see PreviewView.ImplementationMode
 * */
public actual enum class ImplementationMode(
  internal val value: PreviewView.ImplementationMode,
) {
  Compatible(PreviewView.ImplementationMode.COMPATIBLE),
  Performance(PreviewView.ImplementationMode.PERFORMANCE),
  ;

  /**
   * Inverse currently implementation mode.
   * */
  public val inverse: ImplementationMode
    get() = when (this) {
      Compatible -> Performance
      else -> Compatible
    }
}

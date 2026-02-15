package com.ujizin.camposer.state.properties

import androidx.camera.view.PreviewView

internal val ImplementationMode.value: PreviewView.ImplementationMode
  get() = when (this) {
    ImplementationMode.Compatible -> PreviewView.ImplementationMode.COMPATIBLE
    ImplementationMode.Performance -> PreviewView.ImplementationMode.PERFORMANCE
  }

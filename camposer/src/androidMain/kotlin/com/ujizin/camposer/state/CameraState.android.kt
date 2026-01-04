package com.ujizin.camposer.state

import com.ujizin.camposer.session.CameraSession

internal actual fun CameraSession.isToResetConfig(
  isCamSelectorChanged: Boolean,
  isCaptureModeChanged: Boolean,
): Boolean = isCamSelectorChanged

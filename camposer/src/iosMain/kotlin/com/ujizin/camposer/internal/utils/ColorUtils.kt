package com.ujizin.camposer.internal.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import platform.UIKit.UIColor

internal fun Color.toUIColor(): UIColor {
  if (isUnspecified || this == Color.Transparent) {
    return UIColor.clearColor()
  }

  return UIColor(
    red = red.toDouble(),
    green = green.toDouble(),
    blue = blue.toDouble(),
    alpha = alpha.toDouble()
  )
}
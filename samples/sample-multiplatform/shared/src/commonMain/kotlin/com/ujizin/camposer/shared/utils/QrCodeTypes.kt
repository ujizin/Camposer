package com.ujizin.camposer.shared.utils

/** Platform-agnostic bounding box for a detected QR code (coordinates in DP). */
data class QrRect(val left: Int, val top: Int, val right: Int, val bottom: Int) {
  val width: Int get() = right - left
  val height: Int get() = bottom - top
}

/** Platform-agnostic corner point for a detected QR code (coordinates in DP). */
data class QrCorner(val x: Int, val y: Int)

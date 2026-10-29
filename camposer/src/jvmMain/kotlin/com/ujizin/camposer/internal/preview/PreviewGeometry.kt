package com.ujizin.camposer.internal.preview

import androidx.compose.ui.geometry.Offset
import com.ujizin.camposer.state.properties.ScaleType

/**
 * Where the camera frame is drawn inside the preview canvas.
 *
 * All offsets are in canvas pixels. [toPreviewPosition] converts a frame pixel to the preview
 * coordinate space in dp, which is the unit the analyzers report.
 */
internal class PreviewGeometry(
  val imageWidth: Int,
  val imageHeight: Int,
  val canvasWidth: Float,
  val canvasHeight: Float,
  val scale: Float,
  val offsetX: Float,
  val offsetY: Float,
  val isMirrored: Boolean,
  val density: Float,
) {
  val drawWidth: Float
    get() = imageWidth * scale

  val drawHeight: Float
    get() = imageHeight * scale

  fun toPreviewPosition(
    x: Float,
    y: Float,
  ): Offset {
    val sourceX = if (isMirrored) imageWidth - x else x
    return Offset(
      x = (offsetX + sourceX * scale) / density,
      y = (offsetY + y * scale) / density,
    )
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is PreviewGeometry) return false

    return imageWidth == other.imageWidth &&
      imageHeight == other.imageHeight &&
      canvasWidth == other.canvasWidth &&
      canvasHeight == other.canvasHeight &&
      scale == other.scale &&
      offsetX == other.offsetX &&
      offsetY == other.offsetY &&
      isMirrored == other.isMirrored &&
      density == other.density
  }

  override fun hashCode(): Int {
    var result = imageWidth
    result = 31 * result + imageHeight
    result = 31 * result + canvasWidth.hashCode()
    result = 31 * result + canvasHeight.hashCode()
    result = 31 * result + scale.hashCode()
    result = 31 * result + offsetX.hashCode()
    result = 31 * result + offsetY.hashCode()
    result = 31 * result + isMirrored.hashCode()
    result = 31 * result + density.hashCode()
    return result
  }

  override fun toString(): String =
    "PreviewGeometry(image=${imageWidth}x$imageHeight, canvas=${canvasWidth}x$canvasHeight, " +
      "scale=$scale, offset=($offsetX, $offsetY), isMirrored=$isMirrored, density=$density)"

  internal companion object {
    @Suppress("LongParameterList")
    fun compute(
      imageWidth: Int,
      imageHeight: Int,
      canvasWidth: Float,
      canvasHeight: Float,
      scaleType: ScaleType,
      isMirrored: Boolean,
      density: Float,
    ): PreviewGeometry? {
      val hasImage = imageWidth > 0 && imageHeight > 0
      val hasCanvas = canvasWidth > 0F && canvasHeight > 0F
      if (!hasImage || !hasCanvas) return null

      val widthRatio = canvasWidth / imageWidth
      val heightRatio = canvasHeight / imageHeight
      val scale = when (scaleType) {
        ScaleType.FillStart,
        ScaleType.FillCenter,
        ScaleType.FillEnd,
        -> maxOf(widthRatio, heightRatio)

        ScaleType.FitStart,
        ScaleType.FitCenter,
        ScaleType.FitEnd,
        -> minOf(widthRatio, heightRatio)
      }

      val drawWidth = imageWidth * scale
      val drawHeight = imageHeight * scale
      val (offsetX, offsetY) = when (scaleType) {
        ScaleType.FitStart,
        ScaleType.FillStart,
        -> 0F to 0F

        ScaleType.FitCenter,
        ScaleType.FillCenter,
        -> (canvasWidth - drawWidth) / 2F to (canvasHeight - drawHeight) / 2F

        ScaleType.FitEnd,
        ScaleType.FillEnd,
        -> (canvasWidth - drawWidth) to (canvasHeight - drawHeight)
      }

      return PreviewGeometry(
        imageWidth = imageWidth,
        imageHeight = imageHeight,
        canvasWidth = canvasWidth,
        canvasHeight = canvasHeight,
        scale = scale,
        offsetX = offsetX,
        offsetY = offsetY,
        isMirrored = isMirrored,
        density = density,
      )
    }
  }
}

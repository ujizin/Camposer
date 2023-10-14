package com.ujizin.camposer.focus

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp

private class CornerBorder(
    drawScope: DrawScope,
    val x: Float,
    val y: Float,
    val thicknessDp: Dp,
    val brush: Brush,
    val borderSize: Dp
) {

    val thickness: Float
    val borderAdjust: Float
    val cornerSize: Float

    val cornerStartX: Float
    val cornerEndX: Float

    val cornerStartY: Float
    val cornerEndY: Float

    init {
        with(drawScope) {
            thickness = thicknessDp.value * density
            borderAdjust = thickness / ADJUST_THICKNESS_HALF_SIZE

            cornerSize = when {
                borderSize != Dp.Unspecified -> borderSize.value * density
                else -> x / DEFAULT_SIZE
            }

            cornerStartX = x - cornerSize
            cornerStartY = y - cornerSize
            cornerEndX = x - borderAdjust
            cornerEndY = y - borderAdjust
            drawBottomStartCornerLine()
            drawBottomEndCornerLine()
            drawTopStartCornerLine()
            drawTopEndCornerLine()
        }
    }

    private fun DrawScope.drawTopEndCornerLine() {
        drawLine(brush, Offset(cornerStartX, 0f), Offset(x, 0f), thickness)
        drawLine(brush, Offset(cornerEndX, 0f), Offset(cornerEndX, cornerSize), thickness)
    }

    private fun DrawScope.drawTopStartCornerLine() {
        drawLine(brush, Offset(0f, 0f), Offset(cornerSize, 0f), thickness)
        drawLine(brush, Offset(borderAdjust, 0f), Offset(borderAdjust, cornerSize), thickness)
    }

    private fun DrawScope.drawBottomEndCornerLine() {
        drawLine(brush, Offset(cornerStartX, y), Offset(x, y), thickness)
        drawLine(brush, Offset(cornerEndX, y), Offset(cornerEndX, cornerStartY), thickness)
    }

    private fun DrawScope.drawBottomStartCornerLine() {
        drawLine(brush, Offset(0f, y), Offset(cornerSize, y), thickness)
        drawLine(brush, Offset(borderAdjust, y), Offset(borderAdjust, cornerStartY), thickness)
    }

    companion object {
        private const val DEFAULT_SIZE = 4
        private const val ADJUST_THICKNESS_HALF_SIZE = 2
    }
}

internal fun DrawScope.drawCornerBorder(brush: Brush, x: Float, y: Float, thickness: Dp, borderSize: Dp) {
    CornerBorder(this, x, y, thickness, brush, borderSize)
}
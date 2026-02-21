package com.ujizin.camposer.shared.features.camera.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ujizin.camposer.codescanner.CornerPointer
import com.ujizin.camposer.codescanner.FrameRect
import kotlin.math.hypot
import kotlin.math.min

private val linkRegex = Regex("""(?:https?://|www\.)\S+""", RegexOption.IGNORE_CASE)

@Composable
fun QrCodeLinkPopup(
  modifier: Modifier = Modifier,
  text: String?,
) {
  if (text.isNullOrBlank()) return

  val uriHandler = LocalUriHandler.current
  val link = remember(text) { text.extractLinkOrNull() }
  val isClickable = link != null

  Column(
    modifier = modifier
      .widthIn(max = 520.dp)
      .padding(horizontal = 16.dp)
      .border(
        width = 1.dp,
        color = Color(0xFFC86BFF),
        shape = RoundedCornerShape(14.dp),
      )
      .background(
        color = Color(0xCC27163F),
        shape = RoundedCornerShape(14.dp),
      ).clickable(enabled = isClickable) {
        if (link != null) {
          runCatching { uriHandler.openUri(link) }
        }
      }.padding(horizontal = 14.dp, vertical = 10.dp),
  ) {
    Text(
      text = if (isClickable) "QR link detected" else "QR code detected",
      style = MaterialTheme.typography.labelLarge,
      color = Color(0xFFE8C6FF),
      fontWeight = FontWeight.SemiBold,
    )
    Text(
      text = text,
      style = MaterialTheme.typography.bodyMedium,
      color = Color.White,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
fun QrCodeOverlay(
  modifier: Modifier = Modifier,
  frameRect: FrameRect?,
  corners: List<CornerPointer>,
) {
  if (frameRect == null && corners.isEmpty()) return

  Canvas(modifier = modifier) {
    val points = toOffsets(corners) ?: frameRect?.let(::toOffsets) ?: return@Canvas
    if (points.size < 4) return@Canvas

    val ordered = points.orderCorners()
    val edges = listOf(
      ordered.topLeft.distanceTo(ordered.topRight),
      ordered.topRight.distanceTo(ordered.bottomRight),
      ordered.bottomRight.distanceTo(ordered.bottomLeft),
      ordered.bottomLeft.distanceTo(ordered.topLeft),
    )
    val shortestEdge = edges.minOrNull() ?: return@Canvas
    val cornerLength = (shortestEdge * 0.3f).coerceIn(18.dp.toPx(), 58.dp.toPx())
    val strokeWidth = 3.2.dp.toPx()

    val borderColor = Color(0xFFC86BFF)
    val shadowColor = Color(0x80220A3E)
    val fillColor = Color(0x3327164D)

    val shapePath = Path().apply {
      moveTo(ordered.topLeft.x, ordered.topLeft.y)
      lineTo(ordered.topRight.x, ordered.topRight.y)
      lineTo(ordered.bottomRight.x, ordered.bottomRight.y)
      lineTo(ordered.bottomLeft.x, ordered.bottomLeft.y)
      close()
    }
    drawPath(path = shapePath, color = fillColor)
    drawPath(
      path = shapePath,
      color = borderColor.copy(alpha = 0.5f),
      style = Stroke(width = 1.2.dp.toPx()),
    )

    draw3dCorner(
      point = ordered.topLeft,
      firstDirection = ordered.topRight - ordered.topLeft,
      secondDirection = ordered.bottomLeft - ordered.topLeft,
      length = cornerLength,
      strokeWidth = strokeWidth,
      borderColor = borderColor,
      shadowColor = shadowColor,
    )
    draw3dCorner(
      point = ordered.topRight,
      firstDirection = ordered.topLeft - ordered.topRight,
      secondDirection = ordered.bottomRight - ordered.topRight,
      length = cornerLength,
      strokeWidth = strokeWidth,
      borderColor = borderColor,
      shadowColor = shadowColor,
    )
    draw3dCorner(
      point = ordered.bottomRight,
      firstDirection = ordered.topRight - ordered.bottomRight,
      secondDirection = ordered.bottomLeft - ordered.bottomRight,
      length = cornerLength,
      strokeWidth = strokeWidth,
      borderColor = borderColor,
      shadowColor = shadowColor,
    )
    draw3dCorner(
      point = ordered.bottomLeft,
      firstDirection = ordered.topLeft - ordered.bottomLeft,
      secondDirection = ordered.bottomRight - ordered.bottomLeft,
      length = cornerLength,
      strokeWidth = strokeWidth,
      borderColor = borderColor,
      shadowColor = shadowColor,
    )
  }
}

private data class OrderedCorners(
  val topLeft: Offset,
  val topRight: Offset,
  val bottomRight: Offset,
  val bottomLeft: Offset,
)

private fun String.extractLinkOrNull(): String? {
  val value = linkRegex.find(this)?.value ?: return null
  val normalized = value.trimEnd('.', ',', ';', ')', ']', '}')
  return if (normalized.startsWith("http://", ignoreCase = true) ||
    normalized.startsWith("https://", ignoreCase = true)
  ) {
    normalized
  } else {
    "https://$normalized"
  }
}

private fun List<Offset>.orderCorners(): OrderedCorners {
  val topLeft = minBy { it.x + it.y }
  val bottomRight = maxBy { it.x + it.y }
  val topRight = maxBy { it.x - it.y }
  val bottomLeft = minBy { it.x - it.y }
  return OrderedCorners(
    topLeft = topLeft,
    topRight = topRight,
    bottomRight = bottomRight,
    bottomLeft = bottomLeft,
  )
}

private fun Offset.distanceTo(other: Offset): Float = hypot(x - other.x, y - other.y)

private fun DrawScope.draw3dCorner(
  point: Offset,
  firstDirection: Offset,
  secondDirection: Offset,
  length: Float,
  strokeWidth: Float,
  borderColor: Color,
  shadowColor: Color,
) {
  val clampedLength = min(length, size.minDimension / 3f)
  val first = firstDirection.normalizedOrZero()
  val second = secondDirection.normalizedOrZero()
  val firstEnd = point + first * clampedLength
  val secondEnd = point + second * clampedLength
  val shadowOffset = (first + second).normalizedOrZero() * (strokeWidth * 0.8f)

  drawLine(
    color = shadowColor,
    start = point + shadowOffset,
    end = firstEnd + shadowOffset,
    strokeWidth = strokeWidth,
    cap = StrokeCap.Round,
  )
  drawLine(
    color = shadowColor,
    start = point + shadowOffset,
    end = secondEnd + shadowOffset,
    strokeWidth = strokeWidth,
    cap = StrokeCap.Round,
  )

  drawLine(
    color = borderColor,
    start = point,
    end = firstEnd,
    strokeWidth = strokeWidth,
    cap = StrokeCap.Round,
  )
  drawLine(
    color = borderColor,
    start = point,
    end = secondEnd,
    strokeWidth = strokeWidth,
    cap = StrokeCap.Round,
  )
}

private fun Offset.normalizedOrZero(): Offset {
  val distance = getDistance()
  return if (distance == 0f) Offset.Zero else Offset(x / distance, y / distance)
}

private fun DrawScope.toOffsets(corners: List<CornerPointer>): List<Offset>? {
  if (corners.size < 4) return null
  return corners.take(4).map { corner ->
    Offset(
      x = corner.x.dp.toPx(),
      y = corner.y.dp.toPx(),
    )
  }
}

private fun DrawScope.toOffsets(frameRect: FrameRect): List<Offset> =
  listOf(
    Offset(frameRect.left.dp.toPx(), frameRect.top.dp.toPx()),
    Offset(frameRect.right.dp.toPx(), frameRect.top.dp.toPx()),
    Offset(frameRect.right.dp.toPx(), frameRect.bottom.dp.toPx()),
    Offset(frameRect.left.dp.toPx(), frameRect.bottom.dp.toPx()),
  )

package com.ujizin.camposer.focus

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
internal fun FocusTap(
    modifier: Modifier = Modifier,
    offset: Offset,
    onFocus: suspend () -> Unit = {},
    focusContent: @Composable () -> Unit = {}
) {
    val isFocused by remember(offset) { derivedStateOf { offset != Offset.Zero } }
    if (isFocused) {
        val focusMovable = remember(offset) {
            movableContentOf {
                Box(
                    Modifier
                        .then(modifier)
                        .layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints)
                            layout(placeable.width, placeable.height) {
                                val relativeX = offset.x.roundToInt() - placeable.width / 2
                                val relativeY = offset.y.roundToInt() - placeable.height / 2
                                placeable.placeRelative(relativeX, relativeY)
                            }
                        }
                ) {
                    focusContent()
                }
            }
        }
        focusMovable()
        LaunchedEffect(offset) { onFocus() }
    }
}

/**
 * Square corner shape composable, it only has shapes on corner border.
 * */
@Composable
public fun SquareCornerFocus(
    modifier: Modifier = Modifier,
    tapSize: Dp = DefaultFocusSize,
    borderSize: Dp = Dp.Unspecified,
    borderStroke: BorderStroke = DefaultBorderStroke,
) {
    val scaleAnim by scaleAsState()
    Box(
        Modifier
            .size(tapSize)
            .scale(scaleAnim)
            .drawBehind {
                drawCornerBorder(
                    brush = borderStroke.brush,
                    x = size.width,
                    y = size.height,
                    thickness = borderStroke.width,
                    borderSize = borderSize
                )
            }
            .then(modifier),
    )
}


/**
 * Square focus shape composable.
 * */
@Composable
public fun SquareFocus(
    modifier: Modifier = Modifier,
    tapSize: Dp = DefaultFocusSize,
    borderStroke: BorderStroke = DefaultBorderStroke,
) {
    val scaleAnim by scaleAsState()
    Box(
        Modifier
            .size(tapSize)
            .scale(scaleAnim)
            .border(borderStroke)
            .then(modifier),
    )
}

/**
 * Circle focus shape composable.
 * */
@Composable
public fun CircleFocus(
    modifier: Modifier = Modifier,
    tapSize: Dp = DefaultFocusSize,
    borderStroke: BorderStroke = DefaultBorderStroke,
) {
    val scaleAnim by scaleAsState()
    Box(
        Modifier
            .size(tapSize)
            .scale(scaleAnim)
            .border(borderStroke, CircleShape)
            .then(modifier),
    )
}

@Composable
internal fun scaleAsState(
    initialValue: Float = 1.5F,
    targetValue: Float = 1F,
    animationSpec: AnimationSpec<Float>? = null,
): State<Float> {
    var scale by remember { mutableStateOf(initialValue) }
    LaunchedEffect(scale) { scale = targetValue }
    return animateFloatAsState(
        targetValue = scale,
        animationSpec = animationSpec ?: tween(easing = LinearOutSlowInEasing)
    )
}

private val DefaultFocusSize = 64.dp
private val DefaultBorderStroke = BorderStroke(2.dp, Color.White)
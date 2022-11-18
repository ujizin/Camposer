package com.ujizin.sample.feature.camera.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

@Composable
fun Button(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPaddingValues: PaddingValues = PaddingValues(0.dp),
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.9F else 1F)

    Box(
        modifier = Modifier
            .scale(scale)
            .then(modifier)
            .clickable(
                enabled = enabled,
                indication = rememberRipple(bounded = true),
                interactionSource = interactionSource,
                onClick = onClick,
            )
            .padding(contentPaddingValues),
        contentAlignment = Alignment.Center,
        content = content
    )
}
package com.ujizin.sample.feature.camera.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ujizin.sample.feature.camera.model.Flash

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FlashBox(
    modifier: Modifier = Modifier,
    hasFlashUnit: Boolean,
    isVideo: Boolean,
    flashMode: Flash,
    onFlashModeChanged: (Flash) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isVisible by remember(hasFlashUnit) { derivedStateOf { hasFlashUnit && expanded }}
    LazyColumn(modifier) {
        itemsIndexed(Flash.getCurrentValues(isVideo), key = { _, it -> it.name }) { index, flash ->
            AnimatedVisibility(
                visible = isVisible,
                enter = if (index == 0) EnterTransition.None else fadeIn() + slideInVertically(),
                exit = fadeOut() + if (index == 0) ExitTransition.None else slideOutVertically()
            ) {
                FlashButton(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .animateItemPlacement(),
                    tintColor = if (flashMode == flash) Color.Yellow else Color.White,
                    flash = flash
                ) {
                    expanded = false
                    onFlashModeChanged(flash)
                }
            }
        }
    }

    if (!isVisible) {
        FlashButton(enabled = hasFlashUnit, flash = flashMode) { expanded = true }
    }

    LaunchedEffect(isVideo) {
        val hasOption = Flash.getCurrentValues(isVideo).any { it == flashMode }
        if (!hasOption) onFlashModeChanged(Flash.Off)
    }
}

@Composable
private fun FlashButton(
    modifier: Modifier = Modifier,
    flash: Flash,
    tintColor: Color = Color.White,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier.then(Modifier.clip(CircleShape)),
        enabled = enabled,
        contentPaddingValues = PaddingValues(16.dp),
        onClick = onClick,
    ) {
        Image(
            modifier = Modifier.size(32.dp),
            painter = painterResource(flash.drawableRes),
            colorFilter = ColorFilter.tint(if (enabled) tintColor else Color.Gray),
            contentDescription = stringResource(flash.contentRes)
        )
    }
}



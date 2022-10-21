package br.com.devlucasyuji.sample.ui.preview.components;

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.devlucasyuji.camposer.state.FlashMode
import br.com.devlucasyuji.sample.ui.preview.mapper.toFlash
import br.com.devlucasyuji.sample.ui.preview.mapper.toFlashMode
import br.com.devlucasyuji.sample.ui.preview.model.Flash

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FlashBox(
    modifier: Modifier = Modifier,
    flashMode: FlashMode,
    onFlashModeChanged: (FlashMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    LazyColumn(modifier) {
        itemsIndexed(Flash.values(), key = { _, it -> it.name }) { index, flash ->
            AnimatedVisibility(
                visible = expanded,
                enter = if (index == 0) EnterTransition.None else fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                FlashButton(
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .animateItemPlacement(),
                    flash = flash
                ) {
                    expanded = false
                    onFlashModeChanged(flash.toFlashMode())
                }
            }
        }
    }

    if (!expanded) FlashButton(flash = flashMode.toFlash()) { expanded = true }
}

@Composable
private fun FlashButton(
    modifier: Modifier = Modifier,
    flash: Flash,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        onClick = onClick,
    ) {
        Image(
            modifier = Modifier.size(32.dp),
            painter = painterResource(flash.drawableRes),
            contentDescription = stringResource(flash.contentRes)
        )
    }
}



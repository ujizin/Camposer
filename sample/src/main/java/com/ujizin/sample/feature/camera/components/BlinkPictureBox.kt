package com.ujizin.sample.feature.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun BlinkPictureBox(lastPicture: File?, isVideo: Boolean) {
    var picture by remember(Unit) { mutableStateOf(lastPicture) }
    if (!isVideo && lastPicture != picture) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        )

        LaunchedEffect(lastPicture) {
            delay(25)
            picture = lastPicture
        }
    }
}
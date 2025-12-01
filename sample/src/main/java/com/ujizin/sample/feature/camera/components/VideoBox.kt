package com.ujizin.sample.feature.camera.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ujizin.sample.extensions.minutes
import com.ujizin.sample.extensions.seconds
import kotlinx.coroutines.delay

@Composable
fun VideoBox(
  modifier: Modifier = Modifier,
  isRecording: Boolean,
) {
  var timer by remember { mutableStateOf(0) }
  var lastTimer by remember { mutableStateOf(0) }
  AnimatedVisibility(
    modifier = modifier,
    visible = isRecording,
    enter = fadeIn() + slideInVertically(),
    exit = fadeOut() + slideOutVertically(),
  ) {
    val currentTimer = if (isRecording) timer else lastTimer
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .offset(x = (-4).dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        Modifier
          .background(Color.Red, CircleShape)
          .size(8.dp),
      )
      Text(
        text = "${currentTimer.minutes}:${currentTimer.seconds}",
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
      )
    }
  }
  LaunchedEffect(isRecording) {
    while (isRecording) {
      delay(1_000)
      timer++
    }
    lastTimer = timer
    timer = 0
  }
}

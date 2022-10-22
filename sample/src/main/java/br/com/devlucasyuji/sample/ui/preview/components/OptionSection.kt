package br.com.devlucasyuji.sample.ui.preview.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.devlucasyuji.camposer.state.CaptureMode
import br.com.devlucasyuji.sample.ui.preview.model.Option

@Composable
fun OptionSection(
    modifier: Modifier = Modifier,
    captureMode: CaptureMode,
    onOptionChanged: (CaptureMode) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
    ) {
        Option.values().forEach { option ->
            val capMode = option.toCaptureMode()
            Text(
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onOptionChanged(capMode) },
                text = stringResource(id = option.titleRes).replaceFirstChar { it.uppercase() },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (captureMode == capMode) Color.Yellow else Color.White
            )
        }
    }
}

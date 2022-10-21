package br.com.devlucasyuji.sample.ui.preview.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.devlucasyuji.sample.ui.preview.model.Option

@Composable
fun OptionSection(
    modifier: Modifier = Modifier,
    currentOption: Option = Option.Photo,
    onOptionChanged: (Option) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
    ) {
        Option.values().forEach { option ->
            Text(
                modifier = Modifier.clickable { onOptionChanged(option) },
                text = option.displayName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (currentOption == option) Color.Yellow else Color.White
            )
        }
    }
}

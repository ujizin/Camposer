package br.com.devlucasyuji.sample.feature.configuration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.devlucasyuji.sample.R
import br.com.devlucasyuji.sample.components.Section

@Composable
fun ConfigurationScreen(
    viewModel: ConfigurationViewModel = viewModel(),
    onBackPressed: () -> Unit
) {
    Section(
        title = {
            Text(stringResource(id = R.string.configuration).replaceFirstChar { it.uppercase() })
        },
        onBackPressed = onBackPressed,
    ) {
//        val uiState = viewModel
        ConfigurationSection()
    }
}

@Composable
private fun ConfigurationSection() {
    var checked by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ConfigurationOption(
            text = stringResource(id = R.string.configuration_cam_selector),
            checked = checked,
            onCheckedChange = { checked = it }
        )
        ConfigurationOption(
            text = stringResource(id = R.string.configuration_pinch_to_zoom),
            checked = checked,
            onCheckedChange = { checked = it }
        )
        ConfigurationOption(
            text = stringResource(id = R.string.configuration_tap_to_focus),
            checked = checked,
            onCheckedChange = { checked = it }
        )
    }
}

@Composable
private fun ConfigurationOption(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = text.replaceFirstChar { it.uppercase() })
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colors.primary
            )
        )
    }
}
package com.ujizin.sample.feature.configuration

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ujizin.sample.R
import com.ujizin.sample.components.Section
import com.ujizin.sample.domain.User
import org.koin.androidx.compose.get

@Composable
fun ConfigurationScreen(
    viewModel: ConfigurationViewModel = get(),
    onBackPressed: () -> Unit,
) {
    Section(
        title = {
            Text(stringResource(id = R.string.configuration).replaceFirstChar { it.uppercase() })
        },
        onBackPressed = onBackPressed,
    ) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        when (val result: ConfigurationUiState = uiState) {
            ConfigurationUiState.Initial -> Box {}
            is ConfigurationUiState.Success -> {
                ConfigurationSection(result.user) { updateUser ->
                    viewModel.updateUser(updateUser)
                }
            }
        }
    }
}

@Composable
private fun ConfigurationSection(user: User, onConfigurationChange: (User) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ConfigurationOption(
            text = stringResource(id = R.string.configuration_cam_selector),
            checked = user.useCamFront,
            onCheckedChange = { onConfigurationChange(user.copy(useCamFront = !user.useCamFront)) }
        )
        ConfigurationOption(
            text = stringResource(id = R.string.configuration_pinch_to_zoom),
            checked = user.usePinchToZoom,
            onCheckedChange = { onConfigurationChange(user.copy(usePinchToZoom = !user.usePinchToZoom)) }
        )
        ConfigurationOption(
            text = stringResource(id = R.string.configuration_tap_to_focus),
            checked = user.useTapToFocus,
            onCheckedChange = { onConfigurationChange(user.copy(useTapToFocus = !user.useTapToFocus)) }
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

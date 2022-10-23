package br.com.devlucasyuji.sample.feature.camera.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.devlucasyuji.camposer.state.CaptureMode

@Composable
fun ActionBox(
    modifier: Modifier = Modifier,
    captureMode: CaptureMode,
    onTakePicture: () -> Unit,
    onSwitchCamera: () -> Unit,
    onRecording: () -> Unit,
    onOptionChanged: (CaptureMode) -> Unit
) {
    Column(
        modifier = modifier,
    ) {
        OptionSection(
            modifier = Modifier.fillMaxWidth(),
            captureMode = captureMode,
            onOptionChanged = onOptionChanged
        )
        PictureActions(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 32.dp),
            captureMode = captureMode,
            onRecording = onRecording,
            onTakePicture = onTakePicture,
            onSwitchCamera = onSwitchCamera
        )
    }
}
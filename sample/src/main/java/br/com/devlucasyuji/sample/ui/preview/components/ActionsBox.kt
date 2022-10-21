package br.com.devlucasyuji.sample.ui.preview.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.devlucasyuji.sample.ui.preview.model.Option

@Composable
fun ActionBox(
    modifier: Modifier = Modifier,
    onTakePicture: () -> Unit,
    onSwitchCamera: () -> Unit,
    onOptionChanged: (Option) -> Unit
) {
    Column(
        modifier = modifier,
    ) {
        OptionSection(
            modifier = Modifier.fillMaxWidth(),
            onOptionChanged = onOptionChanged
        )
        PictureActions(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 32.dp),
            onTakePicture = onTakePicture,
            onSwitchCamera = onSwitchCamera
        )
    }
}
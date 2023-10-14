package com.ujizin.sample.feature.camera.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ujizin.sample.R

@Composable
fun ConfigurationBox(
    modifier: Modifier = Modifier,
    onConfigurationClick: () -> Unit,
) {
    Box(modifier) {
        Button(
            modifier = Modifier.clip(CircleShape),
            contentPaddingValues = PaddingValues(16.dp),
            onClick = onConfigurationClick,
        ) {
            Image(
                painter = painterResource(id = R.drawable.configuration),
                contentDescription = stringResource(id = R.string.configuration)
            )
        }
    }
}

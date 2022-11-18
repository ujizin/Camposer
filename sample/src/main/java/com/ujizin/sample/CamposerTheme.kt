package com.ujizin.sample

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily

@Composable
fun CamposerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = MaterialTheme.colors.copy(
            primary = colorResource(id = R.color.primary),
            background = colorResource(id = R.color.light_gray),
        ),
        typography = Typography(defaultFontFamily = FontFamily.SansSerif),
        content = content
    )
}

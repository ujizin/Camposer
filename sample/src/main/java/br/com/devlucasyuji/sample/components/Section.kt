package br.com.devlucasyuji.sample.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import br.com.devlucasyuji.sample.R

@Composable
fun Section(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    onBackPressed: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                contentColor = Color.White,
                title = { title() },
                navigationIcon = {
                    IconButton(onClick = { onBackPressed() }) {
                        Icon(Icons.Filled.ArrowBack, stringResource(id = R.string.back))
                    }
                },
            )
        },
        backgroundColor = Color.LightGray.copy(alpha = 0.25F)
    ) { content(it) }
}
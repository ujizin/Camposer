package com.ujizin.sample.components

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.ujizin.sample.R

@Composable
fun Section(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    onBackPressed: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                contentColor = Color.White,
                title = { title() },
                navigationIcon = {
                    NavigationIcon(
                        icon = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.back),
                        onClick = onBackPressed,
                    )
                },
            )
        },
    ) { content(it) }
}

@Composable
fun NavigationIcon(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(modifier = modifier, onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
        )
    }
}
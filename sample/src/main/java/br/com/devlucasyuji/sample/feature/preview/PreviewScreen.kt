package br.com.devlucasyuji.sample.feature.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.devlucasyuji.sample.R
import br.com.devlucasyuji.sample.components.Section
import br.com.devlucasyuji.sample.extensions.capitalize
import coil.compose.AsyncImage
import org.koin.androidx.compose.get
import java.io.File

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun PreviewScreen(
    viewModel: PreviewViewModel = get(),
    onBackPressed: () -> Unit,
) {
    Section(
        title = {
            Text(stringResource(id = R.string.preview).capitalize())
        },
        onBackPressed = onBackPressed,
    ) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        when (val result: PreviewUiState = uiState) {
            PreviewUiState.Initial -> {}
            PreviewUiState.Empty -> {}
            is PreviewUiState.Image -> PreviewImageSection(result.file)
            is PreviewUiState.Video -> {

            }
        }
    }
}

@Composable
private fun PreviewImageSection(file: File) {
    AsyncImage(
        modifier = Modifier.fillMaxSize(),
        model = file,
        contentScale = ContentScale.Fit,
        contentDescription = file.name,
    )
}

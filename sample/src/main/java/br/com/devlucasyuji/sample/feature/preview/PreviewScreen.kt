package br.com.devlucasyuji.sample.feature.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.devlucasyuji.sample.components.BackNavigationIcon
import br.com.devlucasyuji.sample.extensions.observeAsState
import coil.compose.AsyncImage
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import org.koin.androidx.compose.koinViewModel
import java.io.File

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun PreviewScreen(
    viewModel: PreviewViewModel = koinViewModel(),
    onBackPressed: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val result: PreviewUiState = uiState) {
        PreviewUiState.Initial -> {}
        PreviewUiState.Empty -> {}
        is PreviewUiState.Image -> PreviewImageSection(result.file)
        is PreviewUiState.Video -> PreviewVideoSection(result.file)
    }
    BackNavigationIcon(
        modifier = Modifier
            .padding(8.dp)
            .background(Color.Black.copy(alpha = 0.1F), CircleShape),
        onBackPressed = onBackPressed
    )
}

@Composable
fun PreviewVideoSection(file: File) {
    val context = LocalContext.current
    val lifecycle by LocalLifecycleOwner.current.lifecycle.observeAsState()
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            addMediaItem(MediaItem.fromUri(file.toUri()))
            prepare()
        }
    }

    DisposableEffect(AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        factory = { ctx ->
            StyledPlayerView(ctx).apply { this.player = player }
        },
        update = { playerView ->
            when (lifecycle) {
                Lifecycle.Event.ON_PAUSE -> {
                    playerView.onPause()
                    player.pause()
                }

                Lifecycle.Event.ON_RESUME -> playerView.onResume()
                else -> Unit
            }
        }
    )) { onDispose { player.release() } }
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

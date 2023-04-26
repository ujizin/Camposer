package com.ujizin.sample.feature.preview

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ujizin.sample.R
import com.ujizin.sample.components.NavigationIcon
import com.ujizin.sample.extensions.observeAsState
import coil.compose.AsyncImage
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
import org.koin.androidx.compose.koinViewModel
import java.io.File

@Composable
fun PreviewScreen(
    viewModel: PreviewViewModel = koinViewModel(),
    onBackPressed: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val intentSenderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            onBackPressed()
        }

        Log.d("INTENT SENDER", "RESULT: ${it.resultCode}")
    }

    Log.d("PREVIEW SCREEN", "OPA")

    when (val result: PreviewUiState = uiState) {
        PreviewUiState.Initial -> {}
        PreviewUiState.Empty -> {}
        is PreviewUiState.Ready -> {
            val context = LocalContext.current
            when {
                result.isVideo -> PreviewVideoSection(result.file)
                else -> PreviewImageSection(result.file)
            }
            PreviewTopAppBar(
                onBackPressed = onBackPressed,
                onDeleteClick = {
                    viewModel.deleteFile(context, intentSenderLauncher, result.file)
                }
            )
        }

        PreviewUiState.Deleted -> LaunchedEffect(Unit) {
            onBackPressed()
        }
    }
}

@Composable
fun PreviewTopAppBar(
    onBackPressed: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        NavigationIcon(
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.1F), CircleShape),
            icon = Icons.Filled.ArrowBack,
            contentDescription = stringResource(R.string.back),
            onClick = onBackPressed
        )
        NavigationIcon(
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.1F), CircleShape),
            icon = Icons.Filled.Delete,
            contentDescription = stringResource(R.string.delete),
            onClick = onDeleteClick
        )
    }
}

@Composable
private fun PreviewVideoSection(file: File) {
    val context = LocalContext.current
    val lifecycle by LocalLifecycleOwner.current.lifecycle.observeAsState()
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            addMediaItem(MediaItem.fromUri(file.toUri()))
            prepare()
            playWhenReady = true
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

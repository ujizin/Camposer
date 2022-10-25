package br.com.devlucasyuji.sample.feature.gallery

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import java.io.File

@Composable
fun GalleryScreen(viewModel: GalleryViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    when (val result: GalleryUiState = uiState) {
        GalleryUiState.Initial -> GalleryLoading()
        is GalleryUiState.Success -> GallerySection(imageFiles = result.images)

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GallerySection(imageFiles: List<File>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        Modifier.fillMaxSize()
    ) {
        items(imageFiles, { it.name }) { image ->
            Box(
                modifier = Modifier
                    .padding(1.dp)
                    .fillMaxSize()
                    .animateItemPlacement(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1F),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(image)
                        .decoderFactory(VideoFrameDecoder.Factory())
                        .videoFrameMillis(1)
                        .build(),
                    contentScale = ContentScale.Crop,
                    contentDescription = "images",
                    alignment = Alignment.Center,
                )
            }
        }
    }
}

@Composable
private fun GalleryLoading() {
    Box(Modifier.fillMaxSize()) {
        CircularProgressIndicator()
    }
}

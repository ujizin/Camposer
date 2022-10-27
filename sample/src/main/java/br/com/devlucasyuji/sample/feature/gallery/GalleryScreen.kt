package br.com.devlucasyuji.sample.feature.gallery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.devlucasyuji.sample.R
import br.com.devlucasyuji.sample.components.Section
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import org.koin.androidx.compose.get
import java.io.File

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun GalleryScreen(viewModel: GalleryViewModel = get(), onBackPressed: () -> Unit) {
    Section(
        title = {
            Text(stringResource(id = R.string.gallery).replaceFirstChar { it.uppercase() })
        },
        onBackPressed = onBackPressed
    ) {
        Box(Modifier.padding(it)) {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            when (val result: GalleryUiState = uiState) {
                GalleryUiState.Initial -> GalleryLoading()
                is GalleryUiState.Success -> GallerySection(imageFiles = result.images)
                GalleryUiState.Empty -> GalleryEmpty()
            }
        }
    }
}

@Composable
private fun GalleryEmpty() {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier.padding(24.dp),
            textAlign = TextAlign.Center,
            text = stringResource(id = R.string.gallery_empty_description).replaceFirstChar { it.uppercase() },
            fontSize = 18.sp,
            color = Color.Gray,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GallerySection(imageFiles: List<File>) {
    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        items(imageFiles, { it.name }) { image ->
            PlaceholderImage(
                modifier = Modifier
                    .fillMaxSize()
                    .animateItemPlacement()
                    .aspectRatio(1F),
                data = image,
                contentDescription = image.name,
                placeholder = {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.LightGray)
                    )
                }
            )
        }
    }
}

@Composable
private fun PlaceholderImage(
    modifier: Modifier = Modifier,
    data: Any,
    placeholder: @Composable () -> Unit,
    contentDescription: String?,
) {
    var imageState: AsyncImagePainter.State by remember { mutableStateOf(AsyncImagePainter.State.Empty) }
    AsyncImage(
        modifier = modifier,
        model = ImageRequest.Builder(LocalContext.current)
            .data(data)
            .decoderFactory(VideoFrameDecoder.Factory())
            .videoFrameMillis(1)
            .build(),
        onState = { imageState = it },
        contentScale = ContentScale.Crop,
        contentDescription = contentDescription,
    )
    AnimatedVisibility(
        modifier = modifier,
        enter = fadeIn(),
        exit = fadeOut(),
        visible = when (imageState) {
            is AsyncImagePainter.State.Empty,
            is AsyncImagePainter.State.Success -> false

            is AsyncImagePainter.State.Loading,
            is AsyncImagePainter.State.Error -> true
        }
    ) { placeholder() }
}

@Composable
private fun GalleryLoading() {
    Box(Modifier.fillMaxSize()) {
        CircularProgressIndicator()
    }
}

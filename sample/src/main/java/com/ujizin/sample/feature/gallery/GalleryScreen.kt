package com.ujizin.sample.feature.gallery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ujizin.sample.R
import com.ujizin.sample.components.Section
import com.ujizin.sample.extensions.getDuration
import com.ujizin.sample.extensions.minutes
import com.ujizin.sample.extensions.seconds
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFramePercent
import org.koin.androidx.compose.get
import java.io.File

@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel = get(),
    onBackPressed: () -> Unit,
    onPreviewClick: (String) -> Unit,
) {
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
                GalleryUiState.Empty -> GalleryEmpty()
                is GalleryUiState.Success -> GallerySection(
                    imageFiles = result.images,
                    onPreviewClick = onPreviewClick,
                )
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
private fun GallerySection(imageFiles: List<File>, onPreviewClick: (String) -> Unit) {
    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        items(imageFiles, { it.name }) { image ->
            val context = LocalContext.current
            var duration by rememberSaveable { mutableStateOf<Int?>(null) }
            LaunchedEffect(Unit) { duration = image.getDuration(context) }
            PlaceholderImage(
                modifier = Modifier
                    .fillMaxSize()
                    .animateItemPlacement()
                    .aspectRatio(1F)
                    .clickable(onClick = { onPreviewClick(image.path) }),
                data = image,
                contentDescription = image.name,
                placeholder = {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.LightGray)
                    )
                },
            ) {
                duration?.let { duration ->
                    Box(
                        modifier = Modifier.background(Color.Black.copy(0.25F)),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "${duration.minutes}:${duration.seconds}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                            )
                            Icon(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(Color.White, CircleShape),
                                imageVector = Icons.Rounded.PlayArrow,
                                tint = Color.Black,
                                contentDescription = stringResource(id = R.string.play),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceholderImage(
    modifier: Modifier = Modifier,
    data: Any,
    placeholder: @Composable () -> Unit,
    contentDescription: String?,
    innerContent: @Composable () -> Unit,
) {
    var imageState: AsyncImagePainter.State by remember { mutableStateOf(AsyncImagePainter.State.Empty) }
    Box(modifier) {
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = ImageRequest.Builder(LocalContext.current)
                .data(data)
                .decoderFactory(VideoFrameDecoder.Factory())
                .videoFramePercent(0.5)
                .build(),
            onState = { imageState = it },
            contentScale = ContentScale.Crop,
            contentDescription = contentDescription,
        )
        GalleryAnimationVisibility(
            modifier = Modifier.fillMaxSize(),
            visible = when (imageState) {
                is AsyncImagePainter.State.Empty,
                is AsyncImagePainter.State.Success,
                -> false

                is AsyncImagePainter.State.Loading,
                is AsyncImagePainter.State.Error,
                -> true
            }
        ) { placeholder() }

        GalleryAnimationVisibility(
            modifier = Modifier.fillMaxSize(),
            visible = when (imageState) {
                is AsyncImagePainter.State.Empty,
                is AsyncImagePainter.State.Loading,
                is AsyncImagePainter.State.Error,
                -> false

                is AsyncImagePainter.State.Success -> true
            }
        ) { innerContent() }
    }

}

@Composable
private fun GalleryAnimationVisibility(
    modifier: Modifier = Modifier,
    visible: Boolean,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        modifier = modifier,
        enter = fadeIn(),
        exit = fadeOut(),
        visible = visible
    ) { content() }
}

@Composable
private fun GalleryLoading() {
    Box(Modifier.fillMaxSize()) {
        CircularProgressIndicator()
    }
}

package com.taras.pet.movieappcompose.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.taras.pet.movieappcompose.R

@Composable
fun PosterImage(
    url: String?,
    modifier: Modifier = Modifier,
    placeholder: Int = R.drawable.baseline_download_24,
    error: Int = R.drawable.empty_poster_placeholder
) {
    if (url.isNullOrEmpty()) {
        Image(
            painter = painterResource(id = error),
            contentDescription = "Poster placeholder",
            modifier = modifier
        )
    } else {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(url).crossfade(true).build(),
            contentDescription = null,
            placeholder = painterResource(id = placeholder),
            error = painterResource(id = error),
            modifier = modifier
        )
    }
}
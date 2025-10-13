package com.taras.pet.movieappcompose.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Text
import com.taras.pet.movieappcompose.domain.model.Movie
import com.taras.pet.movieappcompose.ui.ui_states.FavoritesUiState
import com.taras.pet.movieappcompose.ui.view_models.FavoritesViewModel

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel = hiltViewModel(),
    onMovieClick: (Int) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FavoritesContent(state = state, onMovieClick = onMovieClick)
}

@Composable
fun FavoritesContent(
    state: FavoritesUiState,
    onMovieClick: (Int) -> Unit
) {
    BackHandler {
        false
    }
    when (state) {
        is FavoritesUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
        is FavoritesUiState.Empty -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text("У вас ще немає улюблених фільмів")
        }
        is FavoritesUiState.Success -> LazyColumn {
            items(state.movies, key = { it.id }) { movie ->
                FavoriteMovieItem(movie, onClick = { onMovieClick(movie.id) })
            }
        }
    }
}

@Composable
fun FavoriteMovieItem(
    movie: Movie,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        PosterImage(
            url = movie.posterUrl,
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.titleMedium
            )
          Text(
                text = movie.overview,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "⭐ ${movie.rating}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = movie.releaseDate,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FavoritesPreview() {
    val fakeMovies = listOf(
        Movie(
            id = 1,
            title = "Inception",
            overview = "Dreams...",
            posterUrl = null,
            backdropUrl = null,
            rating = 8.8,
            releaseDate = "2010",
            genres = listOf("Sci-Fi")
        ),
        Movie(id = 2, title = "The Matrix", overview = "Neo discovers reality...", posterUrl = null, backdropUrl = null, rating = 9.0, releaseDate = "1999", genres = listOf("Action", "Sci-Fi"))
    )
    FavoritesContent(
        state = FavoritesUiState.Success(fakeMovies),
        onMovieClick = {}
    )
}
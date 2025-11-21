package com.taras.pet.movieappcompose.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taras.pet.movieappcompose.domain.model.Movie
import com.taras.pet.movieappcompose.ui.screens.content.EnhancedMovieItem
import com.taras.pet.movieappcompose.ui.screens.content.LoadingView
import com.taras.pet.movieappcompose.ui.ui_states.FavoritesUiState
import com.taras.pet.movieappcompose.ui.view_models.FavoritesViewModel

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel = hiltViewModel(),
    onMovieClick: (Int) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Text(
                    text = "My Favorites",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (state is FavoritesUiState.Success) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(state as FavoritesUiState.Success).movies.size} movies",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        FavoritesContent(state = state, onMovieClick = onMovieClick)
    }
}

@Composable
fun FavoritesContent(
    state: FavoritesUiState,
    onMovieClick: (Int) -> Unit
) {
    BackHandler {
        false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = state is FavoritesUiState.Loading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LoadingView()
        }

        AnimatedVisibility(
            visible = state is FavoritesUiState.Empty,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            EmptyFavoritesView()
        }

        AnimatedVisibility(
            visible = state is FavoritesUiState.Success,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val successState = state as? FavoritesUiState.Success
            if (successState != null) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(successState.movies, key = { it.id }) { movie ->
                        EnhancedMovieItem(
                            movie = movie,
                            onClick = { onMovieClick(movie.id) },
                            true
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyFavoritesView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Large heart icon
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(120.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.FavoriteBorder,
                        contentDescription = "No favorites",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "No Favorites Yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Start adding movies to your favorites\nto see them here",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4f
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
            overview = "A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea.",
            posterUrl = null,
            backdropUrl = null,
            rating = 8.8,
            releaseDate = "2010",
            genres = listOf("Sci-Fi")
        ),
        Movie(
            id = 2,
            title = "The Matrix",
            overview = "Neo discovers that reality as he knows it is a simulation created by machines, and joins a rebellion to free humanity.",
            posterUrl = null,
            backdropUrl = null,
            rating = 9.0,
            releaseDate = "1999",
            genres = listOf("Action", "Sci-Fi")
        ),
        Movie(
            id = 3,
            title = "Interstellar",
            overview = "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.",
            posterUrl = null,
            backdropUrl = null,
            rating = 8.6,
            releaseDate = "2014",
            genres = listOf("Sci-Fi", "Drama")
        )
    )

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    Text(
                        text = "My Favorites",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${fakeMovies.size} movies",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            FavoritesContent(
                state = FavoritesUiState.Success(fakeMovies),
                onMovieClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyFavoritesPreview() {
    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    Text(
                        text = "My Favorites",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            EmptyFavoritesView()
        }
    }
}
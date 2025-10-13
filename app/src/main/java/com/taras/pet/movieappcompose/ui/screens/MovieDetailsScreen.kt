package com.taras.pet.movieappcompose.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.taras.pet.movieappcompose.domain.model.Movie
import com.taras.pet.movieappcompose.ui.ui_states.MovieDetailsUiState
import com.taras.pet.movieappcompose.ui.view_models.MovieDetailsViewModel

@Composable
fun MovieDetailsScreen(
    movieId: Int,
    onBack: () -> Unit,
    viewModel: MovieDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Log.d("MovieScreen", "🎬 Opening MovieDetailsScreen for id = $movieId")

    BackHandler {
        onBack()
    }

    val isConnected by viewModel.isConnected.collectAsState()

    LaunchedEffect(movieId) {
        viewModel.loadMovieDetails(movieId)
    }

    MovieDetailsContent(
        state = state,
        onBack = onBack,
        onToggleFavorite = {
            viewModel.toggleFavorite()
        },
        onRetry = { viewModel.loadMovieDetails(movieId)}
    )
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MovieDetailsViewModel.MovieDetailsEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                is MovieDetailsViewModel.MovieDetailsEvent.NavigateBack -> onBack()
            }
        }
    }
}

@Composable
fun OfflineScreen2(
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp), contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = "No Internet",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Немає підключення до Інтернету",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Перевірте підключення та спробуйте ще раз",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text("Спробувати знову")
            }
        }
    }
}

@Composable
fun MovieDetailsContent(
    state: MovieDetailsUiState,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRetry: () -> Unit
) {
    when (state) {
        is MovieDetailsUiState.Loading -> {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is MovieDetailsUiState.Error -> {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                OfflineScreen2(
                    onRetry
                )
            }
        }

        is MovieDetailsUiState.Success -> {
            val movie = state.movie
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Box {
                    PosterImage(
                        url = movie.backdropUrl ?: movie.posterUrl,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )
                    IconButton(
                        onClick = {
                            onBack()
                        },
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopStart)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(8.dp))
                Text(
                    text = "⭐ ${movie.rating}   •   ${movie.releaseDate}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(8.dp))
                Text(
                    text = movie.genres.joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(12.dp))
                Text(
                    text = movie.overview,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                      //  onBack()
                        onToggleFavorite()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (state.isFavorite) "Видалити з улюблених" else "Додати в улюблені",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MovieDetailsPreview() {
    val movie = Movie(
        id = 1,
        title = "Inception",
        overview = "A mind-bending thriller about dreams within dreams.",
        posterUrl = null,
        backdropUrl = null,
        rating = 8.8,
        releaseDate = "2010",
        genres = listOf("Sci-Fi", "Action")
    )

    MovieDetailsContent(
        state = MovieDetailsUiState.Success(movie, isFavorite = false),
        onBack = {},
        onToggleFavorite = {},
        onRetry = {
            
        }
    )
}
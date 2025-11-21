package com.taras.pet.movieappcompose.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taras.pet.movieappcompose.domain.model.Movie
import com.taras.pet.movieappcompose.ui.components.PosterImage
import com.taras.pet.movieappcompose.ui.screens.content.OfflineScreen
import com.taras.pet.movieappcompose.ui.ui_states.MovieDetailsUiState
import com.taras.pet.movieappcompose.ui.view_models.MovieDetailsViewModel

@Composable
fun MovieDetailsScreen(
    movieId: Int,
    onBack: () -> Unit,
    viewModel: MovieDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler {
        onBack()
    }

    LaunchedEffect(movieId) {
        viewModel.loadMovieDetails(movieId)
    }

    MovieDetailsContent(
        state = state,
        onBack = onBack,
        onToggleFavorite = {
            viewModel.toggleFavorite()
        },
        onRetry = { viewModel.loadMovieDetails(movieId) }
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
fun MovieDetailsContent(
    state: MovieDetailsUiState,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRetry: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = state is MovieDetailsUiState.Loading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LoadingDetailView()
        }

        AnimatedVisibility(
            visible = state is MovieDetailsUiState.Error,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                OfflineScreen(
                    errorMessage = "Connection error. Please retry.",
                    onRetry
                )
            }
        }

        AnimatedVisibility(
            visible = state is MovieDetailsUiState.Success,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 4 })
        ) {
            val successState = state as? MovieDetailsUiState.Success
            if (successState != null) {
                EnhancedMovieDetails(
                    movie = successState.movie,
                    isFavorite = successState.isFavorite,
                    onBack = onBack,
                    onToggleFavorite = onToggleFavorite
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EnhancedMovieDetails(
    movie: Movie,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Hero section with backdrop
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        ) {
            // Backdrop image with blur effect
            PosterImage(
                url = movie.backdropUrl ?: movie.posterUrl,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(8.dp)
            )

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            )

            // Back button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(8.dp)
                    .align(Alignment.TopStart)
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        CircleShape
                    )
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            // Poster and basic info at bottom
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Poster card
                Card(
                    modifier = Modifier
                        .width(120.dp)
                        .height(180.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    PosterImage(
                        url = movie.posterUrl,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Title and basic info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Rating chip
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            movie.rating >= 8.0 -> Color(0xFF4CAF50)
                            movie.rating >= 6.0 -> Color(0xFFFFC107)
                            else -> Color(0xFFF44336)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Rating",
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%.1f", movie.rating),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Content section
        Column(modifier = Modifier.padding(16.dp)) {
            // Release date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Release date",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = movie.releaseDate,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Genres
            if (movie.genres.isNotEmpty()) {
                Text(
                    text = "Genres",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    movie.genres.forEach { genre ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = genre,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Overview
            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = movie.overview,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4f
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Favorite button
            Button(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFavorite)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite)
                        Icons.Filled.Favorite
                    else
                        Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isFavorite)
                        "Remove from favorites"
                    else
                        "Add to favorites",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFavorite)
                        "Remove from Favorites"
                    else
                        "Add to Favorites",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun LoadingDetailView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading details...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MovieDetailsPreview() {
    val movie = Movie(
        id = 1,
        title = "Inception",
        overview = "A mind-bending thriller about dreams within dreams. A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O.",
        posterUrl = null,
        backdropUrl = null,
        rating = 8.8,
        releaseDate = "2010-07-16",
        genres = listOf("Sci-Fi", "Action", "Thriller")
    )

    MaterialTheme {
        EnhancedMovieDetails(
            movie = movie,
            isFavorite = false,
            onBack = {},
            onToggleFavorite = {}
        )
    }
}
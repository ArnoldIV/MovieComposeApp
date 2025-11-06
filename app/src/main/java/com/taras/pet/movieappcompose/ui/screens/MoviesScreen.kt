package com.taras.pet.movieappcompose.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.taras.pet.movieappcompose.data.remote.ConnectivityEvent
import com.taras.pet.movieappcompose.domain.model.Movie
import com.taras.pet.movieappcompose.ui.components.PosterImage
import com.taras.pet.movieappcompose.ui.theme.MovieAppComposeTheme
import com.taras.pet.movieappcompose.ui.view_models.MoviesViewModel
import kotlinx.coroutines.delay

@Composable
fun MoviesScreen(
    viewModel: MoviesViewModel = hiltViewModel(),
    onMovieClick: (Int) -> Unit,
) {
    val offlineMovies by viewModel.offlineMovies.collectAsState()
    val movies = viewModel.pagedMovies.collectAsLazyPagingItems()
    val isConnected by viewModel.isConnected.collectAsState()

    var loadError by remember { mutableStateOf(false) }

    var showRefreshing by remember { mutableStateOf(false) }

    // реагуємо на зміни loadState.refresh із Paging
    LaunchedEffect(movies.loadState.refresh) {
        when (movies.loadState.refresh) {
            is LoadState.Loading -> {
                loadError = false
                showRefreshing = true
            }

            is LoadState.Error -> {
                Log.d("MoviesScreen", "error")
                loadError = true
            }

            else -> {
                // невелика затримка, щоб спінер зник плавно
                delay(500)

                showRefreshing = false
            }
        }
    }

    when {
        isConnected -> {
            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing = showRefreshing),
                onRefresh = { movies.refresh() }) {
                MoviesList(movies = movies, onMovieClick = onMovieClick
                )
            }
        }

        offlineMovies.isNotEmpty() -> {
            LazyColumn {
                items(offlineMovies, key = { it.id }) { movie ->
                    MovieItem(movie, onClick = {
                        Log.d("OfflineMovieItem", "offline movie clicked ${movie.id}")
                        onMovieClick(movie.id)
                    })
                }
            }
          //  OfflineMovieItem(offlineMovies, onMovieClick = onMovieClick)
        }
        else -> {
            MoviesList(movies = movies, onMovieClick = onMovieClick)
        }
    }


    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.connectivityChangeEvent.collect { event ->
            when (event) {
                is ConnectivityEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (loadError) {
        OfflineScreen(
            onRetry = { viewModel.retry(movies) }
        )
    }
}

@Composable
fun OfflineScreen(
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
            Icon(
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

//@Composable
//fun MoviesContent(
//    state: MoviesUiState,
//    onMovieClick: (Int) -> Unit,
//) {
//    when (state) {
//        is MoviesUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
//            CircularProgressIndicator()
//        }
//
//        is MoviesUiState.Error -> Text("Помилка завантаження")
//        is MoviesUiState.Success -> {
//            MoviesList(
//                movies = state.movies,
//                onMovieClick = { movie -> onMovieClick(movie.id) },
//                )
//        }
//
//        is MoviesUiState.Offline -> Text("Offline")
//    }
//}

@Composable
fun MovieItem(
    movie: Movie, onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp)) {
        PosterImage(
            url = movie.posterUrl, modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = movie.title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = movie.overview,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = "⭐ ${movie.rating}", style = MaterialTheme.typography.bodyMedium)
            Text(text = movie.releaseDate, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun MoviesList(
    movies: LazyPagingItems<Movie>, onMovieClick: (Int) -> Unit
) {
    LazyColumn {
        items(movies.itemCount) { index ->
            val movie = movies[index]
            if (movie != null) {
                MovieItem(
                    movie = movie, onClick = {
                        Log.d("OfflineMovieItem", " movie clicked ${movie.id}")
                        onMovieClick(movie.id) }
                )
            }
        }

        // 🔽 Індикатор підвантаження внизу
        movies.apply {
            when {
                loadState.refresh is LoadState.Loading -> {
//                    item {
//                        Box(
//                            Modifier
//                                .fillMaxSize()
//                                .padding(24.dp),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            //CircularProgressIndicator()
//                        }
//                    }
                }

                loadState.append is LoadState.Loading -> {
//                    item {
//                        Box(
//                            Modifier
//                                .fillMaxWidth()
//                                .padding(16.dp),
//                            contentAlignment = Alignment.Center
//                        ) {
//                           // CircularProgressIndicator()
//                        }
//                    }
                }

                loadState.append is LoadState.Error -> {
                    val e = movies.loadState.append as LoadState.Error
                    item {
                        Text(
                            text = "Помилка підвантаження: ${e.error.localizedMessage}",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MoviesPreview() {
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
        ), Movie(
            id = 2,
            title = "The Matrix",
            overview = "Neo discovers reality...",
            posterUrl = null,
            backdropUrl = null,
            rating = 9.0,
            releaseDate = "1999",
            genres = listOf("Action", "Sci-Fi")
        )
    )
    MovieAppComposeTheme {
//        MoviesList(
//            fakeMovies,{}
//        )
    }
}
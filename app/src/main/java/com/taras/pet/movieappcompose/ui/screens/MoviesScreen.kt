package com.taras.pet.movieappcompose.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.taras.pet.movieappcompose.data.remote.ConnectivityEvent
import com.taras.pet.movieappcompose.domain.model.Movie
import com.taras.pet.movieappcompose.ui.screens.content.EnhancedMovieItem
import com.taras.pet.movieappcompose.ui.screens.content.LoadingView
import com.taras.pet.movieappcompose.ui.theme.AppTheme
import com.taras.pet.movieappcompose.ui.ui_states.MoviesUiState
import com.taras.pet.movieappcompose.ui.view_models.MoviesViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MoviesScreen(
    viewModel: MoviesViewModel = hiltViewModel(),
    onMovieClick: (Int) -> Unit,
) {
    val offlineMovies by viewModel.offlineMovies.collectAsStateWithLifecycle()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val movies = viewModel.pagedMovies.collectAsLazyPagingItems()

    LaunchedEffect(movies.loadState.refresh) {
        viewModel.onLoadStateChanged(movies.loadState.refresh)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = uiState is MoviesUiState.Loading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LoadingView()
        }

        AnimatedVisibility(
            visible = uiState is MoviesUiState.Success,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val s = uiState as? MoviesUiState.Success
            if (s != null) {
                SwipeRefresh(
                    state = rememberSwipeRefreshState(s.isRefreshing),
                    onRefresh = { movies.refresh() }
                ) {
                    MoviesList(movies = movies, onMovieClick = onMovieClick)
                }
            }
        }

        AnimatedVisibility(
            visible = uiState is MoviesUiState.Error,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val e = uiState as? MoviesUiState.Error
            if (e != null) {
                OfflineMoviesList(
                    cachedMovies = offlineMovies,
                    onMovieClick = onMovieClick,
                    onRefresh = { movies.refresh() }
                )
            } else viewModel.logCrash("Unexpected state in MoviesUiState")
        }

        AnimatedVisibility(
            visible = uiState is MoviesUiState.Offline,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (!viewModel.isConnected.collectAsState().value) {
                OfflineMoviesList(
                    cachedMovies = offlineMovies,
                    onMovieClick = onMovieClick,
                    onRefresh = { movies.refresh() }
                )
            } else {
                    MoviesList(movies = movies, onMovieClick = onMovieClick)
            }
        }
    }

// Toast effects
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.connectivityChangeEvent.collect { event ->
            when (event) {
                is ConnectivityEvent.ShowToast ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun OfflineMoviesList(
    cachedMovies: List<Movie>,
    onMovieClick: (Int) -> Unit,
    onRefresh: () -> Unit
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = {
            isRefreshing = true
            onRefresh()
            // Reset after a delay since we know it will fail
            coroutineScope.launch {
                delay(1500)
                isRefreshing = false
            }
        }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Offline banner at the top
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.WifiOff,
                                    contentDescription = "Offline",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "You're Offline",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Showing ${cachedMovies.size} cached movies",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Cached movies
            items(cachedMovies) { movie ->
                EnhancedMovieItem(movie, onClick = { onMovieClick(movie.id) }, false)
            }

            // End of cache message
            if (cachedMovies.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = "No more content",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Internet Connection",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Connect to the internet to load more movies",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MoviesList(
    movies: LazyPagingItems<Movie>,
    onMovieClick: (Int) -> Unit
) {
    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(movies.itemCount) { index ->
            val movie = movies[index]
            if (movie != null) {
                EnhancedMovieItem(
                    movie = movie,
                    onClick = { onMovieClick(movie.id) },
                    false
                )
            }
        }

        movies.apply {
            when {
                loadState.refresh is LoadState.Loading -> {
                    // Handled by UiState
                }

                loadState.append is LoadState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 3.dp
                            )
                        }
                    }
                }

                loadState.append is LoadState.Error -> {
                    val e = movies.loadState.append as LoadState.Error
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Failed to load more movies. Error: $e",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { retry() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun <T : Any> List<T>.toLazyPagingItems(): LazyPagingItems<T> {
    return remember {
        Pager(PagingConfig(pageSize = this.size)) {
            object : PagingSource<Int, T>() {
                override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
                    return LoadResult.Page(
                        data = this@toLazyPagingItems,
                        prevKey = null,
                        nextKey = null
                    )
                }

                override fun getRefreshKey(state: PagingState<Int, T>): Int? = null
            }
        }.flow
    }.collectAsLazyPagingItems()
}

@Preview(showBackground = true)
@Composable
fun MoviesPreview() {
    val fakeMovies: List<Movie> = listOf(
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
    AppTheme {
        MoviesList(
            movies = fakeMovies.toLazyPagingItems(),
            onMovieClick = {}
        )
    }
}
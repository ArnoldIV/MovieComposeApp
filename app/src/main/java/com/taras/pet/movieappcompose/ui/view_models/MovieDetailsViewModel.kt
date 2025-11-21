package com.taras.pet.movieappcompose.ui.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taras.pet.movieappcompose.data.remote.NetworkChecker
import com.taras.pet.movieappcompose.domain.AnalyticsService
import com.taras.pet.movieappcompose.domain.repo_interfaces.MovieRepository
import com.taras.pet.movieappcompose.ui.ui_states.MovieDetailsUiState
import com.taras.pet.movieappcompose.util.AnalyticsEvents.ADD_TO_FAVORITES
import com.taras.pet.movieappcompose.util.AnalyticsEvents.REMOVE_FROM_FAVORITES
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    private val networkChecker: NetworkChecker,
    private val repository: MovieRepository,
    private val analytics: AnalyticsService
) : ViewModel() {

    private val _state = MutableStateFlow<MovieDetailsUiState>(MovieDetailsUiState.Loading)
    val state: StateFlow<MovieDetailsUiState> = _state

    private val _events = MutableSharedFlow<MovieDetailsEvent>()
    val events: SharedFlow<MovieDetailsEvent> = _events

    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected

    init {
        observeConnection()
    }

    private fun observeConnection() {
        viewModelScope.launch {
            networkChecker.isConnected.collect { connected ->
                _isConnected.value = connected
            }
        }
    }

    fun loadMovieDetails(id: Int) {

        viewModelScope.launch {

            _state.value = MovieDetailsUiState.Loading
            try {
                if (!isConnected.value) {
                    val cachedMovie = async { repository.getPopularMovieById(id) }
                    val isPopularFavoriteDeferred = async { repository.getFavorites().first() }

                    val popularMovie = cachedMovie.await()
                    val isFavorites = isPopularFavoriteDeferred.await()

                    val isPopularFavorite = isFavorites.any { it.id == popularMovie.id }

                    _state.value = MovieDetailsUiState.Success(
                        movie = popularMovie,
                        isFavorite = isPopularFavorite
                    )
                    return@launch
                } else {
                    analytics.logEvent(
                        "movie_details_opened id${id}",
                    )
                    val movieDeferred = async { repository.getMovieDetails(id) }
                    val favoritesDeferred = async { repository.getFavorites().first() }

                    val movie = movieDeferred.await()
                    val favorites = favoritesDeferred.await()

                    val isFavorite = favorites.any { it.id == movie.id }
                    _state.value = MovieDetailsUiState.Success(
                        movie = movie,
                        isFavorite = isFavorite
                    )
                }
            } catch (e: Exception) {
                _state.value =
                    MovieDetailsUiState.Error(e.message ?: "Error loading details")
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val current = _state.value
            if (current is MovieDetailsUiState.Success) {
                val movie = current.movie
                if (current.isFavorite) {
                    repository.removeFromFavorites(movie)
                    _state.value = current.copy(isFavorite = false)
                    _events.emit(MovieDetailsEvent.ShowToast("Removed from favorites"))
                    _events.emit(MovieDetailsEvent.NavigateBack)
                    analytics.logEvent(
                        name = REMOVE_FROM_FAVORITES,
                        params = mapOf(
                            "movie_id" to movie.id,
                            "movie_title" to movie.title,
                            "timestamp" to System.currentTimeMillis()
                        )
                    )
                } else {
                    repository.addToFavorites(movie)
                    _state.value = current.copy(isFavorite = true)
                    _events.emit(MovieDetailsEvent.ShowToast("Added to favorites"))
                    _events.emit(MovieDetailsEvent.NavigateBack)
                    analytics.logEvent(
                        name = ADD_TO_FAVORITES,
                        params = mapOf(
                            "movie_id" to movie.id,
                            "movie_title" to movie.title,
                            "timestamp" to System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    sealed class MovieDetailsEvent {
        data class ShowToast(val message: String) : MovieDetailsEvent()
        object NavigateBack : MovieDetailsEvent()
    }
}
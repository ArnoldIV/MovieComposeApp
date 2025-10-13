package com.taras.pet.movieappcompose.ui.ui_states

import com.taras.pet.movieappcompose.domain.model.Movie

sealed class MoviesUiState {
    object Loading : MoviesUiState()
    data class Success(val movies: List<Movie>,val isRefreshing: Boolean = false) : MoviesUiState()
    data class Error(
        val message: String,
        val cachedMovies: List<Movie> = emptyList()
    ) : MoviesUiState()
    data class Offline(val cachedMovies: List<Movie>) : MoviesUiState()
}
package com.taras.pet.movieappcompose.ui.ui_states

import com.taras.pet.movieappcompose.domain.model.Movie

sealed class MovieDetailsUiState {
    object Loading : MovieDetailsUiState()
    data class Success(
        val movie: Movie,
        val isFavorite: Boolean
    ) : MovieDetailsUiState()
    data class Error(val message: String) : MovieDetailsUiState()
}
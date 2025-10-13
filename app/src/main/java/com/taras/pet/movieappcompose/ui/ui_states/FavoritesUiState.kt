package com.taras.pet.movieappcompose.ui.ui_states

import com.taras.pet.movieappcompose.domain.model.Movie

sealed class FavoritesUiState {
    object Loading : FavoritesUiState()
    object Empty : FavoritesUiState()
    data class Success(val movies: List<Movie>) : FavoritesUiState()
}
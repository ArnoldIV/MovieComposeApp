package com.taras.pet.movieappcompose.ui.view_models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taras.pet.movieappcompose.domain.repo_interfaces.MovieRepository
import com.taras.pet.movieappcompose.ui.ui_states.MovieDetailsUiState
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
    private val repository: MovieRepository
) : ViewModel() {

    private val _state = MutableStateFlow<MovieDetailsUiState>(MovieDetailsUiState.Loading)
    val state: StateFlow<MovieDetailsUiState> = _state

    private val _events = MutableSharedFlow<MovieDetailsEvent>()
    val events: SharedFlow<MovieDetailsEvent> = _events

    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected

    fun loadMovieDetails(id: Int) {
        viewModelScope.launch {
            _state.value = MovieDetailsUiState.Loading
            try {
                val movieDeferred = async { repository.getMovieDetails(id) }
                val favoritesDeferred = async { repository.getFavorites().first() }

                val movie = movieDeferred.await()
                val favorites = favoritesDeferred.await()

                val isFavorite = favorites.any { it.id == movie.id }
                _state.value = MovieDetailsUiState.Success(
                    movie = movie,
                    isFavorite = isFavorite
                )
            } catch (e: Exception) {
                _state.value = MovieDetailsUiState.Error(e.message ?: "Помилка завантаження деталей")
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
                    _events.emit(MovieDetailsEvent.ShowToast("Видалено з улюблених"))
                    _events.emit(MovieDetailsEvent.NavigateBack)
                } else {
                    repository.addToFavorites(movie)
                    _state.value = current.copy(isFavorite = true)
                    _events.emit(MovieDetailsEvent.ShowToast("Додано в улюблені"))
                    _events.emit(MovieDetailsEvent.NavigateBack)
                }
            }
        }
    }
    sealed class MovieDetailsEvent {
        data class ShowToast(val message: String) : MovieDetailsEvent()
        object NavigateBack : MovieDetailsEvent()
    }
}


//    fun loadMovieDetails(id: Int) {
//        viewModelScope.launch {
//            _state.value = MovieDetailsUiState.Loading
//            try {
//                val movie = repository.getMovieDetails(id)
//                _state.value = MovieDetailsUiState.Success(movie)
//            } catch (e: Exception) {
//                _state.value = MovieDetailsUiState.Error(e.message ?: "Помилка")
//            }
//    }
//}
//
//    fun removeFromFavorite(id: Int) {
//        viewModelScope.launch {
//            val movie = repository.getMovieDetails(id)
//            repository.removeFromFavorites(movie)
//            delay(1000)
//        }
//    }
//
//    fun saveFavorite(id: Int) {
//        viewModelScope.launch {
//            val movie = repository.getMovieDetails(id)
//            repository.addToFavorites(movie)
//            delay(1000)
//        }
//    }
//}
package com.taras.pet.movieappcompose.ui.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.compose.LazyPagingItems
import com.taras.pet.movieappcompose.data.remote.ConnectivityEvent
import com.taras.pet.movieappcompose.data.remote.NetworkChecker
import com.taras.pet.movieappcompose.domain.model.Movie
import com.taras.pet.movieappcompose.domain.repo_interfaces.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor(
    repo: MovieRepository,
    private val networkChecker: NetworkChecker
) : ViewModel() {

    val pagedMovies = repo.getPagedMovies()
        .cachedIn(viewModelScope)

    val offlineMovies = repo.getPopularMovies()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _connectivityChangeEvent = MutableSharedFlow<ConnectivityEvent>()
    val connectivityChangeEvent: SharedFlow<ConnectivityEvent> = _connectivityChangeEvent

    init {
        observeConnection()
    }

    private fun observeConnection() {
        viewModelScope.launch {
            networkChecker.isConnected.collect { connected ->
                _isConnected.value = connected

                if (!connected) {
                    _connectivityChangeEvent.emit(ConnectivityEvent.ShowToast("Інтернет зник"))
                } else {
                    _connectivityChangeEvent.emit(ConnectivityEvent.ShowToast("Інтернет відновлено"))
                }
            }
        }
    }

    /** Викликається при натисканні на кнопку "Спробувати знову" */
    fun retry(movies: LazyPagingItems<Movie>) {
        if (_isConnected.value) {
            movies.retry()
        }
    }

//    private val _state = MutableStateFlow<MoviesUiState>(MoviesUiState.Loading)
//    val state: StateFlow<MoviesUiState> = _state
//
//    private val _connectivityChangeEvent = MutableSharedFlow<ConnectivityEvent>()
//    val connectivityChangeEvent: SharedFlow<ConnectivityEvent> = _connectivityChangeEvent
//
//
//    private var currentPage = 1
//    private var allMovies = mutableListOf<Movie>()
//    private var isLoadingMore = false
//
//    init {
//        loadMovies()
//    }
//
//    fun loadMovies() {
//        viewModelScope.launch {
//            networkChecker.isConnected.collect { connected ->
//                if (!connected) {
//                    // показати офлайн, якщо даних немає
//                    if (!connected && _state.value is MoviesUiState.Success) {
//                        _connectivityChangeEvent.emit(ConnectivityEvent.ShowToast("Інтернет пропав"))
//                    }
//
//                    if (_state.value !is MoviesUiState.Success) {
//                        val cached = repo.getFavorites().firstOrNull().orEmpty()
//                        if (cached.isNotEmpty()) {
//                            _state.value = MoviesUiState.Offline(cached)
//                        } else {
//                            _state.value = MoviesUiState.Error("Немає інтернету")
//                        }
//                    }
//                } else {
//                    try {
//                        val movies = repo.getMovies(currentPage)
//                        _state.value = MoviesUiState.Success(movies)
//                    } catch (e: Exception) {
//                        _state.value = MoviesUiState.Error(e.message ?: "Помилка61")
//                    }
//                }
//            }
//        }
//    }
//
//    fun refresh(){
//        viewModelScope.launch {
//            val currentMovies = (_state.value as? MoviesUiState.Success)?.movies.orEmpty()
//            _state.value = MoviesUiState.Success(currentMovies, isRefreshing = true)
//
//            try {
//                val movies = repo.getMovies(currentPage)
//                _state.value = MoviesUiState.Success(movies, isRefreshing = false)
//            } catch (e: Exception) {
//                _state.value = MoviesUiState.Error(e.message ?: "Помилка оновлення")
//            }
//        }
//    }
//
//
//    fun loadNextPage() {
//        if (isLoadingMore) return
//        isLoadingMore = true
//        currentPage++
//        viewModelScope.launch {
//            try {
//                val movies = repo.getMovies(currentPage)
//                allMovies.addAll(movies)
//                _state.value = MoviesUiState.Success(allMovies.toList())
//            } catch (e: Exception) {
//                _state.value = MoviesUiState.Error("Помилка підвантаження: ${e.message}")
//            } finally {
//                isLoadingMore = false
//            }
//        }
//    }

}
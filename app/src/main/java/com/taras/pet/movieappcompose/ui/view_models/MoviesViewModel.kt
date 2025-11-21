package com.taras.pet.movieappcompose.ui.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.cachedIn
import androidx.paging.compose.LazyPagingItems
import com.taras.pet.movieappcompose.data.remote.ConnectivityEvent
import com.taras.pet.movieappcompose.data.remote.NetworkChecker
import com.taras.pet.movieappcompose.domain.model.Movie
import com.taras.pet.movieappcompose.domain.repo_interfaces.MovieRepository
import com.taras.pet.movieappcompose.ui.ui_states.MoviesUiState
import com.taras.pet.movieappcompose.util.CrashLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor(
    repo: MovieRepository,
    private val networkChecker: NetworkChecker,
    private val crashLogger: CrashLogger,
) : ViewModel() {

    val pagedMovies = repo.getPagedMovies()
        .cachedIn(viewModelScope)

    val offlineMovies = repo.getPopularMovies()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _uiState = MutableStateFlow<MoviesUiState>(MoviesUiState.Loading)
    val uiState: StateFlow<MoviesUiState> = _uiState

    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _connectivityChangeEvent = MutableSharedFlow<ConnectivityEvent>()
    val connectivityChangeEvent: SharedFlow<ConnectivityEvent> = _connectivityChangeEvent

    init {
        observeConnection()
        observePaging()
    }

    private fun observePaging() {
        viewModelScope.launch {
            pagedMovies.collectLatest { pagingData ->
                // pagingData itself does not give a snapshot until it is rendered,
                // so the UI will respond to loadState
            }
        }
    }

    private fun observeConnection() {
        viewModelScope.launch {
            networkChecker.isConnected.collect { connected ->
                _isConnected.value = connected

                if (!connected) {
                    _connectivityChangeEvent.emit(ConnectivityEvent.ShowToast("Lost connection"))
                } else {
                    _connectivityChangeEvent.emit(ConnectivityEvent.ShowToast("Connection restored"))
                }
            }
        }
    }

    fun onLoadStateChanged(state: LoadState) {
        when (state) {
            is LoadState.Loading -> {
                _uiState.value = MoviesUiState.Loading
            }

            is LoadState.NotLoading -> {
                _uiState.value = MoviesUiState.Success(
                    movies = offlineMovies.value, //or snapshot of the submitted data
                    isRefreshing = false
                )
            }

            is LoadState.Error -> {
                logCrash("LoadState error: ${state.error.message}")
                _uiState.value = MoviesUiState.Error(
                    message = state.error.message ?: "Unknown error",
                    cachedMovies = offlineMovies.value
                )
            }
        }
    }

    fun retry(movies: LazyPagingItems<Movie>) {
        if (_isConnected.value) {
            movies.retry()
        }
    }

    fun logCrash(message: String) {
        crashLogger.log(message)
    }
}
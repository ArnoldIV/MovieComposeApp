package com.taras.pet.movieappcompose.domain.repo_interfaces

import androidx.paging.PagingData
import com.taras.pet.movieappcompose.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface  MovieRepository {
    suspend fun getMovies(page:Int): List<Movie>
    fun getPagedMovies(): Flow<PagingData<Movie>>
    suspend fun getMovieDetails(id: Int): Movie

    fun getFavorites(): Flow<List<Movie>>
    fun getPopularMovies(): Flow<List<Movie>>
    suspend fun getPopularMovieById(id: Int): Movie
    suspend fun addToFavorites(movie: Movie)
    suspend fun updatePopularMovies()
    suspend fun removeFromFavorites(movie: Movie)
}
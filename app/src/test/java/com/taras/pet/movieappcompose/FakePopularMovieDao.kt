package com.taras.pet.movieappcompose

import com.taras.pet.movieappcompose.data.local.room.PopularMovieEntity
import com.taras.pet.movieappcompose.data.local.room.dao.PopularMoviesDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakePopularMovieDao : PopularMoviesDao {
    private val storage = mutableListOf<PopularMovieEntity>()

    override fun getAllPopularMovies(): Flow<List<PopularMovieEntity>> = flow {
        emit(storage)
    }

    override suspend fun clearPopularMovies() {
        storage.clear()
    }

    override suspend fun insertAllPopularMovies(movies: List<PopularMovieEntity>) {
        storage.addAll(movies)
    }
}
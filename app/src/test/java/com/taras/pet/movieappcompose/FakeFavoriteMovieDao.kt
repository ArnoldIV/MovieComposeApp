package com.taras.pet.movieappcompose

import com.taras.pet.movieappcompose.data.local.FavoriteMovieDao
import com.taras.pet.movieappcompose.data.local.FavoriteMovieEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeFavoriteMovieDao : FavoriteMovieDao {
    private val storage = mutableListOf<FavoriteMovieEntity>()

    override fun getAllFavorites(): Flow<List<FavoriteMovieEntity>> = flow {
        emit(storage)
    }

    override suspend fun getFavoriteById(id: Int): FavoriteMovieEntity? {
        return storage.find { it.id == id }
    }

    override suspend fun insert(movie: FavoriteMovieEntity) {
        storage.removeAll { it.id == movie.id }
        storage.add(movie)
    }

    override suspend fun delete(movie: FavoriteMovieEntity) {
        storage.removeAll { it.id == movie.id }
    }
}
package com.taras.pet.movieappcompose.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.taras.pet.movieappcompose.data.local.room.PopularMovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PopularMoviesDao {

    @Query("SELECT * FROM popular_movies WHERE id = :id LIMIT 1")
    suspend fun getPopularMovieById(id: Int): PopularMovieEntity?

    @Query("SELECT * FROM popular_movies")
    fun getAllPopularMovies(): Flow<List<PopularMovieEntity>>

    @Query("DELETE FROM popular_movies")
    suspend fun clearPopularMovies()

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAllPopularMovies(movies: List<PopularMovieEntity>)
}
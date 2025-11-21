package com.taras.pet.movieappcompose.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.taras.pet.movieappcompose.data.local.room.dao.FavoriteMovieDao
import com.taras.pet.movieappcompose.data.local.room.dao.PopularMoviesDao

@Database(
    entities = [FavoriteMovieEntity::class, PopularMovieEntity::class],
    version = 2,
    exportSchema = false
)

@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteMovieDao(): FavoriteMovieDao
    abstract fun popularMoviesDao(): PopularMoviesDao
}
package com.taras.pet.movieappcompose.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.taras.pet.movieappcompose.data.local.room.AppDatabase
import com.taras.pet.movieappcompose.data.local.room.dao.FavoriteMovieDao
import com.taras.pet.movieappcompose.data.local.room.dao.PopularMoviesDao


import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext app: Context): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "movies_db"
        )
           // .fallbackToDestructiveMigration(false)
            .addMigrations(MIGRATION_1_2)

            .build()
    }

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
            CREATE TABLE IF NOT EXISTS popular_movies (
                id INTEGER NOT NULL PRIMARY KEY,
                title TEXT NOT NULL,
                overview TEXT NOT NULL,
                posterUrl TEXT,
                backdropUrl TEXT,
                rating REAL NOT NULL,
                releaseDate TEXT,
                genres TEXT NOT NULL
            )
        """.trimIndent())
        }
    }

    @Provides
    @Singleton
    fun provideFavoriteMovieDao(db: AppDatabase): FavoriteMovieDao {
        return db.favoriteMovieDao()
    }

    @Provides
    @Singleton
    fun providePopularMoviesDao(db: AppDatabase): PopularMoviesDao {
        return db.popularMoviesDao()
    }
}
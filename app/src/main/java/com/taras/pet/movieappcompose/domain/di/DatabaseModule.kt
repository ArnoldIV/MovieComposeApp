package com.taras.pet.movieappcompose.domain.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.taras.pet.movieappcompose.data.local.AppDatabase
import com.taras.pet.movieappcompose.data.local.FavoriteMovieDao


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
        ).build()
    }

    @Provides
    @Singleton
    fun provideFavoriteMovieDao(db: AppDatabase): FavoriteMovieDao {
        return db.favoriteMovieDao()
    }
}
package com.taras.pet.movieappcompose.data.di

import android.content.Context
import com.taras.pet.movieappcompose.data.local.secure.SecureDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecureModule {

    @Provides
    @Singleton
    fun provideSecureDataStore(
        @ApplicationContext context: Context
    ): SecureDataStore {
        return SecureDataStore(context)
    }
}
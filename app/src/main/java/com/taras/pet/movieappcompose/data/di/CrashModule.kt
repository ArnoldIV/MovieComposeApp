package com.taras.pet.movieappcompose.data.di

import com.taras.pet.movieappcompose.data.analytics.CrashlyticsService
import com.taras.pet.movieappcompose.util.CrashLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object CrashModule {

    @Provides
    fun provideCrashLogger(): CrashLogger = CrashlyticsService()
}
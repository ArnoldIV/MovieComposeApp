package com.taras.pet.movieappcompose.data.di

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.taras.pet.movieappcompose.data.analytics.FirebaseAnalyticsService
import com.taras.pet.movieappcompose.domain.AnalyticsService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(
        @ApplicationContext context: Context
    ): FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    @Provides
    @Singleton
    fun provideAnalyticsService(
        service: FirebaseAnalyticsService
    ): AnalyticsService = service

}
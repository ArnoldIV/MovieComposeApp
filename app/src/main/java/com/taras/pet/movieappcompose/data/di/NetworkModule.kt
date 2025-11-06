package com.taras.pet.movieappcompose.data.di

import android.content.Context
import com.squareup.moshi.Moshi
import com.taras.pet.movieappcompose.data.remote.ApiKeyInterceptor
import com.taras.pet.movieappcompose.data.remote.MovieApi
import com.taras.pet.movieappcompose.data.remote.NetworkChecker
import com.taras.pet.movieappcompose.data.remote.NetworkCheckerImpl

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideNetworkChecker(
        @ApplicationContext context: Context
    ): NetworkChecker = NetworkCheckerImpl(context)

    @Provides
    @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder().build()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(ApiKeyInterceptor("c59b87d0d2497518247a1253110205da"))
            .build()
    }

    //https://api.themoviedb.org/3/movie/popular?api_key=c59b87d0d2497518247a1253110205da

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient,moshi: Moshi):  Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun provideMovieApi(retrofit: Retrofit): MovieApi {
        return retrofit.create(MovieApi::class.java)
    }
}
package com.taras.pet.movieappcompose.data.di
import android.content.Context
import com.squareup.moshi.Moshi
import com.taras.pet.movieappcompose.BuildConfig
import com.taras.pet.movieappcompose.data.local.secure.SecureDataStore
import com.taras.pet.movieappcompose.data.remote.ApiKeyInterceptor
import com.taras.pet.movieappcompose.data.remote.MovieApi
import com.taras.pet.movieappcompose.data.remote.NetworkChecker
import com.taras.pet.movieappcompose.data.remote.NetworkCheckerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
    fun provideOkHttpClient(
        secureDataStore: SecureDataStore
    ): OkHttpClient {
        val apiKeyFlow = secureDataStore.apiKey

        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val apiKey = runBlocking { apiKeyFlow.first() ?: "" }

                val newUrl = chain.request().url.newBuilder()
                    .addQueryParameter("api_key", apiKey)
                    .build()

                val request = chain.request().newBuilder()
                    .url(newUrl)
                    .build()

                chain.proceed(request)
            }
            .build()
    }

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
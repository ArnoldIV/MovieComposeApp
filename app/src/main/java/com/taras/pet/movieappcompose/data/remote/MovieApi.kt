package com.taras.pet.movieappcompose.data.remote

import com.taras.pet.movieappcompose.data.remote.dto.MovieDetailsResponse
import com.taras.pet.movieappcompose.data.remote.dto.MovieResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieApi {

    @GET("movie/popular")
    suspend fun getMovies(
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("movie/{id}")
    suspend fun getMovieDetails(@Path("id") id: Int): MovieDetailsResponse
}
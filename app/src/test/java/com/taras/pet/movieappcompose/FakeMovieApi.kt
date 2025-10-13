package com.taras.pet.movieappcompose

import com.taras.pet.movieappcompose.data.remote.MovieApi
import com.taras.pet.movieappcompose.data.remote.dto.MovieDetailsResponse
import com.taras.pet.movieappcompose.data.remote.dto.MovieDto
import com.taras.pet.movieappcompose.data.remote.dto.MovieResponse

class FakeMovieApi : MovieApi {
    var moviesResponse: List<MovieDto>? = null
    var detailsResponse: MovieDetailsResponse? = null
    var throwError: Boolean = false

    override suspend fun getMovies(page: Int): MovieResponse {
        if (throwError) throw Exception("Network error")
        return MovieResponse(page, moviesResponse ?: emptyList(), 1, 1)

    }

    override suspend fun getMovieDetails(id: Int): MovieDetailsResponse {
        if (throwError) throw Exception("No details set")
        return detailsResponse ?: throw Exception("No details set")
    }
}
package com.taras.pet.movieappcompose.data.mapper

import com.taras.pet.movieappcompose.data.local.PopularMovieEntity
import com.taras.pet.movieappcompose.data.remote.dto.MovieDto
import com.taras.pet.movieappcompose.domain.model.Movie
import javax.inject.Inject

class MovieDtoMapper  @Inject constructor() {

    fun map(dto: MovieDto): Movie {
        return Movie(
            id = dto.id,
            title = dto.title,
            overview = dto.overview,
            posterUrl = dto.poster_path?.let { "https://image.tmdb.org/t/p/w500$it" },
            backdropUrl = dto.backdrop_path?.let { "https://image.tmdb.org/t/p/w780$it" },
            rating = dto.vote_average,
            releaseDate = dto.release_date,
            genres = dto.genre_ids.map { it }
        )
    }

    fun toPopularEntity(dto: MovieDto): PopularMovieEntity = PopularMovieEntity(
        id = dto.id,
        title = dto.title,
        overview = dto.overview,
        posterUrl = dto.poster_path?.let { "https://image.tmdb.org/t/p/w500$it" },
        backdropUrl = dto.backdrop_path?.let { "https://image.tmdb.org/t/p/w780$it" },
        rating = dto.vote_average,
        releaseDate = dto.release_date,
        genres = dto.genre_ids.map { it }
    )

    fun mapPopularList(dtos: List<MovieDto>): List<PopularMovieEntity> {
        return dtos.map { toPopularEntity(it) }
    }

    fun mapList(dtos: List<MovieDto>): List<Movie> {
        return dtos.map { map(it) }
    }
}
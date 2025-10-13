package com.taras.pet.movieappcompose.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MovieDetailsResponse(
    val id: Int,
    val title: String,
    val overview: String,
    @Json(name = "poster_path") val poster_path: String?,
    @Json(name = "backdrop_path") val backdrop_path: String?,
    @Json(name = "release_date") val release_date: String?,
    @Json(name = "vote_average") val vote_average: Double,
    @Json(name = "vote_count") val vote_count: Int,
    @Json(name = "original_title") val original_title: String,
    @Json(name = "original_language") val original_language: String,
    val adult: Boolean,
    val video: Boolean,
    val popularity: Double,
    val genres: List<GenreDto>
)

@JsonClass(generateAdapter = true)
data class GenreDto(
    val id: Int,
    val name: String
)
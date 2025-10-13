package com.taras.pet.movieappcompose.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MovieDto(
    val id: Int,
    val title: String,
    val original_title: String,
    val overview: String,
    @Json(name = "poster_path") val poster_path: String?,
    @Json(name = "backdrop_path") val backdrop_path: String?,
    @Json(name = "release_date") val release_date: String,
    @Json(name = "vote_average") val vote_average: Double,
    val vote_count: Int,
    val popularity: Double,
    val adult: Boolean,
    val video: Boolean,
    val original_language: String,
    val genre_ids: List<String>
)
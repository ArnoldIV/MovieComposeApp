package com.taras.pet.movieappcompose.domain.model

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl:String?,
    val rating: Double,
    val releaseDate: String,
    val genres: List<String>
)
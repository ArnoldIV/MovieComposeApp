package com.taras.pet.movieappcompose.data.local.room

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromGenres(genres: List<String>?): String {
        return genres?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toGenres(data: String): List<String> {
        return if (data.isEmpty()) emptyList() else data.split(",")
    }
}
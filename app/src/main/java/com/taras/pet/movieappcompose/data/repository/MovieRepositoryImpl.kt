package com.taras.pet.movieappcompose.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.taras.pet.movieappcompose.data.local.FavoriteMovieDao
import com.taras.pet.movieappcompose.data.local.FavoriteMovieEntity
import com.taras.pet.movieappcompose.data.mapper.MovieDtoMapper
import com.taras.pet.movieappcompose.data.remote.MovieApi
import com.taras.pet.movieappcompose.data.remote.MoviePagingSource
import com.taras.pet.movieappcompose.domain.model.Movie
import com.taras.pet.movieappcompose.domain.repo_interfaces.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val api: MovieApi,
    private val mapper: MovieDtoMapper,
    private val favoriteDao: FavoriteMovieDao
) : MovieRepository {

    override suspend fun getMovies(page: Int): List<Movie> {
        val response = api.getMovies(page)
        return mapper.mapList(response.results)
    }

    override fun getPagedMovies(): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 3,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { MoviePagingSource(api, mapper) }
        ).flow
    }

    override suspend fun getMovieDetails(id: Int): Movie {
        return try {

            // якщо API недоступне → дістаємо з локальної БД
            favoriteDao.getFavoriteById(id)?.let { entity ->
                Movie(
                    id = entity.id,
                    title = entity.title,
                    overview = entity.overview,
                    posterUrl = entity.posterUrl,
                    backdropUrl = entity.backdropUrl,
                    rating = entity.rating,
                    releaseDate = entity.releaseDate ?: "N/A",
                    genres = entity.genres
                )
            }
                ?: throw Exception("Movie not found in local database") // якщо й у БД немає → пробросимо помилку

        } catch (e: Exception) {
            val dto = api.getMovieDetails(id)
            Movie(
                id = dto.id,
                title = dto.title,
                overview = dto.overview,
                posterUrl = dto.poster_path?.let { "https://image.tmdb.org/t/p/w500$it" },
                backdropUrl = dto.backdrop_path?.let { "https://image.tmdb.org/t/p/w780$it" },
                rating = dto.vote_average,
                releaseDate = dto.release_date ?: "N/A",
                genres = dto.genres.map { it.name }
            )
        }
    }

    override fun getFavorites(): Flow<List<Movie>> =
        favoriteDao.getAllFavorites().map { entities ->
            entities.map { entity ->
                Movie(
                    id = entity.id,
                    title = entity.title,
                    overview = entity.overview,
                    posterUrl = entity.posterUrl,
                    backdropUrl = entity.backdropUrl,
                    rating = entity.rating,
                    releaseDate = entity.releaseDate ?: "Planing",
                    genres = entity.genres
                )
            }
        }

    override suspend fun addToFavorites(movie: Movie) {
        val entity = FavoriteMovieEntity(
            id = movie.id,
            title = movie.title,
            overview = movie.overview,
            posterUrl = movie.posterUrl,
            backdropUrl = movie.backdropUrl,
            rating = movie.rating,
            releaseDate = movie.releaseDate,
            genres = movie.genres
        )
        favoriteDao.insert(entity)
    }

    override suspend fun removeFromFavorites(movie: Movie) {
        val entity = FavoriteMovieEntity(
            id = movie.id,
            title = movie.title,
            overview = movie.overview,
            posterUrl = movie.posterUrl,
            backdropUrl = movie.backdropUrl,
            rating = movie.rating,
            releaseDate = movie.releaseDate,
            genres = movie.genres
        )
        favoriteDao.delete(entity)
    }
}
package com.taras.pet.movieappcompose.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.firebase.Firebase
import com.google.firebase.perf.performance
import com.taras.pet.movieappcompose.data.local.room.FavoriteMovieEntity
import com.taras.pet.movieappcompose.data.local.room.dao.FavoriteMovieDao
import com.taras.pet.movieappcompose.data.local.room.dao.PopularMoviesDao
import com.taras.pet.movieappcompose.data.mapper.MovieDtoMapper
import com.taras.pet.movieappcompose.data.remote.MovieApi
import com.taras.pet.movieappcompose.data.remote.MoviePagingSource
import com.taras.pet.movieappcompose.domain.model.Movie
import com.taras.pet.movieappcompose.domain.repo_interfaces.MovieRepository
import com.taras.pet.movieappcompose.util.CrashLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val api: MovieApi,
    private val mapper: MovieDtoMapper,
    private val favoriteDao: FavoriteMovieDao,
    private val popularMoviesDao: PopularMoviesDao,
    private val crashLogger: CrashLogger
) : MovieRepository {

    override suspend fun getMovies(page: Int): List<Movie> {
        val trace = Firebase.performance.newTrace("get_movies_page_$page")
        trace.start()

        return try {
            val response = api.getMovies(page)
            mapper.mapList(response.results)
        } catch (e: Exception) {
            crashLogger.log("API getMovies failed: ${e.message}")
            crashLogger.logException(e)
            throw e
        } finally {
            trace.stop()
        }
    }

    override suspend fun updatePopularMovies() {
        popularMoviesDao.clearPopularMovies()
        val entities = mapper.mapPopularList(api.getMovies(1).results)
        Log.d("MovieRepositoryImpl", "entities: $entities")
        popularMoviesDao.insertAllPopularMovies(entities)
    }

    override fun getPagedMovies(): Flow<PagingData<Movie>> {
        return try {
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    prefetchDistance = 3,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = { MoviePagingSource(api, mapper) }
            ).flow
        } catch (e: Exception) {
            crashLogger.log("API getPagedMovies failed ${e.message}")
            crashLogger.logException(e)
            throw e
        }
    }

    override suspend fun getMovieDetails(id: Int): Movie {
        // 1. Local DB
        favoriteDao.getFavoriteById(id)?.let { entity ->
            return Movie(
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

        // 2. API
        val trace = Firebase.performance.newTrace("movie_details_api_$id")
        trace.start()

        return try {
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
        } catch (e: Exception) {
            crashLogger.log("API movieDetails failed: ${e.message}")
            crashLogger.logException(e)
            throw e
        } finally {
            trace.stop()
        }
    }

    override suspend fun getPopularMovieById(id: Int): Movie {
        val movie = popularMoviesDao.getPopularMovieById(id)?.let { entity ->
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
        return movie!!
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

    override fun getPopularMovies(): Flow<List<Movie>> =
        popularMoviesDao.getAllPopularMovies().map { entities ->
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
        try {
            favoriteDao.insert(entity)
        } catch (e: Exception) {
            crashLogger.log("API addToFavorites failed ${e.message}")
            crashLogger.logException(e)
        }
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
        try {
            favoriteDao.delete(entity)
        } catch (e: Exception) {
            crashLogger.log("API removeFromFavorites failed ${e.message}")
            crashLogger.logException(e)
        }

    }
}
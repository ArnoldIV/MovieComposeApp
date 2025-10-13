package com.taras.pet.movieappcompose.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.taras.pet.movieappcompose.data.mapper.MovieDtoMapper
import com.taras.pet.movieappcompose.domain.model.Movie

class MoviePagingSource(
    private val api: MovieApi,
    private val mapper: MovieDtoMapper
) : PagingSource<Int, Movie>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        return try {
            val page = params.key ?: 1
            val response = api.getMovies(page)
            val movies = mapper.mapList(response.results)

            LoadResult.Page(
                data = movies,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.results.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }
}
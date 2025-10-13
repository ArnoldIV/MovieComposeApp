package com.taras.pet.movieappcompose

import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.taras.pet.movieappcompose.data.local.FavoriteMovieEntity
import com.taras.pet.movieappcompose.data.mapper.MovieDtoMapper
import com.taras.pet.movieappcompose.data.remote.ConnectivityEvent
import com.taras.pet.movieappcompose.data.remote.NetworkChecker
import com.taras.pet.movieappcompose.data.remote.dto.GenreDto
import com.taras.pet.movieappcompose.data.remote.dto.MovieDetailsResponse
import com.taras.pet.movieappcompose.data.remote.dto.MovieDto
import com.taras.pet.movieappcompose.data.repository.MovieRepositoryImpl
import com.taras.pet.movieappcompose.domain.model.Movie
import com.taras.pet.movieappcompose.domain.repo_interfaces.MovieRepository
import com.taras.pet.movieappcompose.ui.view_models.MovieDetailsViewModel
import com.taras.pet.movieappcompose.ui.view_models.MoviesViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

//
// MovieDao test
//

class FakeFavoriteMovieDaoTest {

    private val dao = FakeFavoriteMovieDao()

    @Test
    fun `add two movies to DB and get list size`() = runTest {
// Arrange
        val movieEntity = FavoriteMovieEntity(
            id = 1,
            title = "Test",
            overview = "Desc",
            posterUrl = null,
            backdropUrl = null,
            rating = 7.5,
            releaseDate = "2025-01-01",
            genres = listOf("Action")
        )

        val movieEntity2 = FavoriteMovieEntity(
            id = 2,
            title = "Test",
            overview = "Desc",
            posterUrl = null,
            backdropUrl = null,
            rating = 7.5,
            releaseDate = "2025-01-01",
            genres = listOf("Action")
        )

        // Act
        dao.insert(movieEntity)
        dao.insert(movieEntity2)
        val moviesDbList = dao.getAllFavorites()

        // Assert
        Assertions.assertEquals(2, moviesDbList.first().size)
    }

    @Test
    fun `add two movies to DB and delete one, check if remaining movie is the second one`() =
        runTest {

            val movieEntity = FavoriteMovieEntity(
                id = 1,
                title = "Test",
                overview = "Desc",
                posterUrl = null,
                backdropUrl = null,
                rating = 7.5,
                releaseDate = "2025-01-01",
                genres = listOf("Action")
            )

            val movieEntity2 = FavoriteMovieEntity(
                id = 2,
                title = "Test",
                overview = "Desc",
                posterUrl = null,
                backdropUrl = null,
                rating = 7.5,
                releaseDate = "2025-01-01",
                genres = listOf("Action")
            )

            dao.insert(movieEntity)
            dao.insert(movieEntity2)
            val moviesDbList = dao.getAllFavorites()
            val movie2 = dao.getFavoriteById(2)
            dao.delete(movieEntity)

            Assertions.assertEquals(1, moviesDbList.first().size)
            Assertions.assertEquals(2, movie2?.id)
        }

    @Test
    fun `add two movies with same id and check if they replacing`() = runTest {

        val entity1 = FavoriteMovieEntity(
            id = 1,
            title = "Test",
            overview = "Desc",
            posterUrl = null,
            backdropUrl = null,
            rating = 7.5,
            releaseDate = "2025-01-01",
            genres = listOf("Action")
        )

        val entity2 = FavoriteMovieEntity(
            id = 1,
            title = "Test2",
            overview = "Desc2",
            posterUrl = null,
            backdropUrl = null,
            rating = 7.5,
            releaseDate = "2025-01-02",
            genres = listOf("Action2")
        )

        dao.insert(entity1)
        dao.insert(entity2)

        val result = dao.getFavoriteById(1)

        Assertions.assertEquals("Test2", result?.title)
        Assertions.assertEquals(
            1,
            dao.getAllFavorites().first().size
        )
    }

    @Test
    fun `delete uncreated movie from the storage`() = runTest {
        val entity1 = FavoriteMovieEntity(
            id = 1,
            title = "Test",
            overview = "Desc",
            posterUrl = null,
            backdropUrl = null,
            rating = 7.5,
            releaseDate = "2025-01-01",
            genres = listOf("Action")
        )

        val entity2 = FavoriteMovieEntity(
            id = 2,
            title = "Another",
            overview = "Desc",
            posterUrl = null,
            backdropUrl = null,
            rating = 6.0,
            releaseDate = "2025-01-01",
            genres = listOf("Drama")
        )

        dao.insert(entity1)
        dao.delete(entity2)
        val result = dao.getAllFavorites().first()

        Assertions.assertEquals(1, result.size, "розмір списку не 1")
    }
}

//
// Repository test
//


@OptIn(ExperimentalCoroutinesApi::class)
class MovieRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeApi: FakeMovieApi
    private lateinit var fakeDao: FakeFavoriteMovieDao
    private lateinit var mapper: MovieDtoMapper
    private lateinit var repository: MovieRepository
    private lateinit var fakeNetworkChecker: FakeNetworkChecker

    private lateinit var moviesViewModel: MoviesViewModel
    private lateinit var detailsMoviesViewModel: MovieDetailsViewModel

    private val mockRepo = mockk<MovieRepository>(relaxed = true)
    private val mockNetworkChecker = mockk<NetworkChecker>()


    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        fakeApi = FakeMovieApi()
        fakeDao = FakeFavoriteMovieDao()
        mapper = MovieDtoMapper()
        repository = MovieRepositoryImpl(fakeApi, mapper, fakeDao)
        fakeNetworkChecker = FakeNetworkChecker(initialState = true)


        moviesViewModel = MoviesViewModel(repository, fakeNetworkChecker)
        detailsMoviesViewModel = MovieDetailsViewModel(repository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getMovies returns mapped movies from API`() = runTest {
        // Arrange
        fakeApi.moviesResponse = listOf(
            MovieDto(
                id = 1,
                title = "Test",
                original_title = "Test",
                overview = "Desc",
                poster_path = null,
                backdrop_path = null,
                release_date = "2025-01-01",
                vote_average = 7.5,
                vote_count = 100,
                popularity = 200.0,
                adult = false,
                video = false,
                original_language = "en",
                genre_ids = listOf("Action")
            )
        )

        // Act
        val result = repository.getMovies(1)

        // Assert
        Assertions.assertEquals(1, result.size)
        Assertions.assertEquals("Test", result[0].title)
    }

    @Test
    fun `get movies details`() = runTest {
        fakeApi.detailsResponse = MovieDetailsResponse(
            id = 1,
            title = "TestDetails",
            overview = "Desc",
            poster_path = null,
            backdrop_path = null,
            release_date = "2025-01-01",
            vote_average = 7.5,
            vote_count = 100,
            original_title = "Test Original",
            original_language = "en",
            adult = false,
            video = false,
            popularity = 200.0,
            genres = listOf(GenreDto(1, "Action"))
        )

        val result = repository.getMovieDetails(1)

        assertAll(
            "Movie details mapping",
            { Assertions.assertEquals("TestDetails", result.title) },
            { Assertions.assertEquals("Desc", result.overview) },
            { Assertions.assertEquals(listOf("Action"), result.genres) },
            { Assertions.assertEquals("2025-01-01", result.releaseDate) },
            { Assertions.assertEquals(7.5, result.rating) }
        )
    }

    @Test
    fun `movie details return null and throws exception`() = runTest {
        fakeApi.detailsResponse = null
        val exception = assertThrows<Exception> {
            repository.getMovieDetails(1)
        }
        Assertions.assertEquals("No details set", exception.message)
    }

    @Test
    fun `add to favorites and get all favorites`() = runTest {

        val movie = Movie(
            id = 1,
            title = "Test",
            overview = "Desc",
            posterUrl = null,
            backdropUrl = null,
            rating = 7.5,
            releaseDate = "2025-01-01",
            genres = listOf("Action")
        )

        repository.addToFavorites(movie)

        val favMovie = fakeDao.getAllFavorites().first()

        Assertions.assertEquals(1, favMovie.size)
        Assertions.assertEquals(movie.id, favMovie[0].id)
        Assertions.assertEquals(movie.title, favMovie[0].title)
    }

    @Test
    fun `delete from favorites`() = runTest {

        val movie = Movie(
            id = 1,
            title = "Test",
            overview = "Desc",
            posterUrl = null,
            backdropUrl = null,
            rating = 7.5,
            releaseDate = "2025-01-01",
            genres = listOf("Action")
        )

        repository.addToFavorites(movie)

        repository.removeFromFavorites(movie)

        val favMovie = fakeDao.getAllFavorites().first()

        Assertions.assertEquals(0, favMovie.size)
    }

    @Test
    fun `get favorites from repository`() = runTest {

        val movie = Movie(
            id = 1,
            title = "Test",
            overview = "Desc",
            posterUrl = null,
            backdropUrl = null,
            rating = 7.5,
            releaseDate = "2025-01-01",
            genres = listOf("Action")
        )

        val movie2 = Movie(
            id = 2,
            title = "Test",
            overview = "Desc",
            posterUrl = null,
            backdropUrl = null,
            rating = 7.5,
            releaseDate = "2025-01-01",
            genres = listOf("Action")
        )

        repository.addToFavorites(movie)
        repository.addToFavorites(movie2)

        val favorites = repository.getFavorites().first()

        assertAll(
            "Favorites",
            { Assertions.assertEquals(2, favorites.size) },
            { Assertions.assertEquals(movie.id, favorites[0].id) },
            { Assertions.assertEquals(movie.title, favorites[0].title) },
            { Assertions.assertEquals(movie2.id, favorites[1].id) },
            { Assertions.assertEquals(movie2.title, favorites[1].title) },
        )
    }

    @Test
    fun `replace favorites`() = runTest {

        val movie = Movie(
            id = 1,
            title = "Test",
            overview = "Desc",
            posterUrl = null,
            backdropUrl = null,
            rating = 7.5,
            releaseDate = "2025-01-01",
            genres = listOf("Action")
        )

        val movie2 = Movie(
            id = 1,
            title = "Test2",
            overview = "Desc",
            posterUrl = null,
            backdropUrl = null,
            rating = 7.5,
            releaseDate = "2025-01-01",
            genres = listOf("Action")
        )

        repository.addToFavorites(movie)
        repository.addToFavorites(movie2)

        val favorites = repository.getFavorites().first()

        assertAll(
            "Favorites",
            { Assertions.assertEquals(1, favorites.size) },
            { Assertions.assertEquals("Test2", favorites[0].title) },
        )
    }

    @Test
    fun `get favorites and receive empty list`() = runTest {

        val favorites = repository.getFavorites().first()

        Assertions.assertEquals(emptyList<Movie>(), favorites)

    }

    //
    // ViewModel test
    //

}

@OptIn(ExperimentalCoroutinesApi::class)
class MoviesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val mockRepo = mockk<MovieRepository>(relaxed = true)
    private val mockNetworkChecker = mockk<NetworkChecker>()

    private lateinit var viewModel: MoviesViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when connection changes emits correct toast events`() = runTest {
        // Arrange
        val connectivityFlow = flow {
            emit(true)
            emit(false)
            emit(true)
        }
        every { mockNetworkChecker.isConnected } returns connectivityFlow

        viewModel = MoviesViewModel(mockRepo, mockNetworkChecker)

        val emittedMessages = mutableListOf<String>()
        val job = launch {
            viewModel.connectivityChangeEvent.collect { event ->
                if (event is ConnectivityEvent.ShowToast) {
                    emittedMessages.add(event.message)
                }
            }
        }

        // Act
        advanceUntilIdle() // чекаємо завершення всіх collect

        // Assert
        assert(emittedMessages.contains("Інтернет зник"))
        assert(emittedMessages.contains("Інтернет відновлено"))

        job.cancel()
    }

    @Test
    fun `retry calls movies_retry only when connected`() = runTest {
        // Arrange
        every { mockNetworkChecker.isConnected } returns flowOf(true)
        viewModel = MoviesViewModel(mockRepo, mockNetworkChecker)

        val mockLazyPagingItems = mockk<LazyPagingItems<Movie>>(relaxed = true)

        // Act
        viewModel.retry(mockLazyPagingItems)

        // Assert
        verify(exactly = 1) { mockLazyPagingItems.retry() }
    }

    @Test
    fun `retry calls movies_retry only when disconnected`() = runTest {
        // Arrange
        every { mockNetworkChecker.isConnected } returns flowOf(false)
        viewModel = MoviesViewModel(mockRepo, mockNetworkChecker)

        val mockLazyPagingItems = mockk<LazyPagingItems<Movie>>(relaxed = true)

        // Act
        viewModel.retry(mockLazyPagingItems)

        // Assert
        verify(exactly = 0) { mockLazyPagingItems.retry() }
    }

    class TestListCallback : ListUpdateCallback {
        override fun onChanged(position: Int, count: Int, payload: Any?) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
    }

    class TestDiffCallback<T> : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(
            oldItem: T & Any,
            newItem: T & Any
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: T & Any,
            newItem: T & Any
        ): Boolean {
            return oldItem == newItem
        }
    }

    @Test
    fun `mock repo and getPagedMovies and check if UI gets PagingData with movies list2`() =
        runTest {
            // Arrange

            val movie = Movie(
                id = 1,
                title = "Test",
                overview = "Desc",
                posterUrl = null,
                backdropUrl = null,
                rating = 7.5,
                releaseDate = "2025-01-01",
                genres = listOf("Action")
            )

            val movie2 = Movie(
                id = 2,
                title = "Test2",
                overview = "Desc",
                posterUrl = null,
                backdropUrl = null,
                rating = 7.5,
                releaseDate = "2025-01-01",
                genres = listOf("Action")
            )
            every { mockNetworkChecker.isConnected } returns flowOf(true)
            every { mockRepo.getPagedMovies() } returns flowOf(
                PagingData.from(listOf(movie,movie2)))

            viewModel = MoviesViewModel(mockRepo, mockNetworkChecker)

            val mockLazyPagingItems = viewModel.pagedMovies.first()

            val differ = AsyncPagingDataDiffer(
                diffCallback = TestDiffCallback<Movie>(),
                updateCallback = TestListCallback(),
                workerDispatcher = Dispatchers.Main
            )

            differ.submitData(mockLazyPagingItems)
            val list = differ.snapshot().items

            // Act
            advanceUntilIdle() // чекаємо завершення всіх collect

            // Assert
            verify(exactly = 1) { mockRepo.getPagedMovies() }
            Assertions.assertEquals("Test",list.first().title)
            Assertions.assertEquals("Test2",list[1].overview)
            Assertions.assertEquals(2,list.size)
            //assert(emittedMessages.contains("Інтернет відновлено"))
        }
}
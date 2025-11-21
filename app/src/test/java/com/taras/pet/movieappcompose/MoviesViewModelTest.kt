package com.taras.pet.movieappcompose

import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.taras.pet.movieappcompose.data.remote.ConnectivityEvent
import com.taras.pet.movieappcompose.data.remote.NetworkChecker
import com.taras.pet.movieappcompose.domain.model.Movie
import com.taras.pet.movieappcompose.domain.repo_interfaces.MovieRepository
import com.taras.pet.movieappcompose.ui.view_models.MoviesViewModel
import com.taras.pet.movieappcompose.util.CrashLogger
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
class MoviesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val mockRepo = mockk<MovieRepository>(relaxed = true)
    private val mockNetworkChecker = mockk<NetworkChecker>()
    private val mockCrashLogger = mockk<CrashLogger>(relaxed = true)

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

        viewModel = MoviesViewModel(mockRepo, mockNetworkChecker, mockCrashLogger)

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
        viewModel = MoviesViewModel(mockRepo, mockNetworkChecker, mockCrashLogger)

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
        viewModel = MoviesViewModel(mockRepo, mockNetworkChecker, mockCrashLogger)

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
                PagingData.from(listOf(movie, movie2))
            )

            viewModel = MoviesViewModel(mockRepo, mockNetworkChecker, mockCrashLogger)

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
            Assertions.assertEquals("Test", list.first().title)
            Assertions.assertEquals("Test2", list[1].overview)
            Assertions.assertEquals(2, list.size)
            //assert(emittedMessages.contains("Інтернет відновлено"))
        }
}
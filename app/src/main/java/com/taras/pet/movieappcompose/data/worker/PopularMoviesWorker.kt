package com.taras.pet.movieappcompose.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.Firebase
import com.google.firebase.perf.performance
import com.taras.pet.movieappcompose.domain.repo_interfaces.MovieRepository
import com.taras.pet.movieappcompose.util.CrashLogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class PopularMoviesWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: MovieRepository,
    private val crashLogger: CrashLogger
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {

        val trace = Firebase.performance.newTrace("update_popular_movies")
        trace.start()

        return try {
            Log.d(TAG, "Worker started: updating popular movies...")
            repository.updatePopularMovies()
            Log.d(TAG, "Worker completed successfully")
            Result.success()
        } catch (e: Exception) {
            crashLogger.log("PopularMoviesWorker failed: ${e.message}")
            crashLogger.logException(e)
            Result.retry()
        } finally {
            trace.stop()
        }
    }

    companion object {
        private const val TAG = "PopularMoviesWorker"
    }
}
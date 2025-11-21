package com.taras.pet.movieappcompose

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.taras.pet.movieappcompose.data.local.secure.SecureDataStore
import com.taras.pet.movieappcompose.data.worker.PopularMoviesWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class MovieApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var secureDataStore: SecureDataStore

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()

    override fun onCreate() {
        super.onCreate()
        Log.d("PopularMoviesWorker", "Custom WorkManager config loaded")
        schedulePopularMoviesUpdate(this)

        CoroutineScope(Dispatchers.IO).launch {
            val stored = secureDataStore.apiKey.first()
            if (stored == null) {
                secureDataStore.saveApiKey(BuildConfig.API_KEY)
            }
        }
    }

    fun schedulePopularMoviesUpdate(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<PopularMoviesWorker>(
            6, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "popular_movies_refresh",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
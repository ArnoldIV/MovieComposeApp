package com.taras.pet.movieappcompose.data.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.taras.pet.movieappcompose.domain.AnalyticsService
import javax.inject.Inject

class FirebaseAnalyticsService @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) : AnalyticsService {
    override fun logEvent(
        name: String,
        params: Map<String, Any>
    ) {
        val bundle = Bundle()
        params.forEach { (key, value) ->
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Boolean -> bundle.putBoolean(key, value)
                is Double -> bundle.putDouble(key, value)
            }
        }
        firebaseAnalytics.logEvent(name, bundle)    }
}
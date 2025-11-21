package com.taras.pet.movieappcompose.data.analytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.taras.pet.movieappcompose.util.CrashLogger
import javax.inject.Inject

class CrashlyticsService @Inject constructor() : CrashLogger {

    override fun log(message: String) {
        FirebaseCrashlytics.getInstance().log(message)
    }

    override fun logException(throwable: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(throwable)
    }
}
package com.taras.pet.movieappcompose.domain

interface AnalyticsService {
    fun logEvent(name: String, params: Map<String, Any> = emptyMap())
}
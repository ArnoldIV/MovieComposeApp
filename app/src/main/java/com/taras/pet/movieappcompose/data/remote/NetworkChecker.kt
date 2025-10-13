package com.taras.pet.movieappcompose.data.remote

import kotlinx.coroutines.flow.Flow

interface NetworkChecker {
    val isConnected: Flow<Boolean>
}
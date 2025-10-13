package com.taras.pet.movieappcompose

import com.taras.pet.movieappcompose.data.remote.NetworkChecker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeNetworkChecker(
    initialState: Boolean = true
) : NetworkChecker {
    private val _isConnected = MutableStateFlow(initialState)
    override val isConnected: Flow<Boolean> = _isConnected

    fun setConnected(value: Boolean) {
        _isConnected.value = value
    }
}
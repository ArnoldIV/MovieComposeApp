package com.taras.pet.movieappcompose.data.remote

sealed class ConnectivityEvent {
    data class ShowToast(val message: String) : ConnectivityEvent()
}
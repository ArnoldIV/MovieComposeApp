package com.taras.pet.movieappcompose.util

interface CrashLogger {
    fun log(message: String)
    fun logException(throwable: Throwable)
}
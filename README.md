MovieComposeApp is a modern Android application that lets users browse popular movies, view details, and seamlessly switch between online and offline modes.

Tech Stack
Kotlin, Coroutines, Flow
Jetpack Compose (Material 3, state-driven UI)
MVVM + UiState management
Hilt for dependency injection
Retrofit + OkHttp (custom interceptor)
Paging 3 for infinite scrolling
Room Database for offline caching
WorkManager for background syncing
DataStore + Encrypted DataStore for secure local storage
BuildConfig for injecting API keys

Secure API Key Handling
The TMDB API key is securely injected using BuildConfig and additionally stored in Encrypted DataStore, providing an extra layer of protection by encrypting sensitive data with Android’s AES-256 Keystore.

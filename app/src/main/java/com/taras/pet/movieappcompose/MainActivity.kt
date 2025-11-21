package com.taras.pet.movieappcompose

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.FirebaseApp
import com.taras.pet.movieappcompose.ui.screens.FavoritesScreen
import com.taras.pet.movieappcompose.ui.screens.MovieDetailsScreen
import com.taras.pet.movieappcompose.ui.screens.MoviesScreen
import com.taras.pet.movieappcompose.ui.theme.AppTheme
import com.taras.pet.movieappcompose.ui.view_models.MovieDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        setContent {
            AppTheme {
                MovieApp()
            }
        }
        //for testing purpose
//        FirebaseCrashlytics.getInstance().log("Test crash before exception")
//        throw RuntimeException("Test Crash")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieApp() {

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "MovieApp",
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(
                        onClick = {
                        }
                    ) {
                        Icon(
                            imageVector =
                                Icons.Default.Menu,
                            contentDescription = "mainMenuIcon"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "more actions icon"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (currentRoute == "movies" || currentRoute == "favorites") {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == "movies",
                        onClick = {
                            if (currentRoute != "movies") { // 👈 avoids a double
                                navController.navigate("movies") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.Movie, contentDescription = null) },
                        label = { Text("Movies") }
                    )

                    NavigationBarItem(
                        selected = currentRoute == "favorites",
                        onClick = {
                            if (currentRoute != "favorites") { // 👈 avoids a double
                                navController.navigate("favorites") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                        label = { Text("Favorite") }
                    )
                }
            }
        },

        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "movies",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("movies") {
                    MoviesScreen(
                        onMovieClick = { movieId ->

                            navController.navigate("details/$movieId") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        })
                }

                composable("favorites") {
                    FavoritesScreen(onMovieClick = { movieId ->
                        navController.navigate("details/$movieId") {
                            Log.d("Nav", "➡️ Navigate to details/$movieId")

                            launchSingleTop = true
                            restoreState = true
                        }
                    })
                }

                composable(
                    "details/{movieId}",
                    arguments = listOf(navArgument("movieId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val movieId = backStackEntry.arguments!!.getInt("movieId")

                    // Key based on movieId → now each movie will have its own VM
                    val viewModel: MovieDetailsViewModel = hiltViewModel(key = "details_$movieId")

                    MovieDetailsScreen(
                        movieId = movieId,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AppTheme {
        MovieApp()
    }
}
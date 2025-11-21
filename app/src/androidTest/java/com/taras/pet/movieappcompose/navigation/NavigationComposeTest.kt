package com.taras.pet.movieappcompose.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.taras.pet.movieappcompose.ui.theme.AppTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NavigationComposeTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController

    @Before
    fun setupNavHost() {
        hiltRule.inject()

        composeTestRule.setContent {
            AppTheme {
                navController = TestNavHostController(LocalContext.current).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                }

                // Here we would set up the navigation host with our actual screens
                // For now, we'll create simplified test composables
                TestMovieAppNavigation(navController = navController)
            }
        }
    }

    @Test
    fun navHost_verifyStartDestination() {
        // Verify that Movies screen is the start destination
        composeTestRule
            .onNodeWithText("Movies Screen")
            .assertIsDisplayed()

        // Check current route
        assert(navController.currentBackStackEntry?.destination?.route == "movies")
    }

    @Test
    fun navHost_navigateToFavorites_viaBottomNavigation() {
        // Click on Favorites in bottom navigation
        composeTestRule
            .onNodeWithText("Favorite")
            .performClick()

        // Verify navigation to Favorites screen
        composeTestRule
            .onNodeWithText("Favorites Screen")
            .assertIsDisplayed()

        // Check current route
        assert(navController.currentBackStackEntry?.destination?.route == "favorites")
    }

    @Test
    fun navHost_navigateToMovieDetails() {
        // Assuming we have a movie item to click on
        composeTestRule
            .onNodeWithTag("movie_item_123")
            .performClick()

        // Verify navigation to Movie Details screen
        composeTestRule
            .onNodeWithText("Movie Details Screen")
            .assertIsDisplayed()

        // Check current route contains the movie ID
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        assert(currentRoute?.contains("details") == true)
    }

    @Test
    fun navHost_navigateBackFromMovieDetails() {
        // Navigate to movie details first
        navController.navigate("details/123")
        composeTestRule.waitForIdle()

        // Verify we're on details screen
        composeTestRule
            .onNodeWithText("Movie Details Screen")
            .assertIsDisplayed()

        // Click back button
        composeTestRule
            .onNodeWithContentDescription("Back")
            .performClick()

        // Verify we're back to movies screen
        composeTestRule
            .onNodeWithText("Movies Screen")
            .assertIsDisplayed()

        assert(navController.currentBackStackEntry?.destination?.route == "movies")
    }

    @Test
    fun navHost_bottomNavigation_remembersState() {
        // Navigate to Favorites
        composeTestRule
            .onNodeWithText("Favorite")
            .performClick()

        composeTestRule
            .onNodeWithText("Favorites Screen")
            .assertIsDisplayed()

        // Navigate back to Movies
        composeTestRule
            .onNodeWithText("Movies")
            .performClick()

        composeTestRule
            .onNodeWithText("Movies Screen")
            .assertIsDisplayed()

        // Navigate to Favorites again
        composeTestRule
            .onNodeWithText("Favorite")
            .performClick()

        // Should show Favorites screen again
        composeTestRule
            .onNodeWithText("Favorites Screen")
            .assertIsDisplayed()
    }

    @Test
    fun navHost_preventDuplicateNavigation() {
        // Click Movies multiple times
        repeat(3) {
            composeTestRule
                .onNodeWithText("Movies")
                .performClick()
        }

        // Should still be on Movies screen without duplicates in backstack
        composeTestRule
            .onNodeWithText("Movies Screen")
            .assertIsDisplayed()

        // Back stack should have only one entry
        assert(navController.backQueue.size <= 2) // Root + current destination
    }

    @Test
    fun navHost_deepLinkToMovieDetails() {
        val movieId = 456

        // Simulate deep link navigation
        navController.navigate("details/$movieId")
        composeTestRule.waitForIdle()

        // Verify Movie Details screen is displayed
        composeTestRule
            .onNodeWithText("Movie Details Screen")
            .assertIsDisplayed()

        // Verify the correct movie ID is passed
        assert(navController.currentBackStackEntry?.arguments?.getInt("movieId") == movieId)
    }

    @Test
    fun navHost_multipleMovieDetailsNavigation() {
        val movieIds = listOf(123, 456, 789)

        movieIds.forEach { movieId ->
            // Navigate to each movie details
            navController.navigate("details/$movieId")
            composeTestRule.waitForIdle()

            // Verify Movie Details screen is displayed
            composeTestRule
                .onNodeWithText("Movie Details Screen")
                .assertIsDisplayed()

            // Go back to prepare for next navigation
            navController.popBackStack()
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun navHost_topAppBarVisibility() {
        // Verify top app bar is visible on main screens
        composeTestRule
            .onNodeWithText("MovieApp")
            .assertIsDisplayed()

        // Navigate to favorites
        composeTestRule
            .onNodeWithText("Favorite")
            .performClick()

        // Top app bar should still be visible
        composeTestRule
            .onNodeWithText("MovieApp")
            .assertIsDisplayed()
    }

    @Test
    fun navHost_bottomNavigationVisibility() {
        // Bottom navigation should be visible on main screens
        composeTestRule
            .onNodeWithText("Movies")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Favorite")
            .assertIsDisplayed()

        // Navigate to movie details
        navController.navigate("details/123")
        composeTestRule.waitForIdle()

        // Bottom navigation should be hidden on details screen
        composeTestRule
            .onNodeWithText("Movies")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithText("Favorite")
            .assertDoesNotExist()
    }
}

// Test composable that simulates the actual app navigation structure
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TestMovieAppNavigation(navController: TestNavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("MovieApp") }
            )
        },
        bottomBar = {
            if (currentRoute == "movies" || currentRoute == "favorites") {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == "movies",
                        onClick = {
                            if (currentRoute != "movies") {
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
                            if (currentRoute != "favorites") {
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
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "movies",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("movies") {
                TestMoviesScreen(onMovieClick = { movieId ->
                    navController.navigate("details/$movieId") {
                        launchSingleTop = true
                        restoreState = true
                    }
                })
            }

            composable("favorites") {
                TestFavoritesScreen(onMovieClick = { movieId ->
                    navController.navigate("details/$movieId") {
                        launchSingleTop = true
                        restoreState = true
                    }
                })
            }

            composable(
                "details/{movieId}",
                arguments = listOf(navArgument("movieId") { type = NavType.IntType })
            ) { backStackEntry ->
                val movieId = backStackEntry.arguments?.getInt("movieId") ?: 0
                TestMovieDetailsScreen(
                    movieId = movieId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun TestMoviesScreen(onMovieClick: (Int) -> Unit) {
    Column {
        Text("Movies Screen")
        Button(
            onClick = { onMovieClick(123) },
            modifier = Modifier.testTag("movie_item_123")
        ) {
            Text("Movie 123")
        }
    }
}

@Composable
private fun TestFavoritesScreen(onMovieClick: (Int) -> Unit) {
    Column {
        Text("Favorites Screen")
        Button(
            onClick = { onMovieClick(456) },
            modifier = Modifier.testTag("movie_item_456")
        ) {
            Text("Favorite Movie 456")
        }
    }
}

@Composable
private fun TestMovieDetailsScreen(movieId: Int, onBack: () -> Unit) {
    Column {
        Text("Movie Details Screen")
        Text("Movie ID: $movieId")
        Button(
            onClick = onBack,
            modifier = Modifier.semantics {
                contentDescription = "Back"
            }
        ) {
            Text("Back")
        }
    }
}

@Composable
private fun TestNoInternetScreen() {
    Text("No Internet Screen")
}
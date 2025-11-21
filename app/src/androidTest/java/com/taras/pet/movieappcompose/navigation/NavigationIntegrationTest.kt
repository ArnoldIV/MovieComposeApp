package com.taras.pet.movieappcompose.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.taras.pet.movieappcompose.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationIntegrationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun navigationTest_verifyAppLaunches() {
        // Verify the app launches and shows the main UI elements
        composeTestRule
            .onNodeWithText("MovieApp")
            .assertIsDisplayed()
    }

    @Test
    fun navigationTest_bottomNavigationVisible() {
        // Test that bottom navigation items are present
        composeTestRule
            .onNodeWithText("Movies")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Favorite")
            .assertIsDisplayed()
    }

    @Test
    fun navigationTest_navigateToFavoritesAndBack() {
        // Wait for initial load
        composeTestRule.waitForIdle()

        // Click on Favorites in bottom navigation
        composeTestRule
            .onNodeWithText("Favorite")
            .performClick()

        // Wait for navigation to complete
        composeTestRule.waitForIdle()

        // Navigate back to Movies
        composeTestRule
            .onNodeWithText("Movies")
            .performClick()

        // Wait for navigation to complete
        composeTestRule.waitForIdle()

        // Should still be functional
        composeTestRule
            .onNodeWithText("Movies")
            .assertIsDisplayed()
    }

    @Test
    fun navigationTest_topAppBarAlwaysVisible() {
        // Top app bar should be visible on main screens
        composeTestRule
            .onNodeWithText("MovieApp")
            .assertIsDisplayed()

        // Wait for load
        composeTestRule.waitForIdle()

        // Navigate to favorites
        composeTestRule
            .onNodeWithText("Favorite")
            .performClick()

        composeTestRule.waitForIdle()

        // Top app bar should still be visible
        composeTestRule
            .onNodeWithText("MovieApp")
            .assertIsDisplayed()
    }

    @Test
    fun navigationTest_preventDuplicateBottomNavClicks() {
        // Wait for initial load
        composeTestRule.waitForIdle()

        // Click Movies tab multiple times rapidly
        repeat(3) {
            composeTestRule
                .onNodeWithText("Movies")
                .performClick()
        }

        composeTestRule.waitForIdle()

        // Should still be functional
        composeTestRule
            .onNodeWithText("Movies")
            .assertIsDisplayed()

        // Should be able to navigate to other screens
        composeTestRule
            .onNodeWithText("Favorite")
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("Favorite")
            .assertIsDisplayed()
    }

    @Test
    fun navigationTest_topAppBarElementsVisible() {
        // Test that top app bar elements are visible
        composeTestRule
            .onNodeWithText("MovieApp")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("mainMenuIcon")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("more actions icon")
            .assertIsDisplayed()
    }

    @Test
    fun navigationTest_bottomNavigationPersistence() {
        // Test that bottom navigation remains consistent across navigation
        composeTestRule.waitForIdle()

        // Initial state - both tabs visible
        composeTestRule
            .onNodeWithText("Movies")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Favorite")
            .assertIsDisplayed()

        // Navigate to favorites
        composeTestRule
            .onNodeWithText("Favorite")
            .performClick()

        composeTestRule.waitForIdle()

        // Both tabs should still be visible
        composeTestRule
            .onNodeWithText("Movies")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Favorite")
            .assertIsDisplayed()

        // Navigate back to movies
        composeTestRule
            .onNodeWithText("Movies")
            .performClick()

        composeTestRule.waitForIdle()

        // Both tabs should still be visible
        composeTestRule
            .onNodeWithText("Movies")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Favorite")
            .assertIsDisplayed()
    }
}
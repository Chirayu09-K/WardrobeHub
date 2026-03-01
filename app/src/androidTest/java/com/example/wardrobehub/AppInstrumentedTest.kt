package com.example.wardrobehub

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testNavigationToDashboardAndAddItem() {
        authenticate()

        composeRule.onNodeWithTag("add_item_action_card").performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag("item_name_field").assertIsDisplayed()
        composeRule.onNodeWithTag("item_name_field").performTextInput("Automated Test Shirt")
        composeRule.onNodeWithTag("item_color_field").performTextInput("Green")
        
        // Select a category
        composeRule.onNodeWithTag("category_chip_Tops").performClick()
        
        composeRule.onNodeWithTag("cancel_button").performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag("stats_card").assertIsDisplayed()
    }

    @Test
    fun testWardrobeSearchFlow() {
        authenticate()

        composeRule.onNodeWithTag("wardrobe_action_card").performClick()
        composeRule.waitForIdle()
        
        // Wait for screen content (either grid or empty message)
        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule.onAllNodesWithTag("wardrobe_grid").fetchSemanticsNodes().isNotEmpty() ||
            composeRule.onAllNodesWithText("Your closet is empty").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("search_toggle_button").performClick()
        composeRule.onNodeWithTag("search_field").performTextInput("Blue")
        composeRule.onNodeWithTag("search_field").assertTextContains("Blue")
        
        // Close search
        composeRule.onNodeWithTag("wardrobe_back_button").performClick() 
        composeRule.waitForIdle()
        
        // Go back to dashboard
        composeRule.onNodeWithTag("wardrobe_back_button").performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag("welcome_username").assertIsDisplayed()
    }

    @Test
    fun testCategoryFiltering() {
        authenticate()

        composeRule.onNodeWithTag("wardrobe_action_card").performClick()
        composeRule.waitForIdle()

        // Test category tab clicks
        composeRule.onNodeWithTag("category_tab_Tops").performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag("category_tab_Bottoms").performClick()
        composeRule.waitForIdle()
        
        composeRule.onNodeWithTag("category_tab_all").performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun testLogoutFlow() {
        authenticate()

        composeRule.onNodeWithTag("logout_button").performClick()
        composeRule.waitForIdle()

        // Verify we are back on the login screen
        composeRule.onNodeWithTag("login_button").assertIsDisplayed()
    }

    @Test
    fun testDeleteConfirmationDialog() {
        authenticate()

        composeRule.onNodeWithTag("wardrobe_action_card").performClick()
        composeRule.waitForIdle()

        // Try to find any delete button if items exist
        val deleteButtons = composeRule.onAllNodes(SemanticsMatcher("Test tag starts with") { 
            it.config.getOrNull(SemanticsProperties.TestTag)?.startsWith("delete_button_") == true 
        })
        
        if (deleteButtons.fetchSemanticsNodes().isNotEmpty()) {
            deleteButtons[0].performClick()
            composeRule.waitForIdle()
            
            // Check if dialog is shown
            composeRule.onNodeWithText("Delete Item?").assertIsDisplayed()
            composeRule.onNodeWithTag("cancel_delete_button").performClick()
            composeRule.waitForIdle()
        }
    }

    private fun authenticate() {
        composeRule.waitUntil(timeoutMillis = 20000) {
            composeRule.onAllNodesWithTag("login_button").fetchSemanticsNodes().isNotEmpty() ||
            composeRule.onAllNodesWithTag("stats_card").fetchSemanticsNodes().isNotEmpty()
        }

        if (composeRule.onAllNodesWithTag("login_button").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithTag("email_field").performTextReplacement("test@gmail.com")
            composeRule.onNodeWithTag("password_field").performTextReplacement("123456")
            
            composeRule.onNodeWithTag("login_button").performClick()
            
            composeRule.waitUntil(timeoutMillis = 15000) {
                composeRule.onAllNodesWithTag("stats_card").fetchSemanticsNodes().isNotEmpty()
            }
        }
    }
}
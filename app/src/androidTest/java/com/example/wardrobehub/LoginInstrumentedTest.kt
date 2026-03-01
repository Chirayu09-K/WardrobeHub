package com.example.wardrobehub

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testLoginValidation() {
        composeRule.waitUntil(timeoutMillis = 15000) {
            composeRule.onAllNodesWithTag("login_button").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("email_field").performTextInput("invalid-email")
        composeRule.onNodeWithTag("password_field").performTextInput("123")
        composeRule.onNodeWithTag("login_button").performClick()
        
        composeRule.onNodeWithTag("login_button").assertIsDisplayed()
    }

    @Test
    fun testNavigateToRegister() {
        composeRule.waitUntil(timeoutMillis = 15000) {
            composeRule.onAllNodesWithTag("register_button").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("register_button").performClick()
        composeRule.onNodeWithTag("register_username_field").assertIsDisplayed()
    }
}
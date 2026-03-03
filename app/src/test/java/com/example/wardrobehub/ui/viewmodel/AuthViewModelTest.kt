package com.example.wardrobehub.ui.viewmodel

import com.example.wardrobehub.model.User
import com.example.wardrobehub.repository.AuthRepository
import com.example.wardrobehub.utils.AuthUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @Mock
    private lateinit var authRepository: AuthRepository
    private lateinit var authViewModel: AuthViewModel
    
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        authViewModel = AuthViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login success sets Success state`() = runTest {
        val user = User("123", "testuser", "test@example.com")
        whenever(authRepository.login("test@example.com", "password"))
            .thenReturn(flowOf(Result.success(user)))

        authViewModel.login("test@example.com", "password")
        
        // Advance time to allow coroutine to execute
        advanceUntilIdle()

        val state = authViewModel.loginState.value
        assertTrue(state is AuthUiState.Success)
        assertEquals(user, (state as AuthUiState.Success).data)
    }

    @Test
    fun `login failure sets Error state`() = runTest {
        val exception = Exception("Login Failed")
        whenever(authRepository.login("test@example.com", "password"))
            .thenReturn(flowOf(Result.failure(exception)))

        authViewModel.login("test@example.com", "password")
        
        advanceUntilIdle()

        val state = authViewModel.loginState.value
        assertTrue(state is AuthUiState.Error)
        assertEquals("Login Failed", (state as AuthUiState.Error).exception.message)
    }

    @Test
    fun `resetAuthState resets all states to Idle`() = runTest {

        whenever(authRepository.login("test@example.com", "password"))
            .thenReturn(flowOf(Result.success(User())))
        authViewModel.login("test@example.com", "password")
        advanceUntilIdle()

        authViewModel.resetAuthState()

        assertEquals(AuthUiState.Idle, authViewModel.loginState.value)
        assertEquals(AuthUiState.Idle, authViewModel.registerState.value)
        assertEquals(AuthUiState.Idle, authViewModel.passwordResetState.value)
    }
}
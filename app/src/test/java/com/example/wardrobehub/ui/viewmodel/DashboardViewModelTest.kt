package com.example.wardrobehub.ui.viewmodel

import com.example.wardrobehub.model.ClothingItem
import com.example.wardrobehub.model.User
import com.example.wardrobehub.repository.AuthRepository
import com.example.wardrobehub.repository.UserRepository
import com.example.wardrobehub.repository.WardrobeRepository
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
class DashboardViewModelTest {

    @Mock
    private lateinit var authRepository: AuthRepository
    @Mock
    private lateinit var userRepository: UserRepository
    @Mock
    private lateinit var wardrobeRepository: WardrobeRepository
    
    private lateinit var dashboardViewModel: DashboardViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        // Mock default behavior for init
        val user = User("123", "testuser", "test@example.com")
        whenever(authRepository.getCurrentUser()).thenReturn(user)
        whenever(userRepository.getUser("123")).thenReturn(flowOf(Result.success(user)))
        whenever(wardrobeRepository.getWardrobeItems("123")).thenReturn(flowOf(Result.success(emptyList())))
        
        dashboardViewModel = DashboardViewModel(authRepository, userRepository, wardrobeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init fetches user successfully`() = runTest {
        advanceUntilIdle()
        
        val state = dashboardViewModel.userState.value
        assertTrue(state is AuthUiState.Success)
        assertEquals("testuser", (state as AuthUiState.Success).data.username)
    }

    @Test
    fun `init fetches wardrobe items successfully`() = runTest {
        val items = listOf(ClothingItem(id = "1", name = "Shirt"))
        whenever(wardrobeRepository.getWardrobeItems("123")).thenReturn(flowOf(Result.success(items)))
        
        // Re-init to trigger fetch
        dashboardViewModel = DashboardViewModel(authRepository, userRepository, wardrobeRepository)
        advanceUntilIdle()

        val state = dashboardViewModel.wardrobeState.value
        assertTrue(state is AuthUiState.Success)
        assertEquals(1, (state as AuthUiState.Success).data.size)
    }
}
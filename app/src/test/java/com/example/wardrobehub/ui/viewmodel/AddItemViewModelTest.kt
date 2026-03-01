package com.example.wardrobehub.ui.viewmodel

import android.content.Context
import android.net.Uri
import com.example.wardrobehub.model.User
import com.example.wardrobehub.repository.AuthRepository
import com.example.wardrobehub.repository.ImageRepository
import com.example.wardrobehub.repository.WardrobeRepository
import com.example.wardrobehub.utils.AuthUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AddItemViewModelTest {

    @Mock
    private lateinit var authRepository: AuthRepository
    @Mock
    private lateinit var wardrobeRepository: WardrobeRepository
    @Mock
    private lateinit var imageRepository: ImageRepository
    @Mock
    private lateinit var context: Context
    @Mock
    private lateinit var uri: Uri

    private lateinit var addItemViewModel: AddItemViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        addItemViewModel = AddItemViewModel(authRepository, wardrobeRepository, imageRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addClothingItem success with image`() = runTest {
        val user = User("123", "testuser", "test@example.com")
        whenever(authRepository.getCurrentUser()).thenReturn(user)
        
        // Mock image upload callback
        doAnswer { invocation ->
            val callback = invocation.getArgument<(String) -> Unit>(2)
            callback("http://image.url")
            null
        }.whenever(imageRepository).uploadImage(any(), any(), any())

        whenever(wardrobeRepository.addClothingItem(any(), any()))
            .thenReturn(Result.success(Unit))

        addItemViewModel.addClothingItem(context, "Shirt", "Tops", "Blue", uri)
        
        advanceUntilIdle()

        assertTrue(addItemViewModel.addItemState.value is AuthUiState.Success)
    }

    @Test
    fun `addClothingItem failure without user`() = runTest {
        whenever(authRepository.getCurrentUser()).thenReturn(null)

        addItemViewModel.addClothingItem(context, "Shirt", "Tops", "Blue", null)
        
        advanceUntilIdle()

        val state = addItemViewModel.addItemState.value
        assertTrue(state is AuthUiState.Error)
        assertEquals("User not logged in", (state as AuthUiState.Error).exception.message)
    }
}
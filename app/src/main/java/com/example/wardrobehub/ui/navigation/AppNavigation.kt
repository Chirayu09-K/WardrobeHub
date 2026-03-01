package com.example.wardrobehub.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.wardrobehub.ui.screens.*
import com.example.wardrobehub.ui.viewmodel.*
import com.example.wardrobehub.utils.AuthUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeHubApp(viewModelFactory: ViewModelFactory) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        NavHost(navController = navController, startDestination = "splash", modifier = Modifier.padding(paddingValues)) {
            composable("splash") {
                SplashScreen(onTimeout = { navController.navigate("login") { popUpTo("splash") { inclusive = true } } })
            }
            composable("login") {
                val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
                val loginState by authViewModel.loginState.collectAsState()

                LaunchedEffect(loginState) {
                    when (val state = loginState) {
                        is AuthUiState.Success -> {
                            navController.navigate("dashboard") { popUpTo("login") { inclusive = true } }
                            authViewModel.resetAuthState()
                        }
                        is AuthUiState.Error -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(state.exception.message ?: "An error occurred")
                            }
                            authViewModel.resetAuthState()
                        }
                        else -> Unit
                    }
                }

                LoginScreen(
                    onLogin = { email, password ->
                        authViewModel.login(email, password)
                    },
                    onNavigateToRegister = { navController.navigate("register") },
                    onNavigateToForgotPassword = { navController.navigate("forgot_password") },
                    authState = loginState
                )
            }
            composable("register") {
                val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
                val registerState by authViewModel.registerState.collectAsState()

                LaunchedEffect(registerState) {
                    when (val state = registerState) {
                        is AuthUiState.Success -> {
                            navController.navigate("dashboard") { popUpTo("login") { inclusive = true } }
                            authViewModel.resetAuthState()
                        }
                        is AuthUiState.Error -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(state.exception.message ?: "An error occurred")
                            }
                            authViewModel.resetAuthState()
                        }
                        else -> Unit
                    }
                }

                RegisterScreen(
                    onRegister = { username, email, password ->
                        authViewModel.register(username, email, password)
                    },
                    onNavigateToLogin = { navController.navigate("login") },
                    authState = registerState
                )
            }
            composable("forgot_password") {
                val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
                val passwordResetState by authViewModel.passwordResetState.collectAsState()

                LaunchedEffect(passwordResetState) {
                    when (val state = passwordResetState) {
                        is AuthUiState.Success -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Password reset link sent to your email")
                            }
                            navController.navigate("login")
                            authViewModel.resetAuthState()
                        }
                        is AuthUiState.Error -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(state.exception.message ?: "An error occurred")
                            }
                            authViewModel.resetAuthState()
                        }
                        else -> Unit
                    }
                }

                ForgotPasswordScreen(
                    onSendResetEmail = {
                        authViewModel.sendPasswordResetEmail(it)
                    },
                    onNavigateToLogin = { navController.navigate("login") },
                    authState = passwordResetState
                )
            }
            composable("dashboard") {
                val dashboardViewModel: DashboardViewModel = viewModel(factory = viewModelFactory)
                val userState by dashboardViewModel.userState.collectAsState()
                val wardrobeState by dashboardViewModel.wardrobeState.collectAsState()

                when (val state = userState) {
                    is AuthUiState.Success -> {
                        val itemCount = if (wardrobeState is AuthUiState.Success<*>) {
                            (wardrobeState as AuthUiState.Success<List<*>>).data.size
                        } else {
                            0
                        }
                        DashboardScreen(
                            username = state.data.username,
                            itemCount = itemCount,
                            onGoToWardrobe = { navController.navigate("wardrobe") },
                            onAddNewItem = { navController.navigate("add_item") },
                            onLogout = {
                                dashboardViewModel.logout()
                                navController.navigate("login") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }
                    is AuthUiState.Error -> {
                        //Navigate back to login here
                    }
                    is AuthUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    else -> Unit
                }
            }
            composable("add_item") {
                val addItemViewModel: AddItemViewModel = viewModel(factory = viewModelFactory)
                val addItemState by addItemViewModel.addItemState.collectAsState()
                val context = LocalContext.current

                LaunchedEffect(addItemState) {
                    when (val state = addItemState) {
                        is AuthUiState.Success -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Item added successfully!")
                            }
                            navController.popBackStack()
                            addItemViewModel.resetState()
                        }
                        is AuthUiState.Error -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(state.exception.message ?: "An error occurred")
                            }
                            addItemViewModel.resetState()
                        }
                        else -> Unit
                    }
                }

                AddItemScreen(
                    onSaveItem = { name, category, color, imageUri ->
                        addItemViewModel.addClothingItem(context, name, category, color, imageUri)
                    },
                    onNavigateBack = { navController.popBackStack() },
                    addItemState = addItemState
                )
            }
            composable("wardrobe") {
                val wardrobeViewModel: WardrobeViewModel = viewModel(factory = viewModelFactory)
                val wardrobeState by wardrobeViewModel.filteredWardrobeState.collectAsState()
                val selectedCategory by wardrobeViewModel.selectedCategory.collectAsState()
                val searchQuery by wardrobeViewModel.searchQuery.collectAsState()

                WardrobeScreen(
                    wardrobeState = wardrobeState,
                    selectedCategory = selectedCategory,
                    searchQuery = searchQuery,
                    onSearchQueryChanged = { wardrobeViewModel.onSearchQueryChanged(it) },
                    onCategorySelected = { wardrobeViewModel.selectCategory(it) },
                    onDeleteItem = { wardrobeViewModel.deleteClothingItem(it) },
                    onNavigateBack = { navController.popBackStack() },
                    onEditItem = { navController.navigate("edit_item/$it") }
                )
            }
            composable(
                "edit_item/{itemId}",
                arguments = listOf(navArgument("itemId") { type = NavType.StringType })
            ) {
                val itemId = it.arguments?.getString("itemId") ?: ""
                val editItemViewModel: EditItemViewModel = viewModel(factory = viewModelFactory)
                val editItemState by editItemViewModel.editItemState.collectAsState()
                val itemState by editItemViewModel.itemState.collectAsState()
                val context = LocalContext.current

                LaunchedEffect(Unit) {
                    editItemViewModel.getItem(itemId)
                }

                LaunchedEffect(editItemState) {
                    when (val state = editItemState) {
                        is AuthUiState.Success -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Item updated successfully!")
                            }
                            navController.popBackStack()
                            editItemViewModel.resetState()
                        }
                        is AuthUiState.Error -> {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(state.exception.message ?: "An error occurred")
                            }
                            editItemViewModel.resetState()
                        }
                        else -> Unit
                    }
                }

                EditItemScreen(
                    item = itemState,
                    onSaveItem = { name, category, color, imageUri ->
                        editItemViewModel.updateClothingItem(context, itemId, name, category, color, imageUri)
                    },
                    onNavigateBack = { navController.popBackStack() },
                    editItemState = editItemState
                )
            }
        }
    }
}
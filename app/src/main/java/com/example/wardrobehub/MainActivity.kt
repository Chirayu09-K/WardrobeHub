package com.example.wardrobehub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wardrobehub.repository.AuthRepositoryImpl
import com.example.wardrobehub.repository.UserRepositoryImpl
import com.example.wardrobehub.ui.auth.*
import com.example.wardrobehub.ui.dashboard.DashboardScreen
import com.example.wardrobehub.ui.theme.WardrobeHubTheme
import com.example.wardrobehub.ui.viewmodel.AuthViewModel
import com.example.wardrobehub.ui.viewmodel.DashboardViewModel
import com.example.wardrobehub.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val authRepository by lazy { AuthRepositoryImpl() }
    private val userRepository by lazy { UserRepositoryImpl() }
    private val viewModelFactory by lazy { ViewModelFactory(authRepository, userRepository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WardrobeHubTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    WardrobeHubApp(viewModelFactory)
                }
            }
        }
    }
}

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

                when (val state = userState) {
                    is AuthUiState.Success -> {
                        DashboardScreen(
                            username = state.data.username,
                            itemCount = 0,
                            onGoToWardrobe = { },
                            onAddNewItem = { },
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
        }
    }
}
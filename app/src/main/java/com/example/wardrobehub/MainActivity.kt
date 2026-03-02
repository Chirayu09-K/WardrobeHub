package com.example.wardrobehub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.wardrobehub.repository.AuthRepositoryImpl
import com.example.wardrobehub.repository.ImageRepositoryImpl
import com.example.wardrobehub.repository.UserRepositoryImpl
import com.example.wardrobehub.repository.WardrobeRepositoryImpl
import com.example.wardrobehub.ui.navigation.WardrobeHubApp
import com.example.wardrobehub.ui.theme.WardrobeHubTheme
import com.example.wardrobehub.ui.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {

    private val authRepository by lazy { AuthRepositoryImpl() }
    private val userRepository by lazy { UserRepositoryImpl() }
    private val wardrobeRepository by lazy { WardrobeRepositoryImpl() }
    private val imageRepository by lazy { ImageRepositoryImpl() }
    private val viewModelFactory by lazy { ViewModelFactory(authRepository, userRepository, wardrobeRepository, imageRepository) }

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
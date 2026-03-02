package com.example.wardrobehub.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.wardrobehub.R
import com.example.wardrobehub.ui.theme.WardrobeHubTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        onTimeout()
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(painter = painterResource(id = R.drawable.app_logo), contentDescription = "App Logo")
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    WardrobeHubTheme {
        SplashScreen(onTimeout = {})
    }
}
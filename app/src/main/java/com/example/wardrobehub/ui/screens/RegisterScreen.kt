package com.example.wardrobehub.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wardrobehub.R
import com.example.wardrobehub.utils.AuthUiState

@Composable
fun RegisterScreen(
    onRegister: (String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    authState: AuthUiState<*>
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val isLoading = authState is AuthUiState.Loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(id = R.drawable.app_logo), contentDescription = "App Logo")
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth().testTag("register_username_field"),
            enabled = !isLoading,
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth().testTag("register_email_field"),
            enabled = !isLoading,
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth().testTag("register_password_field"),
            enabled = !isLoading,
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onRegister(username, email, password)
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().testTag("register_button"),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Register", style = MaterialTheme.typography.bodyLarge)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onNavigateToLogin, enabled = !isLoading, modifier = Modifier.testTag("login_link_button")) {
            Text("Already have an account? Login")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(
        onRegister = { _, _, _ -> },
        onNavigateToLogin = {},
        authState = AuthUiState.Idle
    )
}
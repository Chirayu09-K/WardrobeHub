package com.example.wardrobehub.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wardrobehub.R
import com.example.wardrobehub.ui.theme.WardrobeHubTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    username: String,
    itemCount: Int,
    onGoToWardrobe: () -> Unit,
    onAddNewItem: () -> Unit,
    onLogout: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(it)
        ) {
            Header()
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Welcome, $username!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            QuickStatsCard(itemCount = itemCount)
            Spacer(modifier = Modifier.height(16.dp))
            MainActionButtons(
                onGoToWardrobe = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Coming Soon!")
                    }
                },
                onAddNewItem = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Coming Soon!")
                    }
                }
            )
            Spacer(modifier = Modifier.weight(1f))
            SecondaryActions(onLogout = onLogout)
        }
    }
}

@Composable
fun Header() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(painter = painterResource(id = R.drawable.app_logo), contentDescription = "App Logo")
    }
}

@Composable
fun QuickStatsCard(itemCount: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "👕 Total Items", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = itemCount.toString(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 48.sp
            )
        }
    }
}

@Composable
fun MainActionButtons(onGoToWardrobe: () -> Unit, onAddNewItem: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Button(
            onClick = onGoToWardrobe,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Checkroom, contentDescription = "View Wardrobe")
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "View Wardrobe", style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onAddNewItem,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add New Item")
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Add New Item", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun SecondaryActions(onLogout: () -> Unit) {
    TextButton(
        onClick = onLogout,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout")
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Logout", style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    WardrobeHubTheme {
        DashboardScreen(
            username = "Chirayu",
            itemCount = 12,
            onGoToWardrobe = {},
            onAddNewItem = {},
            onLogout = {}
        )
    }
}
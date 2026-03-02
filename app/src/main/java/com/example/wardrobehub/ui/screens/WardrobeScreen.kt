package com.example.wardrobehub.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.wardrobehub.model.ClothingItem
import com.example.wardrobehub.ui.theme.WardrobeHubTheme
import com.example.wardrobehub.utils.AuthUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WardrobeScreen(
    wardrobeState: AuthUiState<List<ClothingItem>>,
    selectedCategory: String?,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onDeleteItem: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onEditItem: (String) -> Unit
) {
    var isSearching by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<String?>(null) }

    // Delete Confirmation Dialog
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Item?") },
            text = { Text("Are you sure you want to remove this from your wardrobe? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        itemToDelete?.let { onDeleteItem(it) }
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }, modifier = Modifier.testTag("cancel_delete_button")) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearching) {
                        TextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChanged,
                            placeholder = { Text("Search by name or color...") },
                            modifier = Modifier.fillMaxWidth().testTag("search_field"),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    } else {
                        Text("Wardrobe", fontWeight = FontWeight.ExtraBold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (isSearching) {
                            isSearching = false
                            onSearchQueryChanged("")
                        } else {
                            onNavigateBack()
                        }
                    }, modifier = Modifier.testTag("wardrobe_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isSearching = !isSearching }, modifier = Modifier.testTag("search_toggle_button")) {
                        Icon(
                            imageVector = if (isSearching) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (wardrobeState) {
                is AuthUiState.Success -> {
                    val items = wardrobeState.data
                    val categories = items.map { it.category }.distinct()
                    
                    CategoryTabs(categories, selectedCategory, onCategorySelected)
                    
                    if (items.isEmpty()) {
                        EmptyWardrobeMessage(isFiltering = selectedCategory != null || searchQuery.isNotEmpty())
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize().testTag("wardrobe_grid")
                        ) {
                            itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(animationSpec = tween(300, delayMillis = index * 50)) + 
                                            slideInVertically(initialOffsetY = { 50 })
                                ) {
                                    ClothingItemCard(
                                        item = item, 
                                        onDeleteClick = { itemToDelete = item.id }, 
                                        onEditClick = { onEditItem(item.id) }
                                    )
                                }
                            }
                        }
                    }
                }
                is AuthUiState.Error -> {
                    ErrorMessage(message = wardrobeState.exception.message ?: "Sync Error")
                }
                is AuthUiState.Loading -> {
                    LoadingState()
                }
                else -> Unit
            }
        }
    }
}

@Composable
fun ClothingItemCard(item: ClothingItem, onDeleteClick: () -> Unit, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .testTag("clothing_item_card_${item.id}"),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            SubcomposeAsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                },
                error = {
                    Box(
                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.BrokenImage, contentDescription = null, tint = Color.LightGray)
                    }
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 350f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.color,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            Surface(
                modifier = Modifier.padding(12.dp).align(Alignment.TopStart),
                color = Color.Black.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = item.category,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color.White
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ActionButton(icon = Icons.Default.Edit, onClick = onEditClick, containerColor = Color.White.copy(alpha = 0.9f), tag = "edit_button_${item.id}")
                ActionButton(icon = Icons.Default.Delete, onClick = onDeleteClick, containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f), tag = "delete_button_${item.id}")
            }
        }
    }
}

@Composable
fun ActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, containerColor: Color, tag: String) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(32.dp)
            .background(containerColor, CircleShape)
            .testTag(tag)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun CategoryTabs(categories: List<String>, selectedCategory: String?, onCategorySelected: (String?) -> Unit) {
    ScrollableTabRow(
        selectedTabIndex = if (selectedCategory == null) 0 else categories.indexOf(selectedCategory) + 1,
        edgePadding = 16.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        divider = {},
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(tabPositions[if (selectedCategory == null) 0 else categories.indexOf(selectedCategory) + 1]),
                height = 3.dp,
                color = MaterialTheme.colorScheme.primary
            )
        },
        modifier = Modifier.testTag("category_tabs")
    ) {
        Tab(
            selected = selectedCategory == null,
            onClick = { onCategorySelected(null) },
            text = { Text("All", style = MaterialTheme.typography.titleSmall) },
            modifier = Modifier.testTag("category_tab_all")
        )
        categories.forEach { category ->
            Tab(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                text = { Text(category, style = MaterialTheme.typography.titleSmall) },
                modifier = Modifier.testTag("category_tab_$category")
            )
        }
    }
}

@Composable
fun EmptyWardrobeMessage(isFiltering: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Checkroom, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                if (isFiltering) "No matches found" else "Your closet is empty",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ErrorMessage(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(strokeWidth = 3.dp)
    }
}

@Preview(showBackground = true)
@Composable
fun WardrobeScreenPreview() {
    WardrobeHubTheme {
        WardrobeScreen(
            wardrobeState = AuthUiState.Success(emptyList()),
            selectedCategory = null,
            searchQuery = "",
            onSearchQueryChanged = {},
            onCategorySelected = {},
            onDeleteItem = {},
            onNavigateBack = {},
            onEditItem = {}
        )
    }
}
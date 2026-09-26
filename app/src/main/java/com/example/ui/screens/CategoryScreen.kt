package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.BannerAdView
import com.example.model.GameCategory
import com.example.ui.components.CategoryCard
import com.example.ui.components.EmptyStateView
import com.example.ui.components.TopBar
import com.example.ui.theme.*

@Composable
fun CategoryScreen(
    onCategorySelected: (GameCategory) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    var searchQuery by remember { mutableStateOf("") }

    val categories = remember {
        listOf(
            GameCategory.MATH,
            GameCategory.NUMBERS,
            GameCategory.LOGIC,
            GameCategory.WORDS,
            GameCategory.KNOWLEDGE,
            GameCategory.SCIENCE,
            GameCategory.PATTERNS,
            GameCategory.SPEED
        )
    }

    val filteredCategories = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            categories
        } else {
            categories.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.shortDescription.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopBar(
                title = "CHOOSE CATEGORY",
                onBack = onBack
            )
        },
        modifier = modifier.testTag("category_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Search field (Requirement 21)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search categories...", color = TextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = NeonCyan)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CardSurfaceBorder,
                        focusedContainerColor = CardSurface,
                        unfocusedContainerColor = CardSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_search_input")
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (filteredCategories.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Search,
                        title = "No Categories Found",
                        message = "No brain training categories match \"$searchQuery\". Try another term.",
                        actionButtonText = "Clear Search",
                        onActionClick = { searchQuery = "" }
                    )
                }
            } else {
                items(filteredCategories) { category ->
                    CategoryCard(
                        category = category,
                        bestScore = 620 + (category.ordinal * 120),
                        onClick = { onCategorySelected(category) }
                    )
                }
            }

            item {
                BannerAdView(modifier = Modifier.padding(top = 12.dp))
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

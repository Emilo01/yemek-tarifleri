package com.farukayata.yemektarifi.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.farukayata.yemektarifi.data.remote.model.RecipeItem
import com.farukayata.yemektarifi.data.remote.ui.components.NutritionalChart.NutritionalChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRecipeDetailScreen(
    recipe: RecipeItem,
    onBack: () -> Unit
) {
    val scrollState = rememberLazyListState()
    val imageHeight = 300.dp

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(imageHeight)
                ) {
                    AsyncImage(
                        model = recipe.imageUrl,
                        contentDescription = "Tarif görseli",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.3f),
                        color = Color.Black
                    ) {}

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .alpha(0.8f),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = recipe.name,
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Bölge",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (recipe.region.isNotBlank()) recipe.region else "Bölge bilgisi yok",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Süre",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (recipe.duration.isNotBlank()) recipe.duration else "Süre bilgisi yok",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "⚠️ Eksik Ürünler",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (recipe.missingIngredients.isNotEmpty()) {
                            recipe.missingIngredients.forEach { missing ->
                                Text("• $missing")
                            }
                        } else {
                            Text("Eksik Ürün yok ✅")
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "👨‍🍳 Hazırlanışı",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (recipe.description.isNotBlank()) {
                            Text(recipe.description)
                        } else {
                            Text("Hazırlık aşamaları bulunamadı.")
                        }
                    }
                }
            }

            if (recipe.ingredientDetails.isNotBlank()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "🧂 Malzeme Kullanım Detayı",
                                style = MaterialTheme.typography.titleMedium
                            )
                            recipe.ingredientDetails
                                .split("\n")
                                .map { it.trim() }
                                .filter { it.isNotBlank() && it.trim() != "-" }
                                .forEach { line ->
                                    val cleanedLine = line.removePrefix("-").removePrefix("•").trim()
                                    Text("• $cleanedLine")
                                }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    NutritionalChart(
                        nutritionalValues = recipe.nutritionalValues,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "🛒 Malzemeler",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (recipe.ingredients.isNotEmpty()) {
                            recipe.ingredients.forEach { ing ->
                                Text("• $ing")
                            }
                        } else {
                            Text("Malzeme listesi bulunamadı.")
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("📝 Alışveriş Listesi")
                    }
                }
            }
        }
    }
}
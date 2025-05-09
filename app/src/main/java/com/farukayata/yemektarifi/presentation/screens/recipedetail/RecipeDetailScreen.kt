package com.farukayata.yemektarifi.presentation.screens.recipedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.farukayata.yemektarifi.data.remote.model.RecipeItem
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage



@Composable
fun RecipeDetailScreen(recipe: RecipeItem) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            if (recipe.name.isNotBlank()) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "Tarif adı bulunamadı",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }

        item {
            AsyncImage(
                model = recipe.imageUrl,
                contentDescription = "Tarif görseli",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "📍 ${if (recipe.region.isNotBlank()) recipe.region else "Bölge bilgisi yok"}")
                Text(text = "⏱️ ${if (recipe.duration.isNotBlank()) recipe.duration else "Süre bilgisi yok"}")
            }
        }

        item {
            if (recipe.missingIngredients.isNotEmpty()) {
                Text("⚠️ Eksik Ürünler:", style = MaterialTheme.typography.titleMedium)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    recipe.missingIngredients.forEach { missing ->
                        Text("• $missing")
                    }
                }
            } else {
                Text("Eksik Ürün yok ✅", style = MaterialTheme.typography.titleMedium)
            }
        }

        item {
            Text("👨‍🍳 Hazırlanışı:", style = MaterialTheme.typography.titleMedium)
            if (recipe.description.isNotBlank()) {
                Text(recipe.description)
            } else {
                Text("Hazırlık aşamaları bulunamadı.")
            }
        }

        if (recipe.ingredientDetails.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🧂 Malzeme Kullanım Detayı", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
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
        } else {
            item {
                Text(
                    text = "Uygun Malzeme kullanım detayı bulunamadı.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            Text("🛒 Malzemeler:", style = MaterialTheme.typography.titleMedium)
            if (recipe.ingredients.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    recipe.ingredients.forEach { ing ->
                        Text("• $ing")
                    }
                }
            } else {
                Text("Malzeme listesi bulunamadı.")
            }
        }

        // İleride eklenecek butonlar:
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {}) {
                    Text("📝 Alışveriş Listesi")
                }
                Button(onClick = {}) {
                    Text("❤️ Fav")
                }
            }
        }
    }
}

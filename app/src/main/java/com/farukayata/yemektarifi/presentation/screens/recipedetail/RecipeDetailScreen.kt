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
            Text(
                text = recipe.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
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
                Text(text = "📍 ${recipe.region}")
                Text(text = "⏱️ ${recipe.duration}")
            }
        }

        item {
            Text("👨‍🍳 Hazırlanışı:", style = MaterialTheme.typography.titleMedium)
            //Text(recipe.description)
            Text(if (recipe.description.isNotBlank()) recipe.description else "Tarif açıklaması bulunamadı.")

        }

        item {
            Text("🛒 Malzemeler:", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                recipe.ingredients.forEach { ing ->
                    Text("• $ing")
                }
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

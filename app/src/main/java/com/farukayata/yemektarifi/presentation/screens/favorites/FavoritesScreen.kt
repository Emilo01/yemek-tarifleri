package com.farukayata.yemektarifi.presentation.screens.favorites

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.farukayata.yemektarifi.data.remote.UserRepository
import com.farukayata.yemektarifi.data.remote.model.RecipeItem
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.Text
import com.farukayata.yemektarifi.data.remote.ui.components.BottomNavigationBar

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FavoritesScreen(
    userRepository: UserRepository,
    currentUserId: String,
    navController: NavController
) {
    var favorites by remember { mutableStateOf<List<RecipeItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentUserId) {
        scope.launch {
            userRepository.getFavoriteRecipesFromSubcollection(currentUserId).onSuccess {
                favorites = it
                isLoading = false
            }
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Scaffold(
            bottomBar = { BottomNavigationBar(navController) }
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favorites) { recipe ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(8.dp),
                        onClick = {
                            val encodedName = URLEncoder.encode(recipe.name, StandardCharsets.UTF_8.toString())
                            navController.navigate("recipeDetail/$encodedName")
                        }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (recipe.imageUrl.isNotBlank()) {
                                AsyncImage(
                                    model = recipe.imageUrl,
                                    contentDescription = "Yemeğin görseli",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Log.d("ImageCheck", "Gelen görsel URL: ${recipe.imageUrl}")

                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            Text(recipe.name, style = MaterialTheme.typography.titleMedium)
                            Row {
                                Text("⏱️ ${recipe.duration}")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("📍 ${recipe.region}")
                            }
                            if (recipe.summary.isNotBlank()) {
                                Text("✨ ${recipe.summary}", style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                text = if (recipe.missingIngredients.isEmpty()) "Eksik ürün yok ✅"
                                else "Eksik: ${recipe.missingIngredients.joinToString(", ")}"
                            )
                            if (recipe.ingredients.isNotEmpty()) {
                                Text("Malzemeler:\n${recipe.ingredients.joinToString(", ")}")
                            }
                        }
                    }
                }
            }
        }
    }
}
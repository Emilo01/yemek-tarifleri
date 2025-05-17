package com.farukayata.yemektarifi.presentation.screens.recipesuggest

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.farukayata.yemektarifi.data.remote.model.CategorizedItem
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import com.farukayata.yemektarifi.data.remote.model.RecipeItem
import com.farukayata.yemektarifi.presentation.screens.home.HomeViewModel
import com.farukayata.yemektarifi.presentation.screens.recipesuggestion.RecipeSuggestionViewModel

@Composable
fun RecipeSuggestionScreen(
    mealType: String,
    items: List<CategorizedItem>,
    viewModel: RecipeSuggestionViewModel = hiltViewModel(),
    navController: NavHostController,
    homeViewModel: HomeViewModel
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val recipes by viewModel.recipes.collectAsState()

    LaunchedEffect(mealType, items.map { it.name }) {
        // Eğer cachede tarifler varsa onları kullancaz
        if (homeViewModel.recipes.value.isEmpty()) {
            Log.d("RecipeFlow", "generateRecipes çağrıldı: $mealType / items=${items.map { it.name }}")
            viewModel.generateRecipes(mealType, items) { recipes ->
                Log.d("RecipeFlow", "Tarifler set ediliyor: ${recipes.map { it.name }}")
                homeViewModel.setRecipes(recipes)
            }
        } else {
            // Cachedeki tarifleri kullandık
            homeViewModel.loadCachedRecipes()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LottieAnimation(
                        composition = rememberLottieComposition(LottieCompositionSpec.Asset("yemekler_tencere_loading.json")).value,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.size(180.dp)
                    )
                }
            }

            recipes.isEmpty() -> {
                Text(
                    text = "Henüz tarif oluşturulmadı.",
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recipes) { recipe ->
                        val encodedName = URLEncoder.encode(recipe.name, StandardCharsets.UTF_8.toString())
                        RecipeCard(recipe = recipe, onClick = {
                            navController.navigate("recipeDetail/$encodedName")
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeCard(recipe: RecipeItem,onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        //elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            AsyncImage(
                model = recipe.imageUrl,
                contentDescription = "Yemeğin görseli",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = recipe.name.replace("**", ""), style = MaterialTheme.typography.titleMedium)
            Text(text = "⏱️ ${recipe.duration.replace("**", "")}")
            Text(text = "📍 ${recipe.region.replace("**", "")}")

            Spacer(modifier = Modifier.height(8.dp))

            if (recipe.summary.isNotBlank()) {
                Text(
                    text = "✨ ${recipe.summary}",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
            }


            if (recipe.missingIngredients.isEmpty()) {
                Text(text = "Eksik ürün yok ✅")
            } else {
                Text(text = "⚠️ Eksik Ürünler: ${recipe.missingIngredients.joinToString(", ").replace("**", "")}")
            }


            Spacer(modifier = Modifier.height(8.dp))

            //Text(text = "Tarif:\n${recipe.description.replace("**", "")}")
            //Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Malzemeler:\n${recipe.ingredients.joinToString(", ").replace("**", "")}")
        }
    }
}
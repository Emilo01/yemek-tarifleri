package com.farukayata.yemektarifi.presentation.screens.result

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.farukayata.yemektarifi.data.remote.model.CategorizedItem
import com.farukayata.yemektarifi.data.remote.ui.components.CategoryCard
import com.farukayata.yemektarifi.data.remote.ui.components.LoadingAnimation
import androidx.compose.runtime.LaunchedEffect

@Composable
fun ResultScreen(
    navController: NavController,
    userEditedItems: List<CategorizedItem>, //listedeki herşey silinirse diye
    imageUri: Uri?,
    isLoading: Boolean,
    onBack: () -> Unit,
    startReAnalyze: () -> Unit
) {
    LaunchedEffect(Unit) {
        startReAnalyze()
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LoadingAnimation(modifier = Modifier.size(150.dp))
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                imageUri?.let {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = "Tarama Görseli",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (userEditedItems.isEmpty()) {
                    Text(
                        text = "Gösterilecek ürün bulunamadı.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }

            val categoryOrder = listOf(
                "Et ve Et Ürünleri",
                "Balık ve Deniz Ürünleri",
                "Yumurta ve Süt Ürünleri",
                "Tahıllar ve Unlu Mamuller",
                "Baklagiller",
                "Sebzeler",
                "Meyveler",
                "Baharatlar ve Tat Vericiler",
                "Yağlar ve Sıvılar",
                "Konserve ve Hazır Gıdalar",
                "Tatlı Malzemeleri ve Kuruyemişler"
            )

            val grouped = userEditedItems.groupBy { it.category }
            val sortedGroups = categoryOrder.mapNotNull { key -> grouped[key]?.let { key to it } }

            sortedGroups.forEach { (category, items) ->
                item {
                    CategoryCard(categoryName = category, items = items)
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                val editedItems = userEditedItems

                Button(
                    onClick = {
                        navController.currentBackStackEntry?.savedStateHandle?.set("editedItems", editedItems)
                        navController.navigate("mealType") // Bu route MealTypeScreene götürmeli
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "🍳 Hadi Yemek Pişirelim")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ana Sayfaya Dön")
                }
            }
        }
    }
}
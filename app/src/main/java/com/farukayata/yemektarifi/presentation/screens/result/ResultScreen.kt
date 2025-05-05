package com.farukayata.yemektarifi.presentation.screens.result

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.farukayata.yemektarifi.data.remote.model.CategorizedItem
import com.farukayata.yemektarifi.data.remote.ui.components.CategoryCard
import com.farukayata.yemektarifi.data.remote.ui.components.LoadingAnimation
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color

@Composable
fun ResultScreen(
    categorizedItems: List<CategorizedItem>,
    imageUri: Uri?,
    isLoading: Boolean,
    onBack: () -> Unit,
    startReAnalyze: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        LaunchedEffect(Unit) {
            startReAnalyze()
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingAnimation(modifier = Modifier.size(150.dp))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    imageUri?.let {
                        Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = "Tarama Görseli",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (categorizedItems.isEmpty()) {
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

                val grouped = categorizedItems.groupBy { it.category }
                val sortedGroups = categoryOrder.mapNotNull { key -> grouped[key]?.let { key to it } }

                sortedGroups.forEach { (category, items) ->
                    item {
                        CategoryCard(categoryName = category, items = items)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Ana Sayfaya Dön")
            }
        }
    }
}

package com.farukayata.yemektarifi.data.remote.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.EggAlt
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.Store


data class CategoryStyle(val color: Color, val icon: ImageVector)

val categoryStyles = mapOf(
    "Et ve Et Ürünleri" to CategoryStyle(Color(0xFFFFCDD2), Icons.Default.Restaurant),
    "Balık ve Deniz Ürünleri" to CategoryStyle(Color(0xFFB3E5FC), Icons.Default.SetMeal),
    "Yumurta ve Süt Ürünleri" to CategoryStyle(Color(0xFFFFF9C4), Icons.Default.EggAlt),
    "Tahıllar ve Unlu Mamuller" to CategoryStyle(Color(0xFFD7CCC8), Icons.Default.BakeryDining),
    "Baklagiller" to CategoryStyle(Color(0xFFE0F7FA), Icons.Default.Spa),
    "Sebzeler" to CategoryStyle(Color(0xFFC8E6C9), Icons.Default.Grass),
    "Meyveler" to CategoryStyle(Color(0xFFFFF3E0), Icons.Default.LocalGroceryStore),
    "Baharatlar ve Tat Vericiler" to CategoryStyle(Color(0xFFD1C4E9), Icons.Default.LocalDining),
    "Yağlar ve Sıvılar" to CategoryStyle(Color(0xFFFFECB3), Icons.Default.LocalDrink),
    "Konserve ve Hazır Gıdalar" to CategoryStyle(Color(0xFFE1BEE7), Icons.Default.Store), // dış ikon gerekiyorsa vector olarak eklenebilir
    "Tatlı Malzemeleri ve Kuruyemişler" to CategoryStyle(Color(0xFFFFE0B2), Icons.Default.Cookie)
)

package com.farukayata.yemektarifi.data.remote.model

data class RecipeItem(
    val name: String,
    val imageUrl: String,
    val duration: String,
    val region: String,
    val description: String,
    val ingredients: List<String>, //-> malzeme listesi
    val missingIngredients: List<String> = emptyList() //eksik ürü için
)

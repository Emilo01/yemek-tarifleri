package com.farukayata.yemektarifi.domain

import com.farukayata.yemektarifi.data.remote.model.RecipeItem

interface OpenAiRepository {
    suspend fun getRecipes(mealType: String, items: List<String>): List<RecipeItem>
}

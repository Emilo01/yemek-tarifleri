package com.farukayata.yemektarifi.presentation.screens.recipesuggestion

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farukayata.yemektarifi.data.remote.model.CategorizedItem
import com.farukayata.yemektarifi.data.remote.model.RecipeItem
import com.farukayata.yemektarifi.domain.OpenAiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeSuggestionViewModel @Inject constructor(
    private val openAiRepository: OpenAiRepository
) : ViewModel() {

    private val _recipes = MutableStateFlow<List<RecipeItem>>(emptyList())
    val recipes: StateFlow<List<RecipeItem>> = _recipes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /*
    fun generateRecipes(mealType: String, items: List<CategorizedItem>) {
        viewModelScope.launch {
            _isLoading.value = true

            val itemNames = items.map { it.name }
            val response = openAiRepository.getRecipes(mealType, itemNames)

            _recipes.value = response
            _isLoading.value = false
        }
    }

     */
    fun generateRecipes(mealType: String, items: List<CategorizedItem>, onResult: (List<RecipeItem>) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val itemNames = items.map { it.name }
            val response = openAiRepository.getRecipes(mealType, itemNames)

            //ilk tarifi kontrol
            if (response.isNotEmpty()) {
                Log.d("RecipeDebug", "OpenAI'dan gelen tarif açıklaması: ${response[0].description}")
            } else {
                Log.d("RecipeDebug", "OpenAI'dan tarif gelmedi.")
            }

            _recipes.value = response
            _isLoading.value = false
            onResult(response)
        }
    }

}

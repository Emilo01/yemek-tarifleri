package com.farukayata.yemektarifi.presentation.screens.mealtype

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MealTypeViewModel @Inject constructor() : ViewModel() {

    private val _selectedMealType = MutableStateFlow<String?>(null)
    val selectedMealType: StateFlow<String?> = _selectedMealType

    fun selectMealType(mealType: String) {
        _selectedMealType.value = mealType
    }

    fun resetMealType() {
        _selectedMealType.value = null
    }
}

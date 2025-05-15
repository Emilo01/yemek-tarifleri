package com.farukayata.yemektarifi.data.remote.model

data class User(
    val id: String = "",
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val gender: String = "",
    val age: Int = 0,
    val favoriteRecipes: List<String> = emptyList()
)
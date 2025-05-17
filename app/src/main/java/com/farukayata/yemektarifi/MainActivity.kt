package com.farukayata.yemektarifi

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.farukayata.yemektarifi.presentation.screens.home.HomeScreen
import com.farukayata.yemektarifi.ui.theme.YemekTarifiTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.farukayata.yemektarifi.presentation.screens.login.LoginScreen
import com.farukayata.yemektarifi.presentation.screens.mealtype.MealTypeScreen
import com.farukayata.yemektarifi.presentation.screens.profile.ProfileEditScreen
import com.farukayata.yemektarifi.presentation.screens.profile.ProfileScreen
import com.farukayata.yemektarifi.presentation.screens.recipedetail.RecipeDetailScreen
import com.farukayata.yemektarifi.presentation.screens.recipesuggest.RecipeSuggestionScreen
import com.farukayata.yemektarifi.presentation.screens.recipesuggestion.RecipeSuggestionViewModel
import com.farukayata.yemektarifi.presentation.screens.result.ResultScreen
import com.farukayata.yemektarifi.presentation.screens.singup.SignUpScreen
import com.farukayata.yemektarifi.data.remote.UserRepository
import com.farukayata.yemektarifi.data.remote.AuthRepository
import javax.inject.Inject
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.farukayata.yemektarifi.data.remote.model.RecipeItem
import com.farukayata.yemektarifi.presentation.screens.favorites.FavoritesScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YemekTarifiTheme {
                val navController = rememberNavController()
                val viewModel = hiltViewModel<com.farukayata.yemektarifi.presentation.screens.home.HomeViewModel>()
                val suggestionViewModel = hiltViewModel<RecipeSuggestionViewModel>()

                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        LoginScreen(
                            onNavigateToSignUp = { navController.navigate("signup") },
                            onLoginSuccess = { navController.navigate("home") }
                        )
                    }

                    composable("signup") {
                        SignUpScreen(
                            onNavigateToLogin = { navController.navigate("login") },
                            onSignUpSuccess = { navController.navigate("home") }
                        )
                    }

                    composable("home") {
                        HomeScreen(
                            modifier = Modifier.fillMaxSize(),
                            viewModel = viewModel,
                            navController = navController
                        )
                    }

                    composable("profile") {
                        ProfileScreen(
                            onNavigateToEditProfile = { navController.navigate("profile_edit") },
                            navController = navController
                        )
                    }

                    composable("profile_edit") {
                        ProfileEditScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("result") {
                        val imageUri = viewModel.selectedImageUri.collectAsState().value
                        val finalItems = viewModel.userEditedItems.collectAsState().value
                        val isLoading = viewModel.isResultLoading.collectAsState().value

                        ResultScreen(
                            navController = navController,
                            userEditedItems = finalItems,
                            imageUri = imageUri,
                            isLoading = isLoading,
                            onBack = {
                                navController.popBackStack()
                                viewModel.resetResultNavigation()
                            },
                            startReAnalyze = {
                                viewModel.startReAnalyze()
                            }
                        )
                    }

                    composable("mealType") {
                        MealTypeScreen(navController = navController)
                    }

                    composable(
                        route = "recipeSuggestion/{mealType}",
                        arguments = listOf(navArgument("mealType") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val mealTypeArg = backStackEntry.arguments?.getString("mealType") ?: ""
                        val ingredients = viewModel.userEditedItems.collectAsState().value

                        RecipeSuggestionScreen(
                            mealType = mealTypeArg,
                            items = ingredients,
                            navController = navController,
                            homeViewModel = viewModel
                        )
                    }

                    composable(
                        route = "recipeDetail/{recipeName}",
                        arguments = listOf(navArgument("recipeName") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val encodedName = backStackEntry.arguments?.getString("recipeName") ?: ""
                        val recipeName = URLDecoder.decode(encodedName, StandardCharsets.UTF_8.toString())
                        val homeRecipes = viewModel.recipes.collectAsState().value
                        val userId = authRepository.currentUser?.uid ?: ""
                        var recipe by remember { mutableStateOf<RecipeItem?>(null) }
                        var isLoading by remember { mutableStateOf(true) }

                        LaunchedEffect(recipeName, userId) {
                            val homeRecipe = homeRecipes.find { it.name == recipeName }
                            if (homeRecipe != null) {
                                recipe = homeRecipe
                                isLoading = false
                            } else if (userId.isNotBlank()) {
                                val result = userRepository.getFavoriteRecipesFromSubcollection(userId)
                                result.onSuccess { favs ->
                                    recipe = favs.find { it.name == recipeName }
                                }
                                isLoading = false
                            } else {
                                isLoading = false
                            }
                        }

                        when {
                            isLoading -> {
                                CircularProgressIndicator()
                            }
                            recipe != null -> {
                                RecipeDetailScreen(
                                    recipe = recipe!!,
                                    userRepository = userRepository,
                                    currentUserId = userId,
                                    onBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                            else -> {
                                Text("Tarif bulunamadı.")
                            }
                        }
                    }

                    composable("favorites") {
                        FavoritesScreen(
                            userRepository = userRepository,
                            currentUserId = authRepository.currentUser?.uid ?: "",
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}
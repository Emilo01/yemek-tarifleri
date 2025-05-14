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
import java.net.URLDecoder

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
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
                        val recipeName = URLDecoder.decode(backStackEntry.arguments?.getString("recipeName") ?: "", "UTF-8")
                        Log.d("RecipeFlow", "Detay ekranı açıldı, gelen ad: $recipeName")
                        val recipeList = viewModel.recipes.collectAsState().value
                        Log.d("RecipeFlow", "HomeViewModel'daki tarif listesi: ${recipeList.map { it.name }}")
                        val recipe = recipeList.find { it.name == recipeName }
                        Log.d("RecipeFlow", "Eşleşen tarif: ${recipe?.name}")

                        if (recipe != null) {
                            RecipeDetailScreen(recipe = recipe)
                        } else {
                            Text("Tarif bulunamadı: $recipeName")
                        }
                    }
                }
            }
        }
    }
}
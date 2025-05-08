package com.farukayata.yemektarifi

import android.os.Bundle
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
import com.farukayata.yemektarifi.presentation.screens.mealtype.MealTypeScreen
import com.farukayata.yemektarifi.presentation.screens.recipesuggest.RecipeSuggestionScreen
import com.farukayata.yemektarifi.presentation.screens.result.ResultScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YemekTarifiTheme {
                val navController = rememberNavController()
                val viewModel = hiltViewModel<com.farukayata.yemektarifi.presentation.screens.home.HomeViewModel>()

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            modifier = Modifier.fillMaxSize(),
                            viewModel = viewModel,
                            navController = navController
                        )
                    }
                    composable("result") {
                        val imageUri = viewModel.selectedImageUri.collectAsState().value
                        val finalItems = viewModel.userEditedItems.collectAsState().value
                        val isLoading = viewModel.isResultLoading.collectAsState().value

                        ResultScreen(
                            navController = navController,//resultscreen e buton ekledik
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
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}
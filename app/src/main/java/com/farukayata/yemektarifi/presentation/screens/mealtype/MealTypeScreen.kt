package com.farukayata.yemektarifi.presentation.screens.mealtype

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.pager.*
import com.farukayata.yemektarifi.data.remote.repository.MealTypeRepository
import com.farukayata.yemektarifi.data.remote.ui.components.MealTypeCard

@OptIn(ExperimentalPagerApi::class)
@Composable
fun MealTypeScreen(navController: NavController) {

    //val viewModel: MealTypeViewModel = hiltViewModel()
    val homeViewModel = hiltViewModel<com.farukayata.yemektarifi.presentation.screens.home.HomeViewModel>()


    val mealTypes = MealTypeRepository.mealTypes
    val pagerState = rememberPagerState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hangi türde yemek yapmak istersin?",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )

        HorizontalPager(
            count = mealTypes.size,
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 48.dp),
            modifier = Modifier.fillMaxHeight(0.6f)
        ) { page ->
            val meal = mealTypes[page]

            MealTypeCard(
                mealType = meal,
                onClick = {
                    //viewModel.selectMealType(meal.title)
                    homeViewModel.setMealType(meal.title) //selectedMealType burda tuttuk

                    // editedItemsı al ve savedStateHandle ile taşı
                    val editedItems = homeViewModel.userEditedItems.value
                    navController.currentBackStackEntry?.savedStateHandle?.set("editedItems", editedItems)

                    // mealType bilgisini route parametresi olarak taşı
                    navController.navigate("recipeSuggestion/${meal.title}")


                    //navController.navigate("recipeSuggestion")
                }
            )

        }

        Spacer(modifier = Modifier.height(24.dp))

        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier.padding(16.dp)
        )
    }
}
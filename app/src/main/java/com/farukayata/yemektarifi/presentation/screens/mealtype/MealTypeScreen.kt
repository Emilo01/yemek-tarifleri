package com.farukayata.yemektarifi.presentation.screens.mealtype

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    val homeViewModel = hiltViewModel<com.farukayata.yemektarifi.presentation.screens.home.HomeViewModel>()
    val mealTypes = MealTypeRepository.mealTypes
    val pagerState = rememberPagerState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFDEB71), Color(0xFFF8D800))
                )
            )
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
                    homeViewModel.setMealType(meal.title)
                    val editedItems = homeViewModel.userEditedItems.value
                    navController.currentBackStackEntry?.savedStateHandle?.set("editedItems", editedItems)
                    navController.navigate("recipeSuggestion/${meal.title}")
                },
                isSelected = page == pagerState.currentPage
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        //kostüm sayfam
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(mealTypes.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(10.dp)
                        .width(if (isSelected) 28.dp else 10.dp)
                        .background(
                            color = if (isSelected) Color(0xFFFFFFFF) else Color(0xFFFDEB71),
                            shape = MaterialTheme.shapes.small
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        //yemek tipi için info box
        val selectedMeal = mealTypes.getOrNull(pagerState.currentPage)
        if (selectedMeal != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = selectedMeal.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFF8D800),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = selectedMeal.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
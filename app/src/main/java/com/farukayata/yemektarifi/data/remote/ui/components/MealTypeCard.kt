package com.farukayata.yemektarifi.data.remote.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.farukayata.yemektarifi.data.remote.model.MealType
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun MealTypeCard(
    mealType: MealType,
    onClick: () -> Unit,
    isSelected: Boolean
) {
    val scale = animateFloatAsState(if (isSelected) 1.08f else 0.95f)
    val elevation = animateDpAsState(if (isSelected) 16.dp else 4.dp)
    val gradient = if (isSelected) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFFDEB71), Color(0xFFF8D800))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFF8D800), Color(0xFFFDEB71))
        )
    }
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .aspectRatio(0.9f)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .background(gradient)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.value)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = mealType.imageRes),
                contentDescription = mealType.title,
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = mealType.title,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 18.sp
            )
        }
    }
}
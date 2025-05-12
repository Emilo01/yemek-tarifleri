package com.farukayata.yemektarifi.data.remote.ui.components.NutritionalChart

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.farukayata.yemektarifi.data.remote.model.NutritionalValues
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NutritionalChart(
    nutritionalValues: NutritionalValues,
    modifier: Modifier = Modifier
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val (carbs, protein, fat, other) = nutritionalValues.getPercentagesWithOther()

    val total = carbs + protein + fat + other
    val carbsPercent = if (total > 0) (carbs / total) * 100 else 0f
    val proteinPercent = if (total > 0) (protein / total) * 100 else 0f
    val fatPercent = if (total > 0) (fat / total) * 100 else 0f
    val otherPercent = if (total > 0) (other / total) * 100 else 0f

    val carbsAnim by animateFloatAsState(targetValue = if (animationPlayed) carbsPercent else 0f, animationSpec = tween(1000), label = "carbs")
    val proteinAnim by animateFloatAsState(targetValue = if (animationPlayed) proteinPercent else 0f, animationSpec = tween(1000), label = "protein")
    val fatAnim by animateFloatAsState(targetValue = if (animationPlayed) fatPercent else 0f, animationSpec = tween(1000), label = "fat")
    val otherAnim by animateFloatAsState(targetValue = if (animationPlayed) otherPercent else 0f, animationSpec = tween(1000), label = "other")

    LaunchedEffect(true) { animationPlayed = true }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Besin Değerleri (100g)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .size(200.dp)
                .padding(16.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val strokeWidth = 42f

                // Arka plan çemberi
                drawArc(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round),
                    size = Size(width, height)
                )

                // Karbonhidrat (Yeşil)
                drawArc(
                    color = Color(0xFF4CAF50),
                    startAngle = 0f,
                    sweepAngle = (carbsAnim * 3.6f),
                    useCenter = false,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round),
                    size = Size(width, height)
                )
                // Protein (Turuncu)
                drawArc(
                    color = Color(0xFFFF9800),
                    startAngle = (carbsAnim * 3.6f),
                    sweepAngle = (proteinAnim * 3.6f),
                    useCenter = false,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round),
                    size = Size(width, height)
                )
                // Yağ (Sarı)
                drawArc(
                    color = Color(0xFFFFEB3B),
                    startAngle = ((carbsAnim + proteinAnim) * 3.6f),
                    sweepAngle = (fatAnim * 3.6f),
                    useCenter = false,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round),
                    size = Size(width, height)
                )
                // Diğer (Gri)
                drawArc(
                    color = Color(0xFFBDBDBD),
                    startAngle = ((carbsAnim + proteinAnim + fatAnim) * 3.6f),
                    sweepAngle = (otherAnim * 3.6f),
                    useCenter = false,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round),
                    size = Size(width, height)
                )
            }
        }

        // Legend
        Column(
            modifier = Modifier.padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NutrientLegendItem(Color(0xFF4CAF50), "Karbonhidrat", carbs, carbsPercent)
            NutrientLegendItem(Color(0xFFFF9800), "Protein", protein, proteinPercent)
            NutrientLegendItem(Color(0xFFFFEB3B), "Yağ", fat, fatPercent)
            NutrientLegendItem(Color(0xFFBDBDBD), "Diğer", other, otherPercent)
        }
    }
}

@Composable
private fun NutrientLegendItem(
    color: Color,
    name: String,
    value: Float,
    percentage: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .padding(end = 4.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(color = color)
            }
        }
        Text(
            text = "$name: ${String.format("%.1f", value)}g (${String.format("%.1f", percentage)}%)",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
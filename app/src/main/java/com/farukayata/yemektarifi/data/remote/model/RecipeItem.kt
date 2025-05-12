package com.farukayata.yemektarifi.data.remote.model

data class RecipeItem(
    val name: String,
    val imageUrl: String,
    val duration: String,
    val region: String,
    val description: String,
    val ingredients: List<String>, //-> malzeme listesi
    val missingIngredients: List<String> = emptyList(), //eksik ürü için
    val ingredientDetails: String = "",
    val summary: String = "",
    val nutritionalValues: NutritionalValues = NutritionalValues()
)

data class NutritionalValues(
    val carbohydrates: Float = 0f,  // gram cinsinden
    val protein: Float = 0f,         // gram cinsinden
    val fat: Float = 0f              // gram cinsinden
) {
    fun getPercentages(): Triple<Float, Float, Float> {
        val total = carbohydrates + protein + fat
        return if (total > 0) {
            Triple(
                (carbohydrates / total) * 100,
                (protein / total) * 100,
                (fat / total) * 100
            )
        } else {
            Triple(0f, 0f, 0f)
        }
    }
    fun getPercentagesWithOther(): Quadruple<Float, Float, Float, Float> {
        val other = 100f - (carbohydrates + protein + fat)
        val otherValue = if (other < 0f) 0f else other
        return Quadruple(
            carbohydrates,
            protein,
            fat,
            otherValue
        )
    }
}
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

package com.example.projektmbun.views.temp_data_models

import com.example.projektmbun.utils.enums.DifficultyEnum
import com.example.projektmbun.utils.enums.FoodCategoryEnum
import com.example.projektmbun.utils.enums.UnitsEnum

data class TemporaryRecipe(
    var title: String = "",
    var dishType: String = "",
    var imageUrl: String = "",
    var shortDescription: String = "",
    var servings: Int = 1,
    var readyInMinutes: Int = 0,
    var cookingMinutes: Int = 0,
    var preparationMinutes: Int = 0,
    var sourceUrl: String = "",
    var difficulty: DifficultyEnum? = null,
    var pricePerServing: Double? = null,
    var dairyFree: Boolean = false,
    var glutenFree: Boolean = false,
    var nutFree: Boolean = false,
    var vegan: Boolean = false,
    var vegetarian: Boolean = false,
    var pescetarian: Boolean = false,
    var popularityScore: Double = 0.0
)

data class TemporaryIngredient(
    var description: String? = "",
    var amount: Double? = 0.0,
    var unit: UnitsEnum? = null,
    var price: Double = 0.0,
    var isOptional: Boolean = false
)

data class TemporaryFood(
    var name: String = "",
    var category: FoodCategoryEnum? = null,
)

data class TemporaryInstruction(
    val step: Int = 0,
    var description: String = "",
    var imageUrl: String? = ""
)

data class TemporaryEquipment(
    val equipment: String = ""
)

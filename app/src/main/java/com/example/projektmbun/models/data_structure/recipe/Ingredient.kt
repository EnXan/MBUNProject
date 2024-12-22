package com.example.projektmbun.models.data_structure.recipe

import com.example.projektmbun.utils.enums.UnitsEnum
import kotlinx.serialization.Serializable

/**
 * Represents an ingredient associated with a specific recipe in the database.
 * Each ingredient is linked to both a recipe and a food item, with cascading delete behavior.
 *
 * @property id The unique identifier of the ingredient (auto-generated primary key).
 * @property recipeId The ID of the associated recipe in the `recipes` table (foreign key).
 * @property foodId The name of the associated food item in the `food` table (foreign key).
 * @property description A description of the ingredient, providing additional details (optional).
 * @property amount The quantity of the ingredient required for the recipe (optional).
 * @property unit The unit of measurement for the ingredient's quantity (e.g., grams, liters), defined in `UnitsEnum`.
 * @property price The price of the ingredient.
 * @property isOptional Indicates whether the ingredient is optional for the recipe.
 */
@Serializable
data class Ingredient(
    val id: Int? = null,
    val recipeId: Int? = null,
    val foodId: String? = null,
    val description: String?,
    val amount: Double?,
    val unit: UnitsEnum,
    val price: Double,
    val isOptional: Boolean
)
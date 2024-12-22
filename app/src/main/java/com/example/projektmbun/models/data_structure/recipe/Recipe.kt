package com.example.projektmbun.models.data_structure.recipe

import com.example.projektmbun.utils.enums.DifficultyEnum
import kotlinx.serialization.Serializable

/**
 * Represents a recipe in the database, including its metadata, dietary information, and other attributes.
 *
 * @property id The unique identifier of the recipe (auto-generated primary key).
 * @property title The title or name of the recipe.
 * @property image A URL or path to an image representing the recipe.
 * @property shortDescription A brief description of the recipe.
 * @property servings The number of servings the recipe produces.
 * @property readyInMinutes The total time required to prepare and cook the recipe, in minutes.
 * @property cookingMinutes The time required for cooking the recipe, in minutes.
 * @property preparationMinutes The time required for preparing the recipe, in minutes.
 * @property sourceUrl A URL to the original source of the recipe.
 * @property difficulty The difficulty level of the recipe (e.g., "Easy", "Medium", "Hard").
 * @property pricePerServing The cost of ingredients per serving, in monetary units.
 * @property dairyFree Indicates whether the recipe is dairy-free.
 * @property glutenFree Indicates whether the recipe is gluten-free.
 * @property vegan Indicates whether the recipe is vegan.
 * @property vegetarian Indicates whether the recipe is vegetarian.
 * @property pescetarian Indicates whether the recipe is pescetarian.
 * @property popularityScore A score representing the popularity of the recipe (e.g., based on reviews or user ratings).
 */
@Serializable
data class Recipe(
    val id: Int? = null,
    val title: String,
    val imageUrl: String,
    var shortDescription: String,
    val servings: Int,
    val readyInMinutes: Int,
    val cookingMinutes: Int,
    val preparationMinutes: Int,
    val sourceUrl: String,
    val difficulty: DifficultyEnum,
    val pricePerServing: Double?,
    val dairyFree: Boolean,
    val glutenFree: Boolean,
    val vegan: Boolean,
    val vegetarian: Boolean,
    val pescetarian: Boolean,
    val popularityScore: Double,
    val dishType: String,
    val nutFree: Boolean,
)
package com.example.projektmbun.models.data.recipe

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Represents a detailed recipe, including its associated dish types, ingredients, and instructions.
 * Combines the `Recipe` entity with its related entities to provide a comprehensive view of the recipe.
 *
 * @property recipe The core recipe details from the `Recipe` entity.
 * @property dishTypes A list of dish types associated with the recipe, derived from the `DishTypes` entity.
 * @property ingredients A list of ingredients required for the recipe, derived from the `Ingredient` entity.
 * @property instructions A list of instructions for preparing the recipe, derived from the `Instructions` entity.
 */
data class RecipeWithDishTypesAndIngredientsAndInstructions(
    @Embedded val recipe: Recipe,

    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val dishTypes: List<DishTypes>,

    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val ingredients: List<Ingredient>,

    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val instructions: List<Instructions>
)

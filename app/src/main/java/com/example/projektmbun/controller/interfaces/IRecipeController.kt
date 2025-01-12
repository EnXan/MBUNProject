package com.example.projektmbun.controller.interfaces
import com.example.projektmbun.models.data_structure.food_card.FoodCard
import com.example.projektmbun.models.data_structure.recipe.Ingredient
import com.example.projektmbun.models.data_structure.recipe.Instructions
import com.example.projektmbun.models.data_structure.recipe.Recipe
import com.example.projektmbun.views.temp_data_models.TemporaryEquipment
import com.example.projektmbun.views.temp_data_models.TemporaryFood
import com.example.projektmbun.views.temp_data_models.TemporaryIngredient
import com.example.projektmbun.views.temp_data_models.TemporaryInstruction
import com.example.projektmbun.views.temp_data_models.TemporaryRecipe

interface IRecipeController {
    suspend fun addRecipeWithDetails(
        recipe: TemporaryRecipe,
        ingredients: List<TemporaryIngredient>,
        instructions: List<TemporaryInstruction>,
        equipment: List<TemporaryEquipment>,
        food: List<TemporaryFood>
    ): Boolean
    
    suspend fun getRecipesByTitle(query: String): List<Recipe>
    suspend fun getAllAvailableRecipes(): List<Recipe>
    suspend fun getFilteredRecipes(
        foodCards: List<FoodCard>,
        maximumMissingIngredients: Int = 3
    ): List<Recipe>
    suspend fun removeRecipeByTitle(title: String)
    suspend fun getRecipeById(recipeId: Int): Recipe?
    suspend fun getIngredientsByRecipeId(recipeId: Int): List<Ingredient>
    suspend fun getInstructionsByRecipeId(recipeId: Int): List<Instructions>
} 
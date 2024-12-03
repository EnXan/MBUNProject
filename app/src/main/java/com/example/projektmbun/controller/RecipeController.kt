package com.example.projektmbun.controller

import android.util.Log
import com.example.projektmbun.exceptions.RecipeNotFoundException
import com.example.projektmbun.models.daos.RecipeDao
import com.example.projektmbun.models.data.food_card.FoodCard
import com.example.projektmbun.models.data.recipe.Ingredient
import com.example.projektmbun.models.data.recipe.RecipeWithDishTypesAndIngredientsAndInstructions
import com.example.projektmbun.utils.SearchLogic
import com.example.projektmbun.utils.enums.UnitsEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext


/**
 * Controller class responsible for managing `Recipe` operations, including
 * fetching recipes by name, ID, or available ingredients, and providing utility functions
 * such as unit compatibility checks and description shortening.
 *
 * @property recipeDao DAO for performing `Recipe` operations.
 */
class RecipeController(private val recipeDao: RecipeDao) {


    /**
     * Get recipes by their name with fuzzy search.
     * @param name of the recipe.
     * @return List of all found recipes or empty list `List<RecipeWithDishTypesAndIngredientsAndInstructions>`.
     */
    suspend fun getRecipesByName(name: String): List<RecipeWithDishTypesAndIngredientsAndInstructions> = withContext(Dispatchers.IO) {

        val trimmedName = name.trim()
        try {
            val allRecipes = recipeDao.getAllRecipes()

            if (trimmedName.isBlank()) {
                return@withContext allRecipes
            }

            SearchLogic.fuzzySearch(
                query = trimmedName,
                items = allRecipes,
                nameSelector = { recipe -> recipe.recipe.title },
                threshold = -3
            )
        } catch (e: Exception) {
            Log.e("RecipeController", "Error fetching recipes by name: ${e.message}")
            emptyList()
        }
    }

    /**
     * Gets a list of recipes based on the ingredients available in the provided food cards.
     * The method performs the following steps:
     * 1. Retrieves recipes that contain at least one ingredient from the provided list.
     * 2. Calculates missing and matching ingredients for each recipe.
     * 3. Filters out recipes with more than two missing ingredients.
     * 4. Sorts the recipes based on the number of matching ingredients in descending order.
     * 5. Shortens the description of the filtered recipes.
     *
     * @param foodCards A list of `FoodCard` objects representing the user's available ingredients.
     * @return A list of filtered and sorted recipes with a maximum of two missing ingredients.
     * The returned recipes include dish types, ingredients, and instructions.
     *
     * @throws Exception If an error occurs while fetching recipes from the data source.
     */
    suspend fun getAvailableRecipes(foodCards: List<FoodCard>): List<RecipeWithDishTypesAndIngredientsAndInstructions> = withContext(Dispatchers.IO) {

        val ingredientNames = foodCards.map { it.foodId }
        var possibleRecipes: List<RecipeWithDishTypesAndIngredientsAndInstructions> = emptyList()

        val minimumIngredientsRequired = 1
        val maximumMissingIngredients = 2

        // Try to get all recipes with the given ingredients
        try {
            possibleRecipes = recipeDao.getRecipesByIngredientNames(ingredientNames, minimumIngredientsRequired)
        } catch (e: Exception) {
            Log.e("RecipeController", "Error fetching recipes by ingredient names: ${e.message}")
            emptyList<RecipeWithDishTypesAndIngredientsAndInstructions>()
        }

        // Calculate missing and available ingredients for each recipe
        val recipesWithIngredientInfo = possibleRecipes.map { recipe ->
            val totalIngredients = recipe.ingredients.size
            val matchingIngredients = recipe.ingredients.count { ingredient ->
                foodCards.any { foodCard ->
                    ingredient.name == foodCard.foodId && areUnitsCompatible(ingredient, foodCard)
                }
            }
            val missingIngredients = totalIngredients - matchingIngredients
            Triple(recipe, matchingIngredients, missingIngredients)
        }

        // Filter recipes with no more than x missing ingredients and sort by matching ingredients
        val filteredAndSortedRecipes = recipesWithIngredientInfo
            .filter { (_, _, missingIngredients) -> missingIngredients <= maximumMissingIngredients }
            .sortedByDescending { it.second } // Sort by number of matching ingredients
            .map { it.first }

        // Shorten the descriptions
        filteredAndSortedRecipes.forEach { recipe ->
            if(recipe.recipe.shortDescription.isNotEmpty()) {
                recipe.recipe.shortDescription = shortenDescription(recipe.recipe.shortDescription)
            } else {
                recipe.recipe.shortDescription = "No description available"
            }
        }

        return@withContext filteredAndSortedRecipes
    }


    /**
     * Helper function to check if units are compatible.
     * @param ingredient the ingredient to compare to `foodCard`.
     * @param foodCard the food card to compare to `ingredient`.
     */
    private fun areUnitsCompatible(ingredient: Ingredient, foodCard: FoodCard): Boolean {
        // Standard values for 'amount' and 'unit', if null
        val ingredientAmount = ingredient.amount ?: 0.0
        val ingredientUnit = ingredient.unit
        val foodCardAmount = foodCard.quantity
        val foodCardUnit = foodCard.unit

        // Convert quantities into base units
        val ingredientQuantity = ingredientUnit.convertToBase(ingredientAmount)
        val foodCardQuantity = foodCardUnit.convertToBase(foodCardAmount)

        // Check, if quantities are compatible
        return foodCardQuantity >= ingredientQuantity
    }

    /**
     * Gets a recipe by its id.
     * @param recipeId the id of the recipe.
     * @return the recipe with the given id or null if no recipe was found.
     * @throws RecipeNotFoundException if no recipe was found.
     * @throws Exception if an error occurs while fetching the recipe.
     */
    suspend fun getRecipeById(recipeId: Int): RecipeWithDishTypesAndIngredientsAndInstructions = withContext(Dispatchers.IO) {
        try {
            val recipe = recipeDao.getRecipeById(recipeId)
                ?: throw RecipeNotFoundException("The recipe with the id $recipeId was not found.")
            recipe
        }
        catch (e: Exception) {
            Log.e("RecipeController", "Error fetching recipe by ID: $recipeId - ${e.message}", e)
            throw e
        }
    }


    /**
     * Helper function to shorten a recipe description to a maximum of x words.
     * @param description the description to shorten.
     * @param wordLimit the maximum number of words.
     * @return a String with the shortened description.
     */
    private fun shortenDescription(description: String, wordLimit: Int = 15): String {
        val words = description.split(" ")
        return if (words.size > wordLimit) {
            words.take(wordLimit).joinToString(" ") + "..."
        } else {
            description
        }
    }
}
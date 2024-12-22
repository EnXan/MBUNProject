package com.example.projektmbun.controller

import android.util.Log
import com.example.projektmbun.models.cloud.service.RecipeService
import com.example.projektmbun.models.data_structure.food.FoodLocal
import com.example.projektmbun.models.data_structure.food_card.FoodCard
import com.example.projektmbun.utils.enums.DifficultyEnum
import com.example.projektmbun.models.data_structure.recipe.Equipment
import com.example.projektmbun.models.data_structure.recipe.Ingredient
import com.example.projektmbun.models.data_structure.recipe.Instructions
import com.example.projektmbun.models.data_structure.recipe.Recipe
import com.example.projektmbun.utils.enums.FoodCategoryEnum
import com.example.projektmbun.utils.enums.UnitsEnum
import com.example.projektmbun.views.temp_data_models.TemporaryEquipment
import com.example.projektmbun.views.temp_data_models.TemporaryFood
import com.example.projektmbun.views.temp_data_models.TemporaryIngredient
import com.example.projektmbun.views.temp_data_models.TemporaryInstruction
import com.example.projektmbun.views.temp_data_models.TemporaryRecipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecipeController(private val recipeService: RecipeService) {

    /**
     * Adds a recipe to the database with all its details.
     * @param recipe Recipe to add.
     * @param ingredients List of temporary ingredients.
     * @param instructions List of temporary instructions.
     * @return Boolean indicating success or failure.
     */
    suspend fun addRecipeWithDetails(
        recipe: TemporaryRecipe,
        ingredients: List<TemporaryIngredient>,
        instructions: List<TemporaryInstruction>,
        equipment: List<TemporaryEquipment>,
        food: List<TemporaryFood>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Mappe die Daten in die endgültigen Modelle
            val mappedRecipe = Recipe(
                id = null,
                title = recipe.title,
                dishType = recipe.dishType,
                imageUrl = recipe.imageUrl,
                shortDescription = recipe.shortDescription,
                servings = recipe.servings,
                readyInMinutes = recipe.readyInMinutes,
                cookingMinutes = recipe.cookingMinutes,
                preparationMinutes = recipe.preparationMinutes,
                sourceUrl = recipe.sourceUrl,
                difficulty = recipe.difficulty ?: DifficultyEnum.EASY,
                pricePerServing = recipe.pricePerServing ?: 0.0,
                dairyFree = recipe.dairyFree,
                glutenFree = recipe.glutenFree,
                nutFree = recipe.nutFree,
                vegan = recipe.vegan,
                vegetarian = recipe.vegetarian,
                pescetarian = recipe.pescetarian,
                popularityScore = recipe.popularityScore
            )

            val mappedIngredients = ingredients.map { tempIngredient ->
                Ingredient(
                    id = null,
                    recipeId = null, // Wird im Service gesetzt
                    foodId = null,
                    description = tempIngredient.description,
                    amount = tempIngredient.amount ?: 0.0,
                    unit = tempIngredient.unit ?: UnitsEnum.UNBEKANNT,
                    price = tempIngredient.price,
                    isOptional = tempIngredient.isOptional
                )
            }

            val mappedInstructions = instructions.map { tempInstruction ->
                Instructions(
                    id = null,
                    recipeId = null, // Wird im Service gesetzt
                    step = tempInstruction.step,
                    description = tempInstruction.description,
                    imageUrl = tempInstruction.imageUrl
                )
            }

            val mappedEquipment = equipment.map { tempEquipment ->
                Equipment(
                    id = null,
                    instructionId = null, // Wird im Service gesetzt
                    equipment = tempEquipment.equipment
                )
            }

            val mappedFoodLocal = food.map { tempFood ->
                FoodLocal(
                    name = tempFood.name,
                    category = tempFood.category ?: FoodCategoryEnum.UNBEKANNT
                )
            }


            // Übergib alle Daten an den RecipeService
            val success = recipeService.insertFullRecipe(
                recipe = mappedRecipe,
                ingredients = mappedIngredients,
                foodLocal = mappedFoodLocal,
                instructions = mappedInstructions,
                equipment = mappedEquipment
            )

            if (success) {
                Log.d("RecipeController", "Rezept erfolgreich eingefügt: ${mappedRecipe.title}")
                return@withContext true
            } else {
                Log.e("RecipeController", "Fehler beim Einfügen des Rezepts.")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e("RecipeController", "Fehler beim Hinzufügen der Rezeptdetails: ${e.localizedMessage}")
            return@withContext false
        }
    }







    /**
     * Retrieves a recipe by its ID.
     * @param recipeId ID of the recipe.
     * @return The recipe or null if not found.
     */
    suspend fun getRecipeById(recipeId: Int): Recipe? = withContext(Dispatchers.IO) {
        if (recipeId <= 0) {
            throw IllegalArgumentException("Ungültige Rezept-ID: $recipeId")
        }

        try {
            recipeService.getRecipeById(recipeId)
        } catch (e: Exception) {
            Log.e("RecipeController", "Fehler beim Abrufen des Rezepts: ${e.localizedMessage}")
            null
        }
    }

    /**
     * Searches for recipes by their title.
     * @param title The title to search for.
     * @return List of matching recipes.
     */
    suspend fun getRecipesByTitle(title: String): List<Recipe> = withContext(Dispatchers.IO) {
        if (title.isBlank()) {
            return@withContext emptyList()
        }

        try {
            recipeService.getRecipesByTitle(title)
        } catch (e: Exception) {
            Log.e("RecipeController", "Fehler beim Abrufen der Rezepte: ${e.localizedMessage}")
            emptyList()
        }
    }

    /**
     * Retrieves all available recipes.
     * @return List of recipes.
     */
    suspend fun getAllAvailableRecipes(): List<Recipe> = withContext(Dispatchers.IO) {
        try {
            recipeService.getAllAvailableRecipes()
        } catch (e: Exception) {
            Log.e("RecipeController", "Fehler beim Abrufen aller Rezepte: ${e.localizedMessage}")
            emptyList()
        }
    }

    /**
     * Filters available recipes based on food cards, matching ingredients, and missing ingredients.
     * @param foodCards List of available food cards.
     * @param maximumMissingIngredients Maximum number of allowed missing ingredients per recipe.
     * @return List of filtered recipes sorted by matching ingredients.
     */
    suspend fun getFilteredRecipes(
        foodCards: List<FoodCard>,
        maximumMissingIngredients: Int = 2
    ): List<Recipe> = withContext(Dispatchers.IO) {
        val recipes = getAllAvailableRecipes()

        // Fetch and evaluate each recipe in a coroutine-safe manner
        val filteredRecipes = recipes.mapNotNull { recipe ->
            Log.d("RecipeController", "Recipe id: ${recipe.id}")
            val recipeIngredients = recipe.id?.let { recipeService.getIngredientsByRecipeId(it) }
            Log.d("RecipeController", "extracted recipeIngredients: $recipeIngredients")
            val totalIngredients = recipeIngredients?.size
            val matchingIngredients = recipeIngredients?.count { ingredient ->
                foodCards.any { foodCard ->
                    ingredient.foodId.toString() == foodCard.foodId && areUnitsCompatible(ingredient, foodCard)
                }
            }
            val missingIngredients = matchingIngredients?.let { totalIngredients?.minus(it) }

            if (missingIngredients == null) {
                Log.e("RecipeController", "Failed to calculate missing ingredients for recipe due to null value: ${recipe.title}")
            }

            if (missingIngredients!! <= maximumMissingIngredients) {
                Pair(recipe, matchingIngredients)
            } else {
                Log.d("RecipeController", "Missing ingredients are: $missingIngredients and maximum is: $maximumMissingIngredients")
                Log.d("RecipeController", "Recipe ${recipe.title} does not meet the condition.")
                null
            }
        }.sortedByDescending { it.second } // Sort by matching ingredients count
            .map { it.first } // Extract only the recipes

        return@withContext filteredRecipes
    }


    /**
     * Helper function to check if the units of an ingredient and a food card are compatible.
     */
    private fun areUnitsCompatible(ingredient: Ingredient, foodCard: FoodCard): Boolean {
        return ingredient.unit == foodCard.unit
    }

    suspend fun getIngredientsByRecipeId(recipeId: Int): List<Ingredient> {
        return recipeService.getIngredientsByRecipeId(recipeId)
    }

    suspend fun getInstructionsByRecipeId(recipeId: Int): List<Instructions> {
        return recipeService.getInstructionsByRecipeId(recipeId)
    }

    /**
     * Helper function to shorten a recipe description.
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